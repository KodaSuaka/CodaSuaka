<?php

namespace App\Policies;

use App\Models\attandence;
use App\Models\User;
use App\Services\PermissionService;

class AttandencePolicy
{
    public function viewAny(User $user): bool
    {
        return $user->instansi_id !== null;
    }

    public function view(User $user, attandence $attandence): bool
    {
        return $user->instansi_id === $attandence->user?->instansi_id;
    }

    public function create(User $user): bool
    {
        // Attendance is tenant-scoped; a user with no instansi_id would
        // create a record permanently invisible to any tenant-scoped listing.
        return $user->instansi_id !== null;
    }

    public function update(User $user, attandence $attandence): bool
    {
        // Users can only update their own attendance
        return $user->id === $attandence->user_id;
    }

    public function delete(User $user, attandence $attandence): bool
    {
        return app(PermissionService::class)->userHasPermission($user, 'manage:attendance');
    }

    public function restore(User $user, attandence $attandence): bool
    {
        return false;
    }

    public function forceDelete(User $user, attandence $attandence): bool
    {
        return false;
    }
}
