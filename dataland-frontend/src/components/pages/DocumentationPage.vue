<template>
  <TheHeader />
  <div class="documentation-page">
    <div class="container mx-auto" style="padding: var(--spacing-lg) var(--spacing-md)">
      <div style="margin-bottom: var(--spacing-lg)">
        <h1
          style="
            font-size: var(--font-size-xxl);
            font-weight: var(--font-weight-bold);
            color: var(--p-surface-900);
            margin-bottom: var(--spacing-md);
          "
        >
          Documentation
        </h1>
        <p style="font-size: var(--font-size-lg); color: var(--p-surface-600)">
          Welcome to the Dataland documentation. This page provides an overview of the available frameworks and resources on the platform.
        </p>
      </div>

      <div v-if="isLoading" style="text-align: center; padding: var(--spacing-xl) 0">
        <ProgressSpinner />
        <p style="margin-top: var(--spacing-md); color: var(--p-surface-600)">Loading frameworks...</p>
      </div>

      <div v-else-if="error" style="text-align: center; padding: var(--spacing-xl) 0">
        <Message severity="error" :closable="false">
          <p>Error loading frameworks: {{ error }}</p>
        </Message>
      </div>

      <div v-else>
        <div style="margin-bottom: var(--spacing-lg)">
          <h2
            style="
              font-size: var(--font-size-xl);
              font-weight: var(--font-weight-semibold);
              color: var(--p-surface-900);
              margin-bottom: var(--spacing-md);
            "
          >
            Available Frameworks
          </h2>
          <p style="font-size: var(--font-size-base); color: var(--p-surface-600); margin-bottom: var(--spacing-md)">
            Below is a list of all available data frameworks on the Dataland platform:
          </p>
        </div>

        <div v-if="frameworks.length === 0" style="text-align: center; padding: var(--spacing-xl) 0">
          <Message severity="info" :closable="false">
            <p>No frameworks available at the moment.</p>
          </Message>
        </div>

        <div v-else style="display: grid; gap: var(--spacing-md)">
          <Card
            v-for="framework in frameworks"
            :key="framework.framework.id"
            style="cursor: pointer; transition: transform 0.2s ease, box-shadow 0.2s ease"
            @click="navigateToFramework(framework.framework.id)"
            @mouseenter="$event.currentTarget.style.transform = 'translateY(-2px)'"
            @mouseleave="$event.currentTarget.style.transform = 'translateY(0)'"
          >
            <template #content>
              <div style="padding: var(--spacing-md)">
                <h3
                  style="
                    font-size: var(--font-size-lg);
                    font-weight: var(--font-weight-semibold);
                    color: var(--p-surface-900);
                    margin-bottom: var(--spacing-sm);
                  "
                >
                  {{ framework.name }}
                </h3>
                <p style="color: var(--p-surface-600); margin-bottom: var(--spacing-sm)">
                  Framework ID: {{ framework.framework.id }}
                </p>
                <p style="color: var(--p-surface-500); font-size: var(--font-size-sm)">
                  Click to view detailed documentation for this framework
                </p>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </div>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import Message from 'primevue/message';
import ProgressSpinner from 'primevue/progressspinner';
import Card from 'primevue/card';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
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
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const frameworks = ref<FrameworkInfo[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

const loadFrameworks = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const response = await apiClientProvider.apiClients.specificationController.getFrameworks();
    frameworks.value = response.data;
  } catch (err) {
    console.error('Error loading frameworks:', err);
    error.value = err instanceof Error ? err.message : 'Unknown error occurred';
  } finally {
    isLoading.value = false;
  }
};

const navigateToFramework = (frameworkId: string) => {
  router.push(`/documentation/frameworks/${frameworkId}`);
};

onMounted(() => {
  loadFrameworks();
});
</script>

<style scoped>
.documentation-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--p-surface-ground);
  padding-top: var(--spacing-xl);
}

.container {
  max-width: 1200px;
}

.p-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
</style>