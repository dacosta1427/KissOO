<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
    <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Live SSE &middot; KissOO Showcase</title>
  <link rel="stylesheet" href="/vendor/tailwind.css">
  <script src="/vendor/htmx.js" defer></script>
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Live SSE</h1>
      <p class="text-indigo-100 text-sm mt-1">Server-Sent Events streamed straight into a signal.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-3xl mx-auto p-6">
    <section class="bg-white p-6 rounded-xl shadow" data-signals="{clock:'connecting…'}">
      <h2 class="text-lg font-semibold">Live server clock</h2>
      <p class="mt-1 text-sm text-slate-500">
        Click start. Datastar opens an SSE stream to the Kiss backend; each
        <code>datastar-patch-signals</code> event updates the <code>clock</code> signal below.
      </p>
      <div class="mt-4 text-4xl font-mono text-indigo-700" data-text="$clock"></div>
      <button class="mt-4 px-4 py-1 bg-indigo-600 text-white rounded"
              data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=liveClock')">Start live clock</button>
    </section>
  </main>
</body>
</html>
