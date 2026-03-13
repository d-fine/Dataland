---
name: component-patterns
description: Component structure, script setup, props, emits, and lifecycle patterns for Dataland
---

# Component Patterns

## ⚠️ Critical Requirements

1. **Always use `<script setup lang="ts">`** - TypeScript is mandatory
2. **Use Composition API for new components** - Options API is legacy
3. **Don't copy old code** - Assume old code doesn't follow current best practices
4. **Read and understand PrimeVue API** - Don't build unnecessary wrappers

## Component Structure

Standard structure for Dataland components:

```vue
<script setup lang="ts">
// 1. Imports
import { ref, computed, onMounted, inject } from 'vue'
import type Keycloak from 'keycloak-js'

// 2. Props & Emits
const props = defineProps<{
  // props here
}>()

const emit = defineEmits<{
  // events here
}>()

// 3. Injections (Keycloak, etc.)
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!

// 4. API Client Setup
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())

// 5. Reactive State
const data = ref([])
const isLoading = ref(false)

// 6. Computed Properties
const hasData = computed(() => data.value.length > 0)

// 7. Lifecycle Hooks
onMounted(async () => {
  await loadData()
})

// 8. Methods
async function loadData() {
  // implementation
}
</script>

<template>
  <!-- Template using PrimeVue components -->
</template>

<style scoped lang="scss">
/* Use scoped styles ONLY for structural layout */
/* Do NOT style PrimeVue components here */
/* Use Design Tokens (CSS variables) for styling */
.my-component {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: var(--p-content-padding);
}
</style>
```

## Props Definition

### Type-based Props (Recommended)

```ts
// Simple props
const props = defineProps<{
  specificationId: string
  isEditable?: boolean
  count?: number
}>()

// Complex types
const props = defineProps<{
  specification: FrameworkSpecification
  options?: SpecificationOptions
  filters: string[]
}>()

// Props with defaults (Vue 3.5+)
const { 
  isEditable = false,
  pageSize = 10
} = defineProps<{
  isEditable?: boolean
  pageSize?: number
}>()
```

### With Defaults (Vue 3.4 and below)

```ts
const props = withDefaults(defineProps<{
  title: string
  items?: string[]
  pageSize?: number
}>(), {
  items: () => [],
  pageSize: 10
})
```

## Emits Definition

Use named tuple syntax with typed payloads:

```ts
const emit = defineEmits<{
  // Event with payload
  update: [value: FrameworkSpecification]
  
  // Event with multiple parameters
  change: [id: string, value: string]
  
  // Event without payload
  close: []
  
  // Common patterns
  loaded: [count: number]
  error: [message: string]
  selected: [item: FrameworkSpecification | null]
}>()

// Usage
emit('update', specification)
emit('change', 'spec-1', 'new value')
emit('close')
```

## defineModel (Vue 3.4+)

For two-way binding:

```ts
// Basic v-model
const modelValue = defineModel<string>()
modelValue.value = 'new value'  // Emits "update:modelValue"

// Named v-model
const selectedId = defineModel<string>('selectedId')

// With default
const isVisible = defineModel<boolean>('visible', { default: false })
```

Parent usage:
```vue
<MyComponent v-model="searchText" />
<MyComponent v-model:selectedId="currentId" />
<MyComponent v-model:visible="dialogVisible" />
```

## defineExpose

Expose methods or properties to parent via template ref:

```ts
const internalData = ref([])

function refresh() {
  // refresh logic
}

function exportData() {
  return internalData.value
}

defineExpose({
  refresh,
  exportData
})
```

Parent usage:
```vue
<script setup lang="ts">
const childRef = ref()

function handleRefresh() {
  childRef.value?.refresh()
}
</script>

<template>
  <MyComponent ref="childRef" />
</template>
```

## Lifecycle Hooks

```ts
import {
  onBeforeMount,
  onMounted,
  onBeforeUpdate,
  onUpdated,
  onBeforeUnmount,
  onUnmounted
} from 'vue'

// Most common: fetch data on mount
onMounted(async () => {
  await loadInitialData()
})

// Cleanup subscriptions, timers, listeners
onUnmounted(() => {
  clearInterval(timerId)
  eventBus.off('update', handleUpdate)
})

// Less common: respond to updates
onUpdated(() => {
  // After DOM updates
})
```

## Watchers

```ts
import { ref, watch } from 'vue'

const specificationId = ref('')
const data = ref(null)

// Watch single source
watch(specificationId, async (newId, oldId) => {
  if (newId) {
    await fetchSpecification(newId)
  }
})

// Watch with options
watch(
  () => props.frameworkId,
  async (frameworkId) => {
    await loadFrameworkData(frameworkId)
  },
  { immediate: true }  // Run on mount
)

// Watch multiple sources
watch(
  [searchTerm, filters],
  ([term, filters]) => {
    performSearch(term, filters)
  }
)

// Deep watch (use sparingly)
watch(
  complexObject,
  (newVal) => {
    console.log('Complex object changed')
  },
  { deep: true }
)
```

## Provide/Inject Pattern

Used for passing data down the component tree without props:

```ts
// Parent provides
import { provide, ref } from 'vue'

const hideEmptyFields = ref(true)
const editMode = ref(false)

provide('hideEmptyFields', hideEmptyFields)
provide('editMode', editMode)

// Child injects
import { inject } from 'vue'
import type { Ref } from 'vue'

const hideEmptyFields = inject<Ref<boolean>>('hideEmptyFields', ref(false))
const editMode = inject<Ref<boolean>>('editMode', ref(false))

// Or with assertion if required
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!
```

## Conditional Rendering

```vue
<template>
  <!-- Loading state -->
  <div v-if="isLoading">Loading...</div>
  
  <!-- Error state -->
  <div v-else-if="errorMessage" class="error">
    {{ errorMessage }}
  </div>
  
  <!-- Data state -->
  <div v-else-if="hasData">
    <!-- Content -->
  </div>
  
  <!-- Empty state -->
  <div v-else>
    No data available
  </div>
</template>
```

## List Rendering

```vue
<template>
  <!-- Always use :key with unique identifier -->
  <div v-for="spec in specifications" :key="spec.id">
    {{ spec.name }}
  </div>
  
  <!-- With index (only if items have no unique id) -->
  <div v-for="(item, index) in items" :key="`item-${index}`">
    {{ item }}
  </div>
  
  <!-- DataTable handles keys internally -->
  <DataTable :value="items">
    <Column field="name" header="Name" />
  </DataTable>
</template>
```

## Component Registration

Components imported in `<script setup>` are automatically available in template:

```vue
<script setup lang="ts">
// Auto-registered, use PascalCase in template
import TheContent from '@/components/generics/TheContent.vue'
import SpecificationCard from '@/components/resources/SpecificationCard.vue'
import PrimeButton from 'primevue/button'
</script>

<template>
  <TheContent>
    <SpecificationCard />
    <PrimeButton label="Click" />
  </TheContent>
</template>
```
