<?php

namespace Tests\Feature\Notification;

use App\Models\Instansi;
use App\Models\Notification;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class NotificationIndexQueryCountTest extends TestCase
{
    use RefreshDatabase;

    /**
     * GET /api/notifications is paginated and returns raw notification rows
     * (no relation access). Assert query count stays flat as row count grows.
     */
    public function test_query_count_tetap_flat_saat_jumlah_notifikasi_bertambah()
    {
        $instansi = Instansi::factory()->create();
        $staffRole = role::firstOrCreate(['nama_role' => 'Karyawan']);

        $user = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $staffRole->id,
        ]);

        $countFor = function (int $n) use ($user) {
            Notification::where('user_id', $user->id)->delete();

            foreach (range(1, $n) as $i) {
                Notification::create([
                    'user_id' => $user->id,
                    'type' => 'sistem',
                    'title' => "Notif $i",
                    'body' => 'Isi notifikasi',
                    'is_read' => false,
                ]);
            }

            DB::enableQueryLog();
            $response = $this->actingAs($user)->getJson('/api/notifications');
            $response->assertStatus(200);
            $queryCount = count(DB::getQueryLog());
            DB::disableQueryLog();
            DB::flushQueryLog();

            return $queryCount;
        };

        $small = $countFor(5);
        $large = $countFor(20);

        $this->assertSame($small, $large, "Query count grew from {$small} to {$large} as notification count grew — N+1 suspected.");
    }
}
