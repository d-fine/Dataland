<script setup lang="ts">
import { ref, inject, watch, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type Keycloak from 'keycloak-js';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { useSpecifications } from '@/composables/useSpecifications';
import { useDataPointDetails } from '@/composables/useDataPointDetails';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkMetadataPanel from '@/components/resources/specifications/FrameworkMetadataPanel.vue';
import SpecificationSchemaTree from '@/components/resources/specifications/SpecificationSchemaTree.vue';
import DataPointTypeDetailsDialog from '@/components/resources/specifications/DataPointTypeDetailsDialog.vue';
import PrimeSelect from 'primevue/select';
import Message from 'primevue/message';
import PrimeButton from 'primevue/button';
import ProgressSpinner from 'primevue/progressspinner';

// Setup Keycloak and API client
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise')!;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

// Router
const route = useRoute();
const router = useRouter();

// Initialize composables with fetch functions
const {
  frameworks,
  selectedFramework,
  isLoadingFrameworks,
  isLoadingSpecification,
  isLoadingDataPointDetails,
  error: specificationsError,
  loadFrameworks,
  selectFramework,
} = useSpecifications({
  fetchFrameworks: () => apiClientProvider.apiClients.specificationController.listFrameworkSpecifications().then(r => r.data),
  fetchSpecification: (id) => apiClientProvider.apiClients.specificationController.getFrameworkSpecification(id).then(r => r.data),
  fetchDataPointDetails: (id) => apiClientProvider.apiClients.specificationController.getDataPointTypeSpecification(id).then(r => r.data),
  enableBatchDataPointLoading: true,
});

const {
  dataPointDetails,
  isLoading: isLoadingDataPoint,
  error: dataPointError,
  loadDetails: loadDataPointDetails,
  clearDetails: clearDataPointDetails,
  retryLoad: retryDataPointLoad,
} = useDataPointDetails({
  fetchDataPointDetails: (id) => apiClientProvider.apiClients.specificationController.getDataPointTypeSpecification(id).then(r => r.data),
});

// Local state
const selectedFrameworkId = ref<string | null>(null);
const showDataPointDialog = ref(false);
const currentDataPointId = ref<string | null>(null);

// Load frameworks on mount
onMounted(async () => {
  await loadFrameworks();
  
  // Check for framework query parameter and pre-select if present
  const frameworkParam = route.query.framework as string | undefined;
  if (frameworkParam && frameworks.value.some(f => f.framework.id === frameworkParam)) {
    selectedFrameworkId.value = frameworkParam;
    await handleFrameworkChange(frameworkParam);
  }
});

// Watch for framework selection changes
watch(selectedFrameworkId, async (newValue) => {
  if (newValue) {
    await handleFrameworkChange(newValue);
  }
});

// Watch for route query param changes (browser back/forward)
watch(
  () => route.query.framework,
  (newFramework) => {
    if (newFramework && typeof newFramework === 'string' && newFramework !== selectedFrameworkId.value) {
      selectedFrameworkId.value = newFramework;
    }
  }
);

/**
 * Handle framework selection change.
 * Updates URL query param and loads the selected framework specification.
 */
async function handleFrameworkChange(frameworkId: string): Promise<void> {
  // Update URL query param
  if (route.query.framework !== frameworkId) {
    await router.push({ query: { framework: frameworkId } });
  }
  
  // Load framework specification
  await selectFramework(frameworkId);
}

/**
 * Handle "View Details" button click for a data point.
 * Opens the modal and loads data point details.
 */
async function handleViewDetails(dataPointTypeId: string): Promise<void> {
  currentDataPointId.value = dataPointTypeId;
  showDataPointDialog.value = true;
  
  try {
    await loadDataPointDetails(dataPointTypeId);
  } catch (err) {
    // Error is already handled in the composable
    console.error('Failed to load data point details:', err);
  }
}

/**
 * Handle modal close.
 * Clears data point details.
 */
function handleDialogClose(): void {
  showDataPointDialog.value = false;
  clearDataPointDetails();
  currentDataPointId.value = null;
}

/**
 * Retry loading the framework list after an error.
 */
async function retryLoadFrameworks(): Promise<void> {
  await loadFrameworks();
}

/**
 * Retry loading the selected framework after an error.
 */
async function retryLoadSpecification(): Promise<void> {
  if (selectedFrameworkId.value) {
    await selectFramework(selectedFrameworkId.value);
  }
}
</script>

<template>
  <TheContent>
    <div class="specifications-viewer" data-test="specifications-content">
      <div class="header-section">
        <h1>Framework Specifications</h1>
        <p class="description">
          Select a framework to explore its data point structure and definitions.
        </p>
      </div>

      <!-- Framework Selector -->
      <div class="framework-selector-section" data-test="framework-selector">
        <label for="framework-select" class="selector-label">Select Framework</label>
        <div class="selector-wrapper">
          <PrimeSelect
            id="framework-select"
            v-model="selectedFrameworkId"
            :options="frameworks"
            option-label="name"
            option-value="framework.id"
            placeholder="Choose a framework..."
            :loading="isLoadingFrameworks"
            :disabled="isLoadingFrameworks"
            class="framework-select"
            aria-label="Select framework"
          />
          <ProgressSpinner v-if="isLoadingFrameworks" class="spinner-small" />
        </div>
        
        <!-- Framework loading error -->
        <Message v-if="specificationsError && !selectedFramework && !isLoadingSpecification" severity="error" class="error-message">
          {{ specificationsError }}
          <PrimeButton
            label="Retry"
            icon="pi pi-refresh"
            text
            @click="retryLoadFrameworks"
          />
        </Message>
      </div>

      <!-- Content Area -->
      <div v-if="selectedFramework" class="content-area">
        <!-- Framework Metadata -->
        <FrameworkMetadataPanel
          :framework="selectedFramework"
          data-test="framework-metadata"
        />

        <!-- Schema Tree -->
        <div class="schema-section">
          <h2>Data Point Structure</h2>
          
          <!-- Loading indicator for batch data point details -->
          <div v-if="isLoadingDataPointDetails" class="loading-details">
            <ProgressSpinner class="spinner-inline" />
            <span>Loading detailed descriptions...</span>
          </div>
          
          <SpecificationSchemaTree
            :schema="selectedFramework.parsedSchema"
            @view-details="handleViewDetails"
          />
        </div>
      </div>

      <!-- Loading State for Specification -->
      <div v-else-if="isLoadingSpecification" class="loading-state">
        <ProgressSpinner />
        <p>Loading framework specification...</p>
      </div>

      <!-- Specification loading error -->
      <Message v-else-if="specificationsError && selectedFrameworkId" severity="error" class="error-message">
        {{ specificationsError }}
        <PrimeButton
          label="Retry"
          icon="pi pi-refresh"
          text
          @click="retryLoadSpecification"
        />
      </Message>

      <!-- Empty State -->
      <div v-else-if="!selectedFrameworkId && !isLoadingFrameworks" class="empty-state">
        <i class="pi pi-book empty-icon"></i>
        <p>Select a framework to view its specification</p>
      </div>

      <!-- Data Point Details Dialog -->
      <DataPointTypeDetailsDialog
        v-model:visible="showDataPointDialog"
        :data-point-type-id="currentDataPointId"
        :data-point-details="dataPointDetails"
        :is-loading="isLoadingDataPoint"
        :error="dataPointError"
        @retry="retryDataPointLoad"
        @close="handleDialogClose"
      />
    </div>
  </TheContent>
</template>

<style scoped lang="scss">
.specifications-viewer {
  display: flex;
  flex-direction: column;
  gap: 2rem;
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;

  .header-section {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }

  .description {
    color: var(--p-text-secondary-color);
    margin: 0;
  }

  .framework-selector-section {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;

    .selector-label {
      font-weight: 600;
      color: var(--p-text-color);
    }

    .selector-wrapper {
      display: flex;
      align-items: center;
      gap: 1rem;

      .framework-select {
        flex: 1;
        max-width: 500px;
      }

      .spinner-small {
        width: 2rem;
        height: 2rem;
      }
    }
  }

  .content-area {
    display: flex;
    flex-direction: column;
    gap: 2rem;

    .schema-section {
      display: flex;
      flex-direction: column;
      gap: 1rem;

      h2 {
        margin: 0;
      }
      
      .loading-details {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        padding: 1rem;
        background: var(--p-surface-50);
        border-radius: var(--p-border-radius);
        color: var(--p-text-secondary-color);
        font-size: 0.9375rem;
        
        .spinner-inline {
          width: 1.5rem;
          height: 1.5rem;
        }
      }
    }
  }

  .loading-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    padding: 4rem 2rem;
    color: var(--p-text-secondary-color);
  }

  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    padding: 4rem 2rem;
    color: var(--p-text-secondary-color);

    .empty-icon {
      font-size: 4rem;
      color: var(--p-text-muted-color);
    }

    p {
      font-size: 1.125rem;
      margin: 0;
    }
  }

  .error-message {
    margin-top: 0.5rem;
  }
}
</style>
