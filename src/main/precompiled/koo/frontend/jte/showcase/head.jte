@param String title
@param boolean htmx
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="theme-color" content="#5b21b6">
<title>${title} · KissOO Showcase</title>
<link rel="icon" href="data:,">
<link rel="stylesheet" href="/vendor/pico.min.css">
<link rel="stylesheet" href="/vendor/koo.css">
@if(htmx)
<script src="/vendor/htmx.js" defer></script>
@endif
<script type="module" src="/vendor/datastar.js"></script>
<script type="module" src="/vendor/datastar-start.js?v=2"></script>
