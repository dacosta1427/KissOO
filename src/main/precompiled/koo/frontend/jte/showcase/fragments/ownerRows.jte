@param java.util.List<domain.actor.owner.Owner> owners
@if (owners == null || owners.isEmpty())
<tr><td colspan="4">No owners yet &mdash; click &ldquo;New Owner&rdquo;.</td></tr>
@else
@for (domain.actor.owner.Owner o : owners)
<tr>
  <td>${o.getName()}</td>
  <td>${o.getEmail()}</td>
  <td>${o.getPhone()}</td>
  <td>
    <button type="button" class="outline btn-sm" data-on:click="@get('/rest?_class=services/ShowcaseService&amp;_method=crudForm&amp;oid=${o.getOid()}')">Edit</button>
    <button type="button" class="outline btn-sm btn-danger" data-on:click="@get('/rest?_class=services/ShowcaseService&amp;_method=crudDelete&amp;oid=${o.getOid()}')">Delete</button>
  </td>
 </tr>
@endfor
@endif
