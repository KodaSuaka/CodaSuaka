<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Invoice {{ $nomor_invoice }}</title>
    <style>
        body { font-family: Arial, sans-serif; font-size: 12px; color: #222; }
        .header { text-align: center; margin-bottom: 18px; border-bottom: 2px solid #333; padding-bottom: 10px; }
        .header h1 { margin: 0; font-size: 22px; }
        .header p { margin: 3px 0; color: #555; letter-spacing: 1px; }
        table.meta { width: 100%; margin: 16px 0; }
        table.meta td { vertical-align: top; padding: 2px 0; }
        table.items { width: 100%; border-collapse: collapse; margin-top: 8px; }
        table.items th, table.items td { border: 1px solid #333; padding: 8px; text-align: left; }
        table.items th { background: #f0f0f0; font-weight: bold; }
        .text-right { text-align: right; }
        .total-row td { font-weight: bold; font-size: 14px; background: #fafafa; }
        .footer { margin-top: 28px; font-size: 10px; color: #777; text-align: center; border-top: 1px solid #ccc; padding-top: 8px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>CodaSuaka</h1>
        <p>INVOICE PEMBELIAN PAKET</p>
    </div>

    <table class="meta">
        <tr>
            <td style="width:55%">
                <strong>Ditagihkan kepada:</strong><br>
                {{ $trx->instansi->nama_instansi ?? '-' }}<br>
                @if($owner)
                    {{ $owner->name }}<br>
                    {{ $owner->email }}
                @endif
            </td>
            <td style="width:45%" class="text-right">
                <strong>No. Invoice:</strong> {{ $nomor_invoice }}<br>
                <strong>Tanggal:</strong> {{ \Carbon\Carbon::parse($trx->created_at)->translatedFormat('d M Y') }}<br>
                <strong>Status:</strong> {{ strtoupper($trx->status) }}
            </td>
        </tr>
    </table>

    <table class="items">
        <thead>
            <tr>
                <th>Paket</th>
                <th>Masa Aktif</th>
                <th>Periode Langganan</th>
                <th class="text-right">Harga</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>{{ $trx->paket->nama_paket ?? '-' }}</td>
                <td>{{ $trx->paket->durasi_hari ?? '-' }} hari</td>
                <td>
                    {{ \Carbon\Carbon::parse($trx->tanggal_mulai)->translatedFormat('d M Y') }}
                    &mdash;
                    {{ $trx->tanggal_berakhir ? \Carbon\Carbon::parse($trx->tanggal_berakhir)->translatedFormat('d M Y') : '-' }}
                </td>
                <td class="text-right">Rp {{ number_format($trx->total_harga, 0, ',', '.') }}</td>
            </tr>
            <tr class="total-row">
                <td colspan="3" class="text-right">TOTAL</td>
                <td class="text-right">Rp {{ number_format($trx->total_harga, 0, ',', '.') }}</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }} &mdash; Dokumen ini dihasilkan otomatis oleh sistem CodaSuaka Super Admin.
    </div>
</body>
</html>
