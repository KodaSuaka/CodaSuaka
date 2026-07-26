<?php

namespace App\Http\Requests;

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
            'lokasi' => [
                'nullable',
                'string',
                'max:255',
                // Validasi format GPS: "latitude,longitude" (contoh: "-6.2088,106.8456")
                // atau null (jika GPS tidak aktif)
                function ($attribute, $value, $fail) {
                    if ($value === null || $value === '') {
                        return; // nullable, izinkan null
                    }
                    // Cek format: angka decimal, koma, angka decimal
                    if (! preg_match('/^-?\d+\.?\d*,-?\d+\.?\d*$/', $value)) {
                        $fail('Format lokasi tidak valid. Gunakan format: "latitude,longitude" (contoh: "-6.2088,106.8456")');
                    }
                },
            ],
        ];
    }

    public function messages(): array
    {
        return [
            'lokasi.max' => 'Lokasi maksimal 255 karakter.',
        ];
    }
}
