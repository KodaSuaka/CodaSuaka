<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Models\instansi;
use App\Models\paket;
use App\Models\karyawan;
use App\Models\role;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;

class SuperAdminController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:sanctum');
        $this->middleware('super.admin');
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD INSTANSI
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/super-admin/instansis
     * Menampilkan semua instansi beserta pemilik (owner) dan paket.
     */
    public function indexInstansi()
    {
        $instansis = instansi::with(['paket', 'users' => function ($q) {
            $q->whereHas('role', fn($r) => $r->where('nama_role', 'Owner'))
              ->with('profilKaryawan');
        }])->withCount('outlets')->orderBy('created_at', 'desc')->get();

        return response()->json([
            'status' => 'success',
            'data' => $instansis
        ]);
    }

    /**
     * GET /api/super-admin/instansis/{instansi}
     * Detail satu instansi.
     */
    public function showInstansi(instansi $instansi)
    {
        $instansi->load(['paket', 'users' => function ($q) {
            $q->with('profilKaryawan');
        }, 'outlets']);

        return response()->json([
            'status' => 'success',
            'data' => $instansi
        ]);
    }

    /**
     * POST /api/super-admin/instansis
     * Membuat instansi baru beserta owner-nya.
     */
    public function storeInstansi(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_instansi' => 'required|string|max:255',
            'paket_id' => 'nullable|exists:pakets,id',
            'owner_name' => 'required|string|max:255',
            'owner_email' => 'required|email|unique:users,email',
            'owner_password' => 'required|string|min:6',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        // Buat instansi
        $instansi = instansi::create([
            'nama_instansi' => $request->nama_instansi,
            'paket_id' => $request->paket_id,
        ]);

        // Buat owner
        $roleOwner = role::where('nama_role', 'Owner')->first();
        if (!$roleOwner) {
            return response()->json([
                'status' => 'error',
                'message' => 'Role Owner belum tersedia'
            ], 500);
        }

        $user = User::create([
            'name' => $request->owner_name,
            'email' => $request->owner_email,
            'password' => Hash::make($request->owner_password),
            'role_id' => $roleOwner->id,
            'instansi_id' => $instansi->id,
            'outlet_id' => null,
        ]);

        karyawan::create([
            'user_id' => $user->id,
            'nama_lengkap' => $request->owner_name,
            'kontak' => null,
            'foto_profil' => null,
        ]);

        $instansi->load(['paket', 'users' => fn($q) => $q->with('profilKaryawan')]);

        return response()->json([
            'status' => 'success',
            'message' => 'Instansi dan Owner berhasil dibuat',
            'data' => $instansi
        ], 201);
    }

    /**
     * PUT /api/super-admin/instansis/{instansi}
     * Update data instansi.
     */
    public function updateInstansi(Request $request, instansi $instansi)
    {
        $validator = Validator::make($request->all(), [
            'nama_instansi' => 'sometimes|required|string|max:255',
            'paket_id' => 'nullable|exists:pakets,id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $instansi->update($request->only(['nama_instansi', 'paket_id']));

        $instansi->load('paket');

        return response()->json([
            'status' => 'success',
            'message' => 'Instansi berhasil diperbarui',
            'data' => $instansi
        ]);
    }

    /**
     * DELETE /api/super-admin/instansis/{instansi}
     * Hapus instansi (soft — hapus semua data terkait).
     */
    public function destroyInstansi(instansi $instansi)
    {
        // Hapus semua user terkait
        $instansi->users()->delete();
        // Hapus semua outlet terkait
        $instansi->outlets()->delete();
        // Hapus instansi
        $instansi->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Instansi beserta data terkait berhasil dihapus'
        ]);
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD OWNER (User dengan role Owner)
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/super-admin/owners
     * Menampilkan semua owner (bisa filter berdasarkan instansi).
     */
    public function indexOwner(Request $request)
    {
        $query = User::whereHas('role', fn($q) => $q->where('nama_role', 'Owner'))
            ->with(['instansi', 'profilKaryawan']);

        if ($request->instansi_id) {
            $query->where('instansi_id', $request->instansi_id);
        }

        $owners = $query->orderBy('created_at', 'desc')->get();

        return response()->json([
            'status' => 'success',
            'data' => $owners
        ]);
    }

    /**
     * GET /api/super-admin/owners/{user}
     * Detail satu owner.
     */
    public function showOwner(User $user)
    {
        if ($user->role->nama_role !== 'Owner') {
            return response()->json([
                'status' => 'error',
                'message' => 'User bukan seorang Owner'
            ], 404);
        }

        $user->load(['instansi', 'profilKaryawan']);

        return response()->json([
            'status' => 'success',
            'data' => $user
        ]);
    }

    /**
     * POST /api/super-admin/owners
     * Menambahkan owner baru ke instansi yang sudah ada.
     */
    public function storeOwner(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:6',
            'instansi_id' => 'required|exists:instansis,id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $roleOwner = role::where('nama_role', 'Owner')->first();
        if (!$roleOwner) {
            return response()->json([
                'status' => 'error',
                'message' => 'Role Owner belum tersedia'
            ], 500);
        }

        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'password' => Hash::make($request->password),
            'role_id' => $roleOwner->id,
            'instansi_id' => $request->instansi_id,
            'outlet_id' => null,
        ]);

        karyawan::create([
            'user_id' => $user->id,
            'nama_lengkap' => $request->name,
            'kontak' => null,
            'foto_profil' => null,
        ]);

        $user->load(['instansi', 'profilKaryawan']);

        return response()->json([
            'status' => 'success',
            'message' => 'Owner berhasil ditambahkan',
            'data' => $user
        ], 201);
    }

    /**
     * PUT /api/super-admin/owners/{user}
     * Update data owner.
     */
    public function updateOwner(Request $request, User $user)
    {
        if ($user->role->nama_role !== 'Owner') {
            return response()->json([
                'status' => 'error',
                'message' => 'User bukan seorang Owner'
            ], 404);
        }

        $validator = Validator::make($request->all(), [
            'name' => 'sometimes|required|string|max:255',
            'email' => 'sometimes|required|email|unique:users,email,' . $user->id,
            'password' => 'sometimes|required|string|min:6',
            'instansi_id' => 'sometimes|required|exists:instansis,id',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $data = $request->only(['name', 'email', 'instansi_id']);
        if ($request->filled('password')) {
            $data['password'] = Hash::make($request->password);
        }

        $user->update($data);

        // Update nama_lengkap di profil karyawan jika name diubah
        if ($request->filled('name')) {
            karyawan::where('user_id', $user->id)->update([
                'nama_lengkap' => $request->name
            ]);
        }

        $user->load(['instansi', 'profilKaryawan']);

        return response()->json([
            'status' => 'success',
            'message' => 'Owner berhasil diperbarui',
            'data' => $user
        ]);
    }

    /**
     * DELETE /api/super-admin/owners/{user}
     * Hapus owner.
     */
    public function destroyOwner(User $user)
    {
        if ($user->role->nama_role !== 'Owner') {
            return response()->json([
                'status' => 'error',
                'message' => 'User bukan seorang Owner'
            ], 404);
        }

        // Hapus profil karyawan & user
        karyawan::where('user_id', $user->id)->delete();
        $user->delete();

        return response()->json([
            'status' => 'success',
            'message' => 'Owner berhasil dihapus'
        ]);
    }

    // ══════════════════════════════════════════════════════════════
    //  CRUD PAKET (manajemen paket)
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/super-admin/pakets
     * Semua paket (termasuk yang non-aktif).
     */
    public function indexPaket()
    {
        $pakets = paket::orderBy('harga', 'asc')->get();

        return response()->json([
            'status' => 'success',
            'data' => $pakets
        ]);
    }

    /**
     * GET /api/super-admin/pakets/{paket}
     */
    public function showPaket(paket $paket)
    {
        return response()->json([
            'status' => 'success',
            'data' => $paket
        ]);
    }

    /**
     * POST /api/super-admin/pakets
     */
    public function storePaket(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nama_paket' => 'required|string|max:255',
            'harga' => 'required|numeric|min:0',
            'deskripsi' => 'nullable|string',
            'fitur' => 'nullable|string',
            'durasi_hari' => 'required|integer|min:1',
            'max_outlet' => 'nullable|integer|min:1',
            'max_karyawan_per_outlet' => 'nullable|integer|min:1',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $paket = paket::create($request->all());

        return response()->json([
            'status' => 'success',
            'message' => 'Paket berhasil ditambahkan',
            'data' => $paket
        ], 201);
    }

    /**
     * PUT /api/super-admin/pakets/{paket}
     */
    public function updatePaket(Request $request, paket $paket)
    {
        $validator = Validator::make($request->all(), [
            'nama_paket' => 'sometimes|required|string|max:255',
            'harga' => 'sometimes|required|numeric|min:0',
            'deskripsi' => 'nullable|string',
            'fitur' => 'nullable|string',
            'durasi_hari' => 'sometimes|required|integer|min:1',
            'max_outlet' => 'nullable|integer|min:1',
            'max_karyawan_per_outlet' => 'nullable|integer|min:1',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'status' => 'error',
                'message' => 'Validasi gagal',
                'errors' => $validator->errors()
            ], 422);
        }

        $paket->update($request->all());

        return response()->json([
            'status' => 'success',
            'message' => 'Paket berhasil diperbarui',
            'data' => $paket
        ]);
    }

    /**
     * DELETE /api/super-admin/pakets/{paket}
     * Non-aktifkan paket (soft delete).
     */
    public function destroyPaket(paket $paket)
    {
        $paket->update(['is_active' => false]);

        return response()->json([
            'status' => 'success',
            'message' => 'Paket berhasil dinonaktifkan'
        ]);
    }

    // ══════════════════════════════════════════════════════════════
    //  DASHBOARD SUPER ADMIN
    // ══════════════════════════════════════════════════════════════

    /**
     * GET /api/super-admin/dashboard
     * Statistik untuk Super Admin.
     */
    public function dashboard()
    {
        $totalInstansi = instansi::count();
        $totalOwner = User::whereHas('role', fn($q) => $q->where('nama_role', 'Owner'))->count();
        $totalPaket = paket::count();
        $totalPaketAktif = paket::where('is_active', true)->count();
        $totalKaryawan = User::whereHas('role', fn($q) => $q->where('nama_role', 'Karyawan'))->count();

        return response()->json([
            'status' => 'success',
            'data' => [
                'total_instansi' => $totalInstansi,
                'total_owner' => $totalOwner,
                'total_paket' => $totalPaket,
                'total_paket_aktif' => $totalPaketAktif,
                'total_karyawan' => $totalKaryawan,
            ]
        ]);
    }
}
