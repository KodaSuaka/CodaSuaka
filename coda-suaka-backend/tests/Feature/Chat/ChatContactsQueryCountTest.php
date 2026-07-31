<?php

namespace Tests\Feature\Chat;

use App\Models\Chat;
use App\Models\Instansi;
use App\Models\karyawan;
use App\Models\role;
use App\Models\User;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class ChatContactsQueryCountTest extends TestCase
{
    use RefreshDatabase;

    /**
     * GET /api/chat/contacts loads role + profilKaryawan per contact and
     * computes unread_count/last_message. Assert the query count stays flat
     * regardless of how many contacts exist (no N+1 per contact).
     */
    public function test_query_count_tetap_flat_saat_jumlah_kontak_bertambah()
    {
        $instansi = Instansi::factory()->create();
        $staffRole = role::firstOrCreate(['nama_role' => 'Karyawan']);

        $me = User::factory()->create([
            'instansi_id' => $instansi->id,
            'role_id' => $staffRole->id,
        ]);

        $countFor = function (int $n) use ($instansi, $staffRole, $me) {
            User::where('id', '!=', $me->id)->delete();

            foreach (range(1, $n) as $i) {
                $contact = User::factory()->create([
                    'instansi_id' => $instansi->id,
                    'role_id' => $staffRole->id,
                ]);
                karyawan::create([
                    'user_id' => $contact->id,
                    'nama_lengkap' => "Kontak $i",
                ]);
                Chat::create([
                    'pengirim_id' => $contact->id,
                    'penerima_id' => $me->id,
                    'pesan' => "Halo dari $i",
                    'is_read' => false,
                ]);
            }

            DB::enableQueryLog();
            $response = $this->actingAs($me)->getJson('/api/chat/contacts');
            $response->assertStatus(200);
            $queryCount = count(DB::getQueryLog());
            DB::disableQueryLog();
            DB::flushQueryLog();

            return $queryCount;
        };

        $small = $countFor(5);
        $large = $countFor(20);

        $this->assertSame($small, $large, "Query count grew from {$small} to {$large} as contact count grew — N+1 suspected.");
    }
}
