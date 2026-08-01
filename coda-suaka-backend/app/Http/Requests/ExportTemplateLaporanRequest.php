<?php

namespace App\Http\Requests;

use App\Services\PermissionService;
use Illuminate\Foundation\Http\FormRequest;

class ExportTemplateLaporanRequest extends FormRequest
{
    /**
     * Izin 'export:laporan-keuangan' — Owner & Keuangan (lihat config/permissions.php).
     * Ditolak 403 jika tidak punya izin.
     */
    public function authorize(): bool
    {
        $user = $this->user();

        return $user !== null
            && app(PermissionService::class)->userHasPermission($user, 'export:laporan-keuangan');
    }

    public function rules(): array
    {
        return [
            'jenis' => 'required|in:laba_rugi,arus_kas',
            'tipe_usaha' => 'required|in:barang,jasa',
            'bulan' => 'required|integer|between:1,12',
            'tahun' => 'required|integer|min:2000|max:2100',
        ];
    }

    public function messages(): array
    {
        return [
            'jenis.in' => 'Jenis laporan harus laba_rugi atau arus_kas.',
            'tipe_usaha.in' => 'Tipe usaha harus barang atau jasa.',
            'bulan.between' => 'Bulan harus 1-12.',
        ];
    }
}
