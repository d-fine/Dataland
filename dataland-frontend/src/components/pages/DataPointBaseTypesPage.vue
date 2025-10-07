<template>
  <TheHeader />
  <TheContent>
    <div class="datapoint-basetypes-page">
      <div class="structural-container">
        <h1 data-test="baseTypeTitle">{{ dataPointBaseType?.name || 'Data Point Base Type' }} Documentation</h1>
        <p>
          This page displays information about the {{ dataPointBaseTypeId }} data point base type used within the Dataland platform.
        </p>
      </div>

      <div v-if="isLoading" data-test="baseTypeLoading">
        <ProgressSpinner />
        <Message severity="info" variant="simple" size="small" data-test="baseTypeLoadingMessage">
          Loading data point base type...
        </Message>
      </div>

      <Message v-else-if="error" severity="error" variant="simple" size="small" data-test="baseTypeError">
        Error loading data point base type: {{ error }}
      </Message>

      <Message v-else-if="!dataPointBaseType" severity="info" variant="simple" size="small" data-test="baseTypeNotFound">
        Data point base type not found.
      </Message>

      <div v-else data-test="baseTypeContent">
        <DataPointBaseTypeCard :base-type="dataPointBaseType">
          <ExpandableSection
            v-if="dataPointBaseType.businessDefinition"
            title="Business Definition"
            :is-expanded="expandedSubsections.businessDefinition || false"
            @toggle="toggleSubsection('businessDefinition')"
            data-test="businessDefinitionSection"
          >
            <BusinessDefinitionContent :business-definition="dataPointBaseType.businessDefinition" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.validatedBy"
            title="Validated By"
            :is-expanded="expandedSubsections.validatedBy || false"
            @toggle="toggleSubsection('validatedBy')"
            data-test="validatedBySection"
          >
            <ValidatedByContent :validated-by="dataPointBaseType.validatedBy" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.example"
            title="Example"
            :is-expanded="expandedSubsections.example || false"
            @toggle="toggleSubsection('example')"
            data-test="exampleSection"
          >
            <ExampleContent :example="dataPointBaseType.example" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.usedBy && dataPointBaseType.usedBy.length > 0"
            title="Used By Data Point Types"
            :is-expanded="expandedSubsections.usedBy || false"
            @toggle="toggleSubsection('usedBy')"
            data-test="usedBySection"
          >
            <UsedByContent :used-by="dataPointBaseType.usedBy" />
          </ExpandableSection>
        </DataPointBaseTypeCard>
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
import DataPointBaseTypeCard from '@/components/resources/datapoints/DataPointBaseTypeCard.vue';
import ExpandableSection from '@/components/resources/datapoints/ExpandableSection.vue';
import BusinessDefinitionContent from '@/components/resources/datapoints/BusinessDefinitionContent.vue';
import ValidatedByContent from '@/components/resources/datapoints/ValidatedByContent.vue';
import ExampleContent from '@/components/resources/datapoints/ExampleContent.vue';
import UsedByContent from '@/components/resources/datapoints/UsedByContent.vue';

interface DataPointBaseTypeId {
  id: string;
  ref?: string;
}

interface DataSource {
  page?: string;
  tagName?: string;
  fileName?: string;
  fileReference?: string;
  publicationDate?: string;
}

interface Example {
  value?: string;
  quality?: string;
  comment?: string;
  dataSource?: DataSource;
}

interface UsedByItem {
  id: string;
  ref?: string;
}

interface DataPointBaseTypeData {
  dataPointBaseType: DataPointBaseTypeId;
  name: string;
  businessDefinition?: string;
  validatedBy?: string;
  example?: Example;
  usedBy?: UsedByItem[];
}

interface Props {
  dataPointBaseTypeId: string;
}

const props = defineProps<Props>();

const dataPointBaseType = ref<DataPointBaseTypeData | null>(null);
const isLoading = ref<boolean>(true);
const error = ref<string | null>(null);
const expandedSubsections = ref<Record<string, boolean>>({});

/**
 * Loads the data point base type from the API.
 * @returns Promise<void>
 */
const loadDataPointBaseType = async (): Promise<void> => {
  try {
    isLoading.value = true;
    error.value = null;
    const apiClientProvider = new UnauthenticatedApiClientProvider();
    const data = await apiClientProvider.specificationController.getDataPointBaseType(props.dataPointBaseTypeId);
    dataPointBaseType.value = data.data;
  } catch (err) {
    console.error('Error loading data point base type:', err);
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
  await loadDataPointBaseType();
});
</script>

<!-- Only structural layout styles, no PrimeVue overrides -->
<style scoped>
.datapoint-basetypes-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.structural-container {
  max-width: 1200px;
  margin: 0 auto;
}
</style>
