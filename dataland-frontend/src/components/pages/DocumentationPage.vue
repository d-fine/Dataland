<template>
  <TheHeader />
  <TheContent>
    <div class="documentation-structural-container">
      <div class="documentation-header-container">
        <h1>Documentation</h1>
        <span>
          Welcome to the Dataland documentation. This page provides an overview of the available frameworks and resources on the platform.
        </span>
      </div>

      <div v-if="isLoading" class="documentation-loading-container">
        <ProgressSpinner />
        <Message severity="info" variant="simple" size="small" data-test="frameworksLoading">
          Loading frameworks...
        </Message>
      </div>

      <Message v-else-if="error" severity="error" variant="simple" size="small" data-test="frameworksError">
        Error loading frameworks: {{ error }}
      </Message>

      <div v-else class="frameworks-structural-container">
        <div class="frameworks-header-container">
          <h2>Available Frameworks</h2>
          <span>Below is a list of all available data frameworks on the Dataland platform:</span>
        </div>

        <Message v-if="frameworks.length === 0" severity="info" variant="simple" size="small" data-test="noFrameworks">
          No frameworks available at the moment.
        </Message>

        <div v-else class="frameworks-list-container">
          <Card
            v-for="framework in frameworks"
            :key="framework.framework.id"
            @click="navigateToFramework(framework.framework.id)"
            class="framework-card"
            data-test="frameworkCard"
          >
            <template #content>
              <div class="framework-card-content">
                <h3>{{ framework.name }}</h3>
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
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheContent from '@/components/generics/TheContent.vue';
import { UnauthenticatedApiClientProvider } from '@/services/ApiClients';

interface Framework {
  id: string;
  ref: string;
}

interface FrameworkInfo {
  framework: Framework;
  name: string;
}

const router = useRouter();
const frameworks = ref<FrameworkInfo[]>([]);
const isLoading = ref<boolean>(true);
const error = ref<string | null>(null);

/**
 * Loads the list of available frameworks from the API.
 * No input parameters. Sets frameworks, isLoading, and error refs.
 * Always awaited on mount.
 */
const loadFrameworks = async (): Promise<void> => {
  try {
    isLoading.value = true;
    error.value = null;
    const apiClientProvider = new UnauthenticatedApiClientProvider();
    const response = await apiClientProvider.specificationController.listFrameworkSpecifications();
    frameworks.value = response.data;
  } catch (err) {
    // Handles error and sets error ref
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

<!--
  No scoped styles are used except for structural layout containers.
  PrimeVue default styling and design tokens are used for all components.
-->
<style scoped>
.documentation-structural-container {
  min-height: calc(100vh - 4rem);
  display: flex;
  flex-direction: column;
  gap: 2rem;
}
.frameworks-list-container {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}
.framework-card {
  cursor: pointer;
  min-width: 220px;
  max-width: 320px;
}
.framework-card-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
