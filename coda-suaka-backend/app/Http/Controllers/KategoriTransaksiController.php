<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreKategoriTransaksiRequest;
use App\Http\Requests\UpdateKategoriTransaksiRequest;
use App\Models\KategoriTransaksi;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class KategoriTransaksiController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(KategoriTransaksi::class, 'kategori_transaksi');
    }

    /**
     * GET /api/kategori-transaksis
     * Daftar kategori transaksi: template global + custom milik instansi user.
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = KategoriTransaksi::forInstansi($user->instansi_id)
            ->orderBy('instansi_id', 'asc')  // global muncul duluan
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
     * Membuat kategori custom untuk instansi user (bukan global).
     */
    public function store(StoreKategoriTransaksiRequest $request)
    {
        $user = $request->user();

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
    public function update(UpdateKategoriTransaksiRequest $request, KategoriTransaksi $kategori_transaksi)
    {
        // Cegah edit kategori global (hanya boleh oleh Super Admin nantinya)
        if ($kategori_transaksi->isGlobal()) {
            return $this->error('Kategori template global tidak dapat diedit', 422);
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
        // Cegah hapus kategori global (template)
        if ($kategori_transaksi->isGlobal()) {
            return $this->error('Kategori template global tidak dapat dihapus', 422);
        }

        $kategori_transaksi->delete();
        return $this->success(null, 'Kategori transaksi berhasil dihapus');
    }
}
