<?php

namespace App\Http\Requests;

use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;
use Illuminate\Validation\Rule;

class StorejadwalRequest extends FormRequest
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
        $user = $this->user();

        return [
            'nama_event' => 'required|string|max:200',
            'deskripsi' => 'nullable|string',
            'tanggal' => 'required|date',
            'kategori' => 'required|in:meeting,training,event,libur,lainnya',
            'outlet_id' => [
                'nullable',
                Rule::exists('outlets', 'id')->where('instansi_id', $user->instansi_id),
            ],
        ];
    }

    public function messages(): array
    {
        return [
            'nama_event.required' => 'Nama event wajib diisi.',
            'tanggal.required' => 'Tanggal wajib diisi.',
            'tanggal.date' => 'Format tanggal tidak valid.',
            'kategori.required' => 'Kategori wajib dipilih.',
            'kategori.in' => 'Kategori tidak valid.',
        ];
    }
}
