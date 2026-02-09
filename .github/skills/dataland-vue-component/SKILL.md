---
name: dataland-vue-component
description: Vue 3 Composition API patterns for Dataland project components. Use when creating new components that connect to backend services (especially specification service), using PrimeVue UI, and following Dataland's authentication and API patterns.
metadata:
  author: Dataland Team
  version: "2026.2.9"
  source: Adapted from antfu-vue skill for Dataland project specifics
  base: Vue 3.5, TypeScript, PrimeVue, Pinia, Keycloak
---

# Dataland Vue Component Development

> For Vue 3.5 components in the Dataland frontend. Always use Composition API with `<script setup lang="ts">`.

## Preferences

- **Always** use `<script setup lang="ts">` over Options API
- **Always** use TypeScript, never JavaScript
- For performance, prefer `shallowRef` over `ref` if deep reactivity is not needed
- **Read and understand the PrimeVue API** - PrimeVue provides everything you need
- Use PrimeVue components for UI (DataTable, Button, Dialog, etc.)
- **Do not build unnecessary component wrappers** - use PrimeVue's component API directly
- Follow Dataland authentication patterns with Keycloak injection
- Use `ApiClientProvider` for all backend API calls
- Create composables for reusable logic instead of utility functions with state
- **Do not use FormKit** - use PrimeVue form elements with `zod` for validation
- **Do not use PrimeFlex classes** - PrimeFlex is sunsetted
- **Do not use Material Icons** - use PrimeIcons instead

## Core Patterns

| Topic | Description | Reference |
|-------|-------------|-----------|
| Component Setup | `<script setup>`, defineProps, defineEmits, defineModel, component structure | [component-patterns](references/component-patterns.md) |
| API Integration | ApiClientProvider, Keycloak injection, error handling, loading states | [api-integration](references/api-integration.md) |
| PrimeVue Usage | DataTable, Dialog, Button, Form components, PassThrough API, Design Tokens | [primevue-usage](references/primevue-usage.md) |
| State Management | Pinia stores, reactive state, computed values | [state-management](references/state-management.md) |
| Styling Rules | Design Tokens, CSS variables, scoped styles guidelines, PassThrough API | [dataland-frontend-coding-guidelines](references/dataland-frontend-coding-guidelines.md) |
| Styling Rules | Design Tokens, CSS variables, scoped styles guidelines, PassThrough API | [dataland-frontend-coding-guidelines](references/dataland-frontend-coding-guidelines.md) |

## Quick Start Template

### Basic Component Connecting to Specification Service

```vue
<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import type Keycloak from 'keycloak-js'
import { ApiClientProvider } from '@/services/ApiClients'
import { assertDefined } from '@/utils/TypeScriptUtils'
import TheContent from '@/components/generics/TheContent.vue'
import PrimeButton from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

// Props with TypeScript
const props = defineProps<{
  frameworkId?: string
}>()

// Events
const emit = defineEmits<{
  loaded: [count: number]
  error: [message: string]
}>()

// Keycloak injection (Dataland standard pattern)
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())

// Reactive state - use shallowRef for large data arrays
const specifications = ref<FrameworkSpecification[]>([])
const isLoading = ref(false)
const errorMessage = ref<string>()

// Computed values
const hasData = computed(() => specifications.value.length > 0)
const displayCount = computed(() => `${specifications.value.length} specifications`)

// Lifecycle
onMounted(async () => {
  await loadSpecifications()
})

// Methods
async function loadSpecifications() {
  try {
    isLoading.value = true
    errorMessage.value = undefined
    
    // API call via ApiClientProvider
    const response = await apiClientProvider.apiClients.specificationController
      .getFrameworkSpecifications()
    
    specifications.value = response.data
    emit('loaded', specifications.value.length)
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to load specifications'
    errorMessage.value = message
    emit('error', message)
  } finally {
    isLoading.value = false
  }
}

async function handleRefresh() {
  await loadSpecifications()
}
</script>

<template>
  <TheContent>
    <div class="specification-component">
      <div class="header-section">
        <h1>Specifications</h1>
        <PrimeButton
          label="Refresh"
          icon="pi pi-refresh"
          :loading="isLoading"
          @click="handleRefresh"
        />
      </div>

      <DataTable
        v-if="hasData"
        :value="specifications"
        :loading="isLoading"
        paginator
        :rows="10"
        data-test="specifications-table"
      >
        <Column field="id" header="ID" sortable />
        <Column field="name" header="Name" sortable />
      </DataTable>

      <div v-else-if="!isLoading && !errorMessage" class="empty-state">
        No specifications available
      </div>

      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
  </TheContent>
</template>

<style scoped lang="scss">
.specification-component {
  // Structural layout only
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: 1rem;
  
  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;
  }

  .empty-state {
    text-align: center;
    padding: 2rem;
    color: var(--p-text-secondary-color); // Use Design Token
  }

  .error-message {
    padding: 1rem;
    border-radius: var(--p-border-radius); // Use Design Token
    color: var(--p-danger-color); // Use Design Token
    background: var(--p-danger-50); // Use Design Token
  }
}
</style>
```

## Key Dataland Patterns

### 1. Authentication & API Setup

```ts
// Always inject Keycloak for authenticated components
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!

// Create API client provider
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())

// Use specific controller
const result = await apiClientProvider.apiClients.specificationController.getSomething()
```

### 2. Loading & Error States

```ts
const isLoading = ref(false)
const errorMessage = ref<string>()

async function fetchData() {
  try {
    isLoading.value = true
    errorMessage.value = undefined
    // API call
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Unknown error'
  } finally {
    isLoading.value = false
  }
}
```

### 3. PrimeVue Components

```vue
<template>
  <!-- Buttons with icons and loading states -->
  <PrimeButton label="Save" icon="pi pi-check" :loading="isSaving" />
  
  <!-- Data tables with pagination -->
  <DataTable :value="items" paginator :rows="10" :loading="isLoading">
    <Column field="name" header="Name" sortable />
  </DataTable>
  
  <!-- Dialogs -->
  <Dialog v-model:visible="showDialog" header="Details" modal>
    <!-- Content -->
  </Dialog>
</template>
```

### 4. Prefer shallowRef for Performance

```ts
// For large data arrays or complex objects where only top-level changes matter
const specifications = shallowRef<FrameworkSpecification[]>([])

// Update entire array, not push/splice
specifications.value = [...specifications.value, newItem]

// For simple reactive values, use ref
const isLoading = ref(false)
const selectedId = ref<string>()
```

## Common Imports

```ts
// Vue core
import { ref, shallowRef, computed, watch, onMounted, inject } from 'vue'
import type { Ref } from 'vue'

// Dataland utilities
import { ApiClientProvider } from '@/services/ApiClients'
import { assertDefined } from '@/utils/TypeScriptUtils'
import type Keycloak from 'keycloak-js'

// Layout components
import TheContent from '@/components/generics/TheContent.vue'
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue'

// PrimeVue (import individually, read the API docs)
import PrimeButton from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'

// Form validation (use zod, NOT FormKit)
import { z } from 'zod'

// Type imports
import type { FrameworkSpecification } from '@clients/specificationservice'
```

## Styling Guidelines

**Critical:** Read [dataland-frontend-coding-guidelines.md](references/dataland-frontend-coding-guidelines.md) for complete styling rules.

**Quick Rules:**
- ✅ Use Design Tokens (CSS variables) for styling
- ✅ Use scoped styles ONLY for structural layout (flex, grid, padding, margin)
- ✅ Use PassThrough API for single component customization
- ❌ Don't use `:deep()` selectors
- ❌ Don't style PrimeVue components in scoped styles
- ❌ Don't hardcode colors, use `var(--p-primary-color)` etc.

```vue
<style scoped lang="scss">
/* ✅ GOOD: Structural layout only */
.my-layout {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: var(--p-content-padding);
  background: var(--p-surface-0);
}

/* ❌ BAD: Don't style PrimeVue components */
/* .my-layout :deep(.p-button) { ... } */
```

## Testing Attributes

Always add `data-test` attributes for e2e tests:

```vue
<template>
  <PrimeButton data-test="save-button" @click="save" />
  <DataTable data-test="specifications-table" :value="specs" />
  <div data-test="error-indicator" v-if="hasError">Error</div>
</template>
```

## Do Not

- ❌ Don't use Options API (`defineComponent` with options) for **new** components
- ❌ Don't use `reactive()` for props or complex state (use `ref`/`shallowRef`)
- ❌ Don't make API calls without proper error handling
- ❌ Don't forget loading states for async operations
- ❌ Don't hardcode API base URLs (use ApiClientProvider)
- ❌ Don't forget `data-test` attributes for interactive elements
- ❌ **Don't use FormKit** - use PrimeVue form elements instead
- ❌ **Don't use PrimeFlex classes** - PrimeFlex is sunsetted
- ❌ **Don't use Material Icons** - use PrimeIcons instead
- ❌ **Don't use `:deep()` selectors** - use PrimeVue's PassThrough API
- ❌ **Don't access PrimeVue CSS classes on local scope** - modify globally via presets
- ❌ **Don't use scoped styles for component styling** - only for structural layout
- ❌ **Don't build unnecessary component wrappers** - use PrimeVue API directly
- ❌ **Don't copy old code** - assume old code doesn't follow current best practices
- ❌ **Don't use FormKit** - use PrimeVue form elements instead
- ❌ **Don't use PrimeFlex classes** - PrimeFlex is sunsetted
- ❌ **Don't use Material Icons** - use PrimeIcons instead
- ❌ **Don't use `:deep()` selectors** - use PrimeVue's PassThrough API
- ❌ **Don't access PrimeVue CSS classes on local scope** - modify globally via presets
- ❌ **Don't use scoped styles for component styling** - only for structural layout
- ❌ **Don't build unnecessary component wrappers** - use PrimeVue API directly
- ❌ **Don't copy old code** - assume old code doesn't follow current best practices
