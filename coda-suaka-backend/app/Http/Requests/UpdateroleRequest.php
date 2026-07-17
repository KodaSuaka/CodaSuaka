<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdateroleRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        $roleId = $this->route('role')?->id;

        return [
            'nama_role' => [
                'required',
                'string',
                'max:50',
                'unique:roles,nama_role,' . $roleId,
            ],
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
