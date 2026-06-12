<?php

namespace App\Http\Controllers;

use App\Models\transaksi_paket;
use App\Http\Requests\Storetransaksi_paketRequest;
use App\Http\Requests\Updatetransaksi_paketRequest;

class TransaksiPaketController extends Controller
{
    /**
     * Display a listing of the resource.
     */
    public function index()
    {
        //
    }

    /**
     * Show the form for creating a new resource.
     */
    public function create()
    {
        //
    }

    /**
     * Store a newly created resource in storage.
     */
    public function store(Storetransaksi_paketRequest $request)
    {
        //
    }

    /**
     * Display the specified resource.
     */
    public function show(transaksi_paket $transaksi_paket)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(transaksi_paket $transaksi_paket)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Updatetransaksi_paketRequest $request, transaksi_paket $transaksi_paket)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(transaksi_paket $transaksi_paket)
    {
        //
    }
}
