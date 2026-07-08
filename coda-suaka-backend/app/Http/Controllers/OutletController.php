<?php

namespace App\Http\Controllers;

use App\Models\outlet;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class OutletController extends Controller
{

    /**
     * GET /api/outlets?instansi_id=xxx
     * Ambil semua outlet milik instansi user (atau filter by instansi_id param)
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $instansiId = $request->get('instansi_id', $user->instansi_id);

        // Owner/Admin boleh lihat semua outlet instansinya
        $outlets = outlet::where('instansi_id', $instansiId)
            ->orderBy('nama_outlet')
            ->get();

        return response()->json([
            'status' => 'success',
            'data' => $outlets
        ]);
    }

    /**
     * POST /api/outlets
     */
    public function store(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        $validator = Validator::make($request->all(), [
            'nama_outlet' => 'required|string|max:150',
            'alamat_outlet' => 'nullable|string',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $outlet = outlet::create([
            'nama_outlet' => $request->nama_outlet,
            'alamat_outlet' => $request->alamat_outlet,
            'instansi_id' => $instansiId,
            'is_active' => $request->is_active ?? true,
        ]);

        return response()->json([
            'status' => 'success',
            'message' => 'Outlet berhasil ditambahkan',
            'data' => $outlet
        ], 201);
    }

    /**
     * GET /api/outlets/{outlet}
     */
    public function show(Request $request, outlet $outlet)
    {
        if ($outlet->instansi_id !== $request->user()->instansi_id) {
            return response()->json(['status' => 'error', 'message' => 'Forbidden'], 403);
        }
        return response()->json(['status' => 'success', 'data' => $outlet]);
    }

    /**
     * PUT /api/outlets/{outlet}
     */
    public function update(Request $request, outlet $outlet)
    {
        if ($outlet->instansi_id !== $request->user()->instansi_id) {
            return response()->json(['status' => 'error', 'message' => 'Forbidden'], 403);
        }

        $validator = Validator::make($request->all(), [
            'nama_outlet' => 'sometimes|required|string|max:150',
            'alamat_outlet' => 'nullable|string',
            'is_active' => 'boolean',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $outlet->update($request->only(['nama_outlet', 'alamat_outlet', 'is_active']));

        return response()->json([
            'status' => 'success',
            'message' => 'Outlet berhasil diperbarui',
            'data' => $outlet
        ]);
    }

    /**
     * DELETE /api/outlets/{outlet}
     */
    public function destroy(Request $request, outlet $outlet)
    {
        if ($outlet->instansi_id !== $request->user()->instansi_id) {
            return response()->json(['status' => 'error', 'message' => 'Forbidden'], 403);
        }
        $outlet->delete();
        return response()->json(['status' => 'success', 'message' => 'Outlet berhasil dihapus']);
    }
}
