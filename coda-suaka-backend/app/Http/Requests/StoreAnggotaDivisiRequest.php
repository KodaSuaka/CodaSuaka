<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreAnggotaDivisiRequest extends FormRequest
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
            'divisi_id' => 'required|exists:divisis,id',
            'karyawan_id' => 'required|exists:karyawans,id',
        ];
    }

    public function messages(): array
    {
        return [
            'divisi_id.required' => 'Divisi wajib dipilih.',
            'divisi_id.exists' => 'Divisi tidak valid.',
            'karyawan_id.required' => 'Karyawan wajib dipilih.',
            'karyawan_id.exists' => 'Karyawan tidak valid.',
        ];
    }
}
