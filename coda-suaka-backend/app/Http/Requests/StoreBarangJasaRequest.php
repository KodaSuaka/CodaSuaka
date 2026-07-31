<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreBarangJasaRequest extends FormRequest
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
        return [
            'nama' => 'required|string|max:150',
            'jenis' => 'required|in:barang,jasa',
            'kategori' => 'nullable|string|max:50',
            'satuan' => 'required|string|max:50',
            'harga_jual' => 'required|numeric|min:0',
            'harga_beli' => 'nullable|numeric|min:0',
            'stok' => 'nullable|integer|min:0',
            'is_active' => 'nullable|boolean',
            'keterangan' => 'nullable|string',
        ];
    }

    public function messages(): array
    {
        return [
            'nama.required' => 'Nama wajib diisi.',
            'jenis.required' => 'Jenis wajib dipilih.',
            'jenis.in' => 'Jenis harus barang atau jasa.',
            'satuan.required' => 'Satuan wajib diisi.',
            'harga_jual.required' => 'Harga jual wajib diisi.',
            'harga_jual.numeric' => 'Harga jual harus berupa angka.',
        ];
    }
}
