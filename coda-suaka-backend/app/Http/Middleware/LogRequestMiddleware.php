<?php

namespace App\Http\Middleware;

use App\Services\RequestLogService;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

/**
 * Mencatat trace setiap request API ke tabel request_logs.
 * Aman terhadap pengecualian: jika request gagal, tetap dicatat status 500.
 */
class LogRequestMiddleware
{
    public function __construct(private readonly RequestLogService $logService)
    {
    }

    public function handle(Request $request, Closure $next): Response
    {
        $start = hrtime(true);

        try {
            $response = $next($request);
            $this->record($request, $response, $start);

            return $response;
        } catch (\Throwable $e) {
            $this->record($request, null, $start, 500);
            throw $e;
        }
    }

    private function record(Request $request, ?Response $response, float $start, ?int $fallbackStatus = null): void
    {
        $durationMs = (hrtime(true) - $start) / 1_000_000;

        // Jika tidak ada response (error), gunakan status fallback.
        $status = $response?->getStatusCode() ?? $fallbackStatus;

        $this->logService->record($request, $response, $durationMs, $status);
    }
}
