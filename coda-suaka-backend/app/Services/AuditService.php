<?php

namespace App\Services;

use App\Models\AuditLog;
use App\Models\User;
use Illuminate\Database\Eloquent\Model;

class AuditService
{
    /**
     * Catat event audit ke таблицу audit_logs.
     */
    public function log(
        string $event,
        Model $model,
        ?array $oldValues = null,
        ?array $newValues = null,
        ?User $user = null
    ): AuditLog {
        $request = request();

        return AuditLog::create([
            'instansi_id'   => $model->instansi_id ?? $user?->instansi_id,
            'auditable_type' => get_class($model),
            'auditable_id'   => $model->getKey(),
            'event'          => $event,
            'old_values'     => $oldValues,
            'new_values'     => $newValues,
            'user_id'        => $user?->id ?? $request?->user()?->id,
            'ip_address'     => $request?->ip(),
            'user_agent'     => $request?->userAgent(),
        ]);
    }

    /**
     * Catat event created — snapshot new_values.
     */
    public function created(Model $model, ?User $user = null): AuditLog
    {
        return $this->log('created', $model, null, $model->toArray(), $user);
    }

    /**
     * Catat event updated — snapshot old vs new.
     */
    public function updated(Model $model, array $original, ?User $user = null): AuditLog
    {
        return $this->log('updated', $model, $original, $model->getChanges(), $user);
    }

    /**
     * Catat event deleted — snapshot old_values.
     */
    public function deleted(Model $model, ?User $user = null): AuditLog
    {
        return $this->log('deleted', $model, $model->toArray(), null, $user);
    }

    /**
     * Catat event approval (approved / rejected).
     */
    public function approval(string $event, Model $model, ?User $user = null): AuditLog
    {
        return $this->log($event, $model, null, ['status_approval' => $model->status_approval], $user);
    }
}
