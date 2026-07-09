<?php

namespace App\Models\Scopes;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Scope;
use Illuminate\Support\Facades\Auth;

class TenantScope implements Scope
{
    /**
     * The column to filter by (for direct instansi_id columns).
     */
    protected ?string $column = null;

    /**
     * A closure for complex relationship-based filtering.
     *
     * @var callable|null
     */
    protected $callback = null;

    /**
     * Create a new tenant scope.
     *
     * Accepts either a column name (string) for direct filtering,
     * or a callable for relationship-based filtering.
     *
     * Signature for callable: fn(Builder $builder, User $user): void
     */
    public function __construct(string|callable $columnOrCallback)
    {
        if (is_callable($columnOrCallback)) {
            $this->callback = $columnOrCallback;
        } else {
            $this->column = $columnOrCallback;
        }
    }

    public function apply(Builder $builder, Model $model): void
    {
        // Skip scope when no authenticated user (e.g., CLI commands, login)
        if (!Auth::hasUser()) {
            return;
        }

        $user = Auth::user();

        if ($this->callback !== null) {
            // Only apply callback if user has an instansi_id to avoid "parameter must not be null" errors
            if ($user->instansi_id !== null) {
                call_user_func($this->callback, $builder, $user);
            } else {
                // If user has no instansi (e.g. Super Admin), they shouldn't see any tenant-specific data
                $builder->whereRaw('1 = 0');
            }
        } elseif ($this->column !== null) {
            if ($user->instansi_id !== null) {
                $builder->where($this->column, $user->instansi_id);
            } else {
                $builder->whereRaw('1 = 0');
            }
        }
    }
}
