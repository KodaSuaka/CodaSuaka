<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdateBarangJasaRequest extends FormRequest
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
            'nama' => 'sometimes|required|string|max:150',
            'jenis' => 'sometimes|required|in:barang,jasa',
            'satuan' => 'sometimes|required|string|max:50',
            'harga_jual' => 'sometimes|required|numeric|min:0',
            'harga_beli' => 'sometimes|nullable|numeric|min:0',
            'stok' => 'sometimes|nullable|integer|min:0',
            'is_active' => 'sometimes|nullable|boolean',
            'keterangan' => 'sometimes|nullable|string',
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
