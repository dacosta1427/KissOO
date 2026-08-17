<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>KissOO Hypermedia Showcase</title>
  <script src="/vendor/tailwind.js"></script>
  <script src="/vendor/htmx.js" defer></script>
  <script type="module" src="/vendor/datastar.js"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow">
    <h1 class="text-2xl font-bold">KissOO Hypermedia Showcase</h1>
    <p class="text-indigo-100 text-sm mt-1">Server-rendered JTE + Datastar + HTMX + Tailwind &mdash; no Svelte, no build step.</p>
  </header>

  <main class="max-w-4xl mx-auto p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
    <a href="/showcase/datastar"
       class="block p-6 bg-white rounded-xl shadow hover:shadow-lg hover:ring-2 hover:ring-indigo-400 transition">
      <h2 class="text-xl font-semibold text-indigo-700">Datastar</h2>
      <p class="mt-2 text-sm text-slate-600">Reactive signals, a JSON server round-trip, and live SSE.</p>
    </a>

    <a href="/showcase/htmx"
       class="block p-6 bg-white rounded-xl shadow hover:shadow-lg hover:ring-2 hover:ring-indigo-400 transition">
      <h2 class="text-xl font-semibold text-indigo-700">HTMX</h2>
      <p class="mt-2 text-sm text-slate-600">JTE-rendered HTML fragments swapped into the DOM.</p>
    </a>

    <a href="/showcase/sse"
       class="block p-6 bg-white rounded-xl shadow hover:shadow-lg hover:ring-2 hover:ring-indigo-400 transition">
      <h2 class="text-xl font-semibold text-indigo-700">Live SSE</h2>
      <p class="mt-2 text-sm text-slate-600">Server-Sent Events streamed straight into signals.</p>
    </a>

    <a href="/showcase/auth"
       class="block p-6 bg-white rounded-xl shadow hover:shadow-lg hover:ring-2 hover:ring-indigo-400 transition">
      <h2 class="text-xl font-semibold text-indigo-700">Auth</h2>
      <p class="mt-2 text-sm text-slate-600">Login flow + session reuse via the X-Kiss-Uuid header.</p>
    </a>
  </main>

  <footer class="max-w-4xl mx-auto p-6 text-xs text-slate-400">
    Every page is produced by <code>org.kissweb.templates.TemplateProvider</code> (JTE) and served by the
    Kiss REST backend. Interactivity comes entirely from hypermedia attributes.
  </footer>
</body>
</html>
