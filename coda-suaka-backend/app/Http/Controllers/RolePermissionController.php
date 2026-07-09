<?php

namespace App\Http\Controllers;

use App\Models\role_permission;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Gate;
use Illuminate\Support\Facades\Validator;

class RolePermissionController extends Controller
{
    public function __construct()
    {
    }

    public function index(Request $request)
    {
        Gate::authorize('manage-roles');

        $query = role_permission::with('role');
        if ($request->has('role_id')) {
            $query->where('role_id', $request->role_id);
        }
        $permissions = $query->orderBy('role_id')->get();
        return response()->json(['status' => 'success', 'data' => $permissions]);
    }

    public function store(Request $request)
    {
        Gate::authorize('manage-roles');

        $validator = Validator::make($request->all(), [
            'role_id' => 'required|exists:roles,id',
            'permission' => 'required|string|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        // Cek unique
        $exists = role_permission::where('role_id', $request->role_id)
            ->where('permission', $request->permission)
            ->exists();
        if ($exists) {
            return response()->json(['status' => 'error', 'message' => 'Permission sudah ada untuk role ini'], 409);
        }

        $perm = role_permission::create([
            'role_id' => $request->role_id,
            'permission' => $request->permission,
        ]);

        return response()->json(['status' => 'success', 'message' => 'Permission berhasil ditambahkan', 'data' => $perm], 201);
    }

    public function destroy(role_permission $role_permission)
    {
        Gate::authorize('manage-roles');
        $role_permission->delete();
        return response()->json(['status' => 'success', 'message' => 'Permission berhasil dihapus']);
    }

    /**
     * POST /api/role-permissions/sync
     * Sinkronisasi permissions untuk suatu role (replace all)
     */
    public function sync(Request $request)
    {
        Gate::authorize('manage-roles');

        $validator = Validator::make($request->all(), [
            'role_id' => 'required|exists:roles,id',
            'permissions' => 'required|array',
            'permissions.*' => 'string|max:100',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        try {
            DB::beginTransaction();

            role_permission::where('role_id', $request->role_id)->delete();

            $newPermissions = [];
            foreach ($request->permissions as $perm) {
                $newPermissions[] = role_permission::create([
                    'role_id' => $request->role_id,
                    'permission' => $perm,
                ]);
            }

            DB::commit();

            return response()->json([
                'status' => 'success',
                'message' => 'Permissions berhasil disinkronisasi',
                'data' => $newPermissions
            ]);
        } catch (\Throwable $e) {
            DB::rollBack();

            return response()->json([
                'status' => 'error',
                'message' => 'Gagal menyinkronisasi permissions: ' . $e->getMessage(),
            ], 500);
        }
    }
}
