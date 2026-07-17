<?php

namespace App\Http\Requests;

use App\Models\Divisi;
use App\Models\karyawan;
use Illuminate\Contracts\Validation\ValidationRule;
use Illuminate\Foundation\Http\FormRequest;

class UpdatepenugasanRequest extends FormRequest
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
            'judul' => 'sometimes|required|string|max:200',
            'deskripsi' => 'nullable|string',
            'penanggung_jawab_id' => [
                'sometimes',
                'required',
                function ($attribute, $value, $fail) use ($user) {
                    if (!karyawan::whereHas('user', function ($q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        })->where('id', $value)->exists()) {
                        $fail('Karyawan tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'divisi_id' => [
                'nullable',
                function ($attribute, $value, $fail) use ($user) {
                    if ($value && !Divisi::whereHas('outlet', function ($q) use ($user) {
                            $q->where('instansi_id', $user->instansi_id);
                        })->where('id', $value)->exists()) {
                        $fail('Divisi tidak ditemukan di instansi Anda');
                    }
                },
            ],
            'tenggat' => 'nullable|date',
            'status' => 'sometimes|in:belum,proses,selesai,batal',
        ];
    }

    public function messages(): array
    {
        return [
            'judul.required' => 'Judul tugas wajib diisi.',
            'penanggung_jawab_id.required' => 'Penanggung jawab wajib dipilih.',
            'tenggat.date' => 'Format tanggal tenggat tidak valid.',
            'status.in' => 'Status tidak valid.',
        ];
    }
}
