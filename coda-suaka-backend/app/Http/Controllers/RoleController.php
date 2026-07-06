<?php

namespace App\Http\Controllers;

use App\Models\role;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class RoleController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:sanctum');
    }

    public function index()
    {
        $roles = role::orderBy('nama_role')->get();
        return response()->json(['status' => 'success', 'data' => $roles]);
    }

    public function store(Request $request)
    {
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
        return response()->json(['status' => 'success', 'data' => $role->load('permissions')]);
    }

    public function update(Request $request, role $role)
    {
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
        $role->delete();
        return response()->json(['status' => 'success', 'message' => 'Role berhasil dihapus']);
    }
}
