<?php

namespace App\Http\Controllers;

use App\Http\Requests\ImportNotaPembelianRequest;
use App\Http\Requests\StoreNotaRequest;
use App\Models\Nota;
use App\Services\KasirService;
use App\Services\NotaExportService;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;

class NotaController extends Controller
{
    use ApiResponse;

    protected KasirService $kasirService;

    protected NotaExportService $notaExportService;

    public function __construct(KasirService $kasirService, NotaExportService $notaExportService)
    {
        $this->kasirService = $kasirService;
        $this->notaExportService = $notaExportService;
    }

    /**
     * GET /api/nota
     */
    public function index(Request $request)
    {
        $this->authorize('viewAny', Nota::class);

        $query = Nota::query();

        if ($request->has('tipe')) {
            $query->where('tipe', $request->tipe);
        }

        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        if ($request->has('status')) {
            $query->where('status', $request->status);
        }

        if ($request->filled('start_date')) {
            $query->where('tanggal', '>=', $request->start_date);
        }

        if ($request->filled('end_date')) {
            $query->where('tanggal', '<=', $request->end_date);
        }

        $query->orderBy('tanggal', 'desc')->orderBy('created_at', 'desc');

        return $this->paginated($query->paginate($request->integer('per_page', 50)));
    }

    /**
     * POST /api/nota
     */
    public function store(StoreNotaRequest $request)
    {
        $this->authorize('create', Nota::class);

        try {
            if ($request->tipe === 'penjualan') {
                $nota = $this->kasirService->buatNotaPenjualan($request->validated(), $request->user());

                return $this->success($nota, 'Nota penjualan berhasil dibuat', 201);
            }

            $nota = $this->kasirService->buatNotaPembelian($request->validated(), $request->user());

            return $this->success($nota, 'Nota pembelian berhasil dibuat', 201);
        } catch (ValidationException $e) {
            return $this->error(collect($e->errors())->flatten()->first() ?? $e->getMessage(), 422);
        }
    }

    /**
     * POST /api/nota/import
     */
    public function import(ImportNotaPembelianRequest $request)
    {
        $this->authorize('import', Nota::class);

        try {
            $nota = $this->kasirService->importNotaPembelian($request->file('file'), $request->except('file'), $request->user());

            return $this->success($nota->load('items'), 'Nota pembelian berhasil diimpor', 201);
        } catch (ValidationException $e) {
            return $this->error(collect($e->errors())->flatten()->first() ?? $e->getMessage(), 422);
        }
    }

    /**
     * GET /api/nota/{nota}
     */
    public function show(Nota $nota)
    {
        $this->authorize('view', $nota);

        $nota->load('items.barangJasa', 'outlet', 'kategoriTransaksi', 'transaksiKas', 'createdByUser');

        return $this->success($nota);
    }

    /**
     * GET /api/nota/{nota}/pdf
     */
    public function cetak(Nota $nota)
    {
        $this->authorize('export', $nota);

        return $this->notaExportService->generateNotaPdf($nota);
    }
}
