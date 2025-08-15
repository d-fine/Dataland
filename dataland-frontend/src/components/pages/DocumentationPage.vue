<template>
  <TheHeader />
  <div class="documentation-page">
    <div class="container mx-auto">
      <div>
        <h1>Documentation</h1>
        <p>
          Welcome to the Dataland documentation. This page provides an overview of the available frameworks and
          resources on the platform.
        </p>
      </div>

      <div v-if="isLoading">
        <ProgressSpinner />
        <p>Loading frameworks...</p>
      </div>

      <div v-else-if="error">
        <Message severity="error" :closable="false">
          <p>Error loading frameworks: {{ error }}</p>
        </Message>
      </div>

      <div v-else>
        <div>
          <h2>Available Frameworks</h2>
          <p>Below is a list of all available data frameworks on the Dataland platform:</p>
        </div>

        <div v-if="frameworks.length === 0">
          <Message severity="info" :closable="false">
            <p>No frameworks available at the moment.</p>
          </Message>
        </div>

        <div>
          <div>
            <Card
              v-for="framework in frameworks"
              :key="framework.framework.id"
              @click="navigateToFramework(framework.framework.id)"
            >
              <template #content>
                <div>
                  <h3>
                    {{ framework.name }}
                  </h3>
                </div>
              </template>
            </Card>
          </div>
        </div>
      </div>
    </div>
  </div>
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

interface FrameworkInfo {
  framework: {
    id: string;
    ref: string;
  };
  name: string;
}

const router = useRouter();

const frameworks = ref<FrameworkInfo[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const loadFrameworks = async () => {
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

const navigateToFramework = (frameworkId: string) => {
  window.open(`/documentation/frameworks/${frameworkId}`, '_blank');
};

onMounted(() => {
  loadFrameworks();
});
</script>

<style scoped>
.documentation-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.container {
  max-width: 1200px;
}
</style>
