<?php

namespace App\Http\Controllers;

use App\Services\NotificationService;
use App\Traits\ApiResponse;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class NotificationController extends Controller
{
    use ApiResponse;

    public function __construct(
        private NotificationService $notificationService,
    ) {
        $this->middleware('auth:sanctum');
    }

    /**
     * Ambil daftar notifikasi user (paginated).
     */
    public function index(Request $request): JsonResponse
    {
        $user = $request->user();
        $perPage = $request->input('per_page', 20);

        $notifications = $this->notificationService->getUserNotifications($user->id, $perPage);

        return response()->json([
            'status' => 'success',
            'message' => 'Daftar notifikasi',
            'data' => $notifications->items(),
            'meta' => [
                'current_page' => $notifications->currentPage(),
                'last_page' => $notifications->lastPage(),
                'per_page' => $notifications->perPage(),
                'total' => $notifications->total(),
            ],
        ]);
    }

    /**
     * Ambil jumlah notifikasi belum dibaca.
     */
    public function unreadCount(Request $request): JsonResponse
    {
        $user = $request->user();
        $count = $this->notificationService->getUnreadCount($user->id);

        return $this->success([
            'unread_count' => $count,
        ]);
    }

    /**
     * Tandai satu notifikasi sudah dibaca.
     */
    public function markAsRead(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        $notification = $this->notificationService->markAsRead($id, $user->id);

        if (! $notification) {
            return $this->error('Notifikasi tidak ditemukan', 404);
        }

        return $this->success($notification, 'Notifikasi ditandai sudah dibaca');
    }

    /**
     * Tandai semua notifikasi sudah dibaca.
     */
    public function markAllAsRead(Request $request): JsonResponse
    {
        $user = $request->user();
        $updated = $this->notificationService->markAllAsRead($user->id);

        return $this->success([
            'updated_count' => $updated,
        ], 'Semua notifikasi ditandai sudah dibaca');
    }

    /**
     * Hapus notifikasi.
     */
    public function destroy(Request $request, int $id): JsonResponse
    {
        $user = $request->user();
        $deleted = $this->notificationService->delete($id, $user->id);

        if (! $deleted) {
            return $this->error('Notifikasi tidak ditemukan', 404);
        }

        return $this->success(null, 'Notifikasi dihapus');
    }
}
