<?php

namespace App\Http\Controllers;

use App\Http\Requests\StoreTemplatePenugasanRequest;
use App\Http\Requests\UpdateTemplatePenugasanRequest;
use App\Models\TemplatePenugasan;
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
     */
    public function index(Request $request)
    {
        $user = $request->user();
        $templates = TemplatePenugasan::orderBy('created_at', 'desc')->get();

        return $this->success([
            'templates' => $templates,
            'max_template' => self::MAX_TEMPLATE,
            'sisa_slot' => max(0, self::MAX_TEMPLATE - $templates->count()),
        ]);
    }

    /**
     * POST /api/template-penugasans
     */
    public function store(StoreTemplatePenugasanRequest $request)
    {
        $user = $request->user();

        // Cek batas maksimal template per instansi
        $count = TemplatePenugasan::count();
        if ($count >= self::MAX_TEMPLATE) {
            return $this->error('Batas maksimal ' . self::MAX_TEMPLATE . ' template telah tercapai', 400);
        }

        $template = TemplatePenugasan::create([
            'nama_template' => $request->nama_template,
            'deskripsi_template' => $request->deskripsi_template,
            'urgency_default' => $request->urgency_default ?? 'sedang',
            'poin_default' => TemplatePenugasan::getPoinForUrgency($request->urgency_default ?? 'sedang'),
            'instansi_id' => $user->instansi_id,
            'created_by' => $user->id,
        ]);

        return $this->success($template, 'Template berhasil ditambahkan', 201);
    }

    /**
     * GET /api/template-penugasans/{templatePenugasan}
     */
    public function show(TemplatePenugasan $templatePenugasan)
    {
        return $this->success($templatePenugasan);
    }

    /**
     * PUT /api/template-penugasans/{templatePenugasan}
     */
    public function update(UpdateTemplatePenugasanRequest $request, TemplatePenugasan $templatePenugasan)
    {
        $data = $request->only(['nama_template', 'deskripsi_template', 'urgency_default']);

        // Recalculate poin jika urgency berubah
        if (isset($data['urgency_default'])) {
            $data['poin_default'] = TemplatePenugasan::getPoinForUrgency($data['urgency_default']);
        }

        $templatePenugasan->update($data);

        return $this->success($templatePenugasan, 'Template berhasil diperbarui');
    }

    /**
     * DELETE /api/template-penugasans/{templatePenugasan}
     */
    public function destroy(TemplatePenugasan $templatePenugasan)
    {
        $templatePenugasan->delete();
        return $this->success(null, 'Template berhasil dihapus');
    }
}
