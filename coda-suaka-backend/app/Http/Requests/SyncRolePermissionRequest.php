<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class SyncRolePermissionRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'role_id' => 'required|exists:roles,id',
            'permissions' => 'required|array',
            'permissions.*' => 'string|max:100',
        ];
    }

    public function messages(): array
    {
        return [
            'role_id.required' => 'Role wajib dipilih.',
            'role_id.exists' => 'Role tidak valid.',
            'permissions.required' => 'Permissions wajib diisi.',
            'permissions.array' => 'Format permissions tidak valid.',
            'permissions.*.max' => 'Setiap permission maksimal 100 karakter.',
        ];
    }
}
