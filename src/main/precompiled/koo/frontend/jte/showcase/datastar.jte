<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
    <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Datastar &middot; KissOO Showcase</title>
  <link rel="stylesheet" href="/vendor/tailwind.css">
  <script src="/vendor/htmx.js" defer></script>
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Datastar</h1>
      <p class="text-indigo-100 text-sm mt-1">Reactive signals &mdash; the frontend is just HTML attributes.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-3xl mx-auto p-6 space-y-6">
    <section class="bg-white p-6 rounded-xl shadow" data-signals="{count:0}">
      <h2 class="text-lg font-semibold">1 &middot; Client-side reactive counter</h2>
      <p class="mt-1 text-sm text-slate-500">Pure browser reactivity &mdash; no server involved.</p>
      <div class="mt-4 flex items-center gap-3">
        <button class="px-4 py-1 bg-indigo-600 text-white rounded" data-on:click="$count--">&minus;</button>
        <span class="text-3xl tabular-nums w-16 text-center" data-text="$count"></span>
        <button class="px-4 py-1 bg-indigo-600 text-white rounded" data-on:click="$count++">+</button>
      </div>
    </section>

    <section class="bg-white p-6 rounded-xl shadow"
             data-signals="{_class:'services/HypermediaTestService',_method:'patchSignals',name:'world',greeting:''}">
      <h2 class="text-lg font-semibold">2 &middot; Server round-trip (JSON signals)</h2>
      <p class="mt-1 text-sm text-slate-500">
        The button POSTs all signals to Kiss; the backend returns a <code>greeting</code> signal that is merged back in.
      </p>
      <div class="mt-4 flex items-end gap-3">
        <label class="text-sm flex flex-col gap-1">
          Name
          <input class="border rounded px-2 py-1" data-bind:name>
        </label>
        <button class="px-4 py-1 bg-emerald-600 text-white rounded" data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=patchSignals')">Call Kiss backend</button>
      </div>
      <p class="mt-4 text-emerald-700 font-medium" data-text="'Server says: ' + $greeting"></p>
    </section>

    <section class="bg-white p-6 rounded-xl shadow">
      <h2 class="text-lg font-semibold">3 &middot; Server-rendered fragment (patch-elements)</h2>
      <p class="mt-1 text-sm text-slate-500">
        The button asks Kiss to render a JTE fragment; the backend returns it as a
        <code>datastar-patch-elements</code> response that morphs the card below.
      </p>
      <button class="mt-4 px-4 py-1 bg-amber-600 text-white rounded"
              data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=fragmentDs')">
        Load Datastar fragment
      </button>
      <div id="ds-card" class="mt-4"></div>
    </section>
  </main>
</body>
</html>
