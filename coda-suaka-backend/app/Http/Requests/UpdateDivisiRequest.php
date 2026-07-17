<?php

namespace App\Http\Requests;

use App\Models\karyawan;
use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdateDivisiRequest extends FormRequest
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
            'nama_divisi' => 'sometimes|required|string|max:100',
            'deskripsi' => 'nullable|string',
            'ketua_karyawan_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !karyawan::whereHas('user', function ($q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        })->where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'outlet_id' => [
                'sometimes',
                'required',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
        ];
    }

    public function messages(): array
    {
        return [
            'nama_divisi.required' => 'Nama divisi wajib diisi.',
            'outlet_id.required' => 'Outlet wajib dipilih.',
            'outlet_id.exists' => 'Outlet tidak valid.',
        ];
    }
}
