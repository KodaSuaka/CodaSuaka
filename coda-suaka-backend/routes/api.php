<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AuthController;

Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    
    // Rute Logout
    Route::post('/logout', [AuthController::class, 'logout']);
    
    // Rute untuk cek profil user yang sedang login (berguna untuk Android nanti)
    Route::get('/user', function (Request $request) {
        return response()->json([
            'status' => 'success',
            'data' => $request->user()->load('role') 
        ]);
    });
});
