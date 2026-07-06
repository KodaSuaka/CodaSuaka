<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\ChatController;

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    
    // Rute Logout
    Route::post('/logout', [AuthController::class, 'logout']);
    
    // Rute untuk cek profil user yang sedang login
    Route::get('/user', function (Request $request) {
        return response()->json([
            'status' => 'success',
            'data' => $request->user()->load('role')
        ]);
    });

    // ─── Chat / Kontak ─────────────────────────────────────
    Route::prefix('chat')->group(function () {
        Route::get('/contacts', [ChatController::class, 'contacts']);
        Route::get('/messages/{user}', [ChatController::class, 'messages']);
        Route::post('/send', [ChatController::class, 'send']);
        Route::put('/read/{user}', [ChatController::class, 'markAsRead']);
    });
});
