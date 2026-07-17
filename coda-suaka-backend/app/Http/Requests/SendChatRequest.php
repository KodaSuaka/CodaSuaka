<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class SendChatRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'penerima_id' => 'required|exists:users,id',
            'pesan' => 'required|string|max:5000',
        ];
    }

    public function messages(): array
    {
        return [
            'penerima_id.required' => 'Penerima wajib dipilih.',
            'penerima_id.exists' => 'Penerima tidak valid.',
            'pesan.required' => 'Pesan wajib diisi.',
            'pesan.max' => 'Pesan maksimal 5000 karakter.',
        ];
    }
}
