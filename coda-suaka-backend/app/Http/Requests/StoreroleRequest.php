<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreroleRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'nama_role' => 'required|string|max:50|unique:roles,nama_role',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_role.required' => 'Nama role wajib diisi.',
            'nama_role.max' => 'Nama role maksimal 50 karakter.',
            'nama_role.unique' => 'Nama role sudah digunakan.',
        ];
    }
}
