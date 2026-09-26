<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreNotaRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        $user = $this->user();

        return [
            'tipe' => 'required|in:penjualan,pembelian',
            'tanggal' => ['required', 'date', 'before_or_equal:today'],
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'kategori_transaksi_id' => [
                'nullable',
                Rule::exists('kategori_transaksis', 'id')
                    ->where(function ($query) use ($user) {
                        $query->whereNull('instansi_id')
                            ->orWhere('instansi_id', $user->instansi_id);
                    }),
            ],
            'pihak_terkait' => 'nullable|string|max:150',
            'metode_pembayaran' => 'nullable|string|max:100',
            'catatan' => 'nullable|string',
            'items' => 'required|array|min:1',
            'items.*.barang_jasa_id' => [
                'nullable',
                Rule::exists('barang_jasas', 'id')->where('instansi_id', $user->instansi_id),
            ],
            // Item produksi menunjuk stok_id → diarahkan ke tabel Stok (bukan BarangJasa).
            'items.*.stok_id' => [
                'nullable',
                Rule::exists('stoks', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'items.*.nama_item' => 'required_without_all:items.*.barang_jasa_id,items.*.stok_id|string|max:150',
            'items.*.jenis' => 'required_without_all:items.*.barang_jasa_id,items.*.stok_id|in:barang,jasa',
            'items.*.kuantitas' => 'required|numeric|min:0.01',
            'items.*.satuan' => 'nullable|string|max:50',
            'items.*.harga_satuan' => 'required|numeric|min:0',
        ];
    }

    public function messages(): array
    {
        return [
            'tipe.required' => 'Tipe nota wajib dipilih.',
            'tipe.in' => 'Tipe nota harus penjualan atau pembelian.',
            'tanggal.required' => 'Tanggal wajib diisi.',
            'tanggal.before_or_equal' => 'Tanggal tidak boleh melebihi hari ini.',
            'outlet_id.exists' => 'Outlet tidak valid.',
            'kategori_transaksi_id.exists' => 'Kategori transaksi tidak valid.',
            'items.required' => 'Minimal 1 item wajib diisi.',
            'items.min' => 'Minimal 1 item wajib diisi.',
            'items.*.barang_jasa_id.exists' => 'Barang/jasa tidak valid.',
            'items.*.nama_item.required_without' => 'Nama item wajib diisi jika tidak memilih dari katalog.',
            'items.*.jenis.required_without' => 'Jenis item wajib diisi jika tidak memilih dari katalog.',
            'items.*.kuantitas.required' => 'Kuantitas wajib diisi.',
            'items.*.harga_satuan.required' => 'Harga satuan wajib diisi.',
        ];
    }
}
