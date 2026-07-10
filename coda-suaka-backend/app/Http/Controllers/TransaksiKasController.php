<?php

namespace App\Http\Controllers;

use App\Models\TransaksiKas;
use App\Traits\ApiResponse;
use Carbon\Carbon;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

class TransaksiKasController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(TransaksiKas::class, 'transaksi_kas');
    }

    /**
     * GET /api/transaksi-kas
     * Daftar entri Buku Kas dengan filter.
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = TransaksiKas::with(['kategoriTransaksi', 'outlet', 'createdBy'])
            ->where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by tipe (masuk/keluar)
        if ($request->has('tipe')) {
            $query->where('tipe', $request->tipe);
        }

        // Filter by kategori
        if ($request->has('kategori_transaksi_id')) {
            $query->where('kategori_transaksi_id', $request->kategori_transaksi_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        // Default order: newest first
        $query->orderBy('tanggal', 'desc')->orderBy('created_at', 'desc');

        $transaksis = $query->paginate($request->get('per_page', 50));

        return $this->paginated($transaksis);
    }

    /**
     * POST /api/transaksi-kas
     */
    public function store(Request $request)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'tanggal' => 'required|date',
            'tipe' => 'required|in:masuk,keluar',
            'nominal' => 'required|numeric|min:0',
            'kategori_transaksi_id' => [
                'nullable',
                Rule::exists('kategori_transaksis', 'id')
                    ->where(function ($query) use ($user) {
                        // Boleh pilih kategori global (instansi_id = null)
                        // atau kategori custom milik instansi user
                        $query->whereNull('instansi_id')
                            ->orWhere('instansi_id', $user->instansi_id);
                    }),
            ],
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'metode_pembayaran' => 'nullable|string|max:100',
            'keterangan' => 'nullable|string',
            'lampiran_url' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $transaksi = TransaksiKas::create([
            'instansi_id' => $user->instansi_id,
            'outlet_id' => $request->outlet_id,
            'kategori_transaksi_id' => $request->kategori_transaksi_id,
            'tanggal' => $request->tanggal,
            'tipe' => $request->tipe,
            'nominal' => $request->nominal,
            'metode_pembayaran' => $request->metode_pembayaran,
            'keterangan' => $request->keterangan,
            'lampiran_url' => $request->lampiran_url,
            'created_by' => $user->id,
        ]);

        $transaksi->load(['kategoriTransaksi', 'outlet', 'createdBy']);

        return $this->success($transaksi, 'Entri kas berhasil ditambahkan', 201);
    }

    /**
     * GET /api/transaksi-kas/{transaksi_kas}
     */
    public function show(TransaksiKas $transaksi_kas)
    {
        $transaksi_kas->load(['kategoriTransaksi', 'outlet', 'createdBy']);
        return $this->success($transaksi_kas);
    }

    /**
     * PUT /api/transaksi-kas/{transaksi_kas}
     */
    public function update(Request $request, TransaksiKas $transaksi_kas)
    {
        $user = $request->user();

        $validator = Validator::make($request->all(), [
            'tanggal' => 'sometimes|required|date',
            'tipe' => 'sometimes|required|in:masuk,keluar',
            'nominal' => 'sometimes|required|numeric|min:0',
            'kategori_transaksi_id' => [
                'nullable',
                Rule::exists('kategori_transaksis', 'id')
                    ->where(function ($query) use ($user) {
                        $query->whereNull('instansi_id')
                            ->orWhere('instansi_id', $user->instansi_id);
                    }),
            ],
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'metode_pembayaran' => 'nullable|string|max:100',
            'keterangan' => 'nullable|string',
            'lampiran_url' => 'nullable|string|max:255',
        ]);

        if ($validator->fails()) {
            return $this->error('Validasi gagal', 422, $validator->errors());
        }

        $transaksi_kas->update($request->only([
            'tanggal', 'tipe', 'nominal', 'kategori_transaksi_id',
            'outlet_id', 'metode_pembayaran', 'keterangan', 'lampiran_url',
        ]));

        $transaksi_kas->load(['kategoriTransaksi', 'outlet', 'createdBy']);

        return $this->success($transaksi_kas, 'Entri kas berhasil diperbarui');
    }

    /**
     * DELETE /api/transaksi-kas/{transaksi_kas}
     */
    public function destroy(TransaksiKas $transaksi_kas)
    {
        // Cegah hapus entri yang berasal dari dokumen transaksi (nota/invoice)
        if ($transaksi_kas->dokumen_transaksi_id !== null) {
            return $this->error('Entri kas dari nota/invoice tidak dapat dihapus langsung. Hapus dokumen asalnya.', 422);
        }

        $transaksi_kas->delete();
        return $this->success(null, 'Entri kas berhasil dihapus');
    }

    /**
     * GET /api/transaksi-kas/saldo
     * Hitung saldo (Total Masuk - Total Keluar) untuk suatu periode.
     */
    public function saldo(Request $request)
    {
        $this->authorize('viewAny', TransaksiKas::class);

        $user = $request->user();

        $query = TransaksiKas::where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        $totalMasuk = (float) $query->clone()->where('tipe', 'masuk')->sum('nominal');
        $totalKeluar = (float) $query->clone()->where('tipe', 'keluar')->sum('nominal');

        return $this->success([
            'total_masuk' => $totalMasuk,
            'total_keluar' => $totalKeluar,
            'saldo_akhir' => $totalMasuk - $totalKeluar,
            'start_date' => $request->start_date,
            'end_date' => $request->end_date,
        ]);
    }

    /**
     * GET /api/transaksi-kas/laporan-lab rugi
     * Hitung Pendapatan - Beban HPP - Beban Operasional = Laba/Rugi.
     */
    public function labaRugi(Request $request)
    {
        $this->authorize('viewAny', TransaksiKas::class);

        $user = $request->user();

        $query = TransaksiKas::where('instansi_id', $user->instansi_id);

        // Filter by outlet
        if ($request->has('outlet_id')) {
            $query->where('outlet_id', $request->outlet_id);
        }

        // Filter by date range
        if ($request->has('start_date')) {
            $query->whereDate('tanggal', '>=', $request->start_date);
        }
        if ($request->has('end_date')) {
            $query->whereDate('tanggal', '<=', $request->end_date);
        }

        $transaksis = $query->with('kategoriTransaksi')->get();

        $pendapatan = 0;
        $hpp = 0;
        $bebanOperasional = 0;

        foreach ($transaksis as $t) {
            $kategori = $t->kategoriTransaksi;
            if (!$kategori) continue;

            if ($kategori->sifat === 'non_operasional') {
                continue; // skip modal, prive, pinjaman
            }

            if ($t->tipe === 'masuk') {
                $pendapatan += (float) $t->nominal;
            } elseif ($t->tipe === 'keluar') {
                if ($kategori->termasuk_hpp) {
                    $hpp += (float) $t->nominal;
                } else {
                    $bebanOperasional += (float) $t->nominal;
                }
            }
        }

        $labaRugi = $pendapatan - $hpp - $bebanOperasional;

        return $this->success([
            'pendapatan' => $pendapatan,
            'hpp' => $hpp,
            'beban_operasional' => $bebanOperasional,
            'laba_rugi' => $labaRugi,
            'start_date' => $request->start_date,
            'end_date' => $request->end_date,
        ]);
    }
}
