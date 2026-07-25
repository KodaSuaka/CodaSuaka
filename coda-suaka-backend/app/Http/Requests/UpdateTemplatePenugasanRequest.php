<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateTemplatePenugasanRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'nama_template' => 'sometimes|required|string|max:100',
            'deskripsi_template' => 'nullable|string',
            'urgency_default' => 'sometimes|nullable|in:urgent,sedang,rendah',
        ];
    }
}
