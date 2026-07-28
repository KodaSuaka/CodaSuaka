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
        .summary { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .summary td { border: 1px solid #333; padding: 6px 10px; }
        .summary .label { background: #f0f0f0; font-weight: bold; width: 50%; }
        .summary .saldo-positif { color: #10731c; font-weight: bold; }
        .summary .saldo-negatif { color: #b00020; font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h1>{{ $judul }}</h1>
        <p>{{ $instansi }}</p>
        <p>Periode: {{ $periode }}</p>
    </div>

    <table class="summary">
        <tr>
            <td class="label">Total Pemasukan</td>
            <td class="text-right">Rp {{ number_format($total_masuk, 0, ',', '.') }}</td>
        </tr>
        <tr>
            <td class="label">Total Pengeluaran</td>
            <td class="text-right">Rp {{ number_format($total_keluar, 0, ',', '.') }}</td>
        </tr>
        <tr>
            <td class="label">Saldo Bersih</td>
            <td class="text-right {{ $saldo_bersih >= 0 ? 'saldo-positif' : 'saldo-negatif' }}">
                Rp {{ number_format($saldo_bersih, 0, ',', '.') }}
            </td>
        </tr>
    </table>

    <table>
        <thead>
            <tr>
                <th>No</th>
                <th>Tanggal</th>
                <th>Kategori</th>
                <th class="text-right">Masuk</th>
                <th class="text-right">Keluar</th>
                <th class="text-right">Saldo</th>
                <th>Metode</th>
                <th>Keterangan</th>
            </tr>
        </thead>
        <tbody>
            @forelse($transaksis as $index => $t)
            @php
                $nominal = $t['nominal'] ?? $t->nominal ?? 0;
                $tipe = $t['tipe'] ?? $t->tipe ?? '';
                $saldo = $t['saldo_berjalan'] ?? null;
                $tanggalRaw = $t['tanggal'] ?? $t->tanggal ?? null;
                $tanggalDisplay = $tanggalRaw ? \Carbon\Carbon::parse($tanggalRaw)->translatedFormat('d M Y') : '-';
            @endphp
            <tr>
                <td>{{ $index + 1 }}</td>
                <td>{{ $tanggalDisplay }}</td>
                <td>{{ $t['kategori_transaksi']['nama_kategori'] ?? $t['kategori'] ?? '-' }}</td>
                <td class="text-right">{{ $tipe === 'masuk' ? 'Rp '.number_format($nominal, 0, ',', '.') : '-' }}</td>
                <td class="text-right">{{ $tipe === 'keluar' ? 'Rp '.number_format($nominal, 0, ',', '.') : '-' }}</td>
                <td class="text-right">{{ $saldo !== null ? 'Rp '.number_format($saldo, 0, ',', '.') : '-' }}</td>
                <td>{{ $t['metode_pembayaran'] ?? $t->metode_pembayaran ?? '-' }}</td>
                <td>{{ $t['keterangan'] ?? $t->keterangan ?? '' }}</td>
            </tr>
            @empty
            <tr><td colspan="8" style="text-align:center;">Tidak ada data transaksi</td></tr>
            @endforelse
        </tbody>
    </table>
    <p style="font-size: 10px; color: #777; margin-top: 4px;">
        *Saldo dihitung berjalan mulai dari awal periode laporan ini (bukan saldo total akun).
    </p>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }}
    </div>
</body>
</html>
