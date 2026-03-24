# KissOO Development Notes

## Build Requirements

### Required JAR Files
The following JARs must be present in `libs/`:
- `abcl.jar` - ABCL Lisp implementation (required for LispService)
  - If missing, Lisp services will fail to compile
  - Workaround: See "Disabling Lisp" below

### Optional Dependencies
- Perst OODBMS: `perst-dcg-4.0.0.jar` (included in libs)

## Disabling Lisp Services

If `abcl.jar` is not available, disable Lisp to allow building:

1. Delete these files:
   - `src/main/core/org/kissweb/lisp/ABCL.java`
   - `src/main/core/org/kissweb/restServer/LispService.java`

2. In `ProcessServlet.java`, comment out the LispService call (around line 476-480):
   ```java
   // Lisp service disabled - requires abcl.jar
   // res = (new LispService()).tryLisp(this, response, _className, _method, injson, outjson);
   ```

**Note:** When updating from the upstream Kiss framework, re-apply these changes if abcl.jar is not available.

## Frontend (SvelteKit)

The SvelteKit frontend is in `src/main/frontend-svelte/`.

### Running
```bash
cd src/main/frontend-svelte
npm run dev
```

### Routes
- `/` - Home page
- `/login` - Login page
- `/signup` - Signup page
- `/users` - User management

### Build
```bash
cd src/main/frontend-svelte
npm run build
```

Note: `.svelte-kit/` is auto-generated and should not be committed.

## Debugging Protocols

### UI/UX Issues (Svelte 5)
1. **Dropdown/Select not populating**: Check if data array is empty, then check reactivity:
   - Ensure options array uses `$state` for reactivity
   - Verify API call returns data (check `res.data`)
   - Mutating `const` arrays doesn't trigger re-render
2. **Form not updating**: Ensure form data uses `$state` and bindings are correct
3. **Component not re-rendering**: Check if props are reactive (use `$state` in parent)

### Authentication Issues
1. **Login fails with correct credentials**:
   - Check `PerstUser.canLogin()` requirements: `active && emailVerified`
   - Verify `emailVerified` flag in database (default is `false` for new users)
   - Check backend logs for `PerstAuth` messages
2. **Signup succeeds but login fails**: Likely `emailVerified = false`
3. **Session expires prematurely**: Check `userInactiveSeconds` (default 1800 seconds)

### Backend Service Issues
1. **Service not found**: Ensure service class is in `backend/services/` package
2. **Method not found**: Verify method signature matches `void methodName(JSONObject injson, JSONObject outjson, Connection db, ProcessServlet servlet)`
3. **Database not available**: Check Perst initialization in `KissInit.groovy`

### Quick Checks
- Run `npm run build` in `src/main/frontend-svelte/` to catch TypeScript errors
- Check browser console for API errors
- Verify backend server is running on port 8080

### Common Pitfalls
1. **Non-reactive arrays in Svelte 5**: Use `$state` for arrays that need reactivity; mutating `const` arrays doesn't trigger re-renders.
2. **Missing `_Success` field**: Ensure backend services set `_Success: true/false` in response JSON.
3. **Unique constraints**: Check `@Indexable(unique=true)` annotations on Perst entities; generate unique IDs for indexed fields.
4. **JSON method signatures**: Verify `JSONObject` methods exist (e.g., `getString` vs `optString`).
5. **Session expiration**: UUIDs expire; re-login if API returns `_ErrorCode: 2`.
6. **Hot reload limitations**: Groovy service changes may require backend restart if not auto-reloaded.

### Mistakes to Avoid (Recent Lessons)
1. **Assuming dropdown issues are only frontend**: Always test backend API independently (curl) before modifying frontend.
2. **Not checking unique constraints**: When creating new records, always ensure indexed fields (like `userId`) are unique.
3. **Overlooking response format**: Backend responses must include `_Success` field; frontend expects it for error handling.
4. **Incomplete testing**: Test both success and failure paths; verify data persistence in database.
5. **Assuming hot reload works**: After modifying Groovy services, verify they are actually reloaded (check logs).