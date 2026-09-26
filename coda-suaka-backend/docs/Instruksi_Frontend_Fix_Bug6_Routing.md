# Instruksi Perubahan Frontend — Bug #6: Routing Berdasarkan Role

> **Bug**: Saat karyawan exit app tanpa logout, saat login ulang langsung masuk ke halaman dashboard pemilik (Owner) alih-alih dashboard karyawan.
>
> **Root Cause**: [`AuthViewModel.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/ui/screen/auth/AuthViewModel.kt) menggunakan `AuthState.Authenticated` sebagai `data object` tanpa membawa role, sehingga [`AppNavigation.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/navigation/AppNavigation.kt) selalu mengarahkan ke `Routes.DASHBOARD` (dashboard pemilik) tanpa membedakan role.
>
> **Status Backend**: ✅ Sudah diperbaiki — tidak ada perubahan backend yang diperlukan untuk Bug #6 ini.

---

## Ringkasan Perubahan

| # | File | Perubahan |
|---|------|-----------|
| 1 | `AuthViewModel.kt` | `AuthState.Authenticated` dari `data object` → `data class(val role: String)`, baca role dari `TokenManager` |
| 2 | `AuthScreen.kt` | Callback `onAuthenticated` menerima parameter `role: String` |
| 3 | `AppNavigation.kt` | Routing berdasarkan role — karyawan ke `DASHBOARD_KARYAWAN`, lainnya ke `DASHBOARD` |

---

## File 1: `AuthViewModel.kt`

**Path**: `app/src/main/java/com/example/codasuaka/ui/screen/auth/AuthViewModel.kt`

### Perubahan A: `AuthState` sealed class

**Sebelum**:
```kotlin
sealed class AuthState {
    data object Loading : AuthState()
    data object Authenticated : AuthState()
    data object Unauthenticated : AuthState()
}
```

**Sesudah**:
```kotlin
sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val role: String) : AuthState()
    data object Unauthenticated : AuthState()
}
```

### Perubahan B: Method `checkAuthStatus()`

**Sebelum**:
```kotlin
internal fun checkAuthStatus() {
    viewModelScope.launch {
        try {
            // Init cache dulu agar AuthInterceptor bisa baca token
            tokenManager.initCache()

            val token = tokenManager.getToken()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            // Verifikasi token via repository (domain layer)
            val isValid = authRepository.verifyToken()
            if (isValid) {
                _authState.value = AuthState.Authenticated
            } else {
                // Token invalid/expired → bersihkan
                tokenManager.clearAuthData()
                _authState.value = AuthState.Unauthenticated
            }
        } catch (_: Exception) {
            // Kalau gagal (misalnya network error), biarkan tetap Authenticated
            // agar user bisa buka offline data atau coba lagi nanti.
            val token = tokenManager.getCachedToken()
            _authState.value = if (!token.isNullOrBlank()) {
                AuthState.Authenticated
            } else {
                AuthState.Unauthenticated
            }
        }
    }
}
```

**Sesudah**:
```kotlin
internal fun checkAuthStatus() {
    viewModelScope.launch {
        try {
            // Init cache dulu agar AuthInterceptor bisa baca token
            tokenManager.initCache()

            val token = tokenManager.getToken()

            if (token.isNullOrBlank()) {
                _authState.value = AuthState.Unauthenticated
                return@launch
            }

            // Verifikasi token via repository (domain layer)
            val isValid = authRepository.verifyToken()
            if (isValid) {
                // Baca role dari tokenManager untuk navigasi berdasarkan role
                val role = tokenManager.getUserRole() ?: "Owner"
                _authState.value = AuthState.Authenticated(role)
            } else {
                // Token invalid/expired → bersihkan
                tokenManager.clearAuthData()
                _authState.value = AuthState.Unauthenticated
            }
        } catch (_: Exception) {
            // Kalau gagal (misalnya network error), biarkan tetap Authenticated
            // agar user bisa buka offline data atau coba lagi nanti.
            val token = tokenManager.getCachedToken()
            _authState.value = if (!token.isNullOrBlank()) {
                val role = tokenManager.getUserRole() ?: "Owner"
                AuthState.Authenticated(role)
            } else {
                AuthState.Unauthenticated
            }
        }
    }
}
```

---

## File 2: `AuthScreen.kt`

**Path**: `app/src/main/java/com/example/codasuaka/ui/screen/auth/AuthScreen.kt`

### Perubahan A: Signature fungsi `AuthScreen`

**Sebelum**:
```kotlin
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit
)
```

**Sesudah**:
```kotlin
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: (role: String) -> Unit,
    onUnauthenticated: () -> Unit
)
```

### Perubahan B: LaunchedEffect di dalam AuthScreen

**Sebelum**:
```kotlin
LaunchedEffect(authState) {
    when (authState) {
        is AuthState.Authenticated -> onAuthenticated()
        is AuthState.Unauthenticated -> onUnauthenticated()
        else -> {}
    }
}
```

**Sesudah**:
```kotlin
LaunchedEffect(authState) {
    when (authState) {
        is AuthState.Authenticated -> onAuthenticated(authState.role)
        is AuthState.Unauthenticated -> onUnauthenticated()
        else -> {}
    }
}
```

---

## File 3: `AppNavigation.kt`

**Path**: `app/src/main/java/com/example/codasuaka/navigation/AppNavigation.kt`

### Perubahan: Callback `onAuthenticated` di `authGraph()`

**Sebelum**:
```kotlin
onAuthenticated = {
    // Navigasi berdasarkan role akan ditentukan oleh AuthScreen
    // Default ke DASHBOARD (Owner), jika Karyawan akan pakai DASHBOARD_KARYAWAN
    navController.navigate(Routes.DASHBOARD) {
        popUpTo(Routes.AUTH) { inclusive = true }
    }
},
```

**Sesudah**:
```kotlin
onAuthenticated = { role ->
    // Navigasi berdasarkan role dari tokenManager
    // Functional roles (Keuangan, Manager, Staff, Karyawan) → DASHBOARD_KARYAWAN
    // Owner → DASHBOARD
    val functionalRoles = listOf("Keuangan", "Manager", "Staff", "Karyawan")
    val destination = if (role in functionalRoles) {
        Routes.DASHBOARD_KARYAWAN
    } else {
        Routes.DASHBOARD
    }
    navController.navigate(destination) {
        popUpTo(Routes.AUTH) { inclusive = true }
    }
},
```

> **Catatan Penting**: Pastikan `Routes.DASHBOARD_KARYAWAN` sudah didefinisikan di dalam `object Routes`. Jika belum, tambahkan:
> ```kotlin
> const val DASHBOARD_KARYAWAN = "dashboard_karyawan"
> ```

---

## Dependency & Prasyarat

### TokenManager
Pastikan [`TokenManager.kt`](coda-suaka-frontend/app/src/main/java/com/example/codasuaka/data/local/TokenManager.kt) memiliki method `getUserRole()`:

```kotlin
suspend fun getUserRole(): String? {
    return withContext(Dispatchers.IO) {
        prefs.getString(KEY_ROLE, null)
    }
}
```

Dan saat login, role disimpan via `saveAuthData()`:
```kotlin
suspend fun saveAuthData(
    token: String,
    userId: Int,
    name: String,
    email: String,
    role: String,  // ← pastikan parameter ini ada
    // ...其他参数
)
```

### API Response
Pastikan endpoint login/register mengembalikan field `role` di response body, misalnya:
```json
{
    "token": "...",
    "user": {
        "id": 1,
        "name": "John",
        "email": "john@example.com",
        "role": "Manager"
    }
}
```

---

## Daftar Role yang Valid

Berdasarkan [`config/roles.php`](coda-suaka-backend/config/roles.php) backend:

| Role | Dashboard |
|------|-----------|
| `Owner` | `Routes.DASHBOARD` (dashboard pemilik) |
| `SuperAdmin` | `Routes.DASHBOARD` (dashboard pemilik) |
| `Admin` | `Routes.DASHBOARD` (dashboard pemilik) |
| `Manager` | `Routes.DASHBOARD_KARYAWAN` |
| `Keuangan` | `Routes.DASHBOARD_KARYAWAN` |
| `Staff` | `Routes.DASHBOARD_KARYAWAN` |
| `Karyawan` | `Routes.DASHBOARD_KARYAWAN` |

---

## Cara Test

1. Login sebagai **karyawan** (role: `Karyawan`/`Manager`/`Staff`/`Keuangan`)
2. Tekan tombol home/tasbih untuk keluar dari app **tanpa logout**
3. Buka kembali app → seharusnya masuk ke `DashboardKaryawanScreen`
4. Login sebagai **owner** → seharusnya masuk ke `DashboardScreen`

---

## Checklist Verifikasi

- [ ] `AuthState.Authenticated` sudah berubah dari `data object` ke `data class(val role: String)`
- [ ] `checkAuthStatus()` membaca role dari `tokenManager.getUserRole()`
- [ ] `AuthScreen` callback `onAuthenticated` menerima parameter `role: String`
- [ ] `AppNavigation` routing berdasarkan role ke dashboard yang sesuai
- [ ] `Routes.DASHBOARD_KARYAWAN` sudah didefinisikan
- [ ] `TokenManager.getUserRole()` berfungsi dan mengembalikan role yang benar
- [ ] Test login sebagai karyawan → masuk dashboard karyawan
- [ ] Test login sebagai owner → masuk dashboard owner
- [ ] Test exit app tanpa logout → sesuai role saat login ulang
