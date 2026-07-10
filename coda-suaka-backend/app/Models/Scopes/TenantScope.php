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
            \Log::debug('[TenantScope] SKIP — no authenticated user', ['model' => get_class($model)]);
            return;
        }

        $user = Auth::user();
        $modelClass = get_class($model);

        if ($this->callback !== null) {
            if ($user->instansi_id !== null) {
                \Log::debug('[TenantScope] APPLY callback', [
                    'model' => $modelClass,
                    'user_id' => $user->id,
                    'user_instansi_id' => $user->instansi_id,
                ]);

                // Log the SQL before applying callback to debug relationship-based filtering
                $sqlBefore = $builder->toSql();
                call_user_func($this->callback, $builder, $user);
                $sqlAfter = $builder->toSql();
                \Log::debug('[TenantScope] CALLBACK applied', [
                    'model' => $modelClass,
                    'sql_before' => $sqlBefore,
                    'sql_after' => $sqlAfter,
                ]);
            } else {
                \Log::debug('[TenantScope] BLOCK — user has no instansi_id (callback mode)', [
                    'model' => $modelClass,
                    'user_id' => $user->id,
                ]);
                $builder->whereRaw('1 = 0');
            }
        } elseif ($this->column !== null) {
            if ($user->instansi_id !== null) {
                \Log::debug('[TenantScope] APPLY direct column', [
                    'model' => $modelClass,
                    'column' => $this->column,
                    'value' => $user->instansi_id,
                ]);
                $builder->where($this->column, $user->instansi_id);
            } else {
                \Log::debug('[TenantScope] BLOCK — user has no instansi_id (column mode)', [
                    'model' => $modelClass,
                    'user_id' => $user->id,
                ]);
                $builder->whereRaw('1 = 0');
            }
        }
    }
}
