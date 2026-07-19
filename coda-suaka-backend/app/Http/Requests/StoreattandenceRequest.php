<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class StoreattandenceRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'lokasi' => 'nullable|string|max:255',
        ];
    }

    public function messages(): array
    {
        return [
            'lokasi.max' => 'Lokasi maksimal 255 karakter.',
        ];
    }
}
