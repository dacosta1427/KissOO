<main>
  <hgroup>
    <h1>HTMX</h1>
    <p>JTE-rendered HTML fragments, swapped into the DOM.</p>
  </hgroup>

  <article>
    <h2>Task list</h2>
    <p>Type a title and click <em>Add task</em>. The backend renders a JTE fragment and HTMX swaps it into the list.</p>
    <div class="row">
      <div class="col-md-9">
        <input id="taskTitle" name="title" placeholder="New task title">
      </div>
      <div class="col-md-3">
        <button hx-get="/rest"
                hx-vals='{"_class":"services/HypermediaTestService","_method":"taskFragment"}'
                hx-include="#taskTitle"
                hx-target="#taskList"
                hx-swap="beforeend">Add task</button>
      </div>
    </div>
    <div id="taskList"></div>
  </article>
</main>
