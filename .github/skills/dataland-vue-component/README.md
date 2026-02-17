# Dataland Vue Component Skill

An adapted skill for creating Vue 3 components in the Dataland project that connect to backend services, particularly the specification service.

## What This Skill Covers

- **Vue 3.5 Composition API** with `<script setup lang="ts">`
- **PrimeVue** component library usage (with strict rules)
- **Design Tokens and CSS Variables** for consistent styling
- **PassThrough API** for component customization (NO `:deep()`)
- **Keycloak** authentication patterns
- **ApiClientProvider** for backend API integration
- **Pinia** state management (both Options and Composition API styles)
- **TypeScript** best practices (mandatory)
- **Performance optimizations** (shallowRef, computed caching)
- **Zod** for form validation (NOT FormKit)

## Critical Rules

⚠️ **DO NOT:**
- Use FormKit (use PrimeVue form elements + zod)
- Use PrimeFlex classes (sunsetted)
- Use Material Icons (use PrimeIcons)
- Use `:deep()` selectors (use PassThrough API)
- Build unnecessary component wrappers
- Style PrimeVue components with scoped styles
- Copy old code without reviewing these guidelines

## When to Use This Skill

Use this skill when:
- Creating new frontend components for Dataland
- Connecting to the specification service or other backend APIs
- Building data tables, forms, or dialogs
- Implementing authenticated API calls
- Managing component or application state

## Skill Structure

```
dataland-vue-component/
├── SKILL.md                          # Main skill overview & quick start
├── GENERATION.md                     # Metadata about this skill
└── references/
    ├── component-patterns.md         # Props, emits, lifecycle, watchers
    ├── api-integration.md            # API calls, error handling, authentication
    ├── primevue-usage.md             # PrimeVue component examples
    └── state-management.md           # ref, computed, Pinia stores
```

## Quick Example

```vue
<script setup lang="ts">
import { ref, computed, onMounted, inject } from 'vue'
import type Keycloak from 'keycloak-js'
import { ApiClientProvider } from '@/services/ApiClients'
import { assertDefined } from '@/utils/TypeScriptUtils'
import PrimeButton from 'primevue/button'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())

const specifications = ref([])
const isLoading = ref(false)

onMounted(async () => {
  await loadSpecifications()
})

async function loadSpecifications() {
  try {
    isLoading.value = true
    const response = await apiClientProvider.apiClients.specificationController
      .getFrameworkSpecifications()
    specifications.value = response.data
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <DataTable :value="specifications" :loading="isLoading">
    <Column field="name" header="Name" />
  </DataTable>
</template>
```

## Key Differences from Base antfu-vue Skill

1. **Strict PrimeVue Rules**: Must read/understand API, no unnecessary wrappers
2. **NO FormKit**: Use PrimeVue form elements + zod for validation
3. **NO PrimeFlex**: Sunsetted, don't use
4. **NO Material Icons**: Use PrimeIcons only
5. **NO `:deep()`**: Use PassThrough API for customization
6. **Design Tokens Required**: Use CSS variables, not hardcoded values
7. **Scoped Styles**: Only for structural layout, never for component styling
8. **Keycloak Integration**: Always inject Keycloak for authenticated components
9. **ApiClientProvider**: Standard pattern for all API calls
10. **Project Structure**: Uses `TheContent`, `MarginWrapper`, and other Dataland-specific layouts
11. **Mixed Patterns**: Acknowledges existing Options API code while promoting Composition API for new components
12. **Data-Test Attributes**: Required for Cypress e2e tests
13. **Pinia Patterns**: Documents both Options API (current standard) and Composition API (modern alternative)

## Getting Started

1. **\u26a0\ufe0f Start here:** Read [dataland-frontend-coding-guidelines.md](references/dataland-frontend-coding-guidelines.md) - Critical rules
2. Review [SKILL.md](SKILL.md) for overview and quick start template
3. Check [api-integration.md](references/api-integration.md) for connecting to specification service
4. See [primevue-usage.md](references/primevue-usage.md) for UI components, PassThrough API, and Design Tokens
5. Read [component-patterns.md](references/component-patterns.md) for component structure
6. Review [state-management.md](references/state-management.md) for state handling

## Related Skills

- **antfu-vue**: Base Vue 3 skill (core Composition API patterns)
- **dataland-vue-component**: This skill (Dataland-specific integration)

## Contributing

When updating this skill:
1. Keep examples aligned with actual Dataland codebase patterns
2. Update when new backend services are added (like specification service)
3. Include both "current reality" and "recommended improvements"
4. Add real examples from the codebase when possible
