<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdateStokRequest extends FormRequest
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
            'nama' => 'sometimes|required|string|max:150',
            'kategori' => 'nullable|string|max:50',
            'satuan' => 'sometimes|required|string|max:50',
            'stok' => 'nullable|numeric|min:0',
            'stok_minimum' => 'nullable|numeric|min:0',
            'harga_beli' => 'nullable|numeric|min:0',
            'is_active' => 'nullable|boolean',
            'keterangan' => 'nullable|string',
        ];
    }

    public function messages(): array
    {
        return [
            'nama.required' => 'Nama stok wajib diisi.',
            'satuan.required' => 'Satuan wajib diisi.',
            'stok.numeric' => 'Stok harus berupa angka.',
            'stok.min' => 'Stok tidak boleh negatif.',
        ];
    }
}
