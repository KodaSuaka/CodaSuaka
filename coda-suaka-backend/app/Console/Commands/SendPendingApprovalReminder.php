<?php

namespace App\Console\Commands;

use App\Models\Notification;
use App\Models\User;
use App\Services\NotificationService;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

class SendPendingApprovalReminder extends Command
{
    /**
     * The name and signature of the console command.
     */
    protected $signature = 'notification:pending-approval';

    /**
     * The console command description.
     */
    protected $description = 'Kirim notifikasi pengingat untuk transaksi keuangan yang masih menunggu approval';

    /**
     * Execute the console command.
     *
     * NOTE: MySQL 5.7 compatible — tidak pakai CTE atau window function.
     */
    public function handle(NotificationService $notificationService): int
    {
        // Ambil semua transaksi kas yang masih pending approval
        $pendingTransaksi = DB::table('transaksi_kas')
            ->where('status_approval', 'pending')
            ->select('id', 'keterangan', 'instansi_id', 'created_by')
            ->get();

        $count = 0;

        foreach ($pendingTransaksi as $transaksi) {
            // Cari user dengan role Keuangan atau Owner di instansi yang sama
            // yang bisa melakukan approval
            $approvers = User::where('instansi_id', $transaksi->instansi_id)
                ->whereHas('role.permissions', function ($query) {
                    $query->where('permission', 'manage:keuangan');
                })
                ->pluck('id')
                ->toArray();

            foreach ($approvers as $approverId) {
                // Cek apakah sudah ada notifikasi pending yang sama dalam 24 jam terakhir
                // untuk mencegah spam notifikasi
                $alreadyNotified = Notification::where('user_id', $approverId)
                    ->where('type', 'keuangan_pending_reminder')
                    ->where('related_id', $transaksi->id)
                    ->where('related_type', 'App\\Models\\TransaksiKas')
                    ->where('created_at', '>=', now()->subDay())
                    ->exists();

                if (! $alreadyNotified) {
                    $notificationService->create(
                        $approverId,
                        'keuangan_pending_reminder',
                        'Pengingat Approval Keuangan',
                        "Masih ada transaksi \"{$transaksi->keterangan}\" yang menunggu approval Anda.",
                        'AccountBalance',
                        '#F59E0B',
                        $transaksi->id,
                        'App\\Models\\TransaksiKas'
                    );
                    $count++;
                }
            }
        }

        $this->info("Berhasil mengirim {$count} notifikasi pengingat approval.");

        return Command::SUCCESS;
    }
}
