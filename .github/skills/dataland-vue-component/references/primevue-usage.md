---
name: primevue-usage
description: Common PrimeVue component patterns used in Dataland frontend
---

# PrimeVue Usage

PrimeVue is the primary UI component library in Dataland. Import components individually.

## ⚠️ Critical Rules

1. **Read and Understand the PrimeVue API** - PrimeVue provides everything you need
2. **Do NOT use FormKit** - FormKit has its own UI. Use PrimeVue form elements instead
3. **Do NOT use PrimeFlex classes** - PrimeFlex is sunsetted
4. **Do NOT use Material Icons** - Use PrimeIcons instead
5. **Do NOT use `:deep()` selectors** - Use PrimeVue's PassThrough API instead
6. **Do NOT build unnecessary component wrappers** - Use the component's API directly
7. **Do NOT access PrimeVue CSS classes on local scope** - Modify globally via presets
8. **Use Design Tokens and CSS Variables** - For consistent styling across components

## Form Validation

For form validation, use `zod` with PrimeVue form elements:

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { z } from 'zod'
import InputText from 'primevue/inputtext'
import PrimeButton from 'primevue/button'

const schema = z.object({
  name: z.string().min(1, 'Name is required'),
  email: z.string().email('Invalid email address'),
  age: z.number().min(18, 'Must be 18 or older')
})

const formData = ref({
  name: '',
  email: '',
  age: 0
})

const errors = ref<Record<string, string>>({})

function handleSubmit() {
  try {
    schema.parse(formData.value)
    errors.value = {}
    // Submit form
  } catch (error) {
    if (error instanceof z.ZodError) {
      errors.value = error.flatten().fieldErrors
    }
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <div class="field">
      <label for="name">Name</label>
      <InputText
        id="name"
        v-model="formData.name"
        :invalid="!!errors.name"
        class="w-full"
      />
      <small v-if="errors.name" class="error">{{ errors.name[0] }}</small>
    </div>
    
    <PrimeButton type="submit" label="Submit" />
  </form>
</template>
</template>
```

See [PrimeVue Forms Documentation](https://primevue.org/inputtext/#forms) for more examples.

## Buttons

```vue
<script setup lang="ts">
import PrimeButton from 'primevue/button'

const isLoading = ref(false)

async function handleSave() {
  isLoading.value = true
  // save logic
  isLoading.value = false
}
</script>

<template>
  <!-- Basic button -->
  <PrimeButton label="Click Me" />
  
  <!-- With icon -->
  <PrimeButton label="Save" icon="pi pi-check" />
  
  <!-- Icon position -->
  <PrimeButton label="Delete" icon="pi pi-trash" icon-pos="right" />
  
  <!-- Loading state -->
  <PrimeButton label="Save" :loading="isLoading" @click="handleSave" />
  
  <!-- Severity variants -->
  <PrimeButton label="Primary" />
  <PrimeButton label="Secondary" severity="secondary" />
  <PrimeButton label="Success" severity="success" />
  <PrimeButton label="Info" severity="info" />
  <PrimeButton label="Warn" severity="warn" />
  <PrimeButton label="Danger" severity="danger" />
  
  <!-- Text/Link buttons -->
  <PrimeButton label="Text" text />
  <PrimeButton label="Link" link />
  
  <!-- With data-test -->
  <PrimeButton 
    label="Submit"
    data-test="submit-button"
    @click="handleSubmit"
  />
</template>
```

## DataTable

```vue
<script setup lang="ts">
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import type { FrameworkSpecification } from '@clients/specificationservice'

const specifications = ref<FrameworkSpecification[]>([])
const selectedSpecs = ref<FrameworkSpecification[]>([])
const isLoading = ref(false)

function handleRowSelect(event: { data: FrameworkSpecification }) {
  console.log('Selected:', event.data)
}
</script>

<template>
  <!-- Basic table -->
  <DataTable 
    :value="specifications"
    :loading="isLoading"
    data-test="specifications-table"
  >
    <Column field="id" header="ID" sortable />
    <Column field="name" header="Name" sortable />
    <Column field="version" header="Version" />
  </DataTable>
  
  <!-- With pagination -->
  <DataTable
    :value="specifications"
    paginator
    :rows="10"
    :rows-per-page-options="[5, 10, 20, 50]"
  >
    <Column field="name" header="Name" />
  </DataTable>
  
  <!-- Selectable rows -->
  <DataTable
    v-model:selection="selectedSpecs"
    :value="specifications"
    selection-mode="multiple"
    data-key="id"
    @row-select="handleRowSelect"
  >
    <Column selection-mode="multiple" style="width: 3rem" />
    <Column field="name" header="Name" />
  </DataTable>
  
  <!-- Filterable columns -->
  <DataTable
    :value="specifications"
    filter-display="row"
  >
    <Column field="name" header="Name" :show-filter-menu="false">
      <template #filter="{ filterModel, filterCallback }">
        <InputText
          v-model="filterModel.value"
          type="text"
          @input="filterCallback()"
          placeholder="Search by name"
        />
      </template>
    </Column>
  </DataTable>
  
  <!-- Custom cell templates -->
  <DataTable :value="specifications">
    <Column field="name" header="Name">
      <template #body="{ data }">
        <strong>{{ data.name }}</strong>
      </template>
    </Column>
    
    <Column header="Actions">
      <template #body="{ data }">
        <PrimeButton
          icon="pi pi-pencil"
          text
          @click="editSpec(data)"
        />
        <PrimeButton
          icon="pi pi-trash"
          text
          severity="danger"
          @click="deleteSpec(data)"
        />
      </template>
    </Column>
  </DataTable>
  
  <!-- Empty state -->
  <DataTable :value="specifications">
    <template #empty>
      <div class="text-center p-4">
        No specifications found.
      </div>
    </template>
    <Column field="name" header="Name" />
  </DataTable>
</template>
```

## Dialog

```vue
<script setup lang="ts">
import Dialog from 'primevue/dialog'
import PrimeButton from 'primevue/button'

const visible = ref(false)
const formData = ref({ name: '', description: '' })

function openDialog() {
  visible.value = true
}

function closeDialog() {
  visible.value = false
  formData.value = { name: '', description: '' }
}

async function handleSave() {
  // Save logic
  closeDialog()
}
</script>

<template>
  <PrimeButton label="Open Dialog" @click="openDialog" />
  
  <!-- Basic dialog -->
  <Dialog
    v-model:visible="visible"
    header="Edit Specification"
    :modal="true"
    :style="{ width: '50vw' }"
  >
    <div class="dialog-content">
      <label>Name</label>
      <InputText v-model="formData.name" class="w-full" />
      
      <label>Description</label>
      <Textarea v-model="formData.description" class="w-full" rows="5" />
    </div>
    
    <template #footer>
      <PrimeButton label="Cancel" text @click="closeDialog" />
      <PrimeButton label="Save" @click="handleSave" />
    </template>
  </Dialog>
</template>

<style scoped lang="scss">
.dialog-content {
  // Structural layout only
  display: flex;
  flex-direction: column;
  gap: 1rem;
  
  label {
    font-weight: 600;
    margin-bottom: 0.25rem;
  }
}
</style>
```

## Form Inputs

```vue
<script setup lang="ts">
import InputText from 'primevue/inputtext'
import Textarea from 'primevue/textarea'
import Select from 'primevue/select'
import MultiSelect from 'primevue/multiselect'
import DatePicker from 'primevue/datepicker'
import Checkbox from 'primevue/checkbox'
import RadioButton from 'primevue/radiobutton'

const formData = ref({
  name: '',
  description: '',
  framework: null,
  tags: [],
  startDate: null,
  isActive: false,
  category: 'type1'
})

const frameworks = ref([
  { label: 'Framework 1', value: 'fw1' },
  { label: 'Framework 2', value: 'fw2' }
])

const availableTags = ref(['tag1', 'tag2', 'tag3'])
</script>

<template>
  <div class="form-grid">
    <!-- Text Input -->
    <div class="field">
      <label for="name">Name</label>
      <InputText id="name" v-model="formData.name" class="w-full" />
    </div>
    
    <!-- Textarea -->
    <div class="field">
      <label for="description">Description</label>
      <Textarea
        id="description"
        v-model="formData.description"
        rows="4"
        class="w-full"
      />
    </div>
    
    <!-- Select -->
    <div class="field">
      <label for="framework">Framework</label>
      <Select
        id="framework"
        v-model="formData.framework"
        :options="frameworks"
        option-label="label"
        option-value="value"
        placeholder="Select a framework"
        class="w-full"
      />
    </div>
    
    <!-- MultiSelect -->
    <div class="field">
      <label for="tags">Tags</label>
      <MultiSelect
        id="tags"
        v-model="formData.tags"
        :options="availableTags"
        placeholder="Select tags"
        class="w-full"
      />
    </div>
    
    <!-- DatePicker -->
    <div class="field">
      <label for="date">Start Date</label>
      <DatePicker
        id="date"
        v-model="formData.startDate"
        date-format="yy-mm-dd"
        class="w-full"
      />
    </div>
    
    <!-- Checkbox -->
    <div class="field-checkbox">
      <Checkbox
        id="active"
        v-model="formData.isActive"
        :binary="true"
      />
      <label for="active">Active</label>
    </div>
    
    <!-- Radio Buttons -->
    <div class="field">
      <label>Category</label>
      <div class="flex gap-3">
        <div class="flex align-items-center">
          <RadioButton
            id="type1"
            v-model="formData.category"
            value="type1"
          />
          <label for="type1" class="ml-2">Type 1</label>
        </div>
        <div class="flex align-items-center">
          <RadioButton
            id="type2"
            v-model="formData.category"
            value="type2"
          />
          <label for="type2" class="ml-2">Type 2</label>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.form-grid {
  // Structural layout only
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  
  .field {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    
    label {
      font-weight: 600;
    }
  }
  
  .field-checkbox {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
}
</style>
```

## Message & Toast

```vue
<script setup lang="ts">
import Message from 'primevue/message'
import { useToast } from 'primevue/usetoast'

const toast = useToast()
const errorMessage = ref<string>()

function showSuccess() {
  toast.add({
    severity: 'success',
    summary: 'Success',
    detail: 'Operation completed successfully',
    life: 3000
  })
}

function showError() {
  toast.add({
    severity: 'error',
    summary: 'Error',
    detail: 'Something went wrong',
    life: 5000
  })
}
</script>

<template>
  <!-- Inline message -->
  <Message v-if="errorMessage" severity="error">
    {{ errorMessage }}
  </Message>
  
  <Message severity="info">
    Information message
  </Message>
  
  <Message severity="warn">
    Warning message
  </Message>
  
  <!-- Buttons to trigger toasts -->
  <PrimeButton label="Success Toast" @click="showSuccess" />
  <PrimeButton label="Error Toast" @click="showError" />
</template>
```

## Tabs

```vue
<script setup lang="ts">
import Tabs from 'primevue/tabs'
import TabList from 'primevue/tablist'
import Tab from 'primevue/tab'
import TabPanels from 'primevue/tabpanels'
import TabPanel from 'primevue/tabpanel'

const activeTab = ref('details')
</script>

<template>
  <Tabs v-model:value="activeTab">
    <TabList>
      <Tab value="details" data-test="detailsTab">Details</Tab>
      <Tab value="specifications" data-test="specificationsTab">Specifications</Tab>
      <Tab value="history" data-test="historyTab">History</Tab>
    </TabList>
    
    <TabPanels>
      <TabPanel value="details">
        <div class="tab-content">
          <!-- Details content -->
        </div>
      </TabPanel>
      
      <TabPanel value="specifications">
        <div class="tab-content">
          <!-- Specifications content -->
        </div>
      </TabPanel>
      
      <TabPanel value="history">
        <div class="tab-content">
          <!-- History content -->
        </div>
      </TabPanel>
    </TabPanels>
  </Tabs>
</template>
```

## Common PrimeVue Icons (PrimeIcons)

**⚠️ Use PrimeIcons, NOT Material Icons**

```vue
<template>
  <!-- Actions -->
  <PrimeButton icon="pi pi-check" />      <!-- Confirm -->
  <PrimeButton icon="pi pi-times" />      <!-- Close/Cancel -->
  <PrimeButton icon="pi pi-pencil" />     <!-- Edit -->
  <PrimeButton icon="pi pi-trash" />      <!-- Delete -->
  <PrimeButton icon="pi pi-plus" />       <!-- Add -->
  <PrimeButton icon="pi pi-minus" />      <!-- Remove -->
  <PrimeButton icon="pi pi-save" />       <!-- Save -->
  
  <!-- Navigation -->
  <PrimeButton icon="pi pi-arrow-left" />  <!-- Back -->
  <PrimeButton icon="pi pi-arrow-right" /> <!-- Forward -->
  <PrimeButton icon="pi pi-chevron-down" /><!-- Dropdown -->
  <PrimeButton icon="pi pi-bars" />        <!-- Menu -->
  
  <!-- Status -->
  <PrimeButton icon="pi pi-refresh" />     <!-- Reload -->
  <PrimeButton icon="pi pi-spinner" />     <!-- Loading -->
  <PrimeButton icon="pi pi-exclamation-triangle" /> <!-- Warning -->
  <PrimeButton icon="pi pi-info-circle" /> <!-- Info -->
  
  <!-- Files -->
  <PrimeButton icon="pi pi-file" />        <!-- File -->
  <PrimeButton icon="pi pi-download" />    <!-- Download -->
  <PrimeButton icon="pi pi-upload" />      <!-- Upload -->
  
  <!-- Other -->
  <PrimeButton icon="pi pi-search" />      <!-- Search -->
  <PrimeButton icon="pi pi-filter" />      <!-- Filter -->
  <PrimeButton icon="pi pi-cog" />         <!-- Settings -->
  <PrimeButton icon="pi pi-user" />        <!-- User -->
</template>
```

## PassThrough API

If you need to customize a single component instance use PassThrough API, **NOT `:deep()`**:

```vue
<script setup lang="ts">
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

const specifications = ref([])

// Custom styling via PassThrough API
const tablePt = {
  table: { class: 'custom-table' },
  header: { class: 'custom-header' },
  body: { class: 'custom-body' }
}
</script>

<template>
  <DataTable
    :value="specifications"
    :pt="tablePt"
  >
    <Column field="name" header="Name" />
  </DataTable>
</template>
```

## Design Tokens and CSS Variables

**Use PrimeVue's Design Tokens (CSS variables) for styling:**

```vue
<template>
  <div class="custom-container">
    <PrimeButton label="Styled Button" />
  </div>
</template>

<style scoped lang="scss">
// Only use scoped styles for structural layout
.custom-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding: var(--p-content-padding); // Use Design Token
  
  // Use CSS variables for colors
  background-color: var(--p-surface-0);
  border: 1px solid var(--p-surface-border);
  border-radius: var(--p-border-radius);
  
  // ❌ DON'T hardcode colors
  // background-color: #ffffff;
  
  // ❌ DON'T use :deep() to style PrimeVue components
  // :deep(.p-button) { ... }
}
</style>
```

### Common Design Token Variables

```scss
// Colors
var(--p-primary-color)
var(--p-primary-contrast-color)
var(--p-surface-0)         // Background
var(--p-surface-50)        // Subtle background
var(--p-surface-border)    // Border color
var(--p-text-color)        // Primary text
var(--p-text-secondary-color)

// Spacing
var(--p-content-padding)
var(--p-inline-spacing)
var(--p-gap)

// Borders
var(--p-border-radius)
var(--p-border-width)

// Status colors
var(--p-success-color)
var(--p-info-color)
var(--p-warn-color)
var(--p-danger-color)
```

## Scoped Styles - Structural Only

**Scoped styles are ONLY for structural layout, NOT for component styling:**

```vue
<template>
  <div class="specification-layout">
    <div class="header-section">
      <h1>Title</h1>
      <PrimeButton label="Action" />
    </div>
    
    <div class="content-section">
      <DataTable :value="data" />
    </div>
  </div>
</template>

<style scoped lang="scss">
// ✅ GOOD: Structural CSS only
.specification-layout {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 1.5rem;
  padding: 1rem;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-section {
  display: flex;
  flex-direction: column;
}

// ❌ BAD: Don't style PrimeVue components
// .header-section h1 {
//   color: #333333;
//   font-size: 24px;
// }

// ❌ BAD: Don't use :deep()
// :deep(.p-button) {
//   background-color: blue;
// }
</style>
```
