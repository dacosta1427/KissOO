<main>
  <hgroup>
    <h1>Datastar</h1>
    <p>Reactive signals &mdash; the frontend is just HTML attributes.</p>
  </hgroup>

  <article data-signals="{count:0}">
    <h2>1 &middot; Client-side reactive counter</h2>
    <p>Pure browser reactivity &mdash; no server involved.</p>
    <div class="row">
      <div class="col-2"><button data-on:click="$count--">&minus;</button></div>
      <div class="col-8"><p class="text-center" data-text="$count" style="font-size:2rem"></p></div>
      <div class="col-2"><button data-on:click="$count++">+</button></div>
    </div>
  </article>

  <article data-signals="{_class:'services/HypermediaTestService',_method:'patchSignals',name:'world',greeting:''}">
    <h2>2 &middot; Server round-trip (JSON signals)</h2>
    <p>The button POSTs all signals to Kiss; the backend returns a <code>greeting</code> signal that is merged back in.</p>
    <div class="row">
      <div class="col-md-8">
        <label>Name
          <input data-bind:name>
        </label>
      </div>
      <div class="col-md-4">
        <button data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=patchSignals')">Call Kiss backend</button>
      </div>
    </div>
    <p class="success-text" data-text="'Server says: ' + $greeting"></p>
  </article>

  <article>
    <h2>3 &middot; Server-rendered fragment (patch-elements)</h2>
    <p>The button asks Kiss to render a JTE fragment; the backend returns it as a
      <code>datastar-patch-elements</code> response that morphs the card below.</p>
    <button data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=fragmentDs')">Load Datastar fragment</button>
    <div id="ds-card"></div>
  </article>
</main>
