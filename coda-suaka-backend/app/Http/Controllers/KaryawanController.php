<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorekaryawanRequest;
use App\Http\Requests\UpdatekaryawanRequest;
use App\Models\karyawan;
use App\Models\User;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;

class KaryawanController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(karyawan::class, 'karyawan', ['except' => ['me']]);
    }

    /**
     * GET /api/karyawans?outlet_id=xxx
     * Daftar karyawan dalam instansi user
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = karyawan::with(['user.role', 'outlet']);

        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        $karyawans = $query->orderBy('nama_lengkap')->get();

        return $this->success($karyawans);
    }

    /**
     * GET /api/karyawans/me
     * Profil karyawan yang sedang login
     */
    public function me(Request $request)
    {
        $karyawan = karyawan::where('user_id', $request->user()->id)
            ->with(['user.role', 'outlet'])
            ->first();

        if (!$karyawan) {
            return $this->error('Profil karyawan tidak ditemukan', 404);
        }

        return $this->success($karyawan);
    }

    /**
     * POST /api/karyawans
     * Tambah karyawan baru (dengan user account)
     */
    public function store(StorekaryawanRequest $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        $result = DB::transaction(function () use ($request, $instansiId) {
            // Buat user account
            $newUser = User::create([
                'name' => $request->nama_lengkap,
                'email' => $request->email,
                'password' => bcrypt($request->password),
                'role_id' => $request->role_id,
                'instansi_id' => $instansiId,
                'outlet_id' => $request->outlet_id,
            ]);

            // Buat profil karyawan
            $karyawan = karyawan::create([
                'user_id' => $newUser->id,
                'nama_lengkap' => $request->nama_lengkap,
                'kontak' => $request->kontak,
                'alamat' => $request->alamat,
                'foto_profil' => null,
                'outlet_id' => $request->outlet_id,
                'sisa_cuti' => $request->sisa_cuti ?? 0,
            ]);

            $karyawan->load(['user.role', 'outlet']);

            return $karyawan;
        });

        return $this->success($result, 'Karyawan berhasil ditambahkan', 201);
    }

    /**
     * GET /api/karyawans/{karyawan}
     */
    public function show(karyawan $karyawan)
    {
        $karyawan->load(['user.role', 'outlet', 'divisi', 'anggotaDivisis']);
        return $this->success($karyawan);
    }

    /**
     * PUT /api/karyawans/{karyawan}
     */
    public function update(UpdatekaryawanRequest $request, karyawan $karyawan)
    {
        $karyawan->update($request->only([
            'nama_lengkap', 'kontak', 'alamat', 'outlet_id', 'sisa_cuti', 'foto_profil'
        ]));

        $karyawan->load(['user.role', 'outlet']);

        return $this->success($karyawan, 'Karyawan berhasil diperbarui');
    }

    /**
     * DELETE /api/karyawans/{karyawan}
     */
    public function destroy(karyawan $karyawan)
    {
        DB::transaction(function () use ($karyawan) {
            // Hapus profil karyawan terlebih dahulu, baru user (karena cascade)
            $karyawan->delete();
            $karyawan->user()->delete();
        });

        return $this->success(null, 'Karyawan berhasil dihapus');
    }
}
