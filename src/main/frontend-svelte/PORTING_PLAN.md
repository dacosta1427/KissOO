# KissOO Frontend Porting Plan

## IMPORTANT INSTRUCTIONS (MUST READ BEFORE STARTING)
- **Working on a pure Svelte 5 project** - Use Svelte 5 runes (`$state`, `$derived`, `$effect`, `$props`), not Svelte 4 syntax.
- **Commit and push after each successful task/todo completion** - Each completed todo must be committed and pushed to the remote repository.
- **Update this plan doc after each successful task/todo completion** - Mark todos as completed and add notes.
- **Read the 2 guides (KissOO-Guide.md and sv5guide.md) AND this plan after each successful task/TODO completion** - This maintains context and ensures consistency.

## Executive Summary
Port all functional screens from the legacy KissOO frontend (`src/main/frontend/`) to the modern Svelte 5 frontend (`src/main/frontend-svelte/`) using Tailwind CSS, ag-grid, and SvelteKit routing.

## Current State
- **Already implemented**: Login, Signup, Users (basic add/delete), Navbar, session management, API modules for Auth and Users.
- **Missing**: Edit functionality for users, all other screens, utility system, custom components.

## Implementation Plan

### Phase 1: Foundation
- [x] **Task 1**: Create API modules for all backend services ✅ **COMPLETED**
- [x] **Task 2**: Implement utility system (`Utils.ts`) with modal dialogs, loading overlays, etc. ✅ **COMPLETED**
- [x] **Task 3**: Create custom Modal component (`Modal.svelte`) ✅ **COMPLETED**
- [ ] **Task 4**: Create AgGrid wrapper component
- [ ] **Task 5**: Update Server.ts with missing methods (binaryCall, fileUploadSend)

### Phase 2: Enhanced Users Page
- [ ] **Task 6**: Add edit user functionality with modal popup
- [ ] **Task 7**: Improve delete confirmation with modal
- [ ] **Task 8**: Add loading states and better error handling

### Phase 3: Core Screens
- [ ] **Task 9**: Create CRUD (phone book) page with ag-grid and edit/delete
- [ ] **Task 10**: Create REST Services demo page
- [ ] **Task 11**: Create Benchmark page
- [ ] **Task 12**: Create File Upload page
- [ ] **Task 13**: Create Ollama AI interface page

### Phase 4: Demo & Placeholder Screens
- [ ] **Task 14**: Create Controls demo page
- [ ] **Task 15**: Create SQLAccess placeholder page
- [ ] **Task 16**: Create Report placeholder page
- [ ] **Task 17**: Create Export placeholder page

### Phase 5: Navigation & Polish
- [ ] **Task 18**: Update Navbar with all page links
- [ ] **Task 19**: Create shared layout improvements
- [ ] **Task 20**: Add mobile responsiveness
- [ ] **Task 21**: Final testing and bug fixes

## Detailed Task Breakdown

### Task 1: API Modules
Create TypeScript modules for each backend service:
- `src/lib/api/Crud.ts` - Phone book CRUD operations
- `src/lib/api/Benchmark.ts` - Performance testing endpoints
- `src/lib/api/FileUpload.ts` - File upload handling
- `src/lib/api/Ollama.ts` - AI/LLM interface
- `src/lib/api/RestServices.ts` - Demo service calls (Groovy, Java, Lisp)
- `src/lib/api/ActorService.ts` - Actor management (if needed)

### Task 2: Utility System
Create `src/lib/utils/Utils.ts` with:
- `showMessage(title, message)` - Modal alerts
- `yesNo(title, message, onYes, onNo)` - Confirmation dialogs
- `waitMessage(message)` / `waitMessageEnd()` - Loading overlays
- `popup_open(id)` / `popup_close()` - Legacy compatibility (wrap modals)
- `loadPage(page, target)` - May not be needed with SvelteKit routing

### Task 3: Modal Component
Create `src/lib/components/Modal.svelte` with:
- Title, content, and action buttons
- Configurable size and styling
- Accessible focus management
- Tailwind CSS styling

### Task 4: AgGrid Wrapper
Create `src/lib/components/AgGridWrapper.svelte`:
- Wrap ag-grid-community with Svelte 5 runes
- Handle selection, row double-click events
- Custom cell renderers if needed
- Responsive sizing

### Task 5: Server.ts Enhancements
Add missing methods to `src/lib/services/Server.ts`:
- `binaryCall(cls, meth, injson)` - For image/data retrieval
- `fileUploadSend(cls, meth, fd, injson, waitMsg, successMessage)` - File uploads
- `callAll(promises, handlers)` - Parallel service calls
- Inactivity timeout handling

### Task 6: Users Edit Functionality
Enhance `src/routes/users/+page.svelte`:
- Add edit button per user row
- Modal popup with edit form (username, password, active)
- Update user via API
- Refresh list after edit

### Task 7: Delete Confirmation Modal
Replace `confirm()` with custom modal:
- Use Modal component for confirmation
- Better UX with loading state

### Task 8: Loading States
Add loading indicators:
- Spinner for data loading
- Disabled buttons during operations
- Error boundaries

### Task 9: CRUD Page
Create `src/routes/crud/+page.svelte`:
- ag-grid table with columns: Last Name, First Name, Phone Number
- New/Edit/Delete buttons with modal popups
- Report and Export buttons (if backend supports)
- Connection to `services.Crud` API

### Task 10: REST Services Demo
Create `src/routes/rest-services/+page.svelte`:
- Two numeric inputs for numbers
- Buttons: Call Groovy, Call Java, Call Lisp
- Output field showing result
- Connection to `services.MyGroovyService`, `services.MyJavaService`, `services.MyLispService`

### Task 11: Benchmark Page
Create `src/routes/benchmark/+page.svelte`:
- Buttons for each operation: Setup, Bulk Insert (100/1000), Select All, Count, Update, Delete, Aggregations
- Results display area
- Connection to `services.Benchmark`

### Task 12: File Upload Page
Create `src/routes/file-upload/+page.svelte`:
- File input component
- Upload button
- Progress indicator
- Connection to `services.FileUpload`

### Task 13: Ollama AI Page
Create `src/routes/ollama/+page.svelte`:
- Model dropdown selection
- Textarea for prompt
- Send button
- Response display area
- Context management
- Connection to `services.OllamaQuery`

### Task 14: Controls Demo
Create `src/routes/controls/+page.svelte`:
- Showcase of UI components: text input, numeric input, date picker, time picker, checkbox, radio buttons, dropdown, listbox, textarea
- Popup demo
- Validation examples

### Task 15-17: Placeholder Pages
Create simple pages with "Coming soon" or "This example requires SQL database" messages.

### Task 18: Navbar Update
Update `src/lib/components/Navbar.svelte`:
- Add links to all new pages when authenticated
- Group pages logically (e.g., "Demos", "Tools")
- Mobile responsive menu

### Task 19: Layout Improvements
Enhance `src/routes/+layout.svelte`:
- Consistent page structure
- Breadcrumbs or page titles
- Footer

### Task 20: Mobile Responsiveness
Ensure all pages work on mobile:
- Responsive ag-grid
- Mobile-friendly modals
- Touch-friendly controls

### Task 21: Final Testing
- Test all pages with backend
- Fix any integration issues
- Ensure session persistence works across pages
- Verify navigation flow

## Backend Service Endpoints Reference
- `services.Users` - getRecords, addRecord, updateRecord, deleteRecord
- `services.Crud` - getRecords, addRecord, updateRecord, deleteRecord, runReport, runExport
- `services.Benchmark` - setupTable, bulkInsert, selectAll, countRecords, bulkUpdate, bulkDelete, aggregateSum, aggregateAvg, aggregateGroupBy
- `services.FileUpload` - upload
- `services.OllamaQuery` - isOllamaUp, listModels, ask
- `services.MyGroovyService` - addNumbers, hasDatabase
- `services.MyJavaService` - addNumbers
- `services.MyLispService` - addNumbers (if enabled)

## Notes
- Legacy frontend uses custom HTML elements (`<text-input>`, `<push-button>`, etc.) → Replace with standard HTML + Tailwind classes
- ag-grid already in package.json
- File upload requires special handling with FormData
- Some backend services may not be fully ported to Perst-only mode (e.g., SQLAccess, Report)

## Success Criteria
- All functional screens ported and working
- Consistent UI/UX with Tailwind styling
- Svelte 5 runes used throughout
- All tests passing
- Documentation updated