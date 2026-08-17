<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Controls &middot; KissOO Showcase</title>
  <link rel="stylesheet" href="/vendor/tailwind.css">
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Controls</h1>
      <p class="text-indigo-100 text-sm mt-1">Datastar two-way binding &mdash; every input drives a live signal.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-3xl mx-auto p-6"
        data-signals="{name:'',color:'red',agree:false,size:'m',volume:50}">
    <section class="bg-white p-6 rounded-xl shadow grid grid-cols-1 md:grid-cols-2 gap-6">
      <div class="space-y-4">
        <label class="block text-sm">Name
          <input data-bind:name class="border rounded px-2 py-1 w-full">
        </label>

        <label class="block text-sm">Favorite color
          <select data-bind:color class="border rounded px-2 py-1 w-full">
            <option value="red">Red</option>
            <option value="green">Green</option>
            <option value="blue">Blue</option>
          </select>
        </label>

        <label class="flex items-center gap-2 text-sm">
          <input type="checkbox" data-bind:agree> I agree to the terms
        </label>

        <div class="text-sm">Size
          <label class="mr-3"><input type="radio" value="s" data-bind:size> S</label>
          <label class="mr-3"><input type="radio" value="m" data-bind:size> M</label>
          <label><input type="radio" value="l" data-bind:size> L</label>
        </div>

        <label class="block text-sm">Volume: <span data-text="$volume"></span>
          <input type="range" min="0" max="100" data-bind:volume class="w-full">
        </label>
      </div>

      <div class="bg-slate-900 text-slate-100 rounded p-4 text-sm">
        <p class="text-emerald-300 font-semibold mb-2">Live preview</p>
        <p>Hello, <span data-text="($name || 'stranger')" class="text-emerald-300"></span>!</p>
        <p>Your favorite color is <span data-text="$color" class="text-emerald-300"></span>.</p>
        <p data-text="$agree ? 'You agreed &check;' : 'You have not agreed yet'"></p>
        <p>Size: <span data-text="$size" class="text-emerald-300"></span> &middot; Volume: <span data-text="$volume"></span></p>
        <pre class="mt-3 text-xs text-slate-400"
             data-text="JSON.stringify({name:$name,color:$color,agree:$agree,size:$size,volume:$volume})"></pre>
      </div>
    </section>
  </main>
</body>
</html>
