<?php

namespace App\Http\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreTemplatePenugasanRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'nama_template' => 'required|string|max:100',
            'deskripsi_template' => 'nullable|string',
            'urgency_default' => 'nullable|in:urgent,sedang,rendah',
        ];
    }

    public function messages(): array
    {
        return [
            'nama_template.required' => 'Nama template wajib diisi',
            'nama_template.max' => 'Nama template maksimal 100 karakter',
            'urgency_default.in' => 'Urgency harus salah satu dari: urgent, sedang, rendah',
        ];
    }
}
