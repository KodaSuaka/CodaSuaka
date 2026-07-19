<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class Storetransaksi_paketRequest extends FormRequest
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
            'paket_id' => 'required|exists:pakets,id',
            'tanggal_mulai' => 'required|date',
            'tanggal_berakhir' => 'nullable|date|after:tanggal_mulai',
            'total_harga' => 'required|numeric|min:0',
            'status' => 'sometimes|in:pending,aktif,kedaluwarsa,dibatalkan',
        ];
    }

    public function messages(): array
    {
        return [
            'paket_id.required' => 'Paket wajib dipilih.',
            'paket_id.exists' => 'Paket tidak valid.',
            'tanggal_mulai.required' => 'Tanggal mulai wajib diisi.',
            'total_harga.required' => 'Total harga wajib diisi.',
            'tanggal_berakhir.after' => 'Tanggal berakhir harus setelah tanggal mulai.',
            'status.in' => 'Status tidak valid.',
        ];
    }
}
