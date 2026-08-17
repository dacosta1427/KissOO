<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
    <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Auth &middot; KissOO Showcase</title>
  <link rel="stylesheet" href="/vendor/tailwind.css">
  <script src="/vendor/htmx.js" defer></script>
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Auth</h1>
      <p class="text-indigo-100 text-sm mt-1">Login, then reuse the session via the X-Kiss-Uuid header.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-3xl mx-auto p-6">
    <section class="bg-white p-6 rounded-xl shadow"
             data-signals="{_class:'',_method:'Login',username:'admin',password:'admin',uuid:'',whoami:'',greeting:''}">
      <h2 class="text-lg font-semibold">Login &amp; session probe</h2>
      <p class="mt-1 text-sm text-slate-500">
        Log in as <code>admin / admin</code>. The backend returns a <code>uuid</code> which we then send back on the
        next request using the <code>X-Kiss-Uuid</code> header (MOD 4). The <code>whoami</code> endpoint is
        <em>not</em> whitelisted, so it only succeeds with a valid session.
      </p>

      <div class="mt-4 grid grid-cols-2 gap-3 max-w-md">
        <label class="text-sm flex flex-col gap-1">Username
          <input class="border rounded px-2 py-1" data-bind:username>
        </label>
        <label class="text-sm flex flex-col gap-1">Password
          <input type="password" class="border rounded px-2 py-1" data-bind:password>
        </label>
      </div>

      <button class="mt-4 px-4 py-1 bg-indigo-600 text-white rounded" data-on:click="@get('/rest?_method=Login')">Login</button>
      <p class="mt-3 text-sm text-slate-600" data-text="'Session uuid: ' + $uuid"></p>

      <button class="mt-4 px-4 py-1 bg-emerald-600 text-white rounded"
              data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=whoami')">Check session</button>
      <p class="mt-3 text-sm text-slate-600" data-text="'whoami=' + $whoami + ' --- greeting=' + $greeting"></p>
    </section>
  </main>
</body>
</html>
