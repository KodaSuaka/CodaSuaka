<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StorekaryawanRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     * Otorisasi ditangani oleh Policy via authorizeResource di controller.
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
            'nama_lengkap' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:6',
            'kontak' => 'nullable|string|max:20',
            'alamat' => 'nullable|string',
            'role_id' => 'required|exists:roles,id',
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'sisa_cuti' => 'nullable|integer|min:0',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_lengkap.required' => 'Nama karyawan wajib diisi.',
            'email.required' => 'Email karyawan wajib diisi.',
            'email.email' => 'Format email tidak valid.',
            'email.unique' => 'Email sudah terdaftar.',
            'password.required' => 'Password wajib diisi.',
            'password.min' => 'Password minimal 6 karakter.',
            'role_id.required' => 'Role wajib dipilih.',
            'role_id.exists' => 'Role tidak valid.',
            'outlet_id.exists' => 'Outlet tidak valid.',
        ];
    }
}
