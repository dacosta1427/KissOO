<main>
  <hgroup>
    <h1>Live SSE</h1>
    <p>Server-Sent Events streamed straight into a signal.</p>
  </hgroup>

  <article data-signals="{clock:'connecting…'}">
    <h2>Live server clock</h2>
    <p>
      Click start. Datastar opens an SSE stream to the Kiss backend; each
      <code>datastar-patch-signals</code> event updates the <code>clock</code> signal below.
    </p>
    <p data-text="$clock" style="font-size:2.5rem;font-family:monospace"></p>
    <button data-on:click="@get('/rest?_class=services/HypermediaTestService&_method=liveClock')">Start live clock</button>
  </article>
</main>
