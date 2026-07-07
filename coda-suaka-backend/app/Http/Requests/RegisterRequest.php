<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class RegisterRequest extends FormRequest
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
            'nama_instansi'=>'required|string|max:100|unique:instansis,nama_instansi',
            'nama_pemilik'=>'required|string|max:100',
            'email'=>'required|string|email|max:100|unique:users,email',
            'password'=>'required|string|min:8',

        ];
    }
    public function messages(): array
    {
        return [
            'nama_instansi.required' => 'Nama instansi atau toko wajib diisi.',
            'email.required'         => 'Email wajib diisi.',
            'email.email'            => 'Format email tidak valid.',
            'email.unique'           => 'Email ini sudah pernah didaftarkan. Silakan login atau gunakan email lain.',
            'password.required'      => 'Password wajib diisi.',
            'password.min'           => 'Password minimal harus 8 karakter.',
        ];
    }
}
