<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdateInstansiRequest extends FormRequest
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
            'nama_instansi' => 'sometimes|required|string|max:255',
            'paket_id' => 'nullable|exists:pakets,id',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_instansi.required' => 'Nama instansi wajib diisi.',
            'paket_id.exists' => 'Paket tidak valid.',
        ];
    }
}
