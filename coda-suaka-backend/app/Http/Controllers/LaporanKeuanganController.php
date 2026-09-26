<?php

namespace App\Http\Controllers;

use App\Http\Requests\ExportTemplateLaporanRequest;
use App\Services\LaporanTemplateExportService;

class LaporanKeuanganController extends Controller
{
    public function __construct(
        private LaporanTemplateExportService $exportService,
    ) {}

    /**
     * GET /api/laporan-keuangan/template/export
     * Unduh template laporan keuangan (Laba Rugi / Arus Kas) ter-prefill dari
     * transaksi periode, mengikuti format divisi keuangan. Otorisasi & validasi
     * di ExportTemplateLaporanRequest.
     */
    public function exportTemplate(ExportTemplateLaporanRequest $request)
    {
        $user = $request->user();

        $path = $this->exportService->generateToFile(
            $user->instansi,
            $request->string('jenis')->toString(),
            $request->string('tipe_usaha')->toString(),
            $request->integer('bulan'),
            $request->integer('tahun'),
        );

        $namaFile = sprintf(
            'laporan_%s_%s_%d_%d.xlsx',
            $request->string('jenis'),
            $request->string('tipe_usaha'),
            $request->integer('bulan'),
            $request->integer('tahun'),
        );

        return response()->download($path, $namaFile)->deleteFileAfterSend(true);
    }
}
