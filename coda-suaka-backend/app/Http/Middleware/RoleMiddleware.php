<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class RoleMiddleware
{
    /**
     * Handle an incoming request.
     * Ensures the authenticated user has one of the specified roles.
     *
     * Usage:
     *   Route::middleware('role:Super Admin')->group(...)
     *   Route::middleware('role:Owner')->group(...)
     *   Route::middleware('role:Super Admin,Owner')->group(...)
     */
    public function handle(Request $request, Closure $next, string ...$roles)
    {
        $user = $request->user();

        if (!$user) {
            return response()->json([
                'status' => 'error',
                'message' => 'Unauthenticated.'
            ], 401);
        }

        $userRole = $user->role?->nama_role;

        foreach ($roles as $role) {
            if (strtolower($userRole) === strtolower(trim($role))) {
                return $next($request);
            }
        }

        return response()->json([
            'status' => 'error',
            'message' => 'Unauthorized. Role Anda tidak memiliki akses ke endpoint ini.'
        ], 403);
    }
}