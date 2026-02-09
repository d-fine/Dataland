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

3. **Create TypeScript types for parsed schema structure**
   - Create [dataland-frontend/src/types/Specifications.ts](dataland-frontend/src/types/Specifications.ts)
   - Define `ParsedSchemaNode` interface: `{ type: 'section', label: string, children: ParsedSchemaNode[] } | { type: 'dataPoint', id: string, ref: string, aliasExport?: string }`
   - Define `FrameworkSpecificationWithParsedSchema` extending generated `FrameworkSpecification` with parsed schema property
   - Import generated types from `@clients/specificationservice`

4. **Create text truncation utility**
   - Create [dataland-frontend/src/utils/TextTruncation.ts](dataland-frontend/src/utils/TextTruncation.ts) or add to existing utils
   - Function `truncateText(text: string, maxLength: number): { truncated: string, needsTruncation: boolean }`
   - Truncate at word boundaries, append "..." if truncated
   - Used for business definition display in schema tree

5. **Create consolidated composable for data fetching and state management**
   - Create [dataland-frontend/src/composables/useSpecifications.ts](dataland-frontend/src/composables/useSpecifications.ts)
   - **Consolidate all specification-related logic here.** This composable will be the single source of truth for the viewer.
   - **Internalize schema parsing:** Move the logic for parsing the schema JSON into this composable. It will transform the raw API response into the `ParsedSchemaNode[]` structure.
   - **Expose reactive state:**
     - `frameworks: Ref<Framework[]>`: List of all available frameworks for the dropdown.
     - `selectedFramework: Ref<FrameworkSpecificationWithParsedSchema | null>`: The currently selected and fully processed framework specification.
     - `isLoadingFrameworks: Ref<boolean>`: Loading state for the initial framework list.
     - `isLoadingSpecification: Ref<boolean>`: Loading state for the detailed framework view.
     - `error: Ref<string | null>`: Error message for display.
   - **Expose methods:**
     - `loadFrameworks()`: Fetches the list of all frameworks.
     - `selectFramework(frameworkId: string)`: Fetches the specification for a given ID and parses its schema.
   - Use `shallowRef` for the parsed schema within `selectedFramework` to optimize performance.

6. **Create composable for data point details**
   - Create [dataland-frontend/src/composables/useDataPointDetails.ts](dataland-frontend/src/composables/useDataPointDetails.ts)
   - Export `useDataPointTypeDetails(dataPointTypeId)` - fetches details for the modal (`/specifications/data-point-types/{id}`).
   - This keeps the modal's data logic separate and reusable.
   - Include `ref` for loading/error states specific to the modal.

7. **Create main specification viewer page**
   - Create [dataland-frontend/src/components/pages/SpecificationsViewer.vue](dataland-frontend/src/components/pages/SpecificationsViewer.vue)
   - **Use the `useSpecifications` composable** to manage all state and data fetching.
   - Top section: PrimeVue `Select` component with frameworks from `useSpecifications()`.
   - On selection change: call `selectFramework(frameworkId)` from the composable and update the URL query param.
   - **Pass data via props:**
     - Pass the `selectedFramework` object as a prop to `FrameworkMetadataPanel`.
     - Pass the `selectedFramework.parsedSchema` as a prop to `SpecificationSchemaTree`.
   - Show "Select a framework to view its specification" empty state message when `selectedFramework` is null.
   - Display loading spinners and error messages based on the composable's state.
   - Wrap in `TheContent` layout component.
   - Add `data-test="framework-selector"` and `data-test="specifications-content"` attributes.

8. **Create framework metadata panel component (Presentational)**
   - Create [dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue](dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue)
   - **This is a presentational component.** It should receive all data via props from its parent (`SpecificationsViewer`).
   - `defineProps<{ framework: FrameworkSpecificationWithParsedSchema }>()`
   - Display: Framework Name, Business Definition, Framework ID from the `framework` prop.
   - Show `referencedReportJsonPath` if present.
   - Use PrimeVue `Card` component with custom layout via PassThrough API.
   - Add `data-test="framework-metadata"` attribute.

9. **Create hierarchical schema tree component (Presentational)**
   - Create [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **This is a presentational component.** It receives the parsed schema tree via props.
   - `defineProps<{ schema: ParsedSchemaNode[] }>()`
   - Follow `MultiLayerDataTableBody.vue` pattern for expandable sections.
   - Use `Set<string>` to track expanded section unique identifiers.
   - Render recursive sections and data points based on the `schema` prop.
   - "View Details" button emits an event with the `dataPointTypeId` to the parent, which then opens the modal.
   - Auto-expand top-level sections on mount.
   - Add `data-test` attributes: `section-header`, `datapoint-name`, `datapoint-definition`, `show-more-toggle`, `view-details-button`.
   - Scoped styles ONLY for structural layout.

10. **Create data point detail modal component**
    - Create [dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue](dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue)
    - Use PrimeVue `Dialog` with `v-model:visible` prop binding.
    - **Use the `useDataPointDetails` composable.** Fetch data when the modal opens, triggered by a `dataPointTypeId` prop.
    - Display sections: Name, Business Definition, Base Type, Constraints, etc.
    - Format technical fields for readability.
    - Include close button with `data-test="close-dialog"`.
    - Use semantic HTML structure (`<dl>`, `<dt>`, `<dd>`).
    - Manage its own loading state within the modal while fetching details.

11. **Add routing configuration**
    - Edit [dataland-frontend/src/router/index.ts](dataland-frontend/src/router/index.ts)
    - Add single route: `{ path: '/specifications', name: 'SpecificationsViewer', component: SpecificationsViewer, meta: { requiresAuth: true } }`
    - Route reads `?framework=<id>` query param on mount to pre-select framework
    - When query param changes (browser back/forward), update selected framework reactively
    - Authentication required via `meta.requiresAuth` (standard Keycloak pattern)

12. **Add to main navigation**
    - Edit main navigation component (likely [dataland-frontend/src/components/generics/TheHeader.vue](dataland-frontend/src/components/generics/TheHeader.vue) or nav menu component)
    - Add "Specifications" menu item linking to `/specifications`
    - Position appropriately for business users (near "Frameworks" or "Documentation" sections if they exist)
    - Use PrimeIcon `pi pi-book` or `pi pi-list` for menu icon
    - Add `data-test="specifications-nav-link"` attribute for e2e testing

13. **Implement strict styling adherence**
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

4. **Styling compliance audit:**
   - Inspect all components in browser DevTools - NO hardcoded colors found
   - All color references use `var(--p-*)` Design Token syntax
   - NO PrimeFlex classes present in any template
   - All icons use PrimeIcons (`pi pi-*`) - search codebase confirms
   - Scoped styles contain ONLY structural CSS properties (flex, grid, padding, margin, gap)
   - NO `:deep()` selectors present - any component customization uses PassThrough API
   - Styles follow Dataland guidelines per reference documentation

5. **E2E test readiness:**
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
- **Empty state UX:** Clear, actionable messaging when no framework selected - prompts user to make selection
- **Framework list sorting:** Frameworks sorted alphabetically by name in dropdown for intuitive discovery
- **Accessibility:** 
  - Keyboard navigation support for dropdown and expandable sections
  - ARIA labels for screen readers on all interactive elements
  - Semantic HTML structure (`<section>`, `<article>`, `<dl>`) for proper document outline
- **Responsive design:** Test on mobile/tablet viewports - dropdown and tree remain usable, touch-friendly interaction areas
- **Error handling:** User-friendly error messages for API failures, invalid framework IDs, network issues
- **Future extensibility:** Component architecture supports potential future features like search/filter within schema tree, bookmarking specific data points, exporting schema documentation

**Decisions**

- **Single-page over two-page:** Simpler UX, reduces navigation overhead, keeps context visible (dropdown always present)
- **Custom expandable sections over PrimeVue Accordion:** Maintains consistency with existing MultiLayerDataTable pattern used throughout Dataland
- **Truncated definitions with toggle:** Balances information density with readability - users see overview and can drill down
- **URL query params:** Enables sharing specific frameworks, browser history navigation works naturally
- **Top-level expanded by default:** Business users see immediate value, nested sections prevent overwhelming information
- **Modal for technical details:** Separates business-focused primary view from developer/technical secondary information