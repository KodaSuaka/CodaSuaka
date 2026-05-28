<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use App\Models\Presensi;
use App\Models\Karyawan;
use Illuminate\Support\Facades\DB;
use Carbon\Carbon;

class PresensiController extends Controller
{
    // --- 1. PROSES CHECK-IN (DATANG) ---
    public function checkIn(Request $request)
    {
        $user = $request->user();

        // Pastikan yang mengakses adalah karyawan
        if ($user->role !== 'karyawan') {
            return response()->json(['status' => 'error', 'message' => 'Hanya karyawan yang bisa melakukan presensi.'], 403);
        }

        // Cari profil karyawan si user ini
        $karyawan = Karyawan::where('id_pengguna', $user->id)->first();
        
        $hariIni = Carbon::today()->toDateString();

        // Cek apakah hari ini sudah pernah check-in
        $sudahPresensi = Presensi::where('id_karyawan', $karyawan->id)
                                 ->where('tanggal', $hariIni)
                                 ->first();

        if ($sudahPresensi) {
            return response()->json(['status' => 'error', 'message' => 'Anda sudah melakukan check-in hari ini.'], 400);
        }

        DB::beginTransaction();
        try {
            // Buat data presensi baru
            $presensi = Presensi::create([
                'id_karyawan' => $karyawan->id,
                'tanggal' => $hariIni,
                'hadir' => Carbon::now()->toTimeString(),
                'status' => 'hadir',
                'koordinat' => $request->koordinat // dikirim dari GPS HP Android nanti
            ]);

            // OTOMATISASI: Ubah status kerja karyawan menjadi 'bekerja'
            $karyawan->update(['status_kerja' => 'bekerja']);

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Berhasil Check-in! Selamat bekerja.',
                'data' => $presensi
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['status' => 'error', 'message' => $e->getMessage()], 500);
        }
    }

    // --- 2. PROSES CHECK-OUT (PULANG) ---
    public function checkOut(Request $request)
    {
        $user = $request->user();
        $karyawan = Karyawan::where('id_pengguna', $user->id)->first();
        $hariIni = Carbon::today()->toDateString();

        // Cari data check-in hari ini yang jam kembalinya masih kosong
        $presensi = Presensi::where('id_karyawan', $karyawan->id)
                             ->where('tanggal', $hariIni)
                             ->whereNull('kembali')
                             ->first();

        if (!$presensi) {
            return response()->json(['status' => 'error', 'message' => 'Anda belum check-in hari ini atau sudah melakukan check-out.'], 400);
        }

        DB::beginTransaction();
        try {
            // Update jam pulang
            $presensi->update([
                'kembali' => Carbon::now()->toTimeString()
            ]);

            // OTOMATISASI: Kembalikan status kerja karyawan menjadi 'bebas'
            $karyawan->update(['status_kerja' => 'bebas']);

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Berhasil Check-out! Hati-hati di jalan.',
                'data' => $presensi
            ], 200);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['status' => 'error', 'message' => $e->getMessage()], 500);
        }
    }

    // --- 3. PROSES IZIN / SAKIT ---
    public function izin(Request $request)
    {
        $user = $request->user();

        if ($user->role !== 'karyawan') {
            return response()->json(['status' => 'error', 'message' => 'Hanya karyawan yang bisa melakukan presensi.'], 403);
        }

        $karyawan = Karyawan::where('id_pengguna', $user->id)->first();
        $hariIni = Carbon::today()->toDateString();

        // Cek apakah hari ini sudah ada data presensi (hadir/sakit/alpa)
        $sudahPresensi = Presensi::where('id_karyawan', $karyawan->id)
                                 ->where('tanggal', $hariIni)
                                 ->first();

        if ($sudahPresensi) {
            return response()->json([
                'status' => 'error', 
                'message' => 'Anda sudah memiliki catatan presensi hari ini.'
            ], 400);
        }

        DB::beginTransaction();
        try {
            // Buat data presensi baru dengan status 'sakit' (sesuai ERD)
            $presensi = Presensi::create([
                'id_karyawan' => $karyawan->id,
                'tanggal' => $hariIni,
                'hadir' => Carbon::now()->toTimeString(), // Jam saat dia klik tombol izin
                'status' => 'sakit', 
                'koordinat' => null // Tidak perlu koordinat kalau izin
            ]);

            // OTOMATISASI: Ubah status kerja karyawan menjadi 'libur'
            $karyawan->update(['status_kerja' => 'libur']);

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Berhasil mencatat izin untuk hari ini.',
                'data' => $presensi
            ], 201);

        } catch (\Exception $e) {
            DB::rollBack();
            return response()->json(['status' => 'error', 'message' => $e->getMessage()], 500);
        }
    }

    // --- 4. REKAP PRESENSI HARI INI (Untuk Owner / Atasan) ---
    public function presensiHariIni(Request $request)
    {
        $user = $request->user();

        // 1. Validasi pastikan hanya Owner yang bisa akses
        if ($user->role !== 'pemilik') {
            return response()->json([
                'status' => 'error',
                'message' => 'Akses ditolak. Hanya pemilik yang dapat melihat rekap presensi.'
            ], 403);
        }

        // 2. Cari data Toko milik Owner ini
        $pemilik = \App\Models\Pemilik::where('id_pengguna', $user->id)->first();
        $toko = \App\Models\Toko::where('id_pemilik', $pemilik->id)->first();

        if (!$toko) {
            return response()->json([
                'status' => 'error',
                'message' => 'Toko tidak ditemukan.'
            ], 404);
        }

        $hariIni = Carbon::today()->toDateString();

        // 3. Ambil seluruh karyawan yang bekerja di toko ini
        $daftarKaryawan = Karyawan::where('id_Toko', $toko->id)->get();
        
        // 4. Petakan (Map) setiap karyawan dengan data presensi mereka hari ini
        $rekapDetail = $daftarKaryawan->map(function ($karyawan) use ($hariIni) {
            // Cari apakah karyawan ini sudah mengisi presensi hari ini
            $presensi = Presensi::where('id_karyawan', $karyawan->id)
                                 ->where('tanggal', $hariIni)
                                 ->first();

            // Tentukan status presensi untuk UI (Hadir, Sakit, atau Tidak Hadir jika belum absen)
            $statusPresensi = 'tidak hadir';
            $jamTercatat = null;

            if ($presensi) {
                $statusPresensi = $presensi->status;
                // Jika statusnya sakit/izin, gunakan jam pembuatan. Jika hadir, gunakan jam hadir.
                $jamTercatat = Carbon::parse($presensi->hadir)->format('H.i');
            }

            return [
                'id_karyawan' => $karyawan->id,
                'nama_lengkap' => $karyawan->nama_lengkap,
                'posisi' => $karyawan->posisi,
                'status_kerja' => $karyawan->status_kerja,
                'jam' => $jamTercatat,
                'status_presensi' => $statusPresensi // 'hadir', 'sakit', atau 'tidak hadir'
            ];
        });

        // 5. Hitung angka akumulasi untuk bagian "Ringkasan Kinerja" di Figma
        $totalKaryawan = $daftarKaryawan->count();
        $totalHadir = $rekapDetail->where('status_presensi', 'hadir')->count();
        $totalSakit = $rekapDetail->where('status_presensi', 'sakit')->count();
        $totalTidakHadir = $totalKaryawan - ($totalHadir + $totalSakit);

        return response()->json([
            'status' => 'success',
            'message' => 'Berhasil mengambil data rekap presensi hari ini.',
            'ringkasan' => [
                'tanggal_raw' => $hariIni,
                'total_karyawan' => $totalKaryawan,
                'total_hadir' => $totalHadir,
                'total_sakit' => $totalSakit,
                'total_tidak_hadir' => $totalTidakHadir,
            ],
            'data' => $rekapDetail
        ], 200);
    }
}