@php
    $wmPath = resource_path('watermarks/koda-suaka.png');
    $wmDataUri = is_file($wmPath)
        ? 'data:image/png;base64,'.base64_encode((string) file_get_contents($wmPath))
        : '';
@endphp
<style>
    body {
        background-image: url('{{ $wmDataUri }}');
        background-repeat: repeat;
    }
</style>
