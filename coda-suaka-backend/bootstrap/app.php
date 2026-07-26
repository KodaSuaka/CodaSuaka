<?php

use App\Http\Middleware\PermissionMiddleware;
use App\Http\Middleware\RoleMiddleware;
use Illuminate\Auth\Access\AuthorizationException;
use Illuminate\Auth\AuthenticationException;
use Illuminate\Database\Eloquent\ModelNotFoundException;
use Illuminate\Database\QueryException;
use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;
use Symfony\Component\HttpKernel\Exception\AccessDeniedHttpException;
use Symfony\Component\HttpKernel\Exception\HttpException;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->alias([
            'role' => RoleMiddleware::class,
            'permission' => PermissionMiddleware::class,
        ]);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        // Always return JSON for API routes on error
        $exceptions->shouldRenderJsonWhen(function (Request $request) {
            return $request->is('api/*') || $request->expectsJson();
        });

        // Handle validation errors
        $exceptions->render(function (ValidationException $e, Request $request) {
            if ($request->is('api/*') || $request->expectsJson()) {
                return response()->json([
                    'status' => 'error',
                    'message' => 'Validasi gagal',
                    'errors' => $e->errors(),
                ], 422);
            }
        });

        // Handle model not found
        $exceptions->render(function (ModelNotFoundException $e, Request $request) {
            if ($request->is('api/*') || $request->expectsJson()) {
                return response()->json([
                    'status' => 'error',
                    'message' => 'Data tidak ditemukan',
                ], 404);
            }
        });

        // Handle authentication exceptions
        $exceptions->render(function (AuthenticationException $e, Request $request) {
            if ($request->is('api/*') || $request->expectsJson()) {
                return response()->json([
                    'status' => 'error',
                    'message' => 'Unauthenticated. Silakan login terlebih dahulu.',
                ], 401);
            }
        });

        // Handle all other exceptions for API routes
        $exceptions->render(function (Throwable $e, Request $request) {
            if ($request->is('api/*') || $request->expectsJson()) {
                // ── Forbidden / Authorization ──────────────────────────────
                if ($e instanceof AuthorizationException || $e instanceof AccessDeniedHttpException) {
                    return response()->json([
                        'status' => 'error',
                        'message' => 'Anda tidak memiliki akses (Forbidden).',
                        'code' => 'FORBIDDEN',
                        'path' => $request->path(),
                    ], 403);
                }

                // ── HTTP exceptions (404, 429, dll) ───────────────────────
                if ($e instanceof HttpExceptionInterface) {
                    $statusCode = $e->getStatusCode();
                    $message = $e->getMessage() ?: match ($statusCode) {
                        404 => 'Resource tidak ditemukan.',
                        405 => 'Method tidak diizinkan.',
                        429 => 'Terlalu banyak permintaan. Silakan coba lagi nanti.',
                        503 => 'Layanan sedang sibuk. Silakan coba lagi.',
                        default => 'Terjadi kesalahan pada server',
                    };

                    $response = [
                        'status' => 'error',
                        'message' => $message,
                        'code' => "HTTP_{$statusCode}",
                        'path' => $request->path(),
                    ];

                    // Di non-debug mode, tetap kirimkan referensi error
                    // agar frontend bisa menampilkan informasi yang berguna
                    if (! config('app.debug') && $statusCode >= 500) {
                        $errorRef = 'ERR-'.strtoupper(substr(md5(microtime()), 0, 8));
                        $response['error_ref'] = $errorRef;
                        logger()->warning("Error ref: {$errorRef} — {$e->getMessage()} in {$e->getFile()}:{$e->getLine()}");
                    }

                    return response()->json($response, $statusCode);
                }

                // ── Database query errors ─────────────────────────────────
                if ($e instanceof QueryException) {
                    $errorRef = 'DB-ERR-'.strtoupper(substr(md5(microtime()), 0, 8));
                    logger()->error("{$errorRef} — {$e->getMessage()} in {$e->getFile()}:{$e->getLine()}");

                    return response()->json([
                        'status' => 'error',
                        'message' => config('app.debug')
                            ? 'Database error: '.$e->getMessage()
                            : 'Terjadi kesalahan database. Silakan coba lagi.',
                        'code' => 'DB_ERROR',
                        'error_ref' => $errorRef,
                        'path' => $request->path(),
                    ], 500);
                }

                // ── Fallback untuk semua error lain ───────────────────────
                if (! $e instanceof ValidationException
                    && ! $e instanceof ModelNotFoundException
                    && ! $e instanceof AuthenticationException) {
                    $statusCode = $e instanceof HttpException
                        ? $e->getStatusCode()
                        : 500;

                    // Selalu log error untuk traceability di production
                    $errorRef = 'SRV-ERR-'.strtoupper(substr(md5(microtime()), 0, 8));
                    logger()->error("{$errorRef} — {$e->getMessage()} in {$e->getFile()}:{$e->getLine()}");

                    $response = [
                        'status' => 'error',
                        'message' => config('app.debug')
                            ? $e->getMessage()
                            : 'Terjadi kesalahan internal server.',
                        'code' => $statusCode >= 500 ? 'SERVER_ERROR' : 'GENERAL_ERROR',
                        'path' => $request->path(),
                    ];

                    // Di production, kirim error_ref agar user bisa melapor
                    if (! config('app.debug')) {
                        $response['error_ref'] = $errorRef;
                    }

                    return response()->json($response, $statusCode);
                }
            }
        });
    })->create();
