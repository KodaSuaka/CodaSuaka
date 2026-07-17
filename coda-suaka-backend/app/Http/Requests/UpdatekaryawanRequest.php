<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class UpdatekaryawanRequest extends FormRequest
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
            'nama_lengkap' => 'sometimes|required|string|max:255',
            'kontak' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'sisa_cuti' => 'nullable|integer|min:0',
            'foto_profil' => 'nullable|string',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_lengkap.required' => 'Nama karyawan wajib diisi.',
            'outlet_id.exists' => 'Outlet tidak valid.',
        ];
    }
}
