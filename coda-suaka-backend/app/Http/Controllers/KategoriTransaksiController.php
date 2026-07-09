<?php

namespace App\Http\Controllers;

use App\Models\KategoriTransaksi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;

class KategoriTransaksiController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(KategoriTransaksi::class, 'kategori_transaksi');
    }

    /**
     * GET /api/kategori-transaksis
     * Daftar kategori transaksi milik instansi user.
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = KategoriTransaksi::where('instansi_id', $user->instansi_id)
            ->orderBy('tipe')
            ->orderBy('nama_kategori');

        // Filter by tipe
        if ($request->has('tipe')) {
            $query->where('tipe', $request->tipe);
        }

        // Filter only active
        if ($request->boolean('active_only')) {
            $query->where('is_active', true);
        }

        $kategoris = $query->get();

        return $this->success($kategoris);
    }

    /**
     * POST /api/kategori-transaksis
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'nama_kategori' => 'required|string|max:150',
            'tipe' => 'required|in:masuk,keluar',
            'sifat' => 'required|in:operasional,non_operasional',
            'termasuk_hpp' => 'sometimes|boolean',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $kategori = KategoriTransaksi::create([
            'instansi_id' => $user->instansi_id,
            'nama_kategori' => $request->nama_kategori,
            'tipe' => $request->tipe,
            'sifat' => $request->sifat,
            'termasuk_hpp' => $request->boolean('termasuk_hpp', false),
            'is_default' => false,
            'is_active' => true,
        ]);

        return $this->success($kategori, 'Kategori transaksi berhasil ditambahkan', 201);
    }

    /**
     * GET /api/kategori-transaksis/{kategori_transaksi}
     */
    public function show(KategoriTransaksi $kategori_transaksi)
    {
        return $this->success($kategori_transaksi);
    }

    /**
     * PUT /api/kategori-transaksis/{kategori_transaksi}
     */
    public function update(Request $request, KategoriTransaksi $kategori_transaksi)
    {
        $validator = Validator::make($request->all(), [
            'nama_kategori' => 'sometimes|required|string|max:150',
            'tipe' => 'sometimes|required|in:masuk,keluar',
            'sifat' => 'sometimes|required|in:operasional,non_operasional',
            'termasuk_hpp' => 'sometimes|boolean',
            'is_active' => 'sometimes|boolean',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $kategori_transaksi->update($request->only([
            'nama_kategori', 'tipe', 'sifat', 'termasuk_hpp', 'is_active',
        ]));

        return $this->success($kategori_transaksi, 'Kategori transaksi berhasil diperbarui');
    }

    /**
     * DELETE /api/kategori-transaksis/{kategori_transaksi}
     */
    public function destroy(KategoriTransaksi $kategori_transaksi)
    {
        // Cegah hapus kategori default
        if ($kategori_transaksi->is_default) {
            return $this->error('Kategori default tidak dapat dihapus', 422);
        }

        $kategori_transaksi->delete();
        return $this->success(null, 'Kategori transaksi berhasil dihapus');
    }
}
