<main>
  <hgroup>
    <h1>Sign in</h1>
    <p>KissOO hypermedia login &mdash; Datastar + PicoCSS, no Svelte.</p>
  </hgroup>

  <!-- id="login-card" is swapped (outer) by the loginPost fragment on success -->
  <article id="login-card" data-signals="{username:'admin',password:'',error:''}">
    <!-- loginPost patches this on failure -->
    <div id="login-result"></div>

    <form data-on:submit="@get('/rest?_class=services/ShowcaseService&amp;_method=loginPost')">
      <label for="username">
        Username
        <input type="text" id="username" name="username" data-bind:username placeholder="Username" autocomplete="username">
      </label>

      <label for="password">
        Password
        <input type="password" id="password" name="password" data-bind:password placeholder="Password" autocomplete="current-password">
      </label>

      <button type="submit">Login</button>
    </form>

    <small>Default admin: <code>admin</code> / <code>admin</code></small>
  </article>
</main>
