# KissOO Test Plan

**Created:** 2026-03-27
**Status:** Active
**Priority:** Based on critical path and risk assessment

---

## Test Strategy

Tests are ordered by **risk and dependency** - foundational features first, then features that depend on them.

---

## Phase 1: Authentication & Session (Critical Foundation)

**Priority:** 🔴 HIGH - All other features depend on this

### Test 1.1: Login Flow
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/login` | Login form displayed | |
| 2 | Enter invalid credentials | Error message shown | |
| 3 | Enter valid admin credentials (admin/admin) | Redirect to home, UUID stored | |
| 4 | Check navbar | Green ball, admin menu items visible | |
| 5 | Refresh page | Session persists, still logged in | |
| 6 | Click Logout | Redirect to home, red ball, no admin menu | |

### Test 1.2: Signup Flow
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/signup` | Signup form displayed | |
| 2 | Enter new user details | Form accepts input | |
| 3 | Submit form | User created, Owner created, linked | |
| 4 | Verify redirect to home | Session established | |
| 5 | Check session storage | userId, ownerId, username stored | |

### Test 1.3: Session Persistence
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login and note UUID | UUID displayed in navbar | |
| 2 | Close browser/tab | Session stored in localStorage | |
| 3 | Reopen and navigate to home | UUID restored, still logged in | |
| 4 | Check owner info in session | ownerId, ownerName available | |

### Test 1.4: Email Verification (if SMTP configured)
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Signup new user | Verification email sent (or link logged) | |
| 2 | Click verification link | Navigate to `/verify-email?token=xxx` | |
| 3 | Verify token valid | Success message, email verified | |
| 4 | Login with new user | Successful login | |

---

## Phase 2: Core CRUD Operations

**Priority:** 🟠 HIGH - Foundation for all data operations

### Test 2.1: Users CRUD
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/users` (as admin) | Users list displayed | |
| 2 | Click "Add User" | Form modal opens | |
| 3 | Fill form and submit | New user created, appears in list | |
| 4 | Click "Edit" on a user | Form populated with data | |
| 5 | Modify and save | Changes persisted | |
| 6 | Click "Delete" | Confirmation dialog, user removed | |

### Test 2.2: Owners CRUD
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/owners` | Owners list displayed | |
| 2 | Create new owner | Owner created | |
| 3 | Edit owner | Changes saved | |
| 4 | Delete owner | Owner removed | |

### Test 2.3: Houses CRUD
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/houses` | Houses list displayed (filtered for non-admin) | |
| 2 | Click "Add House" | Form with cost fields displayed | |
| 3 | Fill all fields (including cost fields) | Form validates | |
| 4 | Submit | House created with cost profile | |
| 5 | Toggle card/table view | View switches correctly | |
| 6 | Edit house | Cost fields editable | |
| 7 | Delete house | House removed | |

---

## Phase 3: Cost Profile System

**Priority:** 🟠 HIGH - New feature, needs validation

### Test 3.1: Cost Profile CRUD
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/cost-profiles` (as admin) | Cost profiles displayed | |
| 2 | Click "Add Cost Profile" | Form with all rate fields | |
| 3 | Fill form (base rate, multipliers, etc.) | Form validates | |
| 4 | Submit | Profile created | |
| 5 | Click "Copy" on a profile | Duplicate created with new name | |
| 6 | Edit profile | Changes saved | |
| 7 | Delete non-standard profile | Profile removed | |

### Test 3.2: Cost Calculation
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Create house with cost fields | House saved with surface, floors, etc. | |
| 2 | Assign cost profile to house | Profile linked | |
| 3 | Call calculateCost API | Correct breakdown returned | |
| 4 | Verify calculation logic | base + size + rooms × multiplier + dogs | |
| 5 | Test with different luxury levels | Multipliers applied correctly | |

### Test 3.3: House-Cost Profile Integration
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Edit house, select cost profile | Profile assigned | |
| 2 | View house details | Cost profile name displayed | |
| 3 | Change cost profile | New profile applied | |
| 4 | Remove cost profile | Standard profile used | |

---

## Phase 4: Schedule Management

**Priority:** 🟡 MEDIUM - Core business logic

### Test 4.1: Schedule Board
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Navigate to `/schedules` | Calendar view displayed | |
| 2 | Toggle to table view | Table with schedules shown | |
| 3 | Click empty cell | Modal opens for new schedule | |
| 4 | Fill schedule form | Form validates | |
| 5 | Submit | Schedule created, appears on board | |
| 6 | Click existing schedule | Edit modal opens | |
| 7 | Drag schedule to new time | Schedule moved | |

### Test 4.2: Cleaner Schedule View
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login as cleaner | Cleaner role detected | |
| 2 | Navigate to `/schedules` | Only own schedules shown | |
| 3 | Click "Start" button | Status changes to "In Progress" | |
| 4 | Click "Complete" button | Modal opens for notes | |
| 5 | Add notes and submit | Status changes to "Completed" | |
| 6 | Verify notes saved | Notes displayed with schedule | |

### Test 4.3: Schedule Filtering
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login as regular user | User role detected | |
| 2 | Navigate to `/schedules` | Only schedules for own houses shown | |
| 3 | Login as admin | Admin role detected | |
| 4 | Navigate to `/schedules` | All schedules shown | |
| 5 | Select date range | Filtered correctly | |

---

## Phase 5: Role-Based Views

**Priority:** 🟡 MEDIUM - User experience

### Test 5.1: Admin View
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login as admin | All menu items visible | |
| 2 | Home page | Admin dashboard with all links | |
| 3 | Houses page | All houses shown, card/table toggle | |
| 4 | Bookings page | All bookings shown | |
| 5 | Cost profiles page | Accessible, full CRUD | |

### Test 5.2: User View (Owner)
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login as regular user | Limited menu items | |
| 2 | Home page | User dashboard with own data | |
| 3 | Houses page | Only own houses shown | |
| 4 | Bookings page | Only bookings for own houses | |
| 5 | Schedules page | Schedules for own houses | |

### Test 5.3: Cleaner View
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Login as cleaner | Cleaner menu items | |
| 2 | Home page | Cleaner dashboard | |
| 3 | Schedules page | Only assigned schedules | |
| 4 | View schedule details | Can start/complete | |

---

## Phase 6: UI/UX Validation

**Priority:** 🟢 LOW - Polish and refinements

### Test 6.1: Responsive Design
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Test on desktop (1920px) | Full layout | |
| 2 | Test on tablet (768px) | Adapted layout | |
| 3 | Test on mobile (375px) | Mobile-optimized | |

### Test 6.2: Internationalization
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Switch to EN | English text | |
| 2 | Switch to NL | Dutch text | |
| 3 | Switch to DE | German text | |
| 4 | Verify all labels translated | No missing translations | |

### Test 6.3: Accessibility
| Step | Action | Expected Result | Pass/Fail |
|------|--------|-----------------|-----------|
| 1 | Tab through form | Focus order logical | |
| 2 | Verify button hints | Title attributes work | |
| 3 | Color contrast | Text readable | |

---

## Test Execution Checklist

### Before Testing
- [ ] Backend server running on port 8080
- [ ] Frontend dev server running
- [ ] Database initialized (run Load Test Data if needed)
- [ ] Test users created (admin, user, cleaner)

### Test Data Setup
| User | Password | Role | Purpose |
|------|----------|------|---------|
| admin | admin | Admin | Full access testing |
| testuser | test123 | User/Owner | Filtered view testing |
| cleaner1 | clean123 | Cleaner | Schedule testing |

### Test Execution Order
1. **Start backend**: `cd /home/dacosta/Projects/KissOO && ./run-kiss.sh`
2. **Start frontend**: `cd src/main/frontend-svelte && npm run dev`
3. **Load test data**: Login as admin → Click "Load Test Data"
4. **Execute Phase 1 tests** (Authentication)
5. **Execute Phase 2 tests** (CRUD)
6. **Execute Phase 3 tests** (Cost Profiles)
7. **Execute Phase 4 tests** (Schedules)
8. **Execute Phase 5 tests** (Role-based views)
9. **Execute Phase 6 tests** (UI/UX)

---

## Bug Tracking

| # | Description | Phase | Severity | Status |
|---|-------------|-------|----------|--------|
| | | | | |

---

## Test Results Summary

| Phase | Total | Passed | Failed | Blocked |
|-------|-------|--------|--------|---------|
| 1: Auth | | | | |
| 2: CRUD | | | | |
| 3: Cost | | | | |
| 4: Schedule | | | | |
| 5: Roles | | | | |
| 6: UI/UX | | | | |
| **Total** | | | | |

---

## Notes

- Document any bugs found in the Bug Tracking table
- Take screenshots of failures
- Note any deviations from expected behavior
- Update CLEANERS2_INTEGRATION_PLAN.md with findings
