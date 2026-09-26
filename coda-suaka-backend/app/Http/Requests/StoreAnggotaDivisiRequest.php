<?php

namespace App\Http\Requests;

use App\Models\Divisi;
use App\Models\karyawan;
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
        $user = $this->user();

        return [
            'divisi_id' => [
                'required',
                function ($attribute, $value, $fail) use ($user) {
                    if (! Divisi::whereHas('outlet', function ($q) use ($user) {
                        $q->where('instansi_id', $user->instansi_id);
                    })->where('id', $value)->exists()) {
                        $fail('Divisi tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'karyawan_id' => [
                'required',
                function ($attribute, $value, $fail) use ($user) {
                    if (! karyawan::whereHas('user', function ($q) use ($user) {
                        $q->where('instansi_id', $user->instansi_id);
                    })->where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
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
