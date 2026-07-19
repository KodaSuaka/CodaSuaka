<?php

namespace App\Http\Controllers;

use App\Http\Requests\Storerole_permissionRequest;
use App\Http\Requests\SyncRolePermissionRequest;
use App\Models\role_permission;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Gate;

class RolePermissionController extends Controller
{
    use ApiResponse;

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
        return $this->success($permissions);
    }

    public function store(Storerole_permissionRequest $request)
    {
        Gate::authorize('manage-roles');

        // Cek unique
        $exists = role_permission::where('role_id', $request->role_id)
            ->where('permission', $request->permission)
            ->exists();
        if ($exists) {
            return $this->error('Permission sudah ada untuk role ini', 409);
        }

        $perm = role_permission::create([
            'role_id' => $request->role_id,
            'permission' => $request->permission,
        ]);

        return $this->success($perm, 'Permission berhasil ditambahkan', 201);
    }

    public function destroy(role_permission $role_permission)
    {
        Gate::authorize('manage-roles');
        $role_permission->delete();
        return $this->success(null, 'Permission berhasil dihapus');
    }

    /**
     * POST /api/role-permissions/sync
     * Sinkronisasi permissions untuk suatu role (replace all)
     */
    public function sync(SyncRolePermissionRequest $request)
    {
        Gate::authorize('manage-roles');

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

            return $this->success($newPermissions, 'Permissions berhasil disinkronisasi');
        } catch (\Throwable $e) {
            DB::rollBack();

            return $this->error('Gagal menyinkronisasi permissions: ' . $e->getMessage(), 500);
        }
    }
}
