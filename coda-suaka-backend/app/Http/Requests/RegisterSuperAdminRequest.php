<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class RegisterSuperAdminRequest extends FormRequest
{
    /**
     * Hanya izinkan registrasi Super Admin jika secret key cocok
     * dengan SUPER_ADMIN_SECRET di environment.
     * Ini mencegah pembuatan akun Super Admin oleh pihak yang tidak berwenang.
     */
    public function authorize(): bool
    {
        $secret = config('auth.super_admin_secret', env('SUPER_ADMIN_SECRET'));

        return $secret && $this->input('secret') === $secret;
    }

    public function rules(): array
    {
        return [
            'secret' => 'required|string',
            'name' => 'required|string|max:255',
            'email' => 'required|email|unique:users,email',
            'password' => 'required|string|min:8',
        ];
    }

    public function messages(): array
    {
        return [
            'secret.required' => 'Registration secret wajib diisi.',
            'name.required' => 'Nama wajib diisi.',
            'email.required' => 'Email wajib diisi.',
            'email.email' => 'Format email tidak sesuai.',
            'email.unique' => 'Email sudah terdaftar.',
            'password.required' => 'Password wajib diisi.',
            'password.min' => 'Password minimal 8 karakter.',
        ];
    }
}
