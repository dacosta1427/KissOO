@param java.util.List<java.util.Map<String, String>> versions
@param String name
<h4>Version history &middot; ${name}</h4>
@if (versions == null || versions.isEmpty())
  <p class="muted">No versions recorded for this object.</p>
@else
  <ul class="timeline">
@for (java.util.Map<String, String> v : versions)
    <li class="point ${v.get("side")}${v.get("current") == "true" ? " current" : ""}">
      <p><strong>v${v.get("id")}</strong> &middot; ${v.get("date")}${v.get("state")}</p>
      <p class="muted">${v.get("delta")}</p>
    </li>
@endfor
  </ul>
@endif
