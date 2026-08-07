<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreStokRequest extends FormRequest
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
            'nama' => 'required|string|max:150',
            'kategori' => 'nullable|string|max:50',
            'satuan' => 'required|string|max:50',
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
            'nama.max' => 'Nama stok maksimal 150 karakter.',
            'satuan.required' => 'Satuan wajib diisi.',
            'stok.numeric' => 'Stok harus berupa angka.',
            'stok.min' => 'Stok tidak boleh negatif.',
            'stok_minimum.numeric' => 'Stok minimum harus berupa angka.',
            'harga_beli.numeric' => 'Harga beli harus berupa angka.',
        ];
    }
}
