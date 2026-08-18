<!-- $uuid is (re)loaded from localStorage so authed Datastar calls can carry X-Kiss-Uuid -->
<main data-signals="{uuid:''}" data-effect="$uuid || ($uuid = localStorage.getItem('kissUuid')||'')">
  <hgroup>
    <h1>Owners (CRUD)</h1>
    <p>Datastar-driven table &mdash; New / Edit / Delete against the Perst store.</p>
  </hgroup>

  <div id="crud-status"></div>

  <p data-text="'Signed in as ' + (localStorage.getItem('kissUser') || 'guest')"></p>

  <!-- New Owner (patched to a form by crudForm) -->
  <div id="crud-form">
    <button data-on:click="@get('/rest?_class=services/ShowcaseService&amp;_method=crudForm')">New Owner</button>
  </div>

  <!-- Perst CVersion history of the opened owner, rendered on the Yohn-fork timeline -->
  <div id="owner-history"></div>

  <article data-signals="{sort:'name',dir:'asc'}">
    <table class="striped hover">
      <thead>
        <tr>
          <th data-on:click="$dir=($sort=='name'&amp;&amp;$dir=='asc')?'desc':'asc'; $sort='name'; @get('/rest?_class=services/ShowcaseService&amp;_method=crudList&amp;sort=name&amp;dir='+$dir)">Name</th>
          <th data-on:click="$dir=($sort=='email'&amp;&amp;$dir=='asc')?'desc':'asc'; $sort='email'; @get('/rest?_class=services/ShowcaseService&amp;_method=crudList&amp;sort=email&amp;dir='+$dir)">Email</th>
          <th data-on:click="$dir=($sort=='phone'&amp;&amp;$dir=='asc')?'desc':'asc'; $sort='phone'; @get('/rest?_class=services/ShowcaseService&amp;_method=crudList&amp;sort=phone&amp;dir='+$dir)">Phone</th>
          <th>Actions</th>
        </tr>
      </thead>
      <!-- rows are loaded on init and refreshed by crudSave/crudDelete -->
      <tbody id="owner-rows"
             data-init="@get('/rest?_class=services/ShowcaseService&amp;_method=crudList')"></tbody>
    </table>
  </article>
</main>
