<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreTemplatePenugasanRequest;
use App\Http\Requests\UpdateTemplatePenugasanRequest;
use App\Models\penugasan;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class TemplatePenugasanController extends Controller
{
    use ApiResponse;

    /**
     * Maksimal template per instansi.
     */
    private const MAX_TEMPLATE = 10;

    /**
     * GET /api/template-penugasans
     *
     * Mengambil data template dari tabel penugasans (is_template = true).
     */
    public function index(Request $request)
    {
        $user = $request->user();

        $query = penugasan::templates()
            ->where(function ($q) use ($user) {
                // Template global (instansi_id = NULL) + template milik instansi user
                $q->whereNull('instansi_id')
                    ->orWhere('instansi_id', $user->instansi_id);
            })
            ->orderBy('created_at', 'desc');

        $templates = $query->get();

        return $this->success([
            'templates' => $templates,
            'max_template' => self::MAX_TEMPLATE,
            'sisa_slot' => max(0, self::MAX_TEMPLATE - $templates->count()),
        ]);
    }

    /**
     * POST /api/template-penugasans
     *
     * Membuat template baru sebagai record penugasan dengan is_template = true.
     */
    public function store(StoreTemplatePenugasanRequest $request)
    {
        $user = $request->user();

        // Cek batas maksimal template per instansi
        $count = penugasan::templates()->where(function ($q) use ($user) {
            $q->whereNull('instansi_id')
                ->orWhere('instansi_id', $user->instansi_id);
        })->count();

        if ($count >= self::MAX_TEMPLATE) {
            return $this->error('Batas maksimal '.self::MAX_TEMPLATE.' template telah tercapai', 400);
        }

        $urgency = $request->urgency_default ?? 'sedang';

        $template = penugasan::create([
            'judul' => $request->nama_template,
            'deskripsi' => $request->deskripsi_template,
            'urgency' => $urgency,
            'poin' => penugasan::getPoinForUrgency($urgency),
            'status' => 'belum',
            'is_template' => true,
            'instansi_id' => $user->instansi_id,
            'created_by' => $user->id,
        ]);

        return $this->success($template, 'Template berhasil ditambahkan', 201);
    }

    /**
     * GET /api/template-penugasans/{templatePenugasan}
     */
    public function show(penugasan $templatePenugasan)
    {
        return $this->success($templatePenugasan);
    }

    /**
     * PUT /api/template-penugasans/{templatePenugasan}
     */
    public function update(UpdateTemplatePenugasanRequest $request, penugasan $templatePenugasan)
    {
        $data = $request->only(['nama_template', 'deskripsi_template', 'urgency_default']);

        // Map field names: nama_template → judul, deskripsi_template → deskripsi
        $mapped = [];
        if (isset($data['nama_template'])) {
            $mapped['judul'] = $data['nama_template'];
        }
        if (isset($data['deskripsi_template'])) {
            $mapped['deskripsi'] = $data['deskripsi_template'];
        }

        // Recalculate poin jika urgency berubah
        if (isset($data['urgency_default'])) {
            $mapped['urgency'] = $data['urgency_default'];
            $mapped['poin'] = penugasan::getPoinForUrgency($data['urgency_default']);
        }

        $templatePenugasan->update($mapped);

        return $this->success($templatePenugasan, 'Template berhasil diperbarui');
    }

    /**
     * DELETE /api/template-penugasans/{templatePenugasan}
     */
    public function destroy(penugasan $templatePenugasan)
    {
        $templatePenugasan->delete();

        return $this->success(null, 'Template berhasil dihapus');
    }
}
