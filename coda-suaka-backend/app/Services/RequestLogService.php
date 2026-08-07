<?php

namespace App\Services;

use App\Models\RequestLog;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Mencatat trace request API untuk audit (super admin only).
 * Payload sensitif (password, token) disensor agar tidak tersimpan mentah.
 */
class RequestLogService
{
    /** Path log-request itu sendiri TIDAK dicatat (hindari infinite loop). */
    private const SKIP_PATHS = [
        'request-logs',
        'super-admin/request-logs',
    ];

    /** Kolom yang disensor nilainya (diganti '***'). */
    private const SENSITIVE_KEYS = [
        'password',
        'password_confirmation',
        'current_password',
        'token',
        'access_token',
        'api_token',
        'authorization',
    ];

    public function record(Request $request, ?Response $response, float $durationMs, ?int $statusOverride = null): ?RequestLog
    {
        if (! $this->shouldLog($request)) {
            return null;
        }

        $user = $request->user();
        $instansiId = $user?->instansi_id;

        if ($instansiId === null && $request->route('instansi_id')) {
            $instansiId = $request->route('instansi_id');
        }

        return RequestLog::create([
            'instansi_id' => $instansiId,
            'user_id' => $user?->id,
            'method' => $request->method(),
            'path' => $request->path(),
            'full_url' => $request->fullUrl(),
            'query_params' => $this->sanitize($request->query()),
            'request_body' => $this->sanitize($this->extractBody($request)),
            'ip_address' => $request->ip(),
            'user_agent' => $this->truncate($request->userAgent(), 500),
            'status_code' => $statusOverride ?? $response?->getStatusCode(),
            'duration_ms' => (int) round($durationMs),
        ]);
    }

    private function shouldLog(Request $request): bool
    {
        if (app()->runningInConsole()) {
            return false;
        }

        if (! $request->is('api/*')) {
            return false;
        }

        foreach (self::SKIP_PATHS as $skip) {
            if (str_contains($request->path(), $skip)) {
                return false;
            }
        }

        return true;
    }

    private function extractBody(Request $request): array
    {
        $content = $request->getContent();

        if ($request->isJson() && $content !== '') {
            $decoded = json_decode($content, true);

            if (is_array($decoded)) {
                return $decoded;
            }
        }

        // Multipart / form: hanya ambil kolom kecil agar log tidak membengkak.
        $data = $request->except(['file', 'foto', 'gambar', 'attachment', '_method']);

        return is_array($data) ? $data : [];
    }

    private function sanitize(array $data): array
    {
        foreach ($data as $key => $value) {
            if (in_array(strtolower((string) $key), self::SENSITIVE_KEYS, true)) {
                $data[$key] = '***';
            } elseif (is_array($value)) {
                // Sensitif di dalam objek nested ikut disensor.
                $data[$key] = $this->sanitize($value);
            }
        }

        return $data;
    }

    private function truncate(?string $value, int $length): ?string
    {
        if ($value === null || mb_strlen($value) <= $length) {
            return $value;
        }

        return mb_substr($value, 0, $length);
    }
}
