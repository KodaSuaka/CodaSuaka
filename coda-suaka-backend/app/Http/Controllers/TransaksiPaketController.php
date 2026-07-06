<?php

namespace App\Http\Controllers;

use App\Models\transaksi_paket;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class TransaksiPaketController extends Controller
{
    public function __construct()
    {
        $this->middleware('auth:sanctum');
    }

    /**
     * GET /api/transaksi-pakets
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $query = transaksi_paket::with(['instansi', 'paket']);

        // Filter by instansi
        if ($request->has('instansi_id')) {
            $query->where('instansi_id', $request->instansi_id);
        } else {
            $query->where('instansi_id', $user->instansi_id);
        }

        $transaksis = $query->orderBy('created_at', 'desc')->get();
        return response()->json(['status' => 'success', 'data' => $transaksis]);
    }

    /**
     * POST /api/transaksi-pakets
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'paket_id' => 'required|exists:pakets,id',
            'tanggal_mulai' => 'required|date',
            'tanggal_berakhir' => 'nullable|date|after:tanggal_mulai',
            'total_harga' => 'required|numeric|min:0',
            'status' => 'sometimes|in:pending,aktif,kedaluwarsa,dibatalkan',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $transaksi = transaksi_paket::create([
            'instansi_id' => $user->instansi_id,
            'paket_id' => $request->paket_id,
            'tanggal_mulai' => $request->tanggal_mulai,
            'tanggal_berakhir' => $request->tanggal_berakhir,
            'total_harga' => $request->total_harga,
            'status' => $request->status ?? 'pending',
        ]);

        $transaksi->load(['instansi', 'paket']);

        return response()->json([
            'status' => 'success',
            'message' => 'Transaksi paket berhasil ditambahkan',
            'data' => $transaksi
        ], 201);
    }

    /**
     * GET /api/transaksi-pakets/{transaksi_paket}
     */
    public function show(transaksi_paket $transaksi_paket)
    {
        $transaksi_paket->load(['instansi', 'paket']);
        return response()->json(['status' => 'success', 'data' => $transaksi_paket]);
    }

    /**
     * PUT /api/transaksi-pakets/{transaksi_paket}
     */
    public function update(Request $request, transaksi_paket $transaksi_paket)
    {
        $validator = Validator::make($request->all(), [
            'status' => 'sometimes|required|in:pending,aktif,kedaluwarsa,dibatalkan',
            'bukti_pembayaran' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json(['status' => 'error', 'message' => 'Validasi gagal', 'errors' => $validator->errors()], 422);
        }

        $transaksi_paket->update($request->only(['status', 'bukti_pembayaran']));
        $transaksi_paket->load(['instansi', 'paket']);

        return response()->json([
            'status' => 'success',
            'message' => 'Transaksi berhasil diperbarui',
            'data' => $transaksi_paket
        ]);
    }
}
