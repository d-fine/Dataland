<template>
  <TheHeader />
  <div class="datapoint-basetypes-page">
    <div class="container mx-auto">
      <div>
        <h1>{{ dataPointBaseType?.name || 'Data Point Base Type' }} Documentation</h1>
        <p>
          This page displays information about the {{ dataPointBaseTypeId }} data point base type used within the
          Dataland platform.
        </p>
      </div>

      <div v-if="isLoading">
        <ProgressSpinner />
        <p>Loading data point base type...</p>
      </div>

      <div v-else-if="error">
        <Message severity="error" :closable="false">
          <p>Error loading data point base type: {{ error }}</p>
        </Message>
      </div>

      <div v-else-if="!dataPointBaseType">
        <Message severity="info" :closable="false">
          <p>Data point base type not found.</p>
        </Message>
      </div>

      <div v-else>
        <DataPointBaseTypeCard :base-type="dataPointBaseType">
          <ExpandableSection
            v-if="dataPointBaseType.businessDefinition"
            title="Business Definition"
            :is-expanded="expandedSubsections.businessDefinition || false"
            @toggle="toggleSubsection('businessDefinition')"
          >
            <BusinessDefinitionContent :business-definition="dataPointBaseType.businessDefinition" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.validatedBy"
            title="Validated By"
            :is-expanded="expandedSubsections.validatedBy || false"
            @toggle="toggleSubsection('validatedBy')"
          >
            <ValidatedByContent :validated-by="dataPointBaseType.validatedBy" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.example"
            title="Example"
            :is-expanded="expandedSubsections.example || false"
            @toggle="toggleSubsection('example')"
          >
            <ExampleContent :example="dataPointBaseType.example" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPointBaseType.usedBy && dataPointBaseType.usedBy.length > 0"
            title="Used By Data Point Types"
            :is-expanded="expandedSubsections.usedBy || false"
            @toggle="toggleSubsection('usedBy')"
          >
            <UsedByContent :used-by="dataPointBaseType.usedBy" />
          </ExpandableSection>
        </DataPointBaseTypeCard>
      </div>
    </div>
  </div>
  <TheFooter />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import Message from 'primevue/message';
import ProgressSpinner from 'primevue/progressspinner';
import { UnauthenticatedApiClientProvider } from '@/services/ApiClients';
import TheHeader from '@/components/generics/TheHeader.vue';
import TheFooter from '@/components/generics/TheFooter.vue';
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
  value?: any;
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
const isLoading = ref(true);
const error = ref<string | null>(null);
const expandedSubsections = ref<Record<string, boolean>>({});

const loadDataPointBaseType = async () => {
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

const toggleSubsection = (subsectionKey: string) => {
  expandedSubsections.value[subsectionKey] = !expandedSubsections.value[subsectionKey];
};

onMounted(() => {
  loadDataPointBaseType();
});
</script>

<style scoped>
.datapoint-basetypes-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.container {
  max-width: 1200px;
}
</style>
