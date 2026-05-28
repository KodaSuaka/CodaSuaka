<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\KaryawanController;
use App\Http\Controllers\Api\PresensiController;

Route::get('/user', function (Request $request) {
    return $request->user();
})->middleware('auth:sanctum');


Route::post('/register/owner', [AuthController::class, 'registerOwner']);
Route::post('/login', [AuthController::class, 'login']);

Route::middleware('auth:sanctum')->group(function () {
    
    Route::post('/logout', [AuthController::class, 'logout']);

    Route::post('/register/karyawan', [AuthController::class, 'registerKaryawan']);
    Route::get('/karyawan', [KaryawanController::class, 'index']);
    Route::put('/karyawan/{id}', [KaryawanController::class, 'update']);
    
    #presensi karyawan
    Route::post('/presensi/checkin', [PresensiController::class, 'checkIn']);
    Route::post('/presensi/checkout', [PresensiController::class, 'checkOut']);
    Route::post('/presensi/izin', [PresensiController::class, 'izin']);

    Route::get('/owner/presensi-hari-ini', [PresensiController::class, 'presensiHariIni']);
});
