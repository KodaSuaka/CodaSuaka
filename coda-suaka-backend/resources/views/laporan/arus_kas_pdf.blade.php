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
        .sub-header { background: #e8e8e8; font-weight: bold; }
        .total-row { font-weight: bold; background: #f9f9f9; }
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
                <th>Aktivitas / Kategori</th>
                <th class="text-right">Masuk</th>
                <th class="text-right">Keluar</th>
                <th class="text-right">Bersih</th>
            </tr>
        </thead>
        <tbody>
            {{-- Arus Kas Operasi --}}
            <tr class="sub-header">
                <td>Arus Kas dari Aktivitas Operasi</td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            @forelse($data['detail_operasi'] ?? [] as $item)
            <tr>
                <td style="padding-left:20px;">{{ $item['kategori'] }}</td>
                <td class="text-right">Rp {{ number_format($item['masuk'] ?? 0, 0, ',', '.') }}</td>
                <td class="text-right">Rp {{ number_format($item['keluar'] ?? 0, 0, ',', '.') }}</td>
                <td class="text-right">Rp {{ number_format(($item['masuk'] ?? 0) - ($item['keluar'] ?? 0), 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr><td colspan="4" style="text-align:center;">Tidak ada transaksi operasi</td></tr>
            @endforelse
            <tr class="total-row">
                <td>Total Arus Kas Operasi</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['arus_kas_operasi'] ?? 0, 0, ',', '.') }}</td>
            </tr>

            {{-- Arus Kas Investasi --}}
            <tr class="sub-header">
                <td>Arus Kas dari Aktivitas Investasi</td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            <tr class="total-row">
                <td>Total Arus Kas Investasi</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['arus_kas_investasi'] ?? 0, 0, ',', '.') }}</td>
            </tr>

            {{-- Arus Kas Pendanaan --}}
            <tr class="sub-header">
                <td>Arus Kas dari Aktivitas Pendanaan</td>
                <td></td>
                <td></td>
                <td></td>
            </tr>
            @forelse($data['detail_pendanaan'] ?? [] as $item)
            <tr>
                <td style="padding-left:20px;">{{ $item['kategori'] }}</td>
                <td class="text-right">Rp {{ number_format($item['masuk'] ?? 0, 0, ',', '.') }}</td>
                <td class="text-right">Rp {{ number_format($item['keluar'] ?? 0, 0, ',', '.') }}</td>
                <td class="text-right">Rp {{ number_format(($item['masuk'] ?? 0) - ($item['keluar'] ?? 0), 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr><td colspan="4" style="text-align:center;">Tidak ada transaksi pendanaan</td></tr>
            @endforelse
            <tr class="total-row">
                <td>Total Arus Kas Pendanaan</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['arus_kas_pendanaan'] ?? 0, 0, ',', '.') }}</td>
            </tr>

            {{-- Total --}}
            <tr class="total-row">
                <td>Kenaikan Bersih Kas</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['kenaikan_bersih_kas'] ?? 0, 0, ',', '.') }}</td>
            </tr>
            <tr>
                <td>Saldo Awal</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['saldo_awal'] ?? 0, 0, ',', '.') }}</td>
            </tr>
            <tr class="total-row">
                <td>Saldo Akhir</td>
                <td></td>
                <td></td>
                <td class="text-right">Rp {{ number_format($data['saldo_akhir'] ?? 0, 0, ',', '.') }}</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }}
    </div>
</body>
</html>
