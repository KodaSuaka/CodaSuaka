<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreroleRequest;
use App\Http\Requests\UpdateroleRequest;
use App\Models\role;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;

class RoleController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
    }

    public function index(Request $request)
    {
        // Semua user authenticated boleh lihat daftar roles (data referensi)
        // CRUD lainnya tetap dilindungi Gate 'manage-roles'
        $user = $request->user();
        if (!Gate::allows('manage-roles') && !app(\App\Services\PermissionService::class)->userHasPermission($user, 'manage:karyawan')) {
            return $this->success([]);
        }
        $roles = role::orderBy('nama_role')->get();
        return $this->success($roles);
    }

    public function store(StoreroleRequest $request)
    {
        Gate::authorize('manage-roles');

        $role = role::create(['nama_role' => $request->nama_role]);
        return $this->success($role, 'Role berhasil ditambahkan', 201);
    }

    public function show(role $role)
    {
        Gate::authorize('manage-roles');
        return $this->success($role->load('permissions'));
    }

    public function update(UpdateroleRequest $request, role $role)
    {
        Gate::authorize('manage-roles');

        $role->update(['nama_role' => $request->nama_role]);
        return $this->success($role, 'Role berhasil diperbarui');
    }

    public function destroy(role $role)
    {
        Gate::authorize('manage-roles');
        $role->delete();
        return $this->success(null, 'Role berhasil dihapus');
    }
}
