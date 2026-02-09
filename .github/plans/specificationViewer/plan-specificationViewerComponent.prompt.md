# Plan: Specification Viewer for Business Users

This component enables authenticated business users (compliance, ESG managers) to explore framework specifications with their data point structures. It provides inline previews with expandable details, focusing on business definitions rather than technical implementation. The component is standalone (no links to framework data views) and uses the existing specification service API.

**Key Decisions:**
- **Authenticated access:** Follows standard Dataland Keycloak integration pattern
- **Business-focused:** Emphasize human-readable names and business definitions over technical schemas
- **Hybrid detail level:** Inline preview with expandable/modal details for data point types
- **Standalone:** Independent from framework data viewing (no cross-navigation)

**Steps**

1. **Generate TypeScript client for specification service**
   - Edit [dataland-frontend/build.gradle.kts](dataland-frontend/build.gradle.kts)
   - Add new task `generateSpecificationServiceClient` following the pattern of existing client tasks (e.g., `generateBackendClient`)
   - Point to OpenAPI spec: `dataland-specification-service/specificationServiceOpenApi.json`
   - Configure output to `build/clients/specificationservice`
   - Add task to `generateClients` dependencies

2. **Register specification service client in API infrastructure**
   - Edit [dataland-frontend/src/services/ApiClients.ts](dataland-frontend/src/services/ApiClients.ts)
   - Add `specificationController` property to `ApiClientProvider.apiClients` object
   - Configure factory with base path `/specifications`
   - Follow pattern used by other controllers (e.g., `backendController`)

3. **Create TypeScript types for parsed schema data**
   - Create new file `dataland-frontend/src/types/Specifications.ts`
   - Define `ParsedSchemaNode` interface for recursive tree structure
   - Define `DataPointReference` interface for leaf data point references
   - Import generated types from `@clients/specificationservice`

4. **Create utility for schema parsing**
   - Create `dataland-frontend/src/utils/SpecificationSchemaParser.ts`
   - Implement function to parse the JSON string schema into typed hierarchical object
   - Handle data point references expansion (id, ref, aliasExport fields)
   - Recursively traverse nested sections vs. leaf nodes

5. **Create composable for specification data fetching**
   - Create `dataland-frontend/src/composables/useSpecifications.ts`
   - Export `useFrameworkSpecifications()` for list view (calls `/specifications/frameworks`)
   - Export `useFrameworkSpecification(frameworkId)` for detail view
   - Export `useDataPointTypeDetails(dataPointTypeId)` for modal details
   - Include loading states, error handling, and caching logic

6. **Create overview page component**
   - Create [dataland-frontend/src/components/pages/SpecificationsOverview.vue](dataland-frontend/src/components/pages/SpecificationsOverview.vue)
   - Use PrimeVue `DataTable` to display all frameworks (from `/specifications/frameworks`)
   - Columns: framework name, business definition summary
   - Add `data-test="specifications-table"` attribute
   - Make rows clickable to navigate to detail view
   - Wrap in `TheContent` layout component

7. **Create framework specification detail page**
   - Create [dataland-frontend/src/components/pages/ViewFrameworkSpecification.vue](dataland-frontend/src/components/pages/ViewFrameworkSpecification.vue)
   - Fetch framework specification by route param `frameworkId`
   - Display framework metadata (name, business definition) in PrimeVue `Card`
   - Call `SpecificationSchemaTree` component to render schema hierarchy
   - Include loading spinner and error handling UI
   - Add breadcrumb navigation back to overview

8. **Create hierarchical schema tree component**
   - Create [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - Use PrimeVue `Accordion` with `:multiple="true"` for sections
   - Recursively render nested sections with self-references
   - For leaf data points: display name (aliasExport) in `AccordionTab` header
   - Show business definition inline in accordion content
   - Add "View Details" button to trigger data point type modal
   - Use `data-test` attributes for e2e testing

9. **Create data point type detail modal**
   - Create [dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue](dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue)
   - Use PrimeVue `Dialog` with `v-model:visible` binding
   - Display full data point type specification (from `/specifications/data-point-types/{id}`)
   - Show: name, business definition, base type, constraints, frameworks that use it
   - Include close button with `data-test="close-dialog"`
   - Format technical fields (constraints) in code blocks for readability

10. **Create framework metadata panel component**
    - Create [dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue](dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue)
    - Display framework ID, name, business definition
    - Show referenced report JSON path if present
    - Use PrimeVue `Card` with structured layout
    - Apply Design Tokens for spacing and colors (e.g., `var(--p-text-secondary-color)`)

11. **Add routing configuration**
    - Edit [dataland-frontend/src/router/index.ts](dataland-frontend/src/router/index.ts)
    - Add route: `{ path: '/specifications', name: 'Specifications Overview', component: SpecificationsOverview }`
    - Add route: `{ path: '/specifications/frameworks/:frameworkId', name: 'View Framework Specification', component: ViewFrameworkSpecification }`
    - Both routes require authentication (keep default setup)
    - Consider adding to main navigation menu if appropriate

12. **Style components following Dataland guidelines**
    - Use scoped styles ONLY for structural layout (flexbox, grid, padding, margins)
    - Use PrimeVue Design Tokens (`var(--p-primary-color)`, `var(--p-surface-0)`) for colors
    - Use PrimeIcons (NOT Material Icons) for icons (e.g., `pi pi-chevron-right`)
    - Use PrimeVue PassThrough API for component-specific styling (if needed)
    - NO `:deep()` selectors, NO hardcoded colors, NO PrimeFlex classes
    - Reference [.github/skills/dataland-vue-component/references/dataland-frontend-coding-guidelines.md](.github/skills/dataland-vue-component/references/dataland-frontend-coding-guidelines.md)

**Verification**

1. **Build verification:**
   - Run `./gradlew dataland-frontend:generateClients` to generate specification service client
   - Check `dataland-frontend/build/clients/specificationservice` for generated types
   - Run `./gradlew dataland-frontend:build` to ensure TypeScript compiles

2. **Functional testing:**
   - Navigate to `/specifications` - should see list of all frameworks
   - Click on a framework - should navigate to detail view
   - Expand accordion sections in schema tree - should show nested structure
   - Click "View Details" on a data point - should open modal with full details
   - Verify all data loads correctly (names, definitions, structure)

3. **Manual checks:**
   - Test with multiple frameworks (LKSG, SFDR, VSME, etc.)
   - Verify business definitions are prominent and readable
   - Check responsive behavior on mobile/tablet viewports
   - Verify loading states show during API calls
   - Verify error messages display if API fails
   - Check that all interactive elements have `data-test` attributes

4. **E2E test considerations:**
   - Selector `data-test="specifications-table"` for overview table
   - Selector `data-test="framework-specification-card"` for detail view
   - Selector `data-test="schema-tree"` for tree component
   - Selector `data-test="data-point-details-dialog"` for modal

**Decisions**

- **Authentication required:** Follows standard Dataland pattern with Keycloak injection
- **No framework data links:** Standalone viewer, focuses on specifications only
- **Hybrid detail level:** Inline preview (name + definition) with modal for full details
- **Business user focus:** Emphasize readable names and business definitions, minimize technical jargon
- **Use PrimeVue Accordion:** Better for hierarchical, self-documenting structure than Tree component
- **Composable pattern:** Centralize API logic for reusability and testability
- **Schema parsing utility:** Separate parsing logic from display logic for maintainability
