@param boolean loggedIn
<main>
  <hgroup>
    <h1>Auth</h1>
    <p>Login, then reuse the session via the X-Kiss-Uuid header.</p>
  </hgroup>

  <article data-signals="{_class:'',_method:'Login',username:'admin',password:'admin',uuid:'',whoami:'',greeting:''}">
    <h2>Login &amp; session probe</h2>
@if(!loggedIn)
    <p>
      Log in as <code>admin / admin</code>. The backend returns a <code>uuid</code> which we then send back on the
      next request using the <code>X-Kiss-Uuid</code> header (MOD 4). The <code>whoami</code> endpoint is
      <em>not</em> whitelisted, so it only succeeds with a valid session.
    </p>
@end if

    <div class="row">
      <div class="col-md-6">
        <label>Username
          <input data-bind:username>
        </label>
      </div>
      <div class="col-md-6">
        <label>Password
          <input type="password" data-bind:password>
        </label>
      </div>
    </div>

    <button data-on:click="@get('/rest?_method=Login')">Login</button>
    <p data-text="'Session uuid: ' + $uuid"></p>

    <button data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=whoami')">Check session</button>
    <p data-text="'whoami=' + $whoami + ' --- greeting=' + $greeting"></p>
  </article>
</main>
