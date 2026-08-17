<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
    <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>HTMX &middot; KissOO Showcase</title>
  <script src="/vendor/tailwind.js"></script>
  <script src="/vendor/htmx.js" defer></script>
  <script type="module" src="/vendor/datastar.js"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">HTMX</h1>
      <p class="text-indigo-100 text-sm mt-1">JTE-rendered HTML fragments, swapped into the DOM.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-3xl mx-auto p-6">
    <section class="bg-white p-6 rounded-xl shadow">
      <h2 class="text-lg font-semibold">Task list</h2>
      <p class="mt-1 text-sm text-slate-500">
        Type a title and click <em>Add task</em>. The backend renders a JTE fragment and HTMX swaps it into the list.
      </p>
      <div class="mt-4 flex gap-3">
        <input id="taskTitle" name="title" class="border rounded px-3 py-1 flex-1" placeholder="New task title">
        <button class="px-4 py-1 bg-indigo-600 text-white rounded"
                hx-get="/rest"
                hx-vals='{"_class":"services/HypermediaTestService","_method":"taskFragment"}'
                hx-include="#taskTitle"
                hx-target="#taskList"
                hx-swap="beforeend">Add task</button>
      </div>
      <div id="taskList" class="mt-4 space-y-2"></div>
    </section>
  </main>
</body>
</html>
