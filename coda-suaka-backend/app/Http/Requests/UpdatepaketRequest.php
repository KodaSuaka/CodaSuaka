<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdatepaketRequest extends FormRequest
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
            'nama_paket' => 'sometimes|required|string|max:255',
            'harga' => 'sometimes|required|numeric|min:0',
            'deskripsi' => 'nullable|string',
            'fitur' => 'nullable|string',
            'durasi_hari' => 'sometimes|required|integer|min:1',
            'max_outlet' => 'nullable|integer|min:1',
            'max_karyawan_per_outlet' => 'nullable|integer|min:1',
            'is_active' => 'boolean',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_paket.required' => 'Nama paket wajib diisi.',
            'harga.required' => 'Harga wajib diisi.',
            'harga.numeric' => 'Harga harus berupa angka.',
            'harga.min' => 'Harga tidak boleh negatif.',
            'durasi_hari.required' => 'Durasi hari wajib diisi.',
            'durasi_hari.integer' => 'Durasi hari harus berupa bilangan bulat.',
            'durasi_hari.min' => 'Durasi hari minimal 1.',
        ];
    }
}
