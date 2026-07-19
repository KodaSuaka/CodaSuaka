<?php

namespace App\Http\Controllers;

use App\Http\Requests\StorepaketRequest;
use App\Http\Requests\UpdatepaketRequest;
use App\Models\paket;
use App\Traits\ApiResponse;

class PaketController extends Controller
{
    use ApiResponse;

    public function __construct()
    {
        $this->authorizeResource(paket::class, 'paket');
    }

    /**
     * GET /api/pakets
     * Semua paket aktif
     */
    public function index()
    {
        $pakets = paket::where('is_active', true)->orderBy('harga', 'asc')->get();
        return $this->success($pakets);
    }

    /**
     * POST /api/pakets
     */
    public function store(StorepaketRequest $request)
    {
        $paket = paket::create($request->all());
        return $this->success($paket, 'Paket berhasil ditambahkan', 201);
    }

    /**
     * GET /api/pakets/{paket}
     */
    public function show(paket $paket)
    {
        return $this->success($paket);
    }

    /**
     * PUT /api/pakets/{paket}
     */
    public function update(UpdatepaketRequest $request, paket $paket)
    {
        $paket->update($request->all());
        return $this->success($paket, 'Paket berhasil diperbarui');
    }

    /**
     * DELETE /api/pakets/{paket}
     */
    public function destroy(paket $paket)
    {
        $paket->update(['is_active' => false]);
        return $this->success(null, 'Paket berhasil dinonaktifkan');
    }
}
