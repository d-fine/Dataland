<template>
  <TheHeader />
  <div class="frameworks-page">
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
          {{ framework?.name || 'Framework' }} Documentation
        </h1>
        <p style="font-size: var(--font-size-lg); color: var(--p-surface-600)">
          This page displays information about the {{ frameworkId }} framework used within the Dataland platform.
        </p>
      </div>

      <div v-if="isLoading" style="text-align: center; padding: var(--spacing-xl) 0">
        <ProgressSpinner />
        <p style="margin-top: var(--spacing-md); color: var(--p-surface-600)">Loading framework...</p>
      </div>

      <div v-else-if="error" style="text-align: center; padding: var(--spacing-xl) 0">
        <Message severity="error" :closable="false">
          <p>Error loading framework: {{ error }}</p>
        </Message>
      </div>

      <div v-else-if="!framework" style="text-align: center; padding: var(--spacing-xl) 0">
        <Message severity="info" :closable="false">
          <p>Framework not found.</p>
        </Message>
      </div>

      <div v-else style="display: flex; flex-direction: column; gap: var(--spacing-xs)">
        <FrameworkCard :framework="framework">
          <ExpandableSection
            v-if="framework.businessDefinition"
            title="Business Definition"
            :is-expanded="expandedSubsections.businessDefinition || false"
            @toggle="toggleSubsection('businessDefinition')"
          >
            <BusinessDefinitionContent :business-definition="framework.businessDefinition" />
          </ExpandableSection>

          <ExpandableSection
            v-if="framework.schema"
            title="Schema"
            :is-expanded="expandedSubsections.schema || false"
            @toggle="toggleSubsection('schema')"
          >
            <SchemaContent :schema="framework.schema" :framework-name="framework.name" />
          </ExpandableSection>
        </FrameworkCard>
      </div>
    </div>
  </div>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import Message from 'primevue/message';
import ProgressSpinner from 'primevue/progressspinner';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import type Keycloak from 'keycloak-js';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
import FrameworkCard from '@/components/resources/datapoints/FrameworkCard.vue';
import ExpandableSection from '@/components/resources/datapoints/ExpandableSection.vue';
import BusinessDefinitionContent from '@/components/resources/datapoints/BusinessDefinitionContent.vue';
import SchemaContent from '@/components/resources/datapoints/SchemaContent.vue';
import type { Framework } from '@/components/resources/datapoints/types';

interface Props {
  frameworkId: string;
}

const props = defineProps<Props>();

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const framework = ref<Framework | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const expandedSubsections = ref<Record<string, boolean>>({});

const loadFramework = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const data = await apiClientProvider.apiClients.specificationController.getFrameworkSpecification(
      props.frameworkId
    );
    framework.value = data.data;
  } catch (err) {
    console.error('Error loading framework:', err);
    error.value = err instanceof Error ? err.message : 'Unknown error occurred';
  } finally {
    isLoading.value = false;
  }
};

const toggleSubsection = (subsectionKey: string) => {
  expandedSubsections.value[subsectionKey] = !expandedSubsections.value[subsectionKey];
};

onMounted(() => {
  loadFramework();
});
</script>

<style scoped>
.frameworks-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--p-surface-ground);
  padding-top: var(--spacing-xl);
}

.container {
  max-width: 1200px;
}
</style>
