<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreStokMutationRequest;
use App\Http\Requests\StoreStokRequest;
use App\Http\Requests\UpdateStokRequest;
use App\Models\Stok;
use App\Services\StokService;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class StokController extends Controller
{
    use ApiResponse;

    public function __construct(
        private StokService $stokService,
    ) {
        $this->authorizeResource(Stok::class, 'stok');
    }

    /**
     * GET /api/stoks
     */
    public function index(Request $request)
    {
        $query = Stok::query();

        if ($request->has('kategori')) {
            $query->where('kategori', $request->kategori);
        }

        if ($request->has('is_active')) {
            $query->where('is_active', $request->boolean('is_active'));
        }

        if ($request->has('search')) {
            $search = $request->search;
            $query->where(fn ($q) => $q
                ->where('nama', 'like', "%{$search}%")
                ->orWhere('kategori', 'like', "%{$search}%"));
        }

        return $this->paginated($query->paginate($request->integer('per_page', 50)));
    }

    /**
     * POST /api/stoks
     */
    public function store(StoreStokRequest $request)
    {
        $data = $request->validated();

        $stok = Stok::create([
            ...$data,
            'stok' => $data['stok'] ?? 0,
            'instansi_id' => $request->user()->instansi_id,
        ]);

        return $this->success($stok, 'Stok berhasil ditambahkan', 201);
    }

    /**
     * GET /api/stoks/{stok}
     */
    public function show(Stok $stok)
    {
        return $this->success($stok);
    }

    /**
     * PUT /api/stoks/{stok}
     */
    public function update(UpdateStokRequest $request, Stok $stok)
    {
        $data = $request->validated();

        // Update langsung kolom stok (tanpa mutasi) TIDAK diperbolehkan.
        // Stok hanya berubah lewat endpoint mutasi agar audit trail terjaga.
        unset($data['stok']);

        $stok->update($data);

        return $this->success($stok, 'Stok berhasil diperbarui');
    }

    /**
     * DELETE /api/stoks/{stok}
     */
    public function destroy(Stok $stok)
    {
        if ($stok->mutations()->exists()) {
            return $this->error('Stok sudah memiliki riwayat mutasi, tidak bisa dihapus. Nonaktifkan saja (is_active=false).', 422);
        }

        $stok->delete();

        return $this->success(null, 'Berhasil dihapus');
    }

    /**
     * POST /api/stoks/{stok}/mutasi
     */
    public function mutasi(StoreStokMutationRequest $request, Stok $stok)
    {
        $this->authorize('mutate', $stok);

        try {
            $mutation = $this->stokService->mutasi(
                stok: $stok,
                jenis: $request->jenis,
                jumlah: $request->jumlah,
                user: $request->user(),
                keterangan: $request->keterangan,
            );
        } catch (\InvalidArgumentException $e) {
            return $this->error($e->getMessage(), 422);
        }

        return $this->success($mutation, 'Mutasi stok berhasil');
    }

    /**
     * GET /api/stoks/{stok}/riwayat
     */
    public function riwayat(Request $request, Stok $stok)
    {
        $this->authorize('viewMutations', $stok);

        $query = $stok->mutations()->latest();

        if ($request->has('jenis')) {
            $query->where('jenis', $request->jenis);
        }

        return $this->paginated($query->paginate($request->integer('per_page', 50)));
    }
}
