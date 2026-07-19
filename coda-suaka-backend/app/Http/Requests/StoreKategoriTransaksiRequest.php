<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StoreKategoriTransaksiRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     */
    public function authorize(): bool
    {
        return true;
    }

    /**
     * Get the validation rules that apply to the request.
     *
     * @return array<string, ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        $user = $this->user();

        return [
            'nama_kategori' => [
                'required',
                'string',
                'max:150',
                Rule::unique('kategori_transaksis')
                    ->where('instansi_id', $user->instansi_id)
                    ->where('tipe', $this->tipe),
            ],
            'tipe' => 'required|in:masuk,keluar',
            'sifat' => 'required|in:operasional,non_operasional',
            'termasuk_hpp' => 'sometimes|boolean',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_kategori.required' => 'Nama kategori wajib diisi.',
            'nama_kategori.unique' => 'Kategori dengan nama dan tipe yang sama sudah ada.',
            'tipe.required' => 'Tipe wajib dipilih.',
            'tipe.in' => 'Tipe harus masuk atau keluar.',
            'sifat.required' => 'Sifat wajib dipilih.',
            'sifat.in' => 'Sifat harus operasional atau non_operasional.',
        ];
    }
}
