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
        th, td { border: 1px solid #333; padding: 5px 8px; text-align: left; }
        th { background: #f0f0f0; font-weight: bold; }
        .text-right { text-align: right; }
        .sub-header { background: #e8e8e8; font-weight: bold; }
        .sub-item td:first-child { padding-left: 24px; }
        .total-row { font-weight: bold; background: #f9f9f9; }
        .laba-row { font-weight: bold; background: #d4edda; }
        .rugi-row { font-weight: bold; background: #f8d7da; }
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
                <th>Komponen</th>
                <th class="text-right">Jumlah (Rp)</th>
            </tr>
        </thead>
        <tbody>
            {{-- PENDAPATAN --}}
            <tr class="sub-header">
                <td colspan="2">PENDAPATAN</td>
            </tr>
            @forelse($pendapatan_per_kategori as $kategori => $jumlah)
            <tr class="sub-item">
                <td>{{ $kategori }}</td>
                <td class="text-right">{{ number_format($jumlah, 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr class="sub-item">
                <td><em>Tidak ada pendapatan</em></td>
                <td class="text-right">0</td>
            </tr>
            @endforelse
            <tr class="total-row">
                <td>Total Pendapatan</td>
                <td class="text-right">Rp {{ number_format($total_pendapatan, 0, ',', '.') }}</td>
            </tr>

            {{-- HPP --}}
            <tr class="sub-header">
                <td colspan="2">HARGA POKOK PENJUALAN (HPP)</td>
            </tr>
            @forelse($hpp_per_kategori as $kategori => $jumlah)
            <tr class="sub-item">
                <td>{{ $kategori }}</td>
                <td class="text-right">{{ number_format($jumlah, 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr class="sub-item">
                <td><em>Tidak ada HPP</em></td>
                <td class="text-right">0</td>
            </tr>
            @endforelse
            <tr class="total-row">
                <td>Total HPP</td>
                <td class="text-right">Rp {{ number_format($total_hpp, 0, ',', '.') }}</td>
            </tr>

            {{-- BEBAN OPERASIONAL --}}
            <tr class="sub-header">
                <td colspan="2">BEBAN OPERASIONAL</td>
            </tr>
            @forelse($beban_per_kategori as $kategori => $jumlah)
            <tr class="sub-item">
                <td>{{ $kategori }}</td>
                <td class="text-right">{{ number_format($jumlah, 0, ',', '.') }}</td>
            </tr>
            @empty
            <tr class="sub-item">
                <td><em>Tidak ada beban</em></td>
                <td class="text-right">0</td>
            </tr>
            @endforelse
            <tr class="total-row">
                <td>Total Beban Operasional</td>
                <td class="text-right">Rp {{ number_format($total_beban, 0, ',', '.') }}</td>
            </tr>

            {{-- PERHITUNGAN LABA/RUGI --}}
            <tr style="background: #f5f5f5;">
                <td><strong>Pendapatan - HPP - Beban</strong></td>
                <td class="text-right">
                    Rp {{ number_format($total_pendapatan, 0, ',', '.') }}
                    - Rp {{ number_format($total_hpp, 0, ',', '.') }}
                    - Rp {{ number_format($total_beban, 0, ',', '.') }}
                </td>
            </tr>
            <tr class="{{ $laba_bersih >= 0 ? 'laba-row' : 'rugi-row' }}">
                <td>{{ $laba_bersih >= 0 ? 'LABA BERSIH' : 'RUGI BERSIH' }}</td>
                <td class="text-right">Rp {{ number_format(abs($laba_bersih), 0, ',', '.') }}</td>
            </tr>
        </tbody>
    </table>

    <div class="footer">
        Dicetak pada: {{ $tanggal_cetak }}
    </div>
</body>
</html>
