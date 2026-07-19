<?php

namespace App\Http\Requests;

use App\Models\TransaksiKas;
use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreTransaksiKasRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        $user = $this->user();

        return [
            'tanggal' => 'required|date|before_or_equal:today',
            'tipe' => 'required|in:masuk,keluar',
            'nominal' => [
                'required',
                'numeric',
                'min:0',
                'max:' . TransaksiKas::NOMINAL_MAX,
            ],
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
            'metode_pembayaran' => [
                'nullable',
                'string',
                'max:100',
                Rule::in(TransaksiKas::METODE_PEMBAYARAN_VALID),
            ],
            'keterangan' => 'nullable|string|max:1000',
            'lampiran_url' => 'nullable|string|max:255',
        ];
    }

    public function messages(): array
    {
        return [
            'tanggal.required' => 'Tanggal transaksi wajib diisi.',
            'tanggal.date' => 'Format tanggal tidak valid.',
            'tanggal.before_or_equal' => 'Tanggal transaksi tidak boleh melebihi hari ini.',
            'tipe.required' => 'Tipe transaksi wajib dipilih.',
            'tipe.in' => 'Tipe transaksi harus masuk atau keluar.',
            'nominal.required' => 'Nominal wajib diisi.',
            'nominal.numeric' => 'Nominal harus berupa angka.',
            'nominal.min' => 'Nominal minimal 0.',
            'nominal.max' => 'Nominal melebihi batas maksimum.',
            'kategori_transaksi_id.exists' => 'Kategori transaksi tidak valid.',
            'outlet_id.exists' => 'Outlet tidak valid.',
            'metode_pembayaran.in' => 'Metode pembayaran tidak valid.',
            'keterangan.max' => 'Keterangan maksimal 1000 karakter.',
            'lampiran_url.max' => 'URL lampiran maksimal 255 karakter.',
        ];
    }
}
