<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreinstansiRequest;
use App\Http\Requests\StoreOwnerRequest;
use App\Http\Requests\UpdateOwnerRequest;
use App\Http\Requests\UpdateInstansiRequest;
use App\Http\Requests\StorepaketRequest;
use App\Http\Requests\UpdatepaketRequest;
use App\Models\User;
use App\Models\instansi;
use App\Models\paket;
use App\Models\karyawan;
use App\Models\role;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class SuperAdminController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
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

        return $this->success($instansis);
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

        return $this->success($instansi);
    }

    /**
     * POST /api/super-admin/instansis
     * Membuat instansi baru beserta owner-nya.
     */
    public function storeInstansi(StoreinstansiRequest $request)
    {
        // Buat instansi
        $instansi = instansi::create([
            'nama_instansi' => $request->nama_instansi,
            'paket_id' => $request->paket_id,
        ]);

        // Buat owner
        $roleOwner = role::where('nama_role', 'Owner')->first();
        if (!$roleOwner) {
            return $this->error('Role Owner belum tersedia', 500);
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

        return $this->success($instansi, 'Instansi dan Owner berhasil dibuat', 201);
    }

    /**
     * PUT /api/super-admin/instansis/{instansi}
     * Update data instansi.
     */
    public function updateInstansi(UpdateInstansiRequest $request, instansi $instansi)
    {
        $instansi->update($request->only(['nama_instansi', 'paket_id']));

        $instansi->load('paket');

        return $this->success($instansi, 'Instansi berhasil diperbarui');
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

        return $this->success(null, 'Instansi beserta data terkait berhasil dihapus');
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

        return $this->success($owners);
    }

    /**
     * GET /api/super-admin/owners/{user}
     * Detail satu owner.
     */
    public function showOwner(User $user)
    {
        if (!$user->role || $user->role->nama_role !== 'Owner') {
            return $this->error('User bukan seorang Owner', 404);
        }

        $user->load(['instansi', 'profilKaryawan']);

        return $this->success($user);
    }

    /**
     * POST /api/super-admin/owners
     * Menambahkan owner baru ke instansi yang sudah ada.
     */
    public function storeOwner(StoreOwnerRequest $request)
    {
        $roleOwner = role::where('nama_role', 'Owner')->first();
        if (!$roleOwner) {
            return $this->error('Role Owner belum tersedia', 500);
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

        return $this->success($user, 'Owner berhasil ditambahkan', 201);
    }

    /**
     * PUT /api/super-admin/owners/{user}
     * Update data owner.
     */
    public function updateOwner(UpdateOwnerRequest $request, User $user)
    {
        if (!$user->role || $user->role->nama_role !== 'Owner') {
            return $this->error('User bukan seorang Owner', 404);
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

        return $this->success($user, 'Owner berhasil diperbarui');
    }

    /**
     * DELETE /api/super-admin/owners/{user}
     * Hapus owner.
     */
    public function destroyOwner(User $user)
    {
        if (!$user->role || $user->role->nama_role !== 'Owner') {
            return $this->error('User bukan seorang Owner', 404);
        }

        // Hapus profil karyawan & user
        karyawan::where('user_id', $user->id)->delete();
        $user->delete();

        return $this->success(null, 'Owner berhasil dihapus');
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

        return $this->success($pakets);
    }

    /**
     * GET /api/super-admin/pakets/{paket}
     */
    public function showPaket(paket $paket)
    {
        return $this->success($paket);
    }

    /**
     * POST /api/super-admin/pakets
     */
    public function storePaket(StorepaketRequest $request)
    {
        $paket = paket::create($request->only([
            'nama_paket', 'harga', 'deskripsi', 'fitur',
            'durasi_hari', 'max_outlet', 'max_karyawan_per_outlet', 'is_active',
        ]));

        return $this->success($paket, 'Paket berhasil ditambahkan', 201);
    }

    /**
     * PUT /api/super-admin/pakets/{paket}
     */
    public function updatePaket(UpdatepaketRequest $request, paket $paket)
    {
        $paket->update($request->only([
            'nama_paket', 'harga', 'deskripsi', 'fitur',
            'durasi_hari', 'max_outlet', 'max_karyawan_per_outlet', 'is_active',
        ]));

        return $this->success($paket, 'Paket berhasil diperbarui');
    }

    /**
     * DELETE /api/super-admin/pakets/{paket}
     * Non-aktifkan paket (soft delete).
     */
    public function destroyPaket(paket $paket)
    {
        $paket->delete();

        return $this->success(null, 'Paket berhasil dihapus');
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

        return $this->success([
            'total_instansi' => $totalInstansi,
            'total_owner' => $totalOwner,
            'total_paket' => $totalPaket,
            'total_paket_aktif' => $totalPaketAktif,
            'total_karyawan' => $totalKaryawan,
        ]);
    }
}
