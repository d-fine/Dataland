# Plan: Transform Specification Viewer to Business-Friendly Display

> **Note:** This plan follows the [dataland-vue-component](.github/skills/dataland-vue-component/SKILL.md) skill patterns throughout. All component modifications use Vue 3 Composition API with `<script setup lang="ts">`, PrimeVue components, Design Tokens for styling, and Dataland API patterns with ApiClientProvider.

**TL;DR:** The current specification viewer structure (dropdown, metadata panel, recursive tree) is sound, but displays technical IDs prominently and uses wrong fields for descriptions. This plan transforms it into a business-friendly explorer by: fixing the business definition bug, showing human-readable data type labels alongside technical IDs, humanizing display names, and optionally batch-loading actual descriptions. Business users will see clear data model structure with readable names while preserving technical details for power users.

**Key Issues Found:**
- **Critical bug:** Business definition shows `aliasExport` ("COMPANY_NAME") instead of actual descriptions
- Technical data point type IDs (`plainDateSfdrDataDate`) displayed as the only identifier throughout tree
- No plain-English data type labels (shows only `plainDate` without "Date" context)
- Framework metadata shows API URLs and JSON paths (too technical, but framework ID itself is useful)
- Missing actual business definitions in tree view (only available via detail endpoint)

**Decisions Made:**
- **Keep recursive tree structure** - Expandable sections work well for hierarchy exploration
- **Two-phase approach** - Phase 1 fixes display bugs and adds human-readable labels (minimal changes); Phase 2 adds optional batch loading for full business definitions (enhancement)
- **Humanize `aliasExport`** - Transform "COMPANY_NAME" → "Company Name" for display
- **Show BOTH readable + technical data types** - Display "Date (plainDate)" or "Text (plainString)" - business users see purpose, technical users see exact type
- **Keep framework ID visible** - Useful identifier for technical users, remove only API URLs and JSON paths
- **Move detailed technical info to modal** - Tree view shows overview with both readable and technical labels
- **Preserve existing composable architecture** - Minimal changes to data flow and state management
- **Follow Dataland skill patterns** - All changes use PrimeVue components, Design Tokens, Composition API, no PrimeFlex/Material Icons

**Steps**

### Phase 1: Fix Display Issues & Add Human-Readable Labels (Essential Changes)

1. **Add base type humanization utility**
   - Edit [dataland-frontend/src/utils/StringFormatter.ts](dataland-frontend/src/utils/StringFormatter.ts)
   - Add function `humanizeDataPointBaseType(baseTypeId: string): string`
   - **Skill compliance:** Pure utility function, no Vue-specific code
   - Map technical base type IDs to user-friendly labels:
     - `plainString` → "Text"
     - `plainDate` → "Date"
     - `plainInteger`, `plainDecimal`, `extendedDecimal` → "Number"
     - `plainBoolean` → "Yes/No"
     - `extendedEnum`, `plainEnum` → "Selection"
     - `extendedArray` → "List"
     - Default: humanize the ID using existing `humanizeStringOrNumber()` ("plaintextDocument" → "Plaintext Document")
   - Return format: Just the human-readable string (combining with technical ID happens in components)
   - Include JSDoc with example mappings and usage notes

2. **Fix business definition display bug in tree component**
   - Edit [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **Skill compliance:** Component already uses `<script setup lang="ts">`, PrimeVue Button, maintains pattern
   - **Current issue:** Line 193 uses `node.aliasExport` as business definition (shows "COMPANY_NAME")
   - **Fix:** Remove the business definition section entirely from data point display (lines 189-204)
   - Keep data point name display but improve it: 
     - Import `humanizeStringOrNumber` from `@/utils/StringFormatter`
     - Use `humanizeStringOrNumber(node.aliasExport || node.key)` for display
     - Transforms: `"COMPANY_NAME"` → `"Company Name"`, `"dataDate"` → `"Data Date"`
   - Remove "Show more/Show less" toggle logic (lines 205-213) since no definition to expand
   - Remove associated functions: `toggleDefinition()`, `isDefinitionExpanded()`, `needsTruncation()`, `getDefinitionDisplay()`
   - Business definitions will be shown only in the detail modal where actual descriptions are available

3. **Add human-readable + technical data type display to tree**
   - Edit [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **Keep** the technical type ID section (lines 210-214), but enhance it
   - Import `humanizeDataPointBaseType` from `@/utils/StringFormatter`
   - Extract base type prefix from `dataPointTypeId`:
     - `"plainDateSfdrDataDate"` → extract `"plainDate"`
     - Use regex: `/^(plain[A-Z][a-z]+|extended[A-Z][a-z]+)/` to capture prefix
     - If no match, show just technical ID
   - Add computed function `getDataTypeDisplay(dataPointTypeId: string): { readable: string, technical: string }`
   - Update template to show:
     ```vue
     <div class="data-point-type">
       <span class="type-label">Type:</span>
       <span class="type-readable">{{ getDataTypeDisplay(node.dataPointTypeId).readable }}</span>
       <span class="type-technical">({{ node.dataPointTypeId }})</span>
     </div>
     ```
   - **Styling with Design Tokens:**
     - `.type-readable`: `color: var(--p-text-color)`, normal weight
     - `.type-technical`: `color: var(--p-text-secondary-color)`, smaller font, monospace
   - **Skill compliance:** Using only PrimeVue Design Tokens, scoped styles for layout only

4. **Add icons to data point cards based on type**
   - Edit [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **Skill compliance:** Use only PrimeIcons (no Material Icons)
   - Add function `getDataPointIcon(dataPointTypeId: string): string`
   - Map based on base type prefix:
     - Date types: `"pi pi-calendar"`
     - String types: `"pi pi-align-left"`
     - Number types: `"pi pi-hashtag"`
     - Boolean types: `"pi pi-check-circle"`
     - Array types: `"pi pi-list"`
     - Enum types: `"pi pi-th-large"`
     - Default: `"pi pi-circle"`
   - Update data point card template:
     ```vue
     <div class="data-point-header">
       <i :class="getDataPointIcon(node.dataPointTypeId)" class="data-point-icon" aria-hidden="true"></i>
       <span class="data-point-name" :data-test="'datapoint-name'">
         {{ humanizeStringOrNumber(node.aliasExport || node.key) }}
       </span>
       <PrimeButton ... />
     </div>
     ```
   - **Styling:** Icon uses `var(--p-primary-color)`, positioned with flexbox gap

5. **Update framework metadata panel for balance**
   - Edit [dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue](dataland-frontend/src/components/resources/specifications/FrameworkMetadataPanel.vue)
   - **Skill compliance:** Component uses `<script setup lang="ts">`, PrimeVue Card
   - **Keep** these sections:
     - Framework Name (title)
     - Business Definition (prominent)
     - Framework ID (useful technical identifier - line 32-34)
   - **Remove** these sections (too technical):
     - Referenced Report Path (line 37-40)
     - API Reference link (line 43-53)
   - Result: Balanced view with business context (name, definition) and technical identifier (ID)
   - **Consider adding:** Framework icon from PrimeIcons (`pi-book` or framework-specific icon if mapping exists)

6. **Update section header styling for clarity**
   - Edit [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **Skill compliance:** Use ONLY Design Tokens, scoped styles ONLY for structural layout
   - Increase visual hierarchy in scoped styles:
     - Top-level sections: larger font size via class on element
     - Use `gap` and `padding` for spacing (structural CSS only)
   - Add hover effect using Design Token: `background: var(--p-surface-hover)` on `.section-header:hover`
   - Add left border accent for expanded sections: `border-left: 3px solid var(--p-primary-color)`
   - **DO NOT use `:deep()`** - all styling should be on custom classes, not PrimeVue internals
   - Maintain existing ARIA attributes and keyboard navigation (already implemented correctly)

### Phase 2: Enhance with Full Business Definitions (Optional Enhancement)

7. **Extend parsed schema types to include enriched data**
   - Edit [dataland-frontend/src/types/Specifications.ts](dataland-frontend/src/types/Specifications.ts)
   - **Skill compliance:** Pure TypeScript types, imported from generated clients
   - Add optional fields to `ParsedDataPoint` interface:
     ```typescript
     export interface ParsedDataPoint {
       // ... existing fields ...
       // Optional enriched fields from batch loading
       businessDefinition?: string;  // Actual description from DataPointTypeSpecification API
       dataPointName?: string;        // Human-readable name from API (may differ from aliasExport)
       baseTypeId?: string;           // Base type ID for display (e.g., "plainDate")
     }
     ```
   - Add JSDoc comment explaining these fields are only populated when batch loading is enabled in Phase 2
   - Keep backward compatible - all new fields are optional

8. **Add batch data point loading to specifications composable**
   - Edit [dataland-frontend/src/composables/useSpecifications.ts](dataland-frontend/src/composables/useSpecifications.ts)
   - **Skill compliance:** Composable pattern with dependency injection (fetch functions as parameters)
   - Add new option to `UseSpecificationsOptions` interface:
     ```typescript
     interface UseSpecificationsOptions {
       fetchFrameworks: () => Promise<Framework[]>;
       fetchSpecification: (id: string) => Promise<FrameworkSpecification>;
       fetchDataPointDetails?: (id: string) => Promise<DataPointTypeSpecification>;  // NEW
       enableBatchDataPointLoading?: boolean;  // NEW - default false
     }
     ```
   - After parsing schema in `selectFramework()`, if `enableBatchDataPointLoading` is true:
     - Extract all unique `dataPointTypeId` values from `parsedSchema` (recursive traversal)
     - Batch fetch in groups of 10-15 using `Promise.all()` to avoid API overload
     - Enrich `ParsedDataPoint` nodes with: `businessDefinition`, `dataPointName`, `baseTypeId` (from `dataPointBaseType.id`)
   - Add new reactive state: `isLoadingDataPointDetails: Ref<boolean>`
   - Expose in return: `isLoadingDataPointDetails`
   - **Error handling:** If batch loading fails partially, log warnings but continue (degrade gracefully)
   - **Performance:** Use sequential batches, not all parallel (e.g., load 15, wait, load next 15)

9. **Display enriched data when available in tree**
   - Edit [dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue](dataland-frontend/src/components/resources/specifications/SpecificationSchemaTree.vue)
   - **Skill compliance:** Use PrimeVue components, Design Tokens for styling
   - Update data point card to conditionally show business definition:
     ```vue
     <!-- Business Definition (if loaded via batch) -->
     <div v-if="node.businessDefinition" class="data-point-definition">
       <p class="definition-text">
         {{ getDefinitionDisplay(node.id, node.businessDefinition) }}
       </p>
       <button
         v-if="needsTruncation(node.businessDefinition)"
         class="definition-toggle"
         @click="toggleDefinition(node.id)"
       >
         {{ isDefinitionExpanded(node.id) ? 'Show less' : 'Show more' }}
       </button>
     </div>
     ```
   - Restore truncation logic (removed in step 2) but make it conditional on enriched data
   - Update type display to use `node.baseTypeId` if available:
     ```vue
     <span class="type-readable">{{ humanizeDataPointBaseType(node.baseTypeId || extractBaseType(node.dataPointTypeId)) }}</span>
     ```
   - **Styling:** Definition text uses `var(--p-text-secondary-color)`, smaller font, subtle appearance

10. **Enable batch loading in main viewer component**
    - Edit [dataland-frontend/src/components/pages/SpecificationsViewer.vue](dataland-frontend/src/components/pages/SpecificationsViewer.vue)
    - **Skill compliance:** Already follows pattern with ApiClientProvider and Keycloak injection
    - Update `useSpecifications` initialization:
      ```typescript
      const {
        frameworks,
        selectedFramework,
        isLoadingFrameworks,
        isLoadingSpecification,
        isLoadingDataPointDetails,  // NEW
        error,
        loadFrameworks,
        selectFramework,
      } = useSpecifications({
        fetchFrameworks: () => apiClientProvider.apiClients.specificationController.listFrameworkSpecifications().then(r => r.data),
        fetchSpecification: (id) => apiClientProvider.apiClients.specificationController.getFrameworkSpecification(id).then(r => r.data),
        fetchDataPointDetails: (id) => apiClientProvider.apiClients.specificationController.getDataPointTypeSpecification(id).then(r => r.data),  // NEW
        enableBatchDataPointLoading: true,  // NEW
      });
      ```
    - Add loading indicator in template after schema tree:
      ```vue
      <div v-if="isLoadingDataPointDetails" class="loading-details">
        <ProgressSpinner class="spinner-inline" />
        <span>Loading detailed descriptions...</span>
      </div>
      ```
    - **Styling:** Use Design Tokens for colors, flexbox for layout (structural CSS only)

11. **Optimize batch loading performance**
    - Edit [dataland-frontend/src/composables/useSpecifications.ts](dataland-frontend/src/composables/useSpecifications.ts)
    - **Skill compliance:** Use `shallowRef` for cache Map (better performance)
    - Implement batching helper function:
      ```typescript
      async function batchLoadDataPointDetails(
        dataPointIds: string[],
        fetchFn: (id: string) => Promise<DataPointTypeSpecification>,
        batchSize: number = 15
      ): Promise<Map<string, DataPointTypeSpecification>> {
        const results = new Map();
        for (let i = 0; i < dataPointIds.length; i += batchSize) {
          const batch = dataPointIds.slice(i, i + batchSize);
          const batchResults = await Promise.allSettled(
            batch.map(id => fetchFn(id))
          );
          batchResults.forEach((result, index) => {
            if (result.status === 'fulfilled') {
              results.set(batch[index], result.value);
            } else {
              console.warn(`Failed to load data point ${batch[index]}:`, result.reason);
            }
          });
        }
        return results;
      }
      ```
    - Add session cache: `const dataPointCache = shallowRef<Map<string, DataPointTypeSpecification>>(new Map())`
    - Check cache before fetching, store results after fetching
    - Add timeout per batch: Use `Promise.race()` with timeout promise (10 seconds)

12. **Update modal to show human-readable + technical data types**
    - Edit [dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue](dataland-frontend/src/components/resources/specifications/DataPointTypeDetailsDialog.vue)
    - **Skill compliance:** Uses PrimeVue Dialog, Composition API, Design Tokens
    - Import `humanizeDataPointBaseType` from `@/utils/StringFormatter`
    - Restructure modal content into sections:
      - **Overview** (always visible):
        - Name (title)
        - Business Definition (prominent paragraph)
        - Data Type display:
          ```vue
          <div class="data-type-display">
            <span class="type-primary">{{ humanizeDataPointBaseType(dataPointDetails.dataPointBaseType.id) }}</span>
            <span class="type-secondary">({{ dataPointDetails.dataPointBaseType.id }})</span>
          </div>
          ```
      - **Technical Details** (use PrimeVue Accordion, default collapsed):
        - Data Point Type ID
        - Base Type ID with link
        - Constraints (formatted)
        - Frameworks using this data point
        - API Reference
    - **Styling with Design Tokens:**
      - `.type-primary`: Larger font, `color: var(--p-text-color)`, semibold
      - `.type-secondary`: Smaller, `color: var(--p-text-secondary-color)`, monospace
    - Use PrimeVue `Accordion` component (not custom collapsible):
      ```vue
      <Accordion :active-index="undefined">
        <AccordionPanel header="Technical Details">
          <template #headericon><i class="pi pi-code"></i></template>
          <!-- Technical fields here -->
        </AccordionPanel>
      </Accordion>
      ```
    - **Skill compliance:** Using PrimeVue Accordion directly, not building custom wrapper

**Verification**

1. **Skill compliance audit:**
   - All modified components use `<script setup lang="ts">` ✓ (already implemented)
   - All styling uses Design Tokens (`var(--p-*)`) - NO hardcoded colors
   - All icons use PrimeIcons (`pi pi-*`) - NO Material Icons
   - NO PrimeFlex classes used anywhere
   - NO `:deep()` selectors in scoped styles
   - Scoped styles contain ONLY structural CSS (flex, grid, padding, margin, gap, positioning)
   - PrimeVue components used directly via their API (Accordion, Button, Dialog, Select, etc.)
   - ApiClientProvider pattern followed for all API calls
   - Composables use dependency injection (fetch functions as parameters)

2. **Visual regression testing:**
   - Navigate to `/specifications` and select "SFDR" framework
   - Verify framework metadata panel shows: Framework name + business definition + Framework ID (three items)
   - Verify NO API URLs or JSON paths visible in metadata panel
   - Verify schema tree sections display with clean labels and clear hierarchy
   - Verify data points show: Icon + Humanized name + Type display + "View Details" button
   - Type display format: "Date (plainDateSfdrDataDate)" - readable label FIRST, technical ID in parentheses
   - Expand nested sections - verify clean display throughout tree

3. **Business definition fix verification:**
   - Phase 1: Data points show NO business definition (removed incorrect one)
   - Click "View Details" on any data point - modal shows correct business definition from API
   - Phase 2 (if implemented): Data points in tree show truncated business definitions with "Show more" toggle
   - Business definitions in tree match those shown in detail modal

4. **Data type display verification:**
   - Each data point in tree shows BOTH readable and technical type:
     - Example: "Date (plainDateSfdrDataDate)"
     - Example: "Number (extendedDecimalScope1GhgEmissionsInTonnes)"
     - Example: "Selection (extendedEnumFiscalYearDeviation)"
   - Readable label is prominent, technical ID is de-emphasized (smaller, secondary color)
   - In detail modal: Same pattern with larger fonts
   - Verify icon matches data type (calendar for dates, hashtag for numbers, etc.)

5. **Framework ID visibility verification:**
   - Framework metadata panel displays Framework ID
   - ID is in monospace font (appropriate for technical identifier)
   - ID is NOT hidden or removed
   - API URLs and JSON paths are NOT visible (removed)

6. **Icon and humanization verification:**
   - Data point names displayed as "Company Name", "Data Date", etc. (humanized from "COMPANY_NAME", "DATA_DATE")
   - Section labels displayed as "General Information", "Environmental Data" (readable capitalization)
   - Icons displayed next to each data point name, appropriate to data type
   - All icons are PrimeIcons (verify in DevTools: class names start with "pi pi-")

7. **Phase 2 batch loading verification** (if implemented):
   - Select framework with many data points (e.g., SFDR with 50+ data points)
   - Observe loading indicator: "Loading detailed descriptions..." appears with spinner
   - After loading, data points in tree show business definitions (not "COMPANY_NAME" text)
   - "Show more" toggle expands full definitions
   - Check browser DevTools Network tab: Verify batched requests (not 100+ simultaneous)
   - Verify reasonable performance: <10 seconds for frameworks with 100+ data points

8. **Performance and UX testing:**
   - Phase 1 only: Framework loads in <2 seconds
   - Phase 2 with batch loading: Full framework with descriptions loads in <10 seconds
   - No UI freezing during loading
   - Smooth scrolling through large schema trees
   - Dropdown remains responsive while specification loads (non-blocking)

9. **Accessibility validation:**
   - Keyboard navigation: Tab through all interactive elements
   - Section expand/collapse with Enter/Space keys
   - Screen reader announces: Section names, data point names, both readable and technical type labels
   - Icons have `aria-hidden="true"` (decorative only)
   - Loading states announced via implicit ARIA live regions (PrimeVue components handle this)
   - Modal has focus trap, closes with Escape key

10. **User comprehension test:**
    - Non-technical user can understand what type of data each field requires (Date, Text, Number, etc.)
    - Technical user can still see exact data point type IDs for API integration
    - Framework ID provides useful reference for technical users
    - Balance achieved: Business clarity without losing technical precision

**Technical Considerations**

- **Skill-compliant styling:** ALL styling uses Design Tokens exclusively - `var(--p-primary-color)`, `var(--p-text-color)`, `var(--p-surface-card)`, etc.
- **Skill-compliant components:** Use PrimeVue Accordion, Button, Dialog, ProgressSpinner directly via their API - no custom wrappers
- **Performance:** `shallowRef` used for data point cache Map (better performance per skill guidance)
- **Icon library:** ONLY PrimeIcons used - no Material Icons anywhere in implementation
- **Layout styling:** Scoped styles ONLY for structural layout properties (display, flex, grid, padding, margin, gap) - NO color, font-size, background in scoped styles (use Design Tokens on elements instead)
- **PrimeVue customization:** If needed, use PassThrough API - NO `:deep()` selectors
- **Backward compatibility:** Phase 1 changes are pure display improvements, no API contract changes
- **Error resilience:** Batch loading errors handled gracefully - partial failure doesn't block UI
- **Caching strategy:** Session-level cache for data point details, cleared on page refresh
- **Mobile responsiveness:** Test on tablet/mobile viewports, ensure 44px minimum touch targets for buttons

**Decisions**

- **Show both readable AND technical types:** Business users see purpose ("Date"), technical users see exact type ("plainDateSfdrDataDate") - best of both worlds
- **Keep framework ID visible:** Useful technical reference, not overwhelming like full API URLs
- **Remove JSON paths and API URLs:** Too technical for overview panel, available in detail modal if needed
- **Humanize aliasExport in-place:** Use existing utility functions, no API changes required
- **Phase 1 removes incorrect definitions, Phase 2 adds correct ones:** Avoid showing wrong information while providing upgrade path
- **Use PrimeVue Accordion for collapsible technical details:** Follow skill guidance - use PrimeVue components directly, don't build custom solutions
- **Batch size of 10-15 data points:** Balance between API load and performance based on typical framework sizes
- **Icon placement before name:** Standard UI pattern, provides visual categorization
- **Follow dataland-vue-component skill throughout:** Ensures consistency with existing codebase, maintainability, and compliance with team standards
