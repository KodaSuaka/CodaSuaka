<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class Storerole_permissionRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'role_id' => 'required|exists:roles,id',
            'permission' => 'required|string|max:100',
        ];
    }

    public function messages(): array
    {
        return [
            'role_id.required' => 'Role wajib dipilih.',
            'role_id.exists' => 'Role tidak valid.',
            'permission.required' => 'Permission wajib diisi.',
            'permission.max' => 'Permission maksimal 100 karakter.',
        ];
    }
}
