<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>{{ $judul }}</title>
    <style>
        body { font-family: Arial, sans-serif; font-size: 12px; }
        .header { text-align: center; margin-bottom: 20px; }
        .header h1 { margin: 0; font-size: 18px; }
        .header p { margin: 2px 0; font-size: 12px; color: #555; }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { border: 1px solid #333; padding: 6px 8px; text-align: left; }
        th { background: #f0f0f0; font-weight: bold; }
        .text-right { text-align: right; }
        .footer { margin-top: 20px; font-size: 10px; color: #777; text-align: center; }
    </style>
</head>
<body>
    <div class="header">
        <h1>{{ $judul }}</h1>
        <p>{{ $instansi }}</p>
        <p>Periode: {{ $periode }}</p>
    </div>

    <table>
        <thead>
            <tr>
                <th>No</th>
                <th>Tanggal</th>
                <th>Kategori</th>
                <th>Tipe</th>
                <th class="text-right">Nominal</th>
                <th>Metode</th>
                <th>Keterangan</th>
            </tr>
        </thead>
        <tbody>
            @forelse($transaksis as $index => $t)
            <tr>
                <td>{{ $index + 1 }}</td>
                <td>{{ $t['tanggal'] ?? $t->tanggal ?? '-' }}</td>
                <td>{{ $t['kategori_transaksi']['nama_kategori'] ?? $t['kategori'] ?? '-' }}</td>
                <td>{{ ($t['tipe'] ?? $t->tipe ?? '') === 'masuk' ? 'Masuk' : 'Keluar' }}</td>
                <td class="text-right">Rp {{ number_format($t['nominal'] ?? $t->nominal ?? 0, 0, ',', '.') }}</td>
                <td>{{ $t['metode_pembayaran'] ?? $t->metode_pembayaran ?? '-' }}</td>
                <td>{{ $t['keterangan'] ?? $t->keterangan ?? '' }}</td>
            </tr>
            @empty
            <tr><td colspan="7" style="text-align:center;">Tidak ada data transaksi</td></tr>
            @endforelse
        </tbody>
    </table>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }}
    </div>
</body>
</html>
