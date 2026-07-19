<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreinstansiRequest extends FormRequest
{
    /**
     * Determine if the user is authorized to make this request.
     * Otorisasi ditangani oleh middleware Super Admin.
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
            'nama_instansi' => 'required|string|max:255',
            'paket_id' => 'nullable|exists:pakets,id',
            'owner_name' => 'required|string|max:255',
            'owner_email' => 'required|email|unique:users,email',
            'owner_password' => 'required|string|min:6',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_instansi.required' => 'Nama instansi wajib diisi.',
            'owner_name.required' => 'Nama owner wajib diisi.',
            'owner_email.required' => 'Email owner wajib diisi.',
            'owner_email.email' => 'Format email tidak valid.',
            'owner_email.unique' => 'Email sudah terdaftar.',
            'owner_password.required' => 'Password wajib diisi.',
            'owner_password.min' => 'Password minimal 6 karakter.',
        ];
    }
}
