<?php

namespace App\Http\Requests;

use App\Models\role;
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
            // Biodata opsional
            'tempat_lahir' => 'nullable|string|max:100',
            'tanggal_lahir' => 'nullable|date|before:today',
            // Owner & Super Admin adalah role platform-level, bukan karyawan —
            // cegah privilege escalation (mis. Manager membuat akun ber-role Owner)
            'role_id' => [
                'required',
                'exists:roles,id',
                function ($attribute, $value, $fail) {
                    if (role::whereIn('nama_role', ['Super Admin', 'Owner'])->where('id', $value)->exists()) {
                        $fail('Role tidak valid untuk akun karyawan.');
                    }
                },
            ],
            // Bug #15: outlet_id nullable — pemilik bisa tambah karyawan sebelum buat outlet
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
            'sisa_cuti' => 'nullable|integer|min:0',
            'tanggal_mulai_kerja' => 'nullable|date',
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
