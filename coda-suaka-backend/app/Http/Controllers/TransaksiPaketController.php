<?php

namespace App\Http\Controllers;

use App\Http\Requests\Storetransaksi_paketRequest;
use App\Http\Requests\Updatetransaksi_paketRequest;
use App\Models\transaksi_paket;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class TransaksiPaketController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(transaksi_paket::class, 'transaksi_paket');
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
        return $this->success($transaksis);
    }

    /**
     * POST /api/transaksi-pakets
     */
    public function store(Storetransaksi_paketRequest $request)
    {
        $user = $request->user();

        $transaksi = transaksi_paket::create([
            'instansi_id' => $user->instansi_id,
            'paket_id' => $request->paket_id,
            'tanggal_mulai' => $request->tanggal_mulai,
            'tanggal_berakhir' => $request->tanggal_berakhir,
            'total_harga' => $request->total_harga,
            'status' => $request->status ?? 'pending',
        ]);

        $transaksi->load(['instansi', 'paket']);

        return $this->success($transaksi, 'Transaksi paket berhasil ditambahkan', 201);
    }

    /**
     * GET /api/transaksi-pakets/{transaksi_paket}
     */
    public function show(transaksi_paket $transaksi_paket)
    {
        $transaksi_paket->load(['instansi', 'paket']);
        return $this->success($transaksi_paket);
    }

    /**
     * PUT /api/transaksi-pakets/{transaksi_paket}
     */
    public function update(Updatetransaksi_paketRequest $request, transaksi_paket $transaksi_paket)
    {
        $transaksi_paket->update($request->only(['status', 'bukti_pembayaran']));
        $transaksi_paket->load(['instansi', 'paket']);

        return $this->success($transaksi_paket, 'Transaksi berhasil diperbarui');
    }
}
