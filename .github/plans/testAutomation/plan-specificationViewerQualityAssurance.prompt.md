# Plan: Automated Quality Assurance for SpecificationsViewer

**TL;DR:** Add comprehensive Cypress component tests for the SpecificationsViewer feature (4 components, 2 composables, 2 utility functions) covering business logic, UI interactions, error handling, and edge cases. Add Cypress e2e tests for the golden path user journey. The project uses Cypress exclusively (no Vitest), following patterns from existing component tests with `cy.mountWithPlugins()`, API mocking via `cy.intercept()`, and `data-test` selectors. Target >80% coverage on new code with focus on critical paths (schema parsing, recursive tree, batch loading). Tests integrate into existing structure under `dataland-frontend/tests/`.

**Key Findings:**
- **No unit test framework:** Project uses Cypress exclusively for all frontend testing (component + e2e, no Vitest/Jest)
- **Component test pattern:** `cy.mountWithPlugins()` helper mounts components with Pinia, PrimeVue, Router, Keycloak mocking
- **E2E tests:** Two types: Frontend Cypress tests (`tests/e2e/specs/`) AND backend Kotlin tests (`dataland-e2etests/`)
- **Existing utilities:** `minimalKeycloakMock()`, `cy.intercept()` for API mocking, fixtures in `../testing/data/`
- **Coverage:** Istanbul integration via `vite-plugin-istanbul` and `@cypress/code-coverage` plugins

**Test Priorities:**
1. **Most Critical:** `SpecificationSchemaTree.vue` (recursive tree, expansion, rendering edge cases)
2. **Critical:** `useSpecifications.ts` composable (schema parsing, batch loading, error handling)
3. **High Priority:** `SpecificationsViewer.vue` (integration), `DataPointTypeDetailsDialog.vue` (modal behavior)
4. **Medium Priority:** `useDataPointDetails.ts` composable, `FrameworkMetadataPanel.vue`
5. **Foundation:** StringFormatter utility functions

**Key Edge Cases to Cover:**
- Schema parsing: deeply nested (5+ levels), empty schemas, malformed JSON, 100+ data points
- Tree rendering: auto-expansion, collapse state management, recursive rendering
- Batch loading (Phase 2): sequential batching, partial failures, graceful degradation
- Loading states: simultaneous loading, rapid framework switching, non-blocking UI
- Error recovery: retry mechanisms, error clearing, persistent errors

**Steps**

## Phase 1: Utility Function Tests (Foundation)

### 1. Add tests for new StringFormatter functions

- Edit [dataland-frontend/tests/component/utils/StringFormatter.cy.ts](dataland-frontend/tests/component/utils/StringFormatter.cy.ts)
- Add test suite for `truncateText()` covering:
  - Text shorter than maxLength (no truncation)
  - Text exactly at maxLength (boundary)
  - Text longer with word boundaries (truncates at last space before limit)
  - Text longer without spaces (hard truncate at maxLength)
  - Empty string handling
  - Single word longer than maxLength
- Add test suite for `humanizeDataPointBaseType()` covering:
  - All mapped types: `plainString` → "Text", `plainDate` → "Date", `plainInteger` → "Number", `plainBoolean` → "Yes/No", `plainEnum` → "Selection", `extendedArray` → "List"
  - Pattern-based fallback: strings containing "date" → "Date"
  - Unknown types fall back to humanized string (e.g., "customTypeId" → "Custom Type Id")
  - Empty string handling
  - Case sensitivity validation
- Follow existing test pattern:
  ```typescript
  describe('Unit test for StringFormatter', () => {
    it('...', () => {
      expect(...).to.equal(...)
    })
  })
  ```
- Use direct function imports, no component mounting needed
- Test file location follows existing convention: `tests/component/utils/`

## Phase 2: Composable Tests (Business Logic)

### 2. Create comprehensive tests for useSpecifications composable

- Create [dataland-frontend/tests/component/composables/useSpecifications.cy.ts](dataland-frontend/tests/component/composables/useSpecifications.cy.ts)
- Test setup: Create mock fetch functions that return controlled data or throw errors
- Mount test component wrapper that uses composable and exposes state for assertions
- Use `cy.intercept()` pattern to mock API responses inline

**Framework loading tests:**
- Empty framework list returns empty array
- Multiple frameworks sort alphabetically by name
- API error sets `error` ref with appropriate message
- Network timeout handled gracefully
- `isLoadingFrameworks` state transitions correctly (false → true → false)

**Framework selection tests:**
- Valid framework ID loads specification and parses schema
- Invalid framework ID (404) sets error state
- API 500 error sets error state with message
- Selecting same framework twice is idempotent (no duplicate API calls)
- Selecting new framework while previous is loading cancels/overwrites correctly

**Schema parsing tests (CRITICAL PATH):**
- Empty schema JSON produces empty `parsedSchema` array
- Schema with only sections (no data points) parses correctly
- Schema with only data points (no sections) parses correctly
- Deeply nested schema (5+ levels) parses full hierarchy
- Schema with mixed sections and data points at same level
- Data points with `aliasExport` use that for display
- Data points without `aliasExport` fall back to key
- Sections with `label` property use that for display
- Malformed JSON structure handled gracefully (error state)

**Batch loading tests (Phase 2 feature):**
- Batch loading disabled: no extra API calls made
- 0 data points: no batch requests
- 1-14 data points: single batch request
- 15-30 data points: two batches with sequential execution
- 100+ data points: multiple batches, verify not all parallel (performance)
- Partial batch failures: successful loads enrich data, failures log warnings
- All batch failures: degrades gracefully, tree still renders
- `isLoadingDataPointDetails` tracks batch loading state
- Cache behavior: duplicate IDs don't trigger multiple requests

**Error recovery:**
- Error state clears on successful retry
- Multiple consecutive errors don't break state
- Loading states reset correctly on error

**Test organization:**
```typescript
describe('Component tests for useSpecifications composable', () => {
  describe('Framework loading', () => {
    it('Should load framework list successfully', () => { ... })
    it('Should handle empty framework list', () => { ... })
    it('Should handle API error during framework loading', () => { ... })
  })
  
  describe('Framework selection', () => { ... })
  describe('Schema parsing', () => { ... })
  describe('Batch data point loading', () => { ... })
})
```

### 3. Create tests for useDataPointDetails composable

- Create [dataland-frontend/tests/component/composables/useDataPointDetails.cy.ts](dataland-frontend/tests/component/composables/useDataPointDetails.cy.ts)
- Test setup: Mock `fetchDataPointDetails` function
- Follow same wrapper component pattern as `useSpecifications` test

**Detail loading tests:**
- Valid data point ID loads details successfully
- Invalid data point ID (404) sets error state
- API 500 error sets error with message
- Loading same ID twice is idempotent
- `isLoading` state transitions correctly (false → true → false)

**State management:**
- `clearDetails()` resets all state (data, error, loading)
- `retryLoad()` with no previous ID is no-op
- `retryLoad()` with previous error re-attempts fetch
- Loading interrupted by `clearDetails()` handles gracefully

**Test organization:**
```typescript
describe('Component tests for useDataPointDetails composable', () => {
  describe('Detail loading', () => { ... })
  describe('State management', () => { ... })
  describe('Error handling', () => { ... })
})
```

## Phase 3: Component Tests (UI & Integration)

### 4. Create tests for FrameworkMetadataPanel component

- Create [dataland-frontend/tests/component/components/resources/specifications/FrameworkMetadataPanel.cy.ts](dataland-frontend/tests/component/components/resources/specifications/FrameworkMetadataPanel.cy.ts)
- Use `cy.mountWithPlugins(FrameworkMetadataPanel, { props: { framework: mockFramework } })`
- Create fixture data in `../testing/data/specifications/` for mock frameworks

**Display tests:**
- Framework name displays as title/heading
- Business definition displays correctly (multi-line text)
- Framework ID displays in correct section
- Missing optional fields (e.g., no business definition) handled gracefully
- Empty business definition shows placeholder or is hidden

**Visual hierarchy:**
- Verify heading level appropriate (h2 or h3)
- Framework ID uses semantic markup (`<dl>` definition list)
- Content structure matches accessibility requirements

**Props validation:**
- Required props missing: component handles gracefully or shows error
- Invalid framework object structure: no runtime errors

**Verify:**
- `data-test="framework-metadata"` attribute present
- All text content matches prop values exactly

**Test organization:**
```typescript
describe('Component tests for FrameworkMetadataPanel', () => {
  it('Should display framework name as heading', () => { ... })
  it('Should display business definition', () => { ... })
  it('Should handle missing optional fields', () => { ... })
})
```

### 5. Create tests for SpecificationSchemaTree component (MOST CRITICAL)

- Create [dataland-frontend/tests/component/components/resources/specifications/SpecificationSchemaTree.cy.ts](dataland-frontend/tests/component/components/resources/specifications/SpecificationSchemaTree.cy.ts)
- **Most complex component:** Recursive rendering, state management, many edge cases
- Use `cy.mountWithPlugins(SpecificationSchemaTree, { props: { schema: mockSchema } })`
- Create various fixture schemas: empty, flat, deeply nested, large (100+ items)
- Use `cy.get('[data-test="section-header"]')` pattern for element selection

**Tree structure rendering:**
- Empty schema renders nothing (no errors, no crash)
- Single top-level section renders correctly
- Multiple top-level sections all render in order
- Deeply nested sections (5 levels) render full hierarchy
- Mix of sections and data points at same level
- Section with no children (empty section) renders with label
- Schema with 100+ data points renders (performance test)

**Expand/collapse functionality:**
- Top-level sections auto-expand on mount
- Nested sections start collapsed by default
- Clicking section header toggles expansion state
- Chevron icon rotates/changes on toggle (verify CSS class or icon change)
- `aria-expanded` attribute updates correctly (true/false)
- Rapid clicking doesn't break state (click 3+ times quickly)
- Expanding nested section within collapsed parent: child state preserved
- Collapsing section hides all nested content immediately
- Expanding multiple sections maintains independent state

**Data point display:**
- Data point name shows humanized `aliasExport` (e.g., "Company Name" from "COMPANY_NAME")
- Data point name falls back to key if no `aliasExport`
- Data point type shows readable label (e.g., "Date") + technical ID in parentheses
- Icons display correctly based on data type:
  - Date types: calendar icon (`pi-calendar`)
  - Text types: text icon (`pi-align-left`)
  - Number types: hashtag icon (`pi-hashtag`)
  - Boolean types: check icon (`pi-check-circle`)
  - Array types: list icon (`pi-list`)
  - Enum types: grid icon (`pi-th-large`)
- Long data point names don't break layout (text overflow handling with ellipsis)
- Long technical IDs display correctly (monospace font in parentheses)
- "View Details" button renders for each data point

**Business definition display (Phase 2 feature):**
- Data point without `businessDefinition` field: no definition section displayed
- Data point with short definition (<150 chars): displays full text, no "Show more" button
- Data point with definition exactly 150 chars: boundary test
- Data point with long definition (>150 chars): truncates at word boundary, shows "Show more" button
- "Show more" button expands full definition text
- "Show less" button re-truncates definition
- Multiple data points with expanded definitions maintain independent state
- Truncation at word boundaries works correctly (doesn't cut mid-word)
- Definition with no spaces: truncates at character limit (fallback)

**Event emission:**
- Clicking "View Details" emits `view-details` event
- Event payload includes correct data point type ID
- Event payload includes `aliasExport` value
- Events from nested data points (level 3+) bubble up correctly
- Multiple "View Details" clicks emit separate events

**Keyboard navigation & accessibility:**
- Tab key focuses section headers in hierarchical order
- Shift+Tab reverses focus order correctly
- Enter key on focused section header toggles expansion
- Space key on focused section header toggles expansion
- Section headers have `role="button"` attribute
- Section headers have `tabindex="0"` attribute
- Icons have `aria-hidden="true"` attribute
- Expanded sections announce state to screen readers
- Focus remains on section header after toggle (no focus loss)

**Performance tests:**
- Schema with 100+ data points renders in <2 seconds
- Expanding section with 50+ children doesn't freeze UI
- Collapsing large section is instantaneous
- Re-rendering with new schema prop updates correctly

**Edge cases:**
- Schema updates while sections expanded: expansion state resets appropriately
- Unmounting component with expanded sections: no memory leaks
- Rapid prop changes (schema updated 3 times quickly): handles gracefully

**Test organization:**
```typescript
describe('Component tests for SpecificationSchemaTree', () => {
  describe('Tree structure rendering', () => {
    it('Should render empty schema without errors', () => { ... })
    it('Should render deeply nested sections', () => { ... })
    it('Should render large schema with 100+ data points', () => { ... })
  })
  
  describe('Expand/collapse functionality', () => {
    it('Should auto-expand top-level sections on mount', () => { ... })
    it('Should toggle section expansion on click', () => { ... })
    it('Should maintain independent expansion state for multiple sections', () => { ... })
  })
  
  describe('Data point display', () => { ... })
  describe('Business definition display', () => { ... })
  describe('Event emission', () => { ... })
  describe('Keyboard navigation', () => { ... })
  describe('Performance', () => { ... })
})
```

### 6. Create tests for DataPointTypeDetailsDialog component

- Create [dataland-frontend/tests/component/components/resources/specifications/DataPointTypeDetailsDialog.cy.ts](dataland-frontend/tests/component/components/resources/specifications/DataPointTypeDetailsDialog.cy.ts)
- Mock Keycloak with `minimalKeycloakMock({ authenticated: true })`
- Use `cy.intercept()` to mock `/specifications/datapoints/{id}` endpoint
- Create fixture for `DataPointTypeSpecification` response

**Dialog visibility:**
- `visible: false` prop: dialog not in DOM or hidden (verify with `should('not.exist')` or `should('not.be.visible')`)
- `visible: true` prop: dialog appears and is visible
- Pressing ESC key closes dialog
- Closing dialog emits `update:visible` event with `false` value
- Clicking backdrop (outside dialog) closes dialog
- Clicking close button closes dialog
- All close methods emit same event with same payload
- Dialog re-opening after close works correctly

**Loading states:**
- Shows spinner while fetching details (`isLoading: true`)
- Spinner disappears when data loaded successfully
- Spinner uses PrimeVue ProgressSpinner component
- Slow API response (simulate 2s delay with `cy.intercept()`): spinner visible throughout
- Multiple rapid opens: each triggers loading state correctly

**Data display:**
- Data point name displays as dialog title/header
- Business definition displays prominently (main content area)
- Base type shows human-readable label (e.g., "Date" not "plainDate")
- Technical details in collapsible PrimeVue Accordion
- Accordion is collapsed by default on open
- Accordion expands on click, collapses on second click
- Constraints array displays as formatted list (bullets or numbered)
- Empty constraints array shows "No constraints" or similar message
- Missing optional fields (e.g., no `aliasExport`) don't cause errors
- Very long business definition (500+ chars) wraps correctly, no horizontal scroll
- Frameworks using this data point: displays as list
- Empty frameworks list shows appropriate message

**Error handling:**
- API 404 error: displays user-friendly error message ("Data point not found")
- API 500 error: displays generic error message ("Failed to load details")
- Network error: displays connection error message
- Error message visible in dialog (doesn't close dialog)
- Retry button appears when error occurs
- Retry button re-fetches data (new API call)
- Successful retry: clears error, displays data
- Failed retry: shows error again (persistent)
- Error persists across dialog close/reopen until retry succeeds

**Props edge cases:**
- `dataPointTypeId` is null: no API call made, shows placeholder/empty state
- `dataPointTypeId` is empty string: handled gracefully
- `visible` changes to false while API loading: request cancelled or ignored
- `visible` changes to true twice rapidly: doesn't duplicate API calls
- Details already loaded, then `visible` becomes true again: shows cached data (or re-fetches based on implementation)
- Props change while dialog open: updates displayed data

**Accessibility:**
- Dialog has `aria-modal="true"` attribute
- Dialog has `aria-labelledby` pointing to title element
- Focus trap: Tab key cycles within dialog only (doesn't escape to page)
- ESC key closes dialog (native PrimeVue Dialog behavior)
- Focus returns to trigger button on close (test in integration test)
- Dialog title uses semantic heading (`<h2>` or similar)

**Verify:**
- `data-test="close-dialog"` attribute on close button
- All interactive elements have proper `data-test` attributes

**Test organization:**
```typescript
describe('Component tests for DataPointTypeDetailsDialog', () => {
  beforeEach(() => {
    cy.intercept('GET', '/specifications/datapoints/*', { fixture: 'specifications/datapoint-details.json' }).as('getDataPoint')
  })
  
  describe('Dialog visibility', () => {
    it('Should not display when visible prop is false', () => { ... })
    it('Should display when visible prop is true', () => { ... })
    it('Should close on ESC key', () => { ... })
  })
  
  describe('Loading states', () => { ... })
  describe('Data display', () => { ... })
  describe('Error handling', () => { ... })
  describe('Accessibility', () => { ... })
})
```

### 7. Create tests for SpecificationsViewer page component (INTEGRATION)

- Create [dataland-frontend/tests/component/components/pages/SpecificationsViewer.cy.ts](dataland-frontend/tests/component/components/pages/SpecificationsViewer.cy.ts)
- **Integration test:** Tests interaction between all child components
- Mock Keycloak with `minimalKeycloakMock({ authenticated: true })`
- Mock Router for URL manipulation
- Use `cy.intercept()` for all API endpoints:
  - `GET /specifications/frameworks` → framework list
  - `GET /specifications/frameworks/{id}` → framework specification with schema
  - `GET /specifications/datapoints/{id}` → data point details
- Create fixtures for multiple framework scenarios
- Use `cy.wait('@aliasName')` pattern to wait for API calls

**Framework selection flow:**
- On mount with no URL param: shows "Select a framework to view its specification" empty state
- Framework dropdown populates after frameworks API response
- Dropdown contains all framework names from API
- Selecting framework from dropdown triggers specification API call
- Selection loads specification and renders metadata panel
- Selection loads specification and renders schema tree
- Framework selection updates URL query param (`?framework={id}`)
- URL updates without page reload (client-side routing)

**URL synchronization:**
- Mount with `?framework=lksg` in URL: auto-selects framework on mount
- Auto-selection triggers API call for that framework
- Invalid framework ID in URL (e.g., `?framework=nonexistent`): shows error message
- Missing framework list: URL param ignored, shows empty state
- Switching frameworks updates URL query param immediately
- Browser back button simulation: framework selection updates (test if possible with Cypress routing)
- Browser forward button simulation: framework selection updates

**Loading states:**
- Spinner in dropdown area while frameworks loading
- Spinner/skeleton in content area while specification loading
- Dropdown remains interactive while specification loads (non-blocking UI)
- User can change framework selection while previous specification loading
- Both loading states can be true simultaneously (initial load)
- Loading state false after successful load
- Loading state false after error

**Error scenarios:**
- Framework list fetch fails (404): error message with retry button in dropdown area
- Framework list fetch fails (500): error message with retry button
- Specification fetch fails: error message with retry button in content area
- Retry button for frameworks re-attempts framework list API call
- Retry button for specification re-attempts specification API call (correct ID)
- Error clears on successful retry
- Error in framework list doesn't block content area
- Error in specification doesn't block dropdown

**Child component integration:**
- FrameworkMetadataPanel receives correct framework prop (verify name matches selected)
- FrameworkMetadataPanel updates when framework changes
- SpecificationSchemaTree receives parsed schema prop
- SpecificationSchemaTree renders sections from parsed schema
- Clicking "View Details" in tree emits event to parent
- Event triggers DataPointTypeDetailsDialog to open (`visible: true`)
- Dialog `dataPointTypeId` prop set correctly from clicked data point
- Dialog displays correct data point details
- Closing dialog sets `visible: false`
- Re-opening dialog for different data point loads new details
- Switching frameworks while dialog open: dialog closes or updates appropriately

**Empty states:**
- No framework selected: shows "Select a framework" message
- Framework with empty schema: metadata panel shows, tree shows empty state
- Framework with no business definition: metadata panel shows name/ID only

**Multiple interactions:**
- Select framework → expand sections → view details → close → switch framework → verify new framework loaded
- Select framework → error on load → retry → success → verify content displays
- Rapid framework switching (3 selections quickly): last selection wins, no stale data

**Verify:**
- `data-test="framework-selector"` attribute on dropdown
- `data-test="specifications-content"` attribute on content area
- `data-test="empty-state"` attribute when no framework selected
- `data-test="loading-indicator"` attributes for loading states
- `data-test="error-message"` and `data-test="retry-button"` for errors

**Test organization:**
```typescript
describe('Component tests for SpecificationsViewer page', () => {
  beforeEach(() => {
    cy.intercept('GET', '/specifications/frameworks', { fixture: 'specifications/framework-list.json' }).as('getFrameworks')
    cy.intercept('GET', '/specifications/frameworks/*', { fixture: 'specifications/lksg-framework.json' }).as('getFramework')
  })
  
  describe('Framework selection flow', () => { ... })
  describe('URL synchronization', () => { ... })
  describe('Loading states', () => { ... })
  describe('Error scenarios', () => { ... })
  describe('Child component integration', () => { ... })
})
```

## Phase 4: End-to-End Tests (Golden Path)

### 8. Create e2e test for specification viewer golden path

- Create [dataland-frontend/tests/e2e/specs/specification-viewer/SpecificationViewerJourney.ts](dataland-frontend/tests/e2e/specs/specification-viewer/SpecificationViewerJourney.ts)
- Use `describeIf` with appropriate execution environments: `['developmentLocal', 'ci', 'developmentCd']`
- **Use real backend:** No API mocking in e2e tests (requires backend services running)
- Add authentication setup: Login as test user before test (use existing auth utilities)
- Add timeouts for API calls: `cy.get(..., { timeout: 10000 })`

**Test scenario: Business user explores framework specifications**

```typescript
describeIf('As a business user, I want to explore framework specifications', {
  executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
}, function(): void {
  it('Should allow complete specification exploration journey', () => {
    // Step 1: Navigate to specifications page
    cy.visit('/specifications')
    cy.url().should('include', '/specifications')
    
    // Step 2: Verify framework selector visible
    cy.get('[data-test="framework-selector"]').should('be.visible')
    
    // Step 3: Select framework (e.g., "LKSG")
    cy.get('[data-test="framework-selector"]').click()
    cy.contains('LKSG').click() // Assumes LKSG framework exists in backend
    
    // Step 4: Verify URL updates
    cy.url().should('include', '?framework=lksg')
    
    // Step 5: Verify metadata panel displays
    cy.get('[data-test="framework-metadata"]').should('be.visible')
    cy.get('[data-test="framework-metadata"]').should('contain', 'LKSG')
    
    // Step 6: Verify schema tree renders
    cy.get('[data-test="section-header"]').should('exist')
    
    // Step 7: Verify top-level sections auto-expanded
    cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'true')
    
    // Step 8: Expand nested section
    cy.get('[data-test="section-header"]').eq(1).click()
    cy.get('[data-test="section-header"]').eq(1).should('have.attr', 'aria-expanded', 'true')
    
    // Step 9: Click "View Details" on a data point
    cy.get('[data-test="view-details-button"]').first().click()
    
    // Step 10: Verify modal opens
    cy.get('[role="dialog"]').should('be.visible')
    cy.get('[role="dialog"]').should('contain', 'Business Definition')
    
    // Step 11: Close modal with ESC key
    cy.get('body').type('{esc}')
    cy.get('[role="dialog"]').should('not.exist')
    
    // Step 12: Switch to different framework
    cy.get('[data-test="framework-selector"]').click()
    cy.contains('SFDR').click() // Assumes SFDR framework exists
    
    // Step 13: Verify URL updates and new framework loads
    cy.url().should('include', '?framework=sfdr')
    cy.get('[data-test="framework-metadata"]').should('contain', 'SFDR')
    
    // Step 14: Browser back button (if supported by Cypress)
    cy.go('back')
    cy.url().should('include', '?framework=lksg')
    cy.get('[data-test="framework-metadata"]').should('contain', 'LKSG')
  })
})
```

**Assertions at each step:**
- URL contains expected query param
- Elements visible using `cy.get('[data-test="..."]').should('be.visible')`
- Text content matches expected framework name
- Tree sections expand/collapse correctly
- Modal dialog appears and disappears
- State persists correctly through navigation

**Prerequisites:**
- Backend services (specification service) running and accessible
- Test user authenticated (setup in `beforeEach` or use existing auth utility)
- At least two frameworks deployed to backend (e.g., LKSG, SFDR)

### 9. Create e2e test for error recovery flow

- Add to [dataland-frontend/tests/e2e/specs/specification-viewer/SpecificationViewerJourney.ts](dataland-frontend/tests/e2e/specs/specification-viewer/SpecificationViewerJourney.ts) or create separate file
- **Challenge:** Simulating backend errors in e2e tests without mocking

**Test scenario: User recovers from temporary backend unavailability**

**Option A: If backend control available:**
```typescript
it('Should allow user to retry after backend error', () => {
  // Simulate backend down (requires infrastructure control)
  // Stop specification service
  
  cy.visit('/specifications')
  
  // Verify error message displays
  cy.get('[data-test="error-message"]').should('be.visible')
  cy.get('[data-test="retry-button"]').should('be.visible')
  
  // Restart specification service
  
  // Click retry
  cy.get('[data-test="retry-button"]').click()
  
  // Verify successful load
  cy.get('[data-test="framework-selector"]').should('be.visible')
})
```

**Option B: If no backend control (simpler approach):**
- Use `cy.intercept()` even in e2e to force error for testing retry mechanism
- This is acceptable for testing error UI/UX even in e2e context
- Or mark as manual test case if e2e infrastructure doesn't support error simulation

**Consider:** This test may be better suited for component tests where mocking is easier. E2E should focus on happy path with real backend.

### 10. Create e2e test for accessibility and keyboard navigation

- Add to specification-viewer e2e specs
- **Focus:** Validate keyboard-only navigation in production-like environment

**Test scenario: Keyboard-only navigation through specifications**

```typescript
it('Should support complete keyboard-only navigation', () => {
  cy.visit('/specifications')
  
  // Tab to framework dropdown
  cy.get('body').tab() // Requires cypress-plugin-tab or cy.realPress()
  cy.focused().should('have.attr', 'data-test', 'framework-selector')
  
  // Select framework with keyboard (Enter/Arrow keys)
  cy.focused().type('{enter}')
  cy.focused().type('{downarrow}')
  cy.focused().type('{enter}')
  
  // Wait for load
  cy.get('[data-test="framework-metadata"]').should('be.visible')
  
  // Tab to first section header
  cy.get('[data-test="section-header"]').first().focus()
  
  // Toggle with Enter key
  cy.focused().type('{enter}')
  cy.focused().should('have.attr', 'aria-expanded', 'false')
  
  // Toggle with Space key
  cy.focused().type(' ')
  cy.focused().should('have.attr', 'aria-expanded', 'true')
  
  // Tab to "View Details" button
  cy.get('[data-test="view-details-button"]').first().focus()
  cy.focused().type('{enter}')
  
  // Verify modal opens
  cy.get('[role="dialog"]').should('be.visible')
  
  // Close with ESC
  cy.get('body').type('{esc}')
  cy.get('[role="dialog"]').should('not.exist')
})
```

**Use:**
- `cy.realPress()` from `cypress-real-events` plugin for realistic keyboard events
- Or `cy.tab()` from `cypress-plugin-tab`
- Or native Cypress `.type()` with special keys

**Validates:**
- ARIA attributes work correctly end-to-end
- Keyboard support functions in real browser
- Focus management doesn't break with real API interactions
- Tab order is logical and complete

## Phase 5: Integration and CI

### 11. Configure test execution scripts and coverage

- Tests automatically integrated via existing [cypress.config.ts](dataland-frontend/cypress.config.ts)
- **No configuration changes needed** - verify functionality:

**Component tests:**
```bash
npm run testcomponent
```
- Runs all `.cy.ts` files in `tests/component/` directory
- Includes new tests for specifications

**E2E tests:**
```bash
npm run testpipeline  # CI environment
npm run cypress       # Local development (opens Cypress UI)
```
- Runs e2e tests with backend dependencies
- Requires local stack or CI environment running

**Coverage:**
- Istanbul plugin already configured in `vite.config.mts`
- Coverage generated automatically with `@cypress/code-coverage` plugin
- View coverage: Check output after test run or view in `coverage/` directory

**Verify execution:**
1. Run `npm run testcomponent` locally - all tests pass
2. Check coverage report for specifications code:
   - `useSpecifications.ts` - Target >80%
   - `useDataPointDetails.ts` - Target >80%
   - `SpecificationSchemaTree.vue` - Target >80%
   - New StringFormatter functions - Target >90%
3. Run `npm run cypress` - e2e tests pass with backend running

**CI Integration:**
- Tests run automatically in pipeline via existing configuration
- No additional setup required
- Pipeline calls `npm run testpipeline` for CI execution

### 12. Create test fixtures for reusable test data

Create standardized test fixtures in `testing/data/specifications/` directory for consistency across all tests.

**Fixture: Framework list**
- Create [testing/data/specifications/framework-list.json](testing/data/specifications/framework-list.json)
- Content: Array of framework metadata objects
- Include: 3-5 frameworks with varied names (LKSG, SFDR, EuTaxonomy, etc.)
- Structure based on `/specifications/frameworks` API response
```json
[
  {
    "id": "lksg",
    "name": "LKSG",
    "businessDefinition": "German Supply Chain Due Diligence Act requirements"
  },
  {
    "id": "sfdr",
    "name": "SFDR",
    "businessDefinition": "Sustainable Finance Disclosure Regulation"
  }
]
```

**Fixture: LKSG framework with schema**
- Create [testing/data/specifications/lksg-framework.json](testing/data/specifications/lksg-framework.json)
- Content: Complete framework specification with JSON schema
- Include: 2-3 top-level sections, nested subsections, 10-15 data points
- Structure based on `/specifications/frameworks/{id}` API response
- Include variety: sections with/without labels, data points with/without aliasExport

**Fixture: Empty framework**
- Create [testing/data/specifications/empty-framework.json](testing/data/specifications/empty-framework.json)
- Content: Framework with empty schema
- Use case: Testing empty state handling

**Fixture: Deeply nested schema**
- Create [testing/data/specifications/nested-schema.json](testing/data/specifications/nested-schema.json)
- Content: Framework with 5+ level nesting
- Use case: Testing recursive rendering limits

**Fixture: Large schema**
- Create [testing/data/specifications/large-schema.json](testing/data/specifications/large-schema.json)
- Content: Framework with 100+ data points across multiple sections
- Use case: Performance testing

**Fixture: Data point details**
- Create [testing/data/specifications/datapoint-details.json](testing/data/specifications/datapoint-details.json)
- Content: Complete data point specification
- Structure based on `/specifications/datapoints/{id}` API response
- Include: businessDefinition, baseType, constraints, usingFrameworks

**Fixture: Data point with constraints**
- Create [testing/data/specifications/datapoint-with-constraints.json](testing/data/specifications/datapoint-with-constraints.json)
- Content: Data point with complex constraint definitions
- Use case: Testing constraint formatting in dialog

**Loading fixtures in tests:**
```typescript
// In component tests
cy.fixture('specifications/framework-list.json').then((frameworks) => {
  cy.intercept('GET', '/specifications/frameworks', frameworks).as('getFrameworks')
})

// Alternative inline
cy.intercept('GET', '/specifications/frameworks', { fixture: 'specifications/framework-list.json' }).as('getFrameworks')
```

**Fixture organization:**
- Store all in `testing/data/specifications/` subdirectory
- Follow existing fixture patterns from other test data
- Use semantic names that indicate content/purpose
- Add README.md in specifications directory documenting each fixture

## Verification Checklist

After implementation, verify:

**Component Tests:**
- [ ] Run `npm run testcomponent` - all tests pass
- [ ] All new test files execute without errors
- [ ] Test output shows: 7 test suites with 50+ individual tests
- [ ] No flaky tests (run 3 times, all pass consistently)

**Coverage Metrics:**
- [ ] `useSpecifications.ts` - >80% line coverage
- [ ] `useDataPointDetails.ts` - >80% line coverage
- [ ] `SpecificationSchemaTree.vue` - >80% line coverage
- [ ] `truncateText()` function - 100% coverage (all branches)
- [ ] `humanizeDataPointBaseType()` function - >90% coverage
- [ ] Critical paths (schema parsing, error handling) - 100% coverage

**E2E Tests:**
- [ ] Run `npm run cypress` (open mode) with backend running locally
- [ ] Golden path test completes successfully
- [ ] Keyboard navigation test passes (at least 1 run)
- [ ] Tests execute in under 2 minutes total
- [ ] Tests clean up state properly (repeatable without manual cleanup)

**CI Integration:**
- [ ] Tests run automatically in CI pipeline
- [ ] Pipeline succeeds with new tests (check CI logs)
- [ ] Coverage reports generated and accessible
- [ ] No new console errors or warnings introduced

**Manual Testing (Regression Check):**
- [ ] Open specification viewer in browser at `/specifications`
- [ ] All functionality works as before (no regressions)
- [ ] No console errors during normal usage
- [ ] Page loads within acceptable time (<2s for initial load)

**Code Quality:**
- [ ] All test files follow existing patterns and conventions
- [ ] Test names clearly describe what is being tested
- [ ] No hardcoded values (use fixtures and constants)
- [ ] Proper use of `data-test` attributes (no CSS class selectors)
- [ ] ESLint passes on all test files
- [ ] TypeScript compiles without errors

**Documentation:**
- [ ] Test fixtures include README.md explaining structure
- [ ] Complex test logic includes comments
- [ ] Any test utilities created are documented

## Testing Best Practices Applied

**Cypress Component Testing:**
- Use `cy.mountWithPlugins()` for consistent component mounting
- Mock external dependencies (Keycloak, API clients) consistently
- Use `cy.intercept()` for API mocking, not manual mock implementations
- Select elements via `data-test` attributes, never CSS classes
- Test user-visible behavior, not implementation details
- Keep tests independent (no shared state between tests)

**Cypress E2E Testing:**
- Use real backend (no mocking) for authentic integration testing
- Test happy paths, not every edge case (those belong in component tests)
- Use `describeIf` for environment-specific tests
- Add appropriate timeouts for slow operations
- Clean up test data if creating/modifying backend state

**General Testing:**
- Arrange-Act-Assert pattern in all tests
- One assertion focus per test (testing one thing)
- Descriptive test names: "Should [expected behavior] when [condition]"
- Test edge cases in isolated tests, not bundled with happy path
- Use fixtures for complex data, inline data for simple cases
- Mock at the boundaries (API layer), not internal functions

**Coverage Strategy:**
- Prioritize critical paths (schema parsing, error handling, state transitions)
- 100% coverage not required - focus on value, not metrics
- Edge cases should have explicit tests (empty data, errors, boundaries)
- UI interactions covered by component tests, not just unit tests

**Common Pitfalls to Avoid:**
- ❌ Don't test PrimeVue internal behavior (e.g., how Dialog renders internally)
- ❌ Don't use `:deep()` selectors in tests - use `data-test` attributes
- ❌ Don't couple tests to component structure (test props/events, not internal state)
- ❌ Don't skip error cases - they're often most important tests
- ❌ Don't write mega-tests that test everything - keep focused and atomic

## Decisions Made

1. **Cypress-only approach:** Project has no Vitest/Jest infrastructure, using Cypress for all tests (component + e2e) maintains consistency and leverages existing patterns
2. **Component wrapper pattern for composables:** Mount minimal Vue wrapper components to test composables in isolation with Cypress, following patterns seen in other projects
3. **Prioritize recursive tree tests:** Most complex logic with highest edge case count, critical for feature correctness
4. **Phase 2 batch loading tests:** Cover optional enhancement feature with dedicated test suite, verify graceful degradation on partial failures
5. **Real backend for e2e:** No API mocking in e2e tests maintains test realism and catches integration issues (except for error simulation where necessary)
6. **Fixtures in shared directory:** Store in `testing/data/specifications/` for reuse across test types and potential future backend tests
7. **Target >80% coverage:** Balances thorough testing with effort, focuses on critical paths rather than arbitrary 100%
8. **data-test attributes assumed present:** Implementation plans specified adding these, tests rely on them for reliable element selection
9. **Accessibility testing in e2e:** Keyboard navigation validated end-to-end to ensure ARIA attributes and focus management work in real browser
10. **Separate test suites:** Organize by file (component, composable, utility, page) for maintainability and parallel execution potential

## Next Steps

After creating all test files per this plan:

1. **Run tests locally:** Verify all component tests pass and coverage meets targets
2. **Review coverage gaps:** Use coverage report to identify untested branches, add targeted tests
3. **E2E validation:** Run e2e tests with local backend stack, verify golden path completes
4. **CI verification:** Push to branch, verify tests run in CI pipeline successfully
5. **Iterate:** Fix flaky tests, improve assertions, refactor duplicate test logic into utilities
6. **Document:** Update project documentation with information about testing the specifications viewer
