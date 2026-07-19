<?php

namespace App\Http\Controllers;

use App\Http\Requests\SendChatRequest;
use App\Models\Chat;
use App\Models\User;
use App\Traits\ApiResponse;
use Illuminate\Http\Request;

class ChatController extends Controller
{
    use ApiResponse;

    /**
     * GET /api/chat/contacts
     * Ambil daftar kontak dalam satu instansi (UMKM) yang sama,
     * dikelompokkan berdasarkan role.
     */
    public function contacts(Request $request)
    {
        $user = $request->user();
        $instansiId = $user->instansi_id;

        // Ambil semua user dalam instansi yang sama, kecuali user yang login
        // Gunakan subquery untuk unread count dan last message guna hindari N+1
        $users = User::where('instansi_id', $instansiId)
            ->where('id', '!=', $user->id)
            ->with(['role', 'profilKaryawan'])
            ->withCount(['pesanDiterima as unread_count' => function ($q) use ($user) {
                $q->where('pengirim_id', $user->id)->where('is_read', false);
            }])
            ->get();

        // Ambil last message untuk setiap pasangan chat dalam satu query
        $contactIds = $users->pluck('id')->toArray();
        $lastMessages = [];
        if (!empty($contactIds)) {
            // Get latest message per contact pair using a subquery approach
            $rawLastMessages = Chat::where(function ($q) use ($user, $contactIds) {
                $q->whereIn('pengirim_id', $contactIds)->where('penerima_id', $user->id);
            })->orWhere(function ($q) use ($user, $contactIds) {
                $q->whereIn('penerima_id', $contactIds)->where('pengirim_id', $user->id);
            })->orderBy('created_at', 'desc')->get()
            ->groupBy(function ($chat) use ($user) {
                return $chat->pengirim_id === $user->id ? $chat->penerima_id : $chat->pengirim_id;
            })->map(fn($msgs) => $msgs->first());

            foreach ($rawLastMessages as $contactId => $message) {
                $lastMessages[$contactId] = $message;
            }
        }

        $contacts = $users->map(function ($kontak) use ($user, $lastMessages) {
            $lastMessage = $lastMessages[$kontak->id] ?? null;

            return [
                'id' => $kontak->id,
                'name' => $kontak->name,
                'email' => $kontak->email,
                'role' => $kontak->role ? $kontak->role->nama_role : 'Unknown',
                'role_id' => $kontak->role_id,
                'nama_lengkap' => $kontak->profilKaryawan ? $kontak->profilKaryawan->nama_lengkap : $kontak->name,
                'foto_profil' => $kontak->profilKaryawan ? $kontak->profilKaryawan->foto_profil : null,
                'unread_count' => (int) $kontak->unread_count,
                'last_message' => $lastMessage ? $lastMessage->pesan : null,
                'last_message_time' => $lastMessage ? $lastMessage->created_at->diffForHumans() : null,
            ];
        });

        // Kelompokkan berdasarkan role
        $grouped = $contacts->groupBy('role')->map(function ($items, $role) {
            return [
                'role' => $role,
                'contacts' => $items->sortByDesc('last_message_time')->values()->toArray(),
            ];
        })->values();

        return $this->success($grouped);
    }

    /**
     * GET /api/chat/messages/{user}
     * Ambil riwayat chat dengan user tertentu.
     */
    public function messages(Request $request, User $user)
    {
        $currentUser = $request->user();

        // Validasi: pastikan satu instansi
        if ($currentUser->instansi_id !== $user->instansi_id) {
            return $this->error('User tidak dalam instansi yang sama.', 403);
        }

        // Ambil pesan antara kedua user
        $messages = Chat::where(function ($q) use ($currentUser, $user) {
            $q->where('pengirim_id', $currentUser->id)
              ->where('penerima_id', $user->id);
        })->orWhere(function ($q) use ($currentUser, $user) {
            $q->where('pengirim_id', $user->id)
              ->where('penerima_id', $currentUser->id);
        })->orderBy('created_at', 'asc')->get();

        // Tandai semua pesan dari user tersebut sebagai sudah dibaca
        Chat::where('pengirim_id', $user->id)
            ->where('penerima_id', $currentUser->id)
            ->where('is_read', false)
            ->update(['is_read' => true]);

        // Format waktu
        $messages = $messages->map(function ($msg) {
            return [
                'id' => $msg->id,
                'pengirim_id' => $msg->pengirim_id,
                'penerima_id' => $msg->penerima_id,
                'pesan' => $msg->pesan,
                'is_read' => $msg->is_read,
                'created_at' => $msg->created_at->toDateTimeString(),
                'waktu' => $msg->created_at->diffForHumans(),
            ];
        });

        return $this->success($messages);
    }

    /**
     * POST /api/chat/send
     * Kirim pesan ke user tertentu.
     */
    public function send(SendChatRequest $request)
    {
        $currentUser = $request->user();
        $penerima = User::find($request->penerima_id);

        if (!$penerima) {
            return $this->error('Penerima tidak ditemukan.', 404);
        }

        // Validasi: pastikan satu instansi
        if ($currentUser->instansi_id !== $penerima->instansi_id) {
            return $this->error('Penerima tidak dalam instansi yang sama.', 403);
        }

        $chat = Chat::create([
            'pengirim_id' => $currentUser->id,
            'penerima_id' => $request->penerima_id,
            'pesan' => $request->pesan,
            'is_read' => false,
        ]);

        return $this->success([
            'id' => $chat->id,
            'pengirim_id' => $chat->pengirim_id,
            'penerima_id' => $chat->penerima_id,
            'pesan' => $chat->pesan,
            'is_read' => $chat->is_read,
            'created_at' => $chat->created_at->toDateTimeString(),
            'waktu' => $chat->created_at->diffForHumans(),
        ], 'Pesan berhasil dikirim.', 201);
    }

    /**
     * PUT /api/chat/read/{user}
     * Tandai semua pesan dari user tertentu sebagai sudah dibaca.
     */
    public function markAsRead(Request $request, User $user)
    {
        $currentUser = $request->user();

        Chat::where('pengirim_id', $user->id)
            ->where('penerima_id', $currentUser->id)
            ->where('is_read', false)
            ->update(['is_read' => true]);

        return $this->success(null, 'Pesan ditandai sudah dibaca.');
    }
}
