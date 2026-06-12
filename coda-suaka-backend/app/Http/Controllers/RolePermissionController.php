<?php

namespace App\Http\Controllers;

use App\Models\role_permission;
use App\Http\Requests\Storerole_permissionRequest;
use App\Http\Requests\Updaterole_permissionRequest;

class RolePermissionController extends Controller
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
    public function store(Storerole_permissionRequest $request)
    {
        //
    }

    /**
     * Display the specified resource.
     */
    public function show(role_permission $role_permission)
    {
        //
    }

    /**
     * Show the form for editing the specified resource.
     */
    public function edit(role_permission $role_permission)
    {
        //
    }

    /**
     * Update the specified resource in storage.
     */
    public function update(Updaterole_permissionRequest $request, role_permission $role_permission)
    {
        //
    }

    /**
     * Remove the specified resource from storage.
     */
    public function destroy(role_permission $role_permission)
    {
        //
    }
}
