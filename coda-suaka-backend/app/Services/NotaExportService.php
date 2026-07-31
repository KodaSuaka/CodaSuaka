<?php

namespace App\Services;

use App\Models\Nota;
use Barryvdh\DomPDF\Facade\Pdf;

class NotaExportService
{
    /**
     * Render nota (penjualan/pembelian) ke PDF untuk dicetak.
     */
    public function generateNotaPdf(Nota $nota)
    {
        $nota->load('items', 'outlet', 'instansi');
        $pdf = Pdf::loadView('kasir.nota_pdf', [
            'nota' => $nota,
            'tanggal_cetak' => now()->translatedFormat('d M Y H:i'),
        ]);

        return $pdf->stream("nota-{$nota->nomor_nota}.pdf");
    }
}
