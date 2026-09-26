<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Chat contacts/messages/mark-as-read queries filter chats by
     * (pengirim_id, penerima_id[, is_read]) — see ChatController::contacts/
     * messages/markAsRead. Only single-column indexes existed on
     * pengirim_id and penerima_id, forcing MySQL into an index_merge
     * (intersect) or a low-selectivity single-column ref scan.
     *
     * EXPLAIN on 2000 seeded chat rows (18 users) before this migration:
     *   - messages()/markAsRead() equivalent (pengirim_id=X AND penerima_id=Y
     *     [AND is_read=0]): type=ref, key=idx_chats_pengirim_id (single
     *     column), rows=113, filtered=4.98% — MySQL scans ~113 rows via the
     *     pengirim_id index to find ~5 that also match penerima_id+is_read.
     *   - messages() query (two OR branches of pengirim/penerima pairs):
     *     type=index_merge intersect(idx_chats_pengirim_id,idx_chats_penerima_id)
     *     on the small (10-row) dataset — two index scans merged in memory.
     *
     * A composite (pengirim_id, penerima_id, is_read) index lets MySQL do a
     * single tight index lookup/range for all of these hot, poll-driven
     * queries instead of over-fetching then filtering.
     *
     * Also drops notifications_user_unread_idx: it duplicates the
     * (user_id, is_read) composite index already created inline in
     * 2026_07_25_000003_create_notifications_table.php — pure dead weight
     * (extra write overhead on every notification insert/read-toggle with
     * no query benefit), added by a later migration that didn't check
     * SHOW INDEX first.
     */
    public function up(): void
    {
        if (Schema::hasTable('chats') && ! Schema::hasIndex('chats', 'idx_chats_pengirim_penerima_read')) {
            Schema::table('chats', function (Blueprint $table) {
                $table->index(['pengirim_id', 'penerima_id', 'is_read'], 'idx_chats_pengirim_penerima_read');
            });
        }

        if (Schema::hasTable('notifications') && Schema::hasIndex('notifications', 'notifications_user_unread_idx')) {
            Schema::table('notifications', function (Blueprint $table) {
                $table->dropIndex('notifications_user_unread_idx');
            });
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        if (Schema::hasTable('chats') && Schema::hasIndex('chats', 'idx_chats_pengirim_penerima_read')) {
            Schema::table('chats', function (Blueprint $table) {
                $table->dropIndex('idx_chats_pengirim_penerima_read');
            });
        }

        if (Schema::hasTable('notifications') && ! Schema::hasIndex('notifications', 'notifications_user_unread_idx')) {
            Schema::table('notifications', function (Blueprint $table) {
                $table->index(['user_id', 'is_read'], 'notifications_user_unread_idx');
            });
        }
    }
};
