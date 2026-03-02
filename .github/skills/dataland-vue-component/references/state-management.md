---
name: state-management
description: Reactive state management with ref, shallowRef, computed, and Pinia stores
---

# State Management

## Local Component State

### ref vs shallowRef

```ts
import { ref, shallowRef } from 'vue'

// Use ref for:
// - Primitive values
// - Simple objects where nested reactivity is needed
const count = ref(0)
const user = ref({ name: 'John', age: 30 })
user.value.age = 31  // Reactive

// Use shallowRef for:
// - Large arrays or objects
// - Data where only top-level changes matter (better performance)
const specifications = shallowRef<FrameworkSpecification[]>([])

// Updating shallowRef - replace entire value
specifications.value = [...specifications.value, newSpec]  // Reactive
specifications.value.push(newSpec)  // NOT reactive

// When to use shallowRef:
// ✅ Large data tables
// ✅ API response data
// ✅ Long lists
// ❌ Forms with nested object updates
// ❌ Small reactive objects
```

### Computed Properties

```ts
import { ref, computed } from 'vue'

const specifications = ref<FrameworkSpecification[]>([])
const searchTerm = ref('')

// Read-only computed
const hasData = computed(() => specifications.value.length > 0)

const filteredSpecs = computed(() => {
  if (!searchTerm.value) return specifications.value
  
  return specifications.value.filter(spec =>
    spec.name.toLowerCase().includes(searchTerm.value.toLowerCase())
  )
})

// Writable computed
const firstSpecName = computed({
  get: () => specifications.value[0]?.name ?? '',
  set: (value) => {
    if (specifications.value[0]) {
      specifications.value[0].name = value
    }
  }
})

// Complex computed with multiple dependencies
const summary = computed(() => {
  const total = specifications.value.length
  const active = specifications.value.filter(s => s.isActive).length
  return `${active} of ${total} active`
})
```

### Reactive References

```ts
// Don't destructure props - loses reactivity
const props = defineProps<{ userId: string }>()
// ❌ const { userId } = props  // Not reactive

// Instead:
// ✅ Use props.userId directly
// ✅ Or use toRef for destructuring
import { toRef } from 'vue'
const userId = toRef(props, 'userId')

// For reactive object properties
import { toRefs } from 'vue'
const { userId, companyId } = toRefs(props)
```

## Pinia Stores

### Store Definition (Options API Style - Current Dataland Pattern)

```ts
// src/stores/SpecificationStore.ts
import { defineStore } from 'pinia'
import type { FrameworkSpecification } from '@clients/specificationservice'

type SpecificationState = {
  specifications: FrameworkSpecification[]
  selectedId: string | undefined
  isLoading: boolean
}

export const useSpecificationStore = defineStore('specificationStore', {
  state: (): SpecificationState => {
    return {
      specifications: [],
      selectedId: undefined,
      isLoading: false
    }
  },
  
  getters: {
    selectedSpecification: (state) => {
      return state.specifications.find(s => s.id === state.selectedId)
    },
    
    activeSpecifications: (state) => {
      return state.specifications.filter(s => s.isActive)
    },
    
    specificationCount: (state) => state.specifications.length
  },
  
  actions: {
    setSpecifications(specifications: FrameworkSpecification[]) {
      this.specifications = specifications
    },
    
    addSpecification(specification: FrameworkSpecification) {
      this.specifications.push(specification)
    },
    
    selectSpecification(id: string) {
      this.selectedId = id
    },
    
    clearSelection() {
      this.selectedId = undefined
    }
  }
})
```

### Store Definition (Composition API Style - Modern Alternative)

```ts
// More aligned with antfu-vue skill recommendations
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { FrameworkSpecification } from '@clients/specificationservice'

export const useSpecificationStore = defineStore('specificationStore', () => {
  // State
  const specifications = ref<FrameworkSpecification[]>([])
  const selectedId = ref<string>()
  const isLoading = ref(false)
  
  // Getters (computed)
  const selectedSpecification = computed(() => 
    specifications.value.find(s => s.id === selectedId.value)
  )
  
  const activeSpecifications = computed(() =>
    specifications.value.filter(s => s.isActive)
  )
  
  const specificationCount = computed(() => specifications.value.length)
  
  // Actions (functions)
  function setSpecifications(newSpecs: FrameworkSpecification[]) {
    specifications.value = newSpecs
  }
  
  function addSpecification(spec: FrameworkSpecification) {
    specifications.value.push(spec)
  }
  
  function selectSpecification(id: string) {
    selectedId.value = id
  }
  
  function clearSelection() {
    selectedId.value = undefined
  }
  
  return {
    // State
    specifications,
    selectedId,
    isLoading,
    // Getters
    selectedSpecification,
    activeSpecifications,
    specificationCount,
    // Actions
    setSpecifications,
    addSpecification,
    selectSpecification,
    clearSelection
  }
})
```

### Using Stores in Components

```vue
<script setup lang="ts">
import { useSpecificationStore } from '@/stores/SpecificationStore'
import { storeToRefs } from 'pinia'

const specStore = useSpecificationStore()

// Destructure reactive state and getters
const { specifications, isLoading, selectedSpecification } = storeToRefs(specStore)

// Actions can be destructured directly (not reactive)
const { selectSpecification, clearSelection } = specStore

// Or call actions on store
function handleSelect(id: string) {
  specStore.selectSpecification(id)
}

// Update state via actions
onMounted(() => {
  specStore.setSpecifications(initialData)
})
</script>

<template>
  <div v-if="isLoading">Loading...</div>
  <DataTable :value="specifications" @row-select="handleSelect">
    <Column field="name" header="Name" />
  </DataTable>
</template>
```

## Shared State Across Tabs (Pinia Shared State)

For state that should sync across browser tabs:

```ts
import { defineStore } from 'pinia'
import { share } from 'pinia-shared-state'

export const useSharedSessionStateStore = defineStore('sharedSessionStateStore', {
  state: () => ({
    refreshToken: undefined as string | undefined,
    refreshTokenExpiryTimestampInMs: undefined as number | undefined
  }),
  
  share: {
    // Enable sharing across tabs
    enable: true,
    // Which properties to share
    initialize: true
  }
})

// Already configured in Dataland, use as is
```

## When to Use What

### Component State (ref/shallowRef)

✅ Use for:
- UI state (open/closed, selected item)
- Form data
- Component-specific state
- Temporary state

```ts
const isDialogVisible = ref(false)
const formData = ref({ name: '', email: '' })
const localSpecifications = ref<FrameworkSpecification[]>([])
```

### Pinia Store

✅ Use for:
- Shared state across multiple components
- Complex state management
- State that needs to persist across navigation
- Global application state

```ts
const userStore = useUserStore()
const specStore = useSpecificationStore()
```

### Props

✅ Use for:
- Parent-to-child communication
- Configuration
- Data display

```ts
const props = defineProps<{
  specificationId: string
  isEditable: boolean
}>()
```

### Provide/Inject

✅ Use for:
- Passing data deep down component tree without props drilling
- Shared services (Keycloak, API clients)
- Theme/configuration

```ts
// Parent
provide('theme', theme)
provide('getKeycloakPromise', getKeycloakPromise)

// Descendant (can be many levels deep)
const theme = inject<Theme>('theme')
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!
```

## Performance Optimization

```ts
// Use shallowRef for large lists
const specifications = shallowRef<FrameworkSpecification[]>([])

// Computed values are cached
const expensiveComputation = computed(() => {
  return specifications.value
    .filter(s => s.isActive)
    .map(s => heavyTransform(s))
})

// Mark static objects as readonly
import { readonly } from 'vue'
const config = readonly({
  apiEndpoint: '/api',
  timeout: 5000
})
```
