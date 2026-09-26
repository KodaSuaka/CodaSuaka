<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class ImportNotaPembelianRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        $user = $this->user();

        return [
            'file' => 'required|file|mimes:xlsx|max:5120',
            'tanggal' => ['required', 'date', 'before_or_equal:today'],
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'pihak_terkait' => 'nullable|string|max:150',
            'metode_pembayaran' => 'nullable|string|max:100',
            'kategori_transaksi_id' => [
                'nullable',
                Rule::exists('kategori_transaksis', 'id')
                    ->where(function ($query) use ($user) {
                        $query->whereNull('instansi_id')
                            ->orWhere('instansi_id', $user->instansi_id);
                    }),
            ],
            'catatan' => 'nullable|string',
        ];
    }

    public function messages(): array
    {
        return [
            'file.required' => 'File wajib diunggah.',
            'file.mimes' => 'File harus berformat .xlsx.',
            'file.max' => 'Ukuran file maksimal 5MB.',
            'tanggal.required' => 'Tanggal wajib diisi.',
            'tanggal.before_or_equal' => 'Tanggal tidak boleh melebihi hari ini.',
            'outlet_id.exists' => 'Outlet tidak valid.',
            'kategori_transaksi_id.exists' => 'Kategori transaksi tidak valid.',
        ];
    }
}
