@param String signals
<form data-signals="${signals}" data-on:submit="@get('/rest?_class=services/ShowcaseService&amp;_method=crudSave')">
  <label>Name<input data-bind:name required></label>
  <label>Email<input data-bind:email></label>
  <label>Phone<input data-bind:phone></label>
  <div id="crud-form-status" class="min-h-[1rem]"></div>
  <div class="row">
    <div class="col-6"><button type="submit">Save</button></div>
    <div class="col-6"><button type="button" class="outline btn-sm" data-on:click="@get('/rest?_class=services/ShowcaseService&amp;_method=crudClear')">Cancel</button></div>
  </div>
</form>
