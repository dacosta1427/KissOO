<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>CRUD &middot; KissOO Showcase</title>
  <script src="/vendor/tailwind.js"></script>
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<!-- $uuid is (re)loaded from localStorage so authed Datastar calls can carry X-Kiss-Uuid -->
<body class="bg-slate-50 text-slate-800 min-h-screen"
      data-signals="{uuid:''}"
      data-effect="$uuid || ($uuid = localStorage.getItem('kissUuid')||'')">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Owners (CRUD)</h1>
      <p class="text-indigo-100 text-sm mt-1">Datastar-driven table &mdash; New / Edit / Delete against the Perst store.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-4xl mx-auto p-6">
    <p class="text-sm text-slate-500 mb-4"
       data-text="'Signed in as ' + (localStorage.getItem('kissUser') || 'guest')"></p>

    <!-- New Owner (patched to a form by crudForm) -->
    <div id="crud-form" class="mb-6">
      <button class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700"
              data-on:click="@get('/rest?_class=services/ShowcaseService&amp;_method=crudForm')">New Owner</button>
    </div>

    <div class="bg-white rounded-xl shadow overflow-hidden">
      <table class="w-full text-sm">
        <thead class="bg-slate-100 text-left">
          <tr>
            <th class="px-3 py-2">Name</th>
            <th class="px-3 py-2">Email</th>
            <th class="px-3 py-2">Phone</th>
            <th class="px-3 py-2">Actions</th>
          </tr>
        </thead>
        <!-- rows are loaded on init and refreshed by crudSave/crudDelete -->
        <tbody id="owner-rows"
               data-init="@get('/rest?_class=services/ShowcaseService&amp;_method=crudList')"></tbody>
      </table>
    </div>
  </main>
</body>
</html>
