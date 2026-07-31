<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreBarangJasaRequest;
use App\Http\Requests\UpdateBarangJasaRequest;
use App\Models\BarangJasa;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class BarangJasaController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(BarangJasa::class, 'barang_jasa');
    }

    /**
     * GET /api/barang-jasas
     */
    public function index(Request $request)
    {
        $query = BarangJasa::query();

        if ($request->has('jenis')) {
            $query->where('jenis', $request->jenis);
        }

        if ($request->has('is_active')) {
            $query->where('is_active', $request->boolean('is_active'));
        }

        return $this->paginated($query->paginate($request->integer('per_page', 50)));
    }

    /**
     * POST /api/barang-jasas
     */
    public function store(StoreBarangJasaRequest $request)
    {
        $barangJasa = BarangJasa::create([
            ...$request->validated(),
            'instansi_id' => $request->user()->instansi_id,
        ]);

        return $this->success($barangJasa, 'Barang/jasa berhasil ditambahkan', 201);
    }

    /**
     * GET /api/barang-jasas/{barang_jasa}
     */
    public function show(BarangJasa $barang_jasa)
    {
        return $this->success($barang_jasa);
    }

    /**
     * PUT /api/barang-jasas/{barang_jasa}
     */
    public function update(UpdateBarangJasaRequest $request, BarangJasa $barang_jasa)
    {
        $barang_jasa->update($request->validated());

        return $this->success($barang_jasa, 'Barang/jasa berhasil diperbarui');
    }

    /**
     * DELETE /api/barang-jasas/{barang_jasa}
     */
    public function destroy(BarangJasa $barang_jasa)
    {
        if ($barang_jasa->notaItems()->exists()) {
            return $this->error('Barang/jasa sudah dipakai di nota, tidak bisa dihapus.', 422);
        }

        $barang_jasa->delete();

        return $this->success(null, 'Berhasil dihapus');
    }
}
