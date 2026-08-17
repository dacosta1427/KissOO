<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="data:,">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Login &middot; KissOO Showcase</title>
  <link rel="stylesheet" href="/vendor/tailwind.css">
  <script type="module" src="/vendor/datastar.js"></script>
  <script type="module" src="/vendor/datastar-start.js?v=2"></script>
</head>
<body class="bg-slate-50 text-slate-800 min-h-screen">
  <header class="bg-indigo-600 text-white p-6 shadow flex items-center justify-between">
    <div>
      <h1 class="text-2xl font-bold">Sign in</h1>
      <p class="text-indigo-100 text-sm mt-1">KissOO hypermedia login &mdash; Datastar + Tailwind, no Svelte.</p>
    </div>
    <a href="/showcase" class="text-indigo-100 hover:text-white text-sm underline">&#8592; Showcase home</a>
  </header>

  <main class="max-w-7xl mx-auto px-4 py-8">
    <div class="flex items-center justify-center min-h-[60vh]">
      <!-- id="login-card" is swapped (outer) by the loginPost fragment on success -->
      <div class="bg-white p-8 rounded-lg shadow-md w-full max-w-md" id="login-card"
           data-signals="{username:'admin',password:'',error:''}">
        <h1 class="text-2xl font-bold text-center mb-6">Sign in</h1>

        <!-- loginPost patches this on failure -->
        <div id="login-result"></div>

        <form class="mt-4"
              data-on:submit="@get('/rest?_class=services/ShowcaseService&amp;_method=loginPost')">
          <div class="mb-4">
            <label for="username" class="block text-gray-700 text-sm font-bold mb-2">Username</label>
            <input type="text" id="username" data-bind:username
                   class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                   placeholder="Username" autocomplete="username">
          </div>

          <div class="mb-4">
            <label for="password" class="block text-gray-700 text-sm font-bold mb-2">Password</label>
            <input type="password" id="password" data-bind:password
                   class="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                   placeholder="Password" autocomplete="current-password">
          </div>

          <button type="submit"
                  class="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline">
            Login
          </button>
        </form>

        <p class="text-gray-500 text-xs text-center mt-4">Default admin: <code>admin</code> / <code>admin</code></p>
      </div>
    </div>
  </main>
</body>
</html>
