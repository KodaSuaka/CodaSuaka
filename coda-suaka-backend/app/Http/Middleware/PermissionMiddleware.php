<?php

namespace App\Http\Middleware;

use App\Services\PermissionService;
use Closure;
use Illuminate\Http\Request;

class PermissionMiddleware
{
    /**
     * Handle an incoming request.
     * Checks if the authenticated user has the required permission(s).
     *
     * Usage in routes:
     *   Route::middleware('permission:manage:outlets')->group(...)
     *   Route::middleware('permission:manage:outlets|manage:karyawan')->group(...)
     */
    public function handle(Request $request, Closure $next, string $permissions)
    {
        $user = $request->user();

        if (!$user) {
            return response()->json([
                'status' => 'error',
                'message' => 'Unauthenticated.'
            ], 401);
        }

        $permissionService = app(PermissionService::class);
        $requiredPermissions = explode('|', $permissions);

        // Super Admin bypasses all permission checks
        if ($user->role?->nama_role === 'Super Admin') {
            return $next($request);
        }

        foreach ($requiredPermissions as $permission) {
            if ($permissionService->userHasPermission($user, trim($permission))) {
                return $next($request);
            }
        }

        return response()->json([
            'status' => 'error',
            'message' => 'Unauthorized. Anda tidak memiliki izin untuk mengakses endpoint ini.'
        ], 403);
    }
}