@param java.util.List<java.util.List<String>> rows

@for(java.util.List<String> r : rows)
<tr><td>${r.get(0)}</td><td>${r.get(1)}</td><td>${r.get(2)}</td></tr>
@endfor
