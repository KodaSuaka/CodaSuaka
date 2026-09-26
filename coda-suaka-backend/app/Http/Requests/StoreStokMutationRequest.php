<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreStokMutationRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    /**
     * @return array<string, ValidationRule|array<mixed>|string>
     */
    public function rules(): array
    {
        return [
            'jenis' => 'required|in:masuk,keluar,penyesuaian',
            'jumlah' => 'required|numeric|gt:0',
            'keterangan' => 'nullable|string|max:255',
        ];
    }

    public function messages(): array
    {
        return [
            'jenis.required' => 'Jenis mutasi wajib dipilih.',
            'jenis.in' => 'Jenis mutasi harus masuk, keluar, atau penyesuaian.',
            'jumlah.required' => 'Jumlah wajib diisi.',
            'jumlah.numeric' => 'Jumlah harus berupa angka.',
            'jumlah.gt' => 'Jumlah harus lebih dari 0.',
            'keterangan.max' => 'Keterangan maksimal 255 karakter.',
        ];
    }
}
