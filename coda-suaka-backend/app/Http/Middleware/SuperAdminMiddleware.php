<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class SuperAdminMiddleware
{
    /**
     * Handle an incoming request.
     * Memastikan user yang terautentikasi memiliki role 'Super Admin'.
     */
    public function handle(Request $request, Closure $next)
    {
        if (!$request->user() || $request->user()->role?->nama_role !== 'Super Admin') {
            return response()->json([
                'status' => 'error',
                'message' => 'Unauthorized. Hanya Super Admin yang dapat mengakses endpoint ini.'
            ], 403);
        }

        return $next($request);
    }
}
