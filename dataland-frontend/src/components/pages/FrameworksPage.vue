<template>
  <TheHeader />
  <TheContent>
    <div class="frameworks-page">
      <div class="structural-container">
        <h1 data-test="frameworkTitle">{{ framework?.name || 'Framework' }} Documentation</h1>
        <p>
          This page displays information about the {{ frameworkId }} framework used within the Dataland platform.
        </p>
      </div>

      <div v-if="isLoading" data-test="frameworkLoading">
        <ProgressSpinner />
        <Message severity="info" variant="simple" size="small" data-test="frameworkLoadingMessage">
          Loading framework...
        </Message>
      </div>

      <Message v-else-if="error" severity="error" variant="simple" size="small" data-test="frameworkError">
        Error loading framework: {{ error }}
      </Message>

      <Message v-else-if="!framework" severity="info" variant="simple" size="small" data-test="frameworkNotFound">
        Framework not found.
      </Message>

      <div v-else data-test="frameworkContent">
        <FrameworkCard :framework="framework">
          <ExpandableSection
            v-if="framework.businessDefinition"
            title="Business Definition"
            :is-expanded="expandedSubsections.businessDefinition || false"
            @toggle="toggleSubsection('businessDefinition')"
            data-test="businessDefinitionSection"
          >
            <BusinessDefinitionContent :business-definition="framework.businessDefinition" />
          </ExpandableSection>

          <ExpandableSection
            v-if="framework.schema"
            title="Schema"
            :is-expanded="expandedSubsections.schema || false"
            @toggle="toggleSubsection('schema')"
            data-test="schemaSection"
          >
            <SchemaContent :schema="framework.schema" :framework-name="framework.name" />
          </ExpandableSection>
        </FrameworkCard>
      </div>
    </div>
  </TheContent>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Message from 'primevue/message';
import ProgressSpinner from 'primevue/progressspinner';
import { UnauthenticatedApiClientProvider } from '@/services/ApiClients';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import TheContent from '@/components/generics/TheContent.vue';
import FrameworkCard from '@/components/resources/datapoints/FrameworkCard.vue';
import ExpandableSection from '@/components/resources/datapoints/ExpandableSection.vue';
import BusinessDefinitionContent from '@/components/resources/datapoints/BusinessDefinitionContent.vue';
import SchemaContent from '@/components/resources/datapoints/SchemaContent.vue';
import type { Framework } from '@/components/resources/datapoints/types';

interface Props {
  frameworkId: string;
}

const props = defineProps<Props>();

const framework = ref<Framework | null>(null);
const isLoading = ref<boolean>(true);
const error = ref<string | null>(null);
const expandedSubsections = ref<Record<string, boolean>>({});

/**
 * Loads the framework from the API.
 * @returns Promise<void>
 */
const loadFramework = async (): Promise<void> => {
  try {
    isLoading.value = true;
    error.value = null;
    const apiClientProvider = new UnauthenticatedApiClientProvider();
    const data = await apiClientProvider.specificationController.getFrameworkSpecification(props.frameworkId);
    framework.value = data.data;
  } catch (err) {
    console.error('Error loading framework:', err);
    error.value = err instanceof Error ? err.message : 'Unknown error occurred';
  } finally {
    isLoading.value = false;
  }
};

/**
 * Toggles the expanded state of a subsection.
 * @param subsectionKey - The key of the subsection to toggle
 */
const toggleSubsection = (subsectionKey: string): void => {
  expandedSubsections.value[subsectionKey] = !expandedSubsections.value[subsectionKey];
};

onMounted(async () => {
  await loadFramework();
});
</script>

<!-- Only structural layout styles, no PrimeVue overrides -->
<style scoped>
.frameworks-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.structural-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
