@param java.util.List<java.util.List<String>> demoRows

<main>
  <hgroup>
    <h1>Controls &amp; Components</h1>
    <p>Every PicoCSS flavor plus the Yohn-fork extras, rendered live with copyable source.
       Each card shows a working example; expand <em>Show HTML</em> to grab the markup.</p>
  </hgroup>

  <div class="controls-layout">
    <aside class="cat-nav">
      <nav>
        <a href="#typography">Typography</a>
        <a href="#links">Links</a>
        <a href="#buttons">Buttons</a>
        <a href="#forms">Form fields</a>
        <a href="#choices">Checkboxes / Radios / Switch</a>
        <a href="#range">Range</a>
        <a href="#file">File</a>
        <a href="#validation">Validation</a>
        <a href="#tables">Tables</a>
        <a href="#cards">Cards</a>
        <a href="#nav-menu">Nav menu</a>
        <a href="#tabs">Tabs</a>
        <a href="#dropdown">Dropdown</a>
        <a href="#progress">Progress &amp; meter</a>
        <a href="#loading">Loading</a>
        <a href="#accordion">Accordion</a>
        <a href="#modal">Modal</a>
        <a href="#tooltip">Tooltip</a>
        <a href="#grid">Grid &amp; layout</a>
        <a href="#interactive">Interactive (Datastar)</a>
        <a href="#dynamic-table">Dynamic table</a>
        <a href="#live-search">Live search (server)</a>
        <a href="#echo">Form &rarr; server</a>
        <a href="#timeline">Timeline (fork)</a>
        <a href="#breadcrumb">Breadcrumb (fork)</a>
        <a href="#tags">Tags (KissOO)</a>
      </nav>
    </aside>

    <div class="components">

      <!-- ===================== Typography ===================== -->
      <article class="component" id="typography">
        <header><h3>Typography</h3><p>Headings, text utilities, lists, rules and inline code.</p></header>
        <div class="example" id="ex-typography">
          <h1>Heading 1</h1>
          <h2>Heading 2</h2>
          <h3>Heading 3</h3>
          <p>A paragraph with <strong>bold</strong>, <em>italic</em>, <mark>mark</mark>, <small>small</small>,
             <del>deleted</del>, <ins>inserted</ins>, <sub>sub</sub>, <sup>sup</sup> and
             <abbr title="HyperText Markup Language">HTML</abbr>.</p>
          <blockquote>"Make things as simple as possible, but not simpler."</blockquote>
          <pre>code block { ok: true }</pre>
          <kbd>Ctrl</kbd> + <kbd>C</kbd>
          <ul><li>Unordered item</li><li>Unordered item</li></ul>
          <ol><li>Ordered item</li><li>Ordered item</li></ol>
          <dl><dt>Term</dt><dd>Definition goes here.</dd></dl>
          <hr>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-typography').textContent = document.getElementById('ex-typography').outerHTML">Show HTML</summary>
          <pre id="src-typography"></pre>
        </details>
      </article>

      <!-- ===================== Links ===================== -->
      <article class="component" id="links">
        <header><h3>Links</h3><p>Inline anchors in the default, secondary and contrast variants.</p></header>
        <div class="example" id="ex-links">
          <p><a href="#">Default link</a></p>
          <p><a class="secondary" href="#">Secondary link</a></p>
          <p><a class="contrast" href="#">Contrast link</a></p>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-links').textContent = document.getElementById('ex-links').outerHTML">Show HTML</summary>
          <pre id="src-links"></pre>
        </details>
      </article>

      <!-- ===================== Buttons ===================== -->
      <article class="component" id="buttons">
        <header><h3>Buttons</h3><p>Action elements in every PicoCSS variant.</p></header>
        <div class="example" id="ex-buttons">
          <button>Primary</button>
          <button class="secondary">Secondary</button>
          <button class="outline">Outline</button>
          <button class="contrast">Contrast</button>
          <button aria-busy="true">Loading</button>
          <button disabled>Disabled</button>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-buttons').textContent = document.getElementById('ex-buttons').outerHTML">Show HTML</summary>
          <pre id="src-buttons"></pre>
        </details>
      </article>

      <!-- ===================== Form fields ===================== -->
      <article class="component" id="forms">
        <header><h3>Form fields</h3><p>Inputs, email, textarea, select and help text.</p></header>
        <div class="example" id="ex-forms">
          <label>Text
            <input type="text" placeholder="Text input">
          </label>
          <label>Email
            <input type="email" placeholder="name@kissoo.dev">
          </label>
          <label>Message
            <textarea rows="3" placeholder="Multiline..."></textarea>
          </label>
          <label>Select
            <select>
              <option>Option one</option>
              <option>Option two</option>
              <option>Option three</option>
            </select>
          </label>
          <small>Help text appears under the field.</small>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-forms').textContent = document.getElementById('ex-forms').outerHTML">Show HTML</summary>
          <pre id="src-forms"></pre>
        </details>
      </article>

      <!-- ===================== Choices ===================== -->
      <article class="component" id="choices">
        <header><h3>Checkboxes, radios &amp; switch</h3><p>Choice controls, including the PicoCSS role="switch".</p></header>
        <div class="example" id="ex-choices">
          <label><input type="checkbox" checked> Checked</label>
          <label><input type="checkbox"> Unchecked</label>
          <fieldset>
            <legend>Radio group</legend>
            <label><input type="radio" name="r1" checked> One</label>
            <label><input type="radio" name="r1"> Two</label>
          </fieldset>
          <label><input type="checkbox" role="switch" checked> Switch on</label>
          <label><input type="checkbox" role="switch"> Switch off</label>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-choices').textContent = document.getElementById('ex-choices').outerHTML">Show HTML</summary>
          <pre id="src-choices"></pre>
        </details>
      </article>

      <!-- ===================== Range ===================== -->
      <article class="component" id="range">
        <header><h3>Range</h3><p>Sliding numeric input.</p></header>
        <div class="example" id="ex-range" data-signals="{vol:50}">
          <label>Volume: <span data-text="$vol"></span>
            <input type="range" min="0" max="100" data-bind:vol>
          </label>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-range').textContent = document.getElementById('ex-range').outerHTML">Show HTML</summary>
          <pre id="src-range"></pre>
        </details>
      </article>

      <!-- ===================== File ===================== -->
      <article class="component" id="file">
        <header><h3>File</h3><p>Native file picker, styled by PicoCSS.</p></header>
        <div class="example" id="ex-file">
          <input type="file">
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-file').textContent = document.getElementById('ex-file').outerHTML">Show HTML</summary>
          <pre id="src-file"></pre>
        </details>
      </article>

      <!-- ===================== Validation ===================== -->
      <article class="component" id="validation">
        <header><h3>Validation &amp; states</h3><p>Invalid, disabled and required field states.</p></header>
        <div class="example" id="ex-validation">
          <label>Invalid
            <input type="text" aria-invalid="true" value="not valid">
          </label>
          <label>Required
            <input type="text" required placeholder="Required field">
          </label>
          <label>Disabled
            <input type="text" disabled value="disabled">
          </label>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-validation').textContent = document.getElementById('ex-validation').outerHTML">Show HTML</summary>
          <pre id="src-validation"></pre>
        </details>
      </article>

      <!-- ===================== Tables ===================== -->
      <article class="component" id="tables">
        <header><h3>Tables</h3><p>Basic, striped, hover (fork) and bordered variants.</p></header>
        <div class="example" id="ex-tables">
          <table class="striped hover">
            <thead><tr><th>Name</th><th>Role</th><th>Status</th></tr></thead>
            <tbody>
              <tr><td>Ada Lovelace</td><td>Owner</td><td>Active</td></tr>
              <tr><td>Linus T.</td><td>Cleaner</td><td>Active</td></tr>
              <tr><td>Grace H.</td><td>Owner</td><td>Paused</td></tr>
            </tbody>
          </table>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-tables').textContent = document.getElementById('ex-tables').outerHTML">Show HTML</summary>
          <pre id="src-tables"></pre>
        </details>
      </article>

      <!-- ===================== Cards ===================== -->
      <article class="component" id="cards">
        <header><h3>Cards</h3><p>The &lt;article&gt; element is PicoCSS's card surface.</p></header>
        <div class="example" id="ex-cards">
          <article>
            <h3>Card title</h3>
            <p>Card body copy describing the item. Cards stack neatly in a grid.</p>
            <button>Action</button>
          </article>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-cards').textContent = document.getElementById('ex-cards').outerHTML">Show HTML</summary>
          <pre id="src-cards"></pre>
        </details>
      </article>

      <!-- ===================== Nav / breadcrumb / pagination ===================== -->
      <article class="component" id="nav-menu">
        <header><h3>Nav, breadcrumb &amp; pagination</h3><p>Horizontal navigation, breadcrumb trail and pager.</p></header>
        <div class="example" id="ex-nav-menu">
          <div class="row">
            <div class="col-md-6">
              <p><strong>Nav</strong></p>
              <nav>
                <ul>
                  <li><a href="#">Home</a></li>
                  <li><a href="#">Docs</a></li>
                  <li><a href="#">About</a></li>
                </ul>
              </nav>
            </div>
            <div class="col-md-6">
              <p><strong>Breadcrumb</strong></p>
              <nav aria-label="Breadcrumb">
                <ul>
                  <li><a href="#">Home</a></li>
                  <li><a href="#">Library</a></li>
                  <li>Current page</li>
                </ul>
              </nav>
              <p><strong>Pagination</strong></p>
              <nav aria-label="Pagination">
                <ul>
                  <li><a href="#" aria-label="Previous">&lsaquo;</a></li>
                  <li><a href="#">1</a></li>
                  <li><a href="#" aria-current="page">2</a></li>
                  <li><a href="#">3</a></li>
                  <li><a href="#" aria-label="Next">&rsaquo;</a></li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-nav-menu').textContent = document.getElementById('ex-nav-menu').outerHTML">Show HTML</summary>
          <pre id="src-nav-menu"></pre>
        </details>
      </article>

      <!-- ===================== Tabs ===================== -->
      <article class="component" id="tabs">
        <header><h3>Tabs</h3><p>Tablist with panels, wired with Datastar so it switches.</p></header>
        <div class="example" id="ex-tabs">
          <div data-signals="{tab:'one'}">
            <nav role="tablist">
              <button role="tab" aria-selected="$tab=='one'" data-on:click="$tab='one'">One</button>
              <button role="tab" aria-selected="$tab=='two'" data-on:click="$tab='two'">Two</button>
            </nav>
            <article role="tabpanel" data-show="$tab=='one'"><p>Content of the first panel.</p></article>
            <article role="tabpanel" data-show="$tab=='two'"><p>Content of the second panel.</p></article>
          </div>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-tabs').textContent = document.getElementById('ex-tabs').outerHTML">Show HTML</summary>
          <pre id="src-tabs"></pre>
        </details>
      </article>

      <!-- ===================== Dropdown ===================== -->
      <article class="component" id="dropdown">
        <header><h3>Dropdown</h3><p>Native disclosure menu built on &lt;details&gt;.</p></header>
        <div class="example" id="ex-dropdown">
          <details class="dropdown">
            <summary>Menu</summary>
            <ul>
              <li><a href="#">Profile</a></li>
              <li><a href="#">Settings</a></li>
              <li><a href="#">Sign out</a></li>
            </ul>
          </details>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-dropdown').textContent = document.getElementById('ex-dropdown').outerHTML">Show HTML</summary>
          <pre id="src-dropdown"></pre>
        </details>
      </article>

      <!-- ===================== Progress & meter ===================== -->
      <article class="component" id="progress">
        <header><h3>Progress &amp; meter</h3><p>Determinate progress and a meter gauge.</p></header>
        <div class="example" id="ex-progress">
          <progress value="60" max="100">60%</progress>
          <meter value="0.6">60%</meter>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-progress').textContent = document.getElementById('ex-progress').outerHTML">Show HTML</summary>
          <pre id="src-progress"></pre>
        </details>
      </article>

      <!-- ===================== Loading ===================== -->
      <article class="component" id="loading">
        <header><h3>Loading</h3><p>Busy button spinner and an indeterminate progress bar.</p></header>
        <div class="example" id="ex-loading">
          <button aria-busy="true">Saving...</button>
          <progress></progress>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-loading').textContent = document.getElementById('ex-loading').outerHTML">Show HTML</summary>
          <pre id="src-loading"></pre>
        </details>
      </article>

      <!-- ===================== Accordion ===================== -->
      <article class="component" id="accordion">
        <header><h3>Accordion</h3><p>Stacked disclosures via &lt;details&gt;.</p></header>
        <div class="example" id="ex-accordion">
          <details open>
            <summary>First section</summary>
            <p>Body of the first section.</p>
          </details>
          <details>
            <summary>Second section</summary>
            <p>Body of the second section.</p>
          </details>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-accordion').textContent = document.getElementById('ex-accordion').outerHTML">Show HTML</summary>
          <pre id="src-accordion"></pre>
        </details>
      </article>

      <!-- ===================== Modal ===================== -->
      <article class="component" id="modal">
        <header><h3>Modal</h3><p>A &lt;dialog&gt;. Click <em>Open dialog</em> to show it modally; close with the buttons.</p></header>
        <div class="example" id="ex-modal">
          <button data-on:click="document.getElementById('demo-dialog').showModal()">Open dialog</button>
          <dialog id="demo-dialog">
            <article>
              <h3>Confirm action</h3>
              <p>Are you sure you want to continue?</p>
              <footer>
                <button class="secondary" data-on:click="document.getElementById('demo-dialog').close()">Cancel</button>
                <button data-on:click="document.getElementById('demo-dialog').close()">OK</button>
              </footer>
            </article>
          </dialog>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-modal').textContent = document.getElementById('ex-modal').outerHTML">Show HTML</summary>
          <pre id="src-modal"></pre>
        </details>
      </article>

      <!-- ===================== Tooltip ===================== -->
      <article class="component" id="tooltip">
        <header><h3>Tooltip</h3><p>Inline tooltips via the data-tooltip attribute.</p></header>
        <div class="example" id="ex-tooltip">
          <p>
            <span data-tooltip="Tooltip on top" data-placement="top">Hover me (top)</span>
            &nbsp;&nbsp;
            <span data-tooltip="Default placement">Hover me (default)</span>
          </p>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-tooltip').textContent = document.getElementById('ex-tooltip').outerHTML">Show HTML</summary>
          <pre id="src-tooltip"></pre>
        </details>
      </article>

      <!-- ===================== Alerts ===================== -->
      <article class="component" id="alerts">
        <header><h3>Alerts &amp; messages</h3><p>Semantic message blocks (role-based colour variants).</p></header>
        <div class="example" id="ex-alerts">
          <p role="alert">Danger / error message.</p>
          <p role="alert" class="secondary">Neutral message.</p>
          <p role="alert" class="contrast">Contrast message.</p>
          <p role="status">Informational status.</p>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-alerts').textContent = document.getElementById('ex-alerts').outerHTML">Show HTML</summary>
          <pre id="src-alerts"></pre>
        </details>
      </article>

      <!-- ===================== Group ===================== -->
      <article class="component" id="group">
        <header><h3>Group</h3><p>Button groups and an input+action row.</p></header>
        <div class="example" id="ex-group">
          <div class="group">
            <button>One</button>
            <button>Two</button>
            <button>Three</button>
          </div>
          <div class="grid">
            <input type="text" placeholder="you@kissoo.dev">
            <button type="button">Subscribe</button>
          </div>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-group').textContent = document.getElementById('ex-group').outerHTML">Show HTML</summary>
          <pre id="src-group"></pre>
        </details>
      </article>

      <!-- ===================== Grid & layout ===================== -->
      <article class="component" id="grid">
        <header><h3>Grid &amp; layout</h3><p>PicoCSS auto .grid plus the Yohn-fork .row / .col-* helpers.</p></header>
        <div class="example" id="ex-grid">
          <div class="row">
            <div class="col-6"><code>.col-6</code></div>
            <div class="col-6"><code>.col-6</code></div>
          </div>
          <div class="row">
            <div class="col-md-4"><code>.col-md-4</code></div>
            <div class="col-md-4"><code>.col-md-4</code></div>
            <div class="col-md-4"><code>.col-md-4</code></div>
          </div>
          <div class="grid">
            <div><code>.grid</code> cell</div>
            <div><code>.grid</code> cell</div>
            <div><code>.grid</code> cell</div>
          </div>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-grid').textContent = document.getElementById('ex-grid').outerHTML">Show HTML</summary>
          <pre id="src-grid"></pre>
        </details>
      </article>

      <!-- ===================== Timeline (Yohn fork) ===================== -->
      <article class="component" id="timeline">
        <header><h3>Timeline (Yohn fork)</h3><p>Vertical timeline &mdash; a component the Yohn fork adds on top of PicoCSS.</p></header>
        <div class="example" id="ex-timeline">
          <ul class="timeline">
            <li class="point left"><p><strong>2024</strong> &mdash; KissOO founded.</p></li>
            <li class="point right"><p><strong>2025</strong> &mdash; First owners onboarded.</p></li>
            <li class="point left"><p><strong>2026</strong> &mdash; Hypermedia showcase shipped.</p></li>
          </ul>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-timeline').textContent = document.getElementById('ex-timeline').outerHTML">Show HTML</summary>
          <pre id="src-timeline"></pre>
        </details>
      </article>

      <!-- ===================== Breadcrumb (Yohn fork) ===================== -->
      <article class="component" id="breadcrumb">
        <header><h3>Breadcrumb (Yohn fork)</h3><p>Hierarchical navigation trail.</p></header>
        <div class="example" id="ex-breadcrumb">
          <nav aria-label="breadcrumb">
            <ul>
              <li><a href="/showcase">Home</a></li>
              <li><a href="/showcase/controls">Showcase</a></li>
              <li aria-current="page">Controls</li>
            </ul>
          </nav>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-breadcrumb').textContent = document.getElementById('ex-breadcrumb').outerHTML">Show HTML</summary>
          <pre id="src-breadcrumb"></pre>
        </details>
      </article>

      <!-- ===================== Tooltip ===================== -->
      <article class="component" id="tooltip">
        <header><h3>Tooltip</h3><p>Hover / focus tooltip via <code>data-tooltip</code>.</p></header>
        <div class="example" id="ex-tooltip">
          <button data-tooltip="Saved!">Hover me</button>
          <span data-tooltip="Inline tooltip text">Inline tooltip</span>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-tooltip').textContent = document.getElementById('ex-tooltip').outerHTML">Show HTML</summary>
          <pre id="src-tooltip"></pre>
        </details>
      </article>

      <!-- ===================== Interactive (Datastar) ===================== -->
      <article class="component" id="interactive">
        <header><h3>Interactive form (Datastar)</h3><p>Two-way bound fields with a live preview and a local submit that echoes the collected values &mdash; pure client-side Datastar signals.</p></header>
        <div class="example" id="ex-interactive" data-signals="{fname:'',femail:'',fagree:false,fsent:''}">
          <label>Name
            <input type="text" placeholder="Your name" data-bind:fname>
          </label>
          <label>Email
            <input type="email" placeholder="you@kissoo.dev" data-bind:femail>
          </label>
          <label><input type="checkbox" data-bind:fagree> I agree to the terms</label>
          <p class="preview" data-text="'Preview: ' + ($fname || 'stranger') + ' (' + ($femail || 'no email') + ')' + ($fagree ? ' agreed' : '')"></p>
          <button data-on:click="$fsent = 'Submitted: ' + $fname + ' / ' + $femail + ' / ' + ($fagree ? 'yes' : 'no')">Submit</button>
          <p data-text="$fsent" class="success-text"></p>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-interactive').textContent = document.getElementById('ex-interactive').outerHTML">Show HTML</summary>
          <pre id="src-interactive"></pre>
        </details>
      </article>

      <!-- ===================== Dynamic table (Yohn fork) ===================== -->
      <article class="component" id="dynamic-table">
        <header><h3>Dynamic table (Yohn fork)</h3><p>The fork's <code>.striped.hover</code> table with a live client-side search filter driven by Datastar signals &mdash; type to filter rows instantly.</p></header>
        <div class="example" id="ex-dynamic-table" data-signals="{q:''}">
          <input type="search" placeholder="Filter table..." data-bind:q>
          <table class="striped hover">
            <thead><tr><th>Name</th><th>Role</th><th>Status</th></tr></thead>
            <tbody>
@for(java.util.List<String> r : demoRows)
              <tr data-show="$q=='' || '${r.get(0)}'.toLowerCase().includes($q.toLowerCase())"><td>${r.get(0)}</td><td>${r.get(1)}</td><td>${r.get(2)}</td></tr>
@endfor
            </tbody>
          </table>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-dynamic-table').textContent = document.getElementById('ex-dynamic-table').outerHTML">Show HTML</summary>
          <pre id="src-dynamic-table"></pre>
        </details>
      </article>

      <!-- ===================== Live search (server-backed, Datastar) ===================== -->
      <article class="component" id="live-search">
        <header><h3>Live search &middot; server-backed (Datastar)</h3><p>A real round-trip: every keystroke <code>@get</code>s the backend, which filters the dataset and patches the table body. This is the canonical list/search pattern for future apps.</p></header>
        <div class="example" id="ex-live-search" data-signals="{q:''}">
          <input type="search" placeholder="Search server-side..." data-bind:q data-on:input="@get('/rest?_class=services/ShowcaseService&_method=liveSearch')">
          <table class="striped hover">
            <thead><tr><th>Name</th><th>Role</th><th>Status</th></tr></thead>
            <tbody id="live-rows">
@for(java.util.List<String> r : demoRows)
              <tr><td>${r.get(0)}</td><td>${r.get(1)}</td><td>${r.get(2)}</td></tr>
@endfor
            </tbody>
          </table>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-live-search').textContent = document.getElementById('ex-live-search').outerHTML">Show HTML</summary>
          <pre id="src-live-search"></pre>
        </details>
      </article>

      <!-- ===================== Form -> server (Datastar) ===================== -->
      <article class="component" id="echo">
        <header><h3>Form &rarr; server (Datastar)</h3><p>Bound fields are sent (via <code>@get</code>) to a real endpoint; the server echoes the result back into the page. This is the canonical form-submit pattern.</p></header>
        <div class="example" id="ex-echo" data-signals="{name:'',email:''}">
          <label>Name
            <input type="text" placeholder="Your name" data-bind:name>
          </label>
          <label>Email
            <input type="email" placeholder="you@kissoo.dev" data-bind:email>
          </label>
          <button data-on:click="@get('/rest?_class=services/ShowcaseService&_method=echo')">Send to server</button>
          <div id="echo-result"></div>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-echo').textContent = document.getElementById('ex-echo').outerHTML">Show HTML</summary>
          <pre id="src-echo"></pre>
        </details>
      </article>

      <!-- ===================== Tags (KissOO) ===================== -->
      <article class="component" id="tags">
        <header><h3>Tags <small>(KissOO extension)</small></h3><p>PicoCSS has no badge, so we add a small .tag chip in koo.css.</p></header>
        <div class="example" id="ex-tags">
          <span class="tag">New</span>
          <span class="tag secondary">Draft</span>
          <span class="tag outline">Beta</span>
        </div>
        <details class="source"><summary data-on:click="document.getElementById('src-tags').textContent = document.getElementById('ex-tags').outerHTML">Show HTML</summary>
          <pre id="src-tags"></pre>
        </details>
      </article>

    </div>
  </div>
</main>
