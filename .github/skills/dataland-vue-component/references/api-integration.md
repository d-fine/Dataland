---
name: api-integration
description: Patterns for connecting to backend services, authentication, and API client usage in Dataland
---

# API Integration

## Setup ApiClientProvider

Standard pattern for authenticated API calls:

```ts
import { inject } from 'vue'
import type Keycloak from 'keycloak-js'
import { ApiClientProvider } from '@/services/ApiClients'
import { assertDefined } from '@/utils/TypeScriptUtils'

// Inject Keycloak promise
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!

// Create API client provider
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)())
```

## Adding Specification Service Client

To add the specification service client to ApiClientProvider:

1. **Generate client from OpenAPI spec** (if not already done)
2. **Import controller** in `src/services/ApiClients.ts`:

```ts
import { SpecificationControllerApi } from '@clients/specificationservice'
```

3. **Add to ApiClients interface**:

```ts
interface ApiClients {
  // ... existing clients
  specificationController: SpecificationControllerApi
}
```

4. **Initialize in constructApiClients()**:

```ts
private constructApiClients(): ApiClients {
  return {
    // ... existing clients
    specificationController: this.getClientFactory('/specifications')(SpecificationControllerApi),
  }
}
```

## API Call Patterns

### Basic GET Request

```ts
import { ref } from 'vue'

const specifications = ref<FrameworkSpecification[]>([])
const isLoading = ref(false)
const errorMessage = ref<string>()

async function loadSpecifications() {
  try {
    isLoading.value = true
    errorMessage.value = undefined
    
    const response = await apiClientProvider.apiClients.specificationController
      .getFrameworkSpecifications()
    
    specifications.value = response.data
  } catch (error) {
    console.error('Failed to load specifications:', error)
    errorMessage.value = error instanceof Error ? error.message : 'Failed to load data'
  } finally {
    isLoading.value = false
  }
}
```

### GET with Parameters

```ts
async function loadSpecification(frameworkId: string) {
  try {
    isLoading.value = true
    
    const response = await apiClientProvider.apiClients.specificationController
      .getFrameworkSpecification(frameworkId)
    
    specification.value = response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      errorMessage.value = 'Specification not found'
    } else {
      errorMessage.value = 'Failed to load specification'
    }
  } finally {
    isLoading.value = false
  }
}
```

### POST Request

```ts
import type { FrameworkSpecificationInput } from '@clients/specificationservice'

const isSaving = ref(false)

async function createSpecification(input: FrameworkSpecificationInput) {
  try {
    isSaving.value = true
    
    const response = await apiClientProvider.apiClients.specificationController
      .createFrameworkSpecification(input)
    
    emit('created', response.data)
    return response.data
  } catch (error) {
    console.error('Failed to create specification:', error)
    throw error
  } finally {
    isSaving.value = false
  }
}
```

### PUT/PATCH Request

```ts
async function updateSpecification(id: string, updates: Partial<FrameworkSpecification>) {
  try {
    isSaving.value = true
    
    const response = await apiClientProvider.apiClients.specificationController
      .updateFrameworkSpecification(id, updates)
    
    specification.value = response.data
    emit('updated', response.data)
  } catch (error) {
    console.error('Failed to update:', error)
    throw error
  } finally {
    isSaving.value = false
  }
}
```

### DELETE Request

```ts
async function deleteSpecification(id: string) {
  try {
    await apiClientProvider.apiClients.specificationController
      .deleteFrameworkSpecification(id)
    
    emit('deleted', id)
  } catch (error) {
    console.error('Failed to delete:', error)
    throw error
  }
}
```

## Error Handling

### Axios Error Handling

```ts
import { AxiosError } from 'axios'

async function fetchData() {
  try {
    const response = await apiClientProvider.apiClients.specificationController.getData()
    return response.data
  } catch (error) {
    if (error instanceof AxiosError) {
      // HTTP error response
      if (error.response) {
        switch (error.response.status) {
          case 404:
            errorMessage.value = 'Resource not found'
            break
          case 403:
            errorMessage.value = 'Access denied'
            break
          case 401:
            errorMessage.value = 'Authentication required'
            break
          default:
            errorMessage.value = `Server error: ${error.response.status}`
        }
      } else if (error.request) {
        // Request made but no response
        errorMessage.value = 'Network error - no response from server'
      } else {
        // Request setup error
        errorMessage.value = 'Failed to make request'
      }
    } else {
      errorMessage.value = error instanceof Error ? error.message : 'Unknown error'
    }
  }
}
```

### User-Friendly Error Messages

```ts
function getErrorMessage(error: unknown): string {
  if (error instanceof AxiosError) {
    const status = error.response?.status
    
    if (status === 404) return 'The requested resource was not found'
    if (status === 403) return 'You do not have permission to access this resource'
    if (status === 401) return 'Please log in to continue'
    if (status === 409) return 'This operation conflicts with existing data'
    if (status && status >= 500) return 'Server error - please try again later'
    
    // Use backend error message if available
    return error.response?.data?.message || 'An error occurred'
  }
  
  return error instanceof Error ? error.message : 'Unknown error occurred'
}

// Usage
try {
  await someApiCall()
} catch (error) {
  errorMessage.value = getErrorMessage(error)
}
```

## Loading States

### Multiple Loading States

```ts
const isLoadingList = ref(false)
const isLoadingDetails = ref(false)
const isSaving = ref(false)
const isDeleting = ref(false)

// Composite loading state
const isAnyLoading = computed(() => 
  isLoadingList.value || isLoadingDetails.value || isSaving.value || isDeleting.value
)
```

### Debounced Search

```ts
import { ref, watch } from 'vue'
import { debounce } from 'lodash-es'  // or implement your own

const searchTerm = ref('')
const searchResults = ref([])
const isSearching = ref(false)

const debouncedSearch = debounce(async (term: string) => {
  if (!term.trim()) {
    searchResults.value = []
    return
  }
  
  try {
    isSearching.value = true
    const response = await apiClientProvider.apiClients.specificationController
      .searchSpecifications(term)
    searchResults.value = response.data
  } finally {
    isSearching.value = false
  }
}, 300)

watch(searchTerm, (newTerm) => {
  debouncedSearch(newTerm)
})
```

## Polling for Updates

```ts
import { ref, onMounted, onUnmounted } from 'vue'

const data = ref([])
let pollingInterval: number | undefined

async function pollData() {
  try {
    const response = await apiClientProvider.apiClients.specificationController.getData()
    data.value = response.data
  } catch (error) {
    console.error('Polling failed:', error)
  }
}

onMounted(() => {
  pollData()  // Initial load
  pollingInterval = window.setInterval(pollData, 30000)  // Every 30s
})

onUnmounted(() => {
  if (pollingInterval) {
    clearInterval(pollingInterval)
  }
})
```

## Using Other Backend Clients

### Backend Service

```ts
// Company data
const companyData = await apiClientProvider.backendClients.companyDataController
  .getCompanyById(companyId)

// Metadata
const metadata = await apiClientProvider.backendClients.metaDataController
  .getDataMetaInfo(dataId)
```

### Community Manager

```ts
// Company roles
const roles = await apiClientProvider.apiClients.companyRolesController
  .getExtendedCompanyRoleAssignments(companyId)

// Access rights
const hasAccess = await apiClientProvider.apiClients.companyRightsController
  .checkCompanyOwnership(companyId)
```

### Document Manager

```ts
// Upload document
const formData = new FormData()
formData.append('file', file)

const result = await apiClientProvider.apiClients.documentController
  .uploadDocument(formData)
```

### QA Service

```ts
// Get QA status
const qaStatus = await apiClientProvider.apiClients.qaController
  .getQaStatus(dataId)
```

## Type Safety

```ts
// Import types from generated clients
import type { 
  FrameworkSpecification,
  FrameworkSpecificationInput,
  SpecificationListResponse
} from '@clients/specificationservice'

// Use types for state
const specifications = ref<FrameworkSpecification[]>([])
const selectedSpec = ref<FrameworkSpecification | null>(null)

// Use types for function parameters
async function createSpec(input: FrameworkSpecificationInput): Promise<FrameworkSpecification> {
  const response = await apiClientProvider.apiClients.specificationController
    .createFrameworkSpecification(input)
  return response.data
}
```
