<?php

namespace App\Http\Controllers;

use App\Models\role;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Facades\Validator;

class RoleController extends Controller
{
    public function __construct()
    {
    }

    public function index(Request $request)
    {
<<<<<<< Updated upstream
        // Semua user authenticated boleh lihat daftar roles (data referensi)
        // CRUD lainnya tetap dilindungi Gate 'manage-roles'
=======
        $user = $request->user();
        if (!Gate::allows('manage-roles') && !app(\App\Services\PermissionService::class)->userHasPermission($user, 'manage:karyawan')) {
            return response()->json(['status' => 'success', 'data' => []]);
        }
>>>>>>> Stashed changes
        $roles = role::orderBy('nama_role')->get();
        return response()->json(['status' => 'success', 'data' => $roles]);
    }

    public function store(Request $request)
    {
        Gate::authorize('manage-roles');

        $validator = Validator::make($request->all(), [
            'nama_role' => 'required|string|max:50|unique:roles,nama_role',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $role = role::create(['nama_role' => $request->nama_role]);
        return response()->json(['status' => 'success', 'message' => 'Role berhasil ditambahkan', 'data' => $role], 201);
    }

    public function show(role $role)
    {
        Gate::authorize('manage-roles');
        return response()->json(['status' => 'success', 'data' => $role->load('permissions')]);
    }

    public function update(Request $request, role $role)
    {
        Gate::authorize('manage-roles');

        $validator = Validator::make($request->all(), [
            'nama_role' => 'required|string|max:50|unique:roles,nama_role,' . $role->id,
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $role->update(['nama_role' => $request->nama_role]);
        return response()->json(['status' => 'success', 'message' => 'Role berhasil diperbarui', 'data' => $role]);
    }

    public function destroy(role $role)
    {
        Gate::authorize('manage-roles');
        $role->delete();
        return response()->json(['status' => 'success', 'message' => 'Role berhasil dihapus']);
    }
}
