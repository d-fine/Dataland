# Plan: Business-Friendly Specification Viewer Component

An integrated frontend component with a framework selector dropdown at the top, followed by an expandable schema table for the chosen framework. Business users select a framework and explore its data point structure in a human-readable, explorable format with truncated business definitions and detail modals.

**Key Decisions:**
- **Single-page design:** Framework selector dropdown + expandable schema table below (no separate overview/detail pages)
- **Display pattern:** Custom expandable sections following MultiLayerDataTable pattern with chevron icons (NOT PrimeVue Accordion)
- **Data point visibility:** Human-readable name + truncated business definition (150 chars), "Show more" expands, "View Details" button for technical specs
- **Navigation:** Single entry point in main navigation menu - users select framework from dropdown
- **Initial state:** Top-level sections auto-expanded, nested sections collapsed
- **URL integration:** Selected framework stored in URL query param (e.g., `/specifications?framework=lksg`) for shareability
- **Business focus:** Emphasize `aliasExport` (human-readable names) over technical IDs, definitions over constraints
- **Standalone:** Independent from framework data viewing (no cross-navigation to data views)

**Steps**

1. **Generate TypeScript client for specification service**
   - Edit [dataland-frontend/build.gradle.kts](dataland-frontend/build.gradle.kts)
   - Add `generateSpecificationServiceClient` task following pattern of `generateBackendClient`
   - Point to OpenAPI spec: `dataland-specification-service/specificationServiceOpenApi.json`
   - Configure output to `build/clients/specificationservice`
   - Add to `generateClients` task dependencies

2. **Register specification service client in API infrastructure**
   - Edit [dataland-frontend/src/services/ApiClients.ts](dataland-frontend/src/services/ApiClients.ts)
   - Add `specificationController` property to `ApiClientProvider.apiClients`
   - Configure with base path `/specifications`
   - Follow existing pattern from `backendController` registration

3. **Understand and document schema structure from API**
   - Review specification service OpenAPI spec: `dataland-specification-service/specificationServiceOpenApi.json`
   - Examine example response from `/specifications/frameworks/{id}` endpoint
   - Document schema JSON structure with example showing:
     - How sections are represented (objects with nested properties)
     - How data points are identified (`$ref` property pointing to data point type)
     - Label properties (`label`, `aliasExport` for human-readable names)
     - Nesting depth and recursive structure patterns
   - Include example in code comments for reference during parsing implementation
   - **Example structure to look for:**
     ```json
     {
       "frameworkId": "lksg",
       "name": "LKSG",
       "schema": {
         "type": "object",
         "properties": {
           "generalInfo": {
             "type": "object",
             "label": "General Information",
             "properties": {
               "companyName": {
                 "$ref": "#/definitions/DataPointTypeId-123",
                 "label": "Company Name",
                 "aliasExport": "Company Name"
               }
             }
           }
         }
       }
     }
     ```

4. **Create TypeScript types for parsed schema structure**
   - Create [dataland-frontend/src/types/Specifications.ts](dataland-frontend/src/types/Specifications.ts)
   - Define `ParsedSchemaNode` interface: `{ type: 'section', label: string, children: ParsedSchemaNode[] } | { type: 'dataPoint', id: string, ref: string, aliasExport?: string, businessDefinition?: string }`
   - Define `FrameworkSpecificationWithParsedSchema` extending generated `FrameworkSpecification` with parsed schema property
   - Import generated types from `@clients/specificationservice`
   - Add JSDoc comments referencing the schema structure documented in step 3

5. **Add text truncation utility function**
   - Add to existing [dataland-frontend/src/utils/StringFormatter.ts](dataland-frontend/src/utils/StringFormatter.ts)
   - Function `truncateText(text: string, maxLength: number): { truncated: string, needsTruncation: boolean }`
   - Truncate at word boundaries, append "..." if truncated
   - Return both truncated text and flag indicating if truncation occurred
   - Used for business definition display in schema tree (150 char limit)

6. **Create consolidated composable for data fetching and state management**
   - Create [dataland-frontend/src/composables/useSpecifications.ts](dataland-frontend/src/composables/useSpecifications.ts)
   - **Follow Dataland composable pattern:** Accept fetch functions as parameters for testability (see `usePortfolioOverview` pattern)
   - **Define options interface:**
     ```typescript
     interface UseSpecificationsOptions {
       fetchFrameworks: () => Promise<Framework[]>
       fetchSpecification: (id: string) => Promise<FrameworkSpecification>
     }
     ```
   - **Internalize schema parsing:** Parse the schema JSON within `selectFramework()` to transform raw API response into `ParsedSchemaNode[]` structure
   - **Parse schema based on structure from step 3:**
     - Traverse schema properties recursively
     - Identify sections: objects with nested `properties`
     - Identify data points: objects with `$ref` property
     - Extract labels using `aliasExport` (preferred) or `label` property
     - Build hierarchical `ParsedSchemaNode` tree
   - **Expose reactive state:**
     - `frameworks: Ref<Framework[]>`: List of all available frameworks for dropdown
     - `selectedFramework: Ref<FrameworkSpecificationWithParsedSchema | null>`: Currently selected and parsed framework
     - `isLoadingFrameworks: Ref<boolean>`: Loading state for framework list (shows spinner in dropdown area)
     - `isLoadingSpecification: Ref<boolean>`: Loading state for specification (shows skeleton/spinner in content area, non-blocking for dropdown)
     - `error: Ref<string | null>`: Error message for user display
   - **Expose methods:**
     - `loadFrameworks()`: Fetches framework list via provided function
     - `selectFramework(frameworkId: string)`: Fetches specification, parses schema, updates state
   - Use `shallowRef` for `selectedFramework` to optimize performance with large nested structures
   - Include comprehensive error handling with user-friendly messages

7. **Create composable for data point details**
   - Create [dataland-frontend/src/composables/useDataPointDetails.ts](dataland-frontend/src/composables/useDataPointDetails.ts)
   - **Follow dependency injection pattern:**
     ```typescript
     interface UseDataPointDetailsOptions {
       fetchDataPointDetails: (id: string) => Promise<DataPointTypeDetails>
     }
     export function useDataPointDetails({ fetchDataPointDetails }: UseDataPointDetailsOptions)
     ```
   - Expose `dataPointDetails: Ref<DataPointTypeDetails | null>`, `isLoading: Ref<boolean>`, `error: Ref<string | null>`
   - Method `loadDetails(dataPointTypeId: string)` - fetches details for the modal
   - This keeps the modal's data logic separate and reusable
   - Include retry mechanism or clear error recovery pattern

8. **Create main specification viewer page**
   - Create [dataland-frontend/src/components/pages/SpecificationsViewer.vue](dataland-frontend/src/components/pages/SpecificationsViewer.vue)
   - **Setup Keycloak and API client:**
     ```typescript
     const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!
     const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())
     ```
   - **Initialize `useSpecifications` composable with fetch functions:**
     ```typescript
     const { frameworks, selectedFramework, isLoadingFrameworks, isLoadingSpecification, error, loadFrameworks, selectFramework } = useSpecifications({
       fetchFrameworks: () => apiClientProvider.apiClients.specificationController.getFrameworks().then(r => r.data),
       fetchSpecification: (id) => apiClientProvider.apiClients.specificationController.getFrameworkSpecification(id).then(r => r.data)
     })
     ```
   - Top section: PrimeVue `Select` component (v4) with frameworks list
   - Add `aria-label="Select framework"` for accessibility
   - On selection change: call `selectFramework(frameworkId)` and update URL query param
   - **URL synchronization:** Watch route query param, call `selectFramework()` on mount if param present
   - **Pass data via props:**
     - Pass the `selectedFramework` object as a prop to `FrameworkMetadataPanel`
     - Pass the `selectedFramework.parsedSchema` as a prop to `SpecificationSchemaTree`
   - Show "Select a framework to view its specification" empty state when `selectedFramework` is null
   - Display loading states:
     - Spinner in dropdown area while `isLoadingFrameworks` is true
     - Skeleton/spinner in content area while `isLoadingSpecification` is true (non-blocking)
   - Error display with "Retry" button that calls appropriate load function
   - Wrap in `TheContent` layout component
   - Add `data-test="framework-selector"` and `data-test="specifications-content"` attributes

9. **Create framework metadata panel component (Presentational)**
   - Create [dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue](dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue)
   - **This is a presentational component.** It receives all data via props from parent (`SpecificationsViewer`)
   - `defineProps<{ framework: FrameworkSpecificationWithParsedSchema }>()`
   - Display: Framework Name (as title), Business Definition, Framework ID, Referenced Report Path (if present)
   - Use PrimeVue `Card` component with semantic HTML structure inside
   - **Only use PassThrough API if default Card styling doesn't meet requirements** - otherwise use Card's template slots
   - Semantic structure: `<dl>` for definition list with `<dt>` labels and `<dd>` values
   - Add `data-test="framework-metadata"` attribute
   - Ensure good visual hierarchy: Name prominent, ID de-emphasized

10. **Create hierarchical schema tree component (Presentational)**
    - Create [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
    - **This is a presentational component.** It receives the parsed schema tree via props
    - `defineProps<{ schema: ParsedSchemaNode[] }>()`
    - Follow `MultiLayerDataTableBody.vue` pattern for expandable sections with chevron icons
    - Use `Set<string>` to track expanded section unique identifiers (generate unique ID per section using path)
    - Render recursive sections and data points based on the `schema` prop
    - **Accessibility attributes:**
      - `aria-expanded="true|false"` on section headers
      - `role="button"` and `tabindex="0"` on clickable section headers
      - Keyboard navigation: Enter/Space to toggle sections
    - For each data point:
      - Display `aliasExport` (human-readable name) prominently
      - Truncate business definition to 150 chars using `truncateText()` utility
      - "Show more"/"Show less" toggle (inline, no modal) for definition expansion
      - "View Details" button emits `view-details` event with `dataPointTypeId`
    - "View Details" button emits an event with the `dataPointTypeId` to parent, which opens the modal
    - Auto-expand top-level sections on mount using `onMounted()`
    - Add `data-test` attributes: `section-header`, `datapoint-name`, `datapoint-definition`, `show-more-toggle`, `view-details-button`
    - Scoped styles ONLY for structural layout (flex, grid, padding, margin, gap)

11. **Create data point detail modal component**
    - Create [dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue](dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue)
    - Use PrimeVue `Dialog` with `v-model:visible` prop binding
    - `defineProps<{ visible: boolean, dataPointTypeId: string | null }>()`
    - `defineEmits<{ 'update:visible': [value: boolean] }>()`
    - **Setup API client and initialize composable:**
      ```typescript
      const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())
      const { dataPointDetails, isLoading, error, loadDetails } = useDataPointDetails({
        fetchDataPointDetails: (id) => apiClientProvider.apiClients.specificationController.getDataPointType(id).then(r => r.data)
      })
      ```
    - Watch `visible` prop: when becomes true and `dataPointTypeId` is set, call `loadDetails(dataPointTypeId)`
    - Display sections: Name (title), Business Definition (prominent), Base Type, Constraints (formatted), Frameworks using this data point
    - Format technical fields for readability (JSON formatted, lists as bullets)
    - **Accessibility:**
      - `aria-modal="true"` on Dialog
      - Focus trap within modal
      - `aria-label="Data point details"` on Dialog
    - Include close button with `data-test="close-dialog"`
    - Use semantic HTML structure: `<dl>`, `<dt>`, `<dd>` for definition lists
    - Show loading spinner within modal content area while `isLoading` is true
    - Display error message with retry button if fetch fails

12. **Add routing configuration**
    - Edit [dataland-frontend/src/router/index.ts](dataland-frontend/src/router/index.ts)
    - Add single route: `{ path: '/specifications', name: 'SpecificationsViewer', component: SpecificationsViewer, meta: { requiresAuth: true } }`
    - Route reads `?framework=<id>` query param on mount to pre-select framework
    - When query param changes (browser back/forward), update selected framework reactively
    - Authentication required via `meta.requiresAuth` (standard Keycloak pattern)

13. **Add to main navigation**
    - Edit main navigation component (likely [dataland-frontend/src/components/generics/TheHeader.vue](dataland-frontend/src/components/generics/TheHeader.vue) or nav menu component)
    - Add "Specifications" menu item linking to `/specifications`
    - Position appropriately for business users (near "Frameworks" or "Documentation" sections if they exist)
    - Use PrimeIcon `pi pi-book` or `pi pi-list` for menu icon
    - Ensure keyboard navigation works (Tab key, Enter to activate)
    - Add `data-test="specifications-nav-link"` attribute for e2e testing

14. **Implement strict styling adherence**
    - Review all created components for compliance with Dataland frontend guidelines
    - **ONLY** use PrimeVue Design Tokens for colors:
      - `var(--p-primary-color)`, `var(--p-surface-0)`, `var(--p-text-color)`, `var(--p-text-secondary-color)`
      - `var(--p-surface-card)`, `var(--p-border-radius)`, `var(--p-dialog-header-background)`
    - **NO** hardcoded colors (e.g., `#ffffff`, `rgb()`, color names)
    - **NO** PrimeFlex classes (e.g., `p-3`, `flex`, `grid`)
    - **NO** `:deep()` CSS selectors - use PrimeVue PassThrough API for component customization
    - **ONLY** PrimeIcons (`pi pi-*`) - do NOT use Material Icons
    - Scoped styles **ONLY** for structural layout properties: `display`, `flex`, `grid`, `padding`, `margin`, `gap`, positioning
    - Reference [.github/skills/dataland-vue-component/references/dataland-frontend-coding-guidelines.md](.github/skills/dataland-vue-component/references/dataland-frontend-coding-guidelines.md) throughout implementation

**Verification**

1. **Build verification:**
   - Run `./gradlew dataland-frontend:generateClients` - specification service client generated successfully
   - Check that `dataland-frontend/build/clients/specificationservice` directory exists with generated TypeScript files
   - Run `./gradlew dataland-frontend:build` - TypeScript compiles without errors

2. **Functional testing:**
   - Navigate to `/specifications` - see framework selector dropdown and empty state prompt
   - Select framework "LKSG" from dropdown - URL updates to `/specifications?framework=lksg`
   - Framework metadata panel appears below dropdown showing name, definition, ID
   - Schema tree renders with top-level sections auto-expanded
   - Expand/collapse nested sections - chevron icons update correctly (down when expanded, left when collapsed)
   - Business definitions truncated at 150 chars - "Show more" reveals full text, "Show less" re-truncates
   - Click "View Details" on a data point - modal opens showing full specification with base type, constraints, frameworks
   - Close modal with close button or click outside - returns to viewer
   - Change framework selection to "SFDR" - URL updates, new schema loads without page reload
   - Verify loading states (spinner) appear during API calls
   - Test browser back/forward navigation - framework selection updates correctly from URL
   - Test direct URL navigation `/specifications?framework=sfdr` - framework pre-selected on page load
   - Test error scenarios: invalid framework ID shows error message, API failure handled gracefully

3. **Business user validation:**
   - Framework selection is intuitive and discoverable via dropdown
   - Business definitions are prominent, easy to read, and not cluttered with technical jargon
   - Human-readable names (`aliasExport`) are displayed prominently throughout
   - Navigation is clear - users can easily switch between frameworks and explore data points
   - Technical details (IDs, constraints, base types) are de-emphasized or hidden until "View Details" clicked
   - Information hierarchy is clear: Framework → Sections → Data Points → Detailed Specifications

4. **Accessibility validation:**
   - Keyboard navigation: Tab through all interactive elements, Enter/Space to activate
   - Screen reader testing: NVDA/JAWS announces all content correctly
   - Expandable sections have `aria-expanded` attribute updating correctly
   - Modal has proper focus trap, closes with Escape key
   - Form controls have associated labels
   - Loading states announced via `aria-live` regions
   - Color contrast meets WCAG AA standards (use browser DevTools accessibility audit)

5. **Styling compliance audit:**
   - Inspect all components in browser DevTools - NO hardcoded colors found
   - All color references use `var(--p-*)` Design Token syntax
   - NO PrimeFlex classes present in any template
   - All icons use PrimeIcons (`pi pi-*`) - search codebase confirms
   - Scoped styles contain ONLY structural CSS properties (flex, grid, padding, margin, gap)
   - NO `:deep()` selectors present - any component customization uses PassThrough API
   - Styles follow Dataland guidelines per reference documentation

6. **E2E test readiness:**
   - All interactive elements have `data-test` attributes for test automation
   - Key selectors ready:
     - `data-test="framework-selector"` - dropdown component
     - `data-test="framework-metadata"` - metadata panel
     - `data-test="section-header"` - expandable section headers
     - `data-test="datapoint-name"` - data point names
     - `data-test="view-details-button"` - buttons to open modal
     - `data-test="close-dialog"` - modal close button
   - Ready for Cypress/Playwright test implementation

**Technical Considerations**

- **URL state management:** Framework selection persisted in URL query param for shareability and browser back/forward navigation support
- **Performance optimization:** Use `shallowRef` for parsed schema to avoid unnecessary deep reactivity overhead with large nested objects
- **Composable pattern:** Follow existing Dataland pattern of dependency injection (pass fetch functions as parameters) for testability and consistency with `usePortfolioOverview`
- **Empty state UX:** Clear, actionable messaging when no framework selected - prompts user to make selection
- **Framework list sorting:** Frameworks sorted alphabetically by name in dropdown for intuitive discovery
- **Loading states:** Separate granular loading indicators - dropdown area vs content area - allowing users to switch frameworks while specification loads (non-blocking UI)
- **Accessibility:** 
  - Keyboard navigation support for dropdown and expandable sections (Tab, Enter, Space keys)
  - ARIA attributes: `aria-expanded`, `aria-label`, `aria-modal`, `aria-live` for loading announcements
  - Screen reader support for all interactive elements
  - Semantic HTML structure (`<section>`, `<article>`, `<dl>`, `<dt>`, `<dd>`) for proper document outline
  - Focus management in modal (focus trap, return focus on close)
- **Responsive design:** Test on mobile/tablet viewports - dropdown and tree remain usable, touch-friendly interaction areas (minimum 44px touch targets)
- **Error handling:** User-friendly error messages for API failures, invalid framework IDs, network issues, with "Retry" buttons for recovery
- **Schema parsing robustness:** Handle edge cases - missing labels (fall back to property keys), deeply nested structures, empty sections, malformed refs
- **Future extensibility:** Component architecture supports potential future features like search/filter within schema tree, bookmarking specific data points, exporting schema documentation

**Decisions**

- **Single-page over two-page:** Simpler UX, reduces navigation overhead, keeps context visible (dropdown always present)
- **Custom expandable sections over PrimeVue Accordion:** Maintains consistency with existing MultiLayerDataTable pattern used throughout Dataland
- **Truncated definitions with toggle:** Balances information density with readability - users see overview and can drill down
- **URL query params:** Enables sharing specific frameworks, browser history navigation works naturally
- **Top-level expanded by default:** Business users see immediate value, nested sections prevent overwhelming information
- **Modal for technical details:** Separates business-focused primary view from developer/technical secondary information