<template>
  <TheHeader />
  <TheContent>
    <div class="documentation-page">
      <div class="structural-container">
        <h1 data-test="documentationTitle">Documentation</h1>
        <p>
          Welcome to the Dataland documentation. This page provides an overview of the available frameworks and resources on the platform.
        </p>
      </div>

      <div v-if="isLoading" data-test="frameworksLoading">
        <ProgressSpinner />
        <Message severity="info" variant="simple" size="small" data-test="frameworksLoadingMessage">
          Loading frameworks...
        </Message>
      </div>

      <Message v-else-if="error" severity="error" variant="simple" size="small" data-test="frameworksError">
        Error loading frameworks: {{ error }}
      </Message>

      <Message v-else-if="frameworks.length === 0" severity="info" variant="simple" size="small" data-test="frameworksNotFound">
        No frameworks available at the moment.
      </Message>

      <div v-else data-test="frameworksContent">
        <div>
          <h2 data-test="availableFrameworksTitle">Available Frameworks</h2>
          <p>Below is a list of all available data frameworks on the Dataland platform:</p>
        </div>
        <div class="frameworks-list">
          <Card
            v-for="framework in frameworks"
            :key="framework.framework.id"
            @click="navigateToFramework(framework.framework.id)"
            data-test="frameworkCard"
          >
            <template #content>
              <div>
                <h3 data-test="frameworkName">{{ framework.name }}</h3>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </TheContent>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Message from 'primevue/message';
import ProgressSpinner from 'primevue/progressspinner';
import Card from 'primevue/card';
import { UnauthenticatedApiClientProvider } from '@/services/ApiClients';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheContent from '@/components/generics/TheContent.vue';

interface FrameworkInfo {
  framework: {
    id: string;
    ref: string;
  };
  name: string;
}

const router = useRouter();

const frameworks = ref<FrameworkInfo[]>([]);
const isLoading = ref<boolean>(true);
const error = ref<string | null>(null);

/**
 * Loads the list of frameworks from the API.
 * @returns Promise<void>
 */
const loadFrameworks = async (): Promise<void> => {
  try {
    isLoading.value = true;
    error.value = null;
    const apiClientProvider = new UnauthenticatedApiClientProvider();
    const response = await apiClientProvider.specificationController.listFrameworkSpecifications();
    frameworks.value = response.data;
  } catch (err) {
    console.error('Error loading frameworks:', err);
    error.value = err instanceof Error ? err.message : 'Unknown error occurred';
  } finally {
    isLoading.value = false;
  }
};

/**
 * Navigates to the framework documentation page in a new tab.
 * @param frameworkId - The ID of the framework to navigate to
 */
const navigateToFramework = (frameworkId: string): void => {
  window.open(`/documentation/frameworks/${frameworkId}`, '_blank');
};

onMounted(async () => {
  await loadFrameworks();
});
</script>

<!-- Only structural layout styles, no PrimeVue overrides -->
<style scoped>
.documentation-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.structural-container {
  max-width: 1200px;
  margin: 0 auto;
}

.frameworks-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}
</style>
