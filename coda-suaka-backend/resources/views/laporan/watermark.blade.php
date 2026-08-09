@php
    // DomPDF merender background-image lewat ekstensi GD. Bila GD tidak ada,
    // emit background-image akan melempar exception saat render → seluruh PDF
    // gagal. Jadi watermark hanya dipasang bila GD tersedia (produksi) dan
    // file watermark ada; tanpa GD, PDF tetap tercetak tanpa watermark.
    $wmPath = resource_path('watermarks/koda-suaka.png');
    $wmDataUri = (function_exists('imagecreatetruecolor') && is_file($wmPath))
        ? 'data:image/png;base64,'.base64_encode((string) file_get_contents($wmPath))
        : '';
@endphp
@if ($wmDataUri !== '')
<style>
    body {
        background-image: url('{{ $wmDataUri }}');
        background-repeat: repeat;
    }
</style>
@endif
