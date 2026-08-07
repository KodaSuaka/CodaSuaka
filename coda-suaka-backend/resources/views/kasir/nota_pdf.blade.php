<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Nota {{ $nota->nomor_nota }}</title>
    <style>
        body { font-family: Arial, sans-serif; font-size: 12px; }
        .header { text-align: center; margin-bottom: 20px; }
        .header h1 { margin: 0; font-size: 18px; }
        .header p { margin: 2px 0; font-size: 12px; color: #555; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #333; padding: 6px 8px; text-align: left; }
        th { background: #f0f0f0; font-weight: bold; }
        .text-right { text-align: right; }
        .total-row td { font-weight: bold; }
        .footer { margin-top: 20px; font-size: 10px; color: #777; text-align: center; }
    </style>
    @include('laporan.watermark')
</head>
<body>
    <div class="header">
        <h1>{{ $nota->instansi->nama_instansi ?? '' }}</h1>
        <p>NOTA {{ strtoupper($nota->tipe) }}</p>
        <p>No. Nota: {{ $nota->nomor_nota }}</p>
        <p>Tanggal: {{ \Carbon\Carbon::parse($nota->tanggal)->translatedFormat('d M Y') }}</p>
    </div>

    <table>
        <thead>
            <tr>
                <th>No</th>
                <th>Nama</th>
                <th>Jenis</th>
                <th class="text-right">Kuantitas</th>
                <th>Satuan</th>
                <th class="text-right">Harga Satuan</th>
                <th class="text-right">Subtotal</th>
            </tr>
        </thead>
        <tbody>
            @forelse($nota->items as $index => $item)
            <tr>
                <td>{{ $index + 1 }}</td>
                <td>{{ $item->nama_item }}</td>
                <td>{{ ucfirst($item->jenis) }}</td>
                <td class="text-right">{{ $item->kuantitas }}</td>
                <td>{{ $item->satuan }}</td>
                <td class="text-right">Rp {{ number_format($item->harga_satuan, 0, ',', '.') }}</td>
                <td class="text-right">Rp {{ number_format($item->subtotal, 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr><td colspan="7" style="text-align:center;">Tidak ada item</td></tr>
            @endforelse
            <tr class="total-row">
                <td colspan="6" class="text-right">Total</td>
                <td class="text-right">Rp {{ number_format($nota->total, 0, ',', '.') }}</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }}
    </div>
</body>
</html>
