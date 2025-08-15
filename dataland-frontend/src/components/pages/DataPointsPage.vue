<template>
  <TheHeader />
  <div class="datapoints-page">
    <div class="container mx-auto">
      <div>
        <h1>{{ dataPoint?.name || 'Data Point' }} Documentation</h1>
        <p>
          This page displays information about the {{ dataPointTypeId }} data point type used within the Dataland
          platform.
        </p>
      </div>

      <div v-if="isLoading">
        <ProgressSpinner />
        <p>Loading data point...</p>
      </div>

      <div v-else-if="error">
        <Message severity="error" :closable="false">
          <p>Error loading data point: {{ error }}</p>
        </Message>
      </div>

      <div v-else-if="!dataPoint">
        <Message severity="info" :closable="false">
          <p>Data point not found.</p>
        </Message>
      </div>

      <div v-else>
        <DataPointCard :data-point="dataPoint">
          <ExpandableSection
            v-if="dataPoint.businessDefinition"
            title="Business Definition"
            :is-expanded="expandedSubsections.businessDefinition || false"
            @toggle="toggleSubsection('businessDefinition')"
          >
            <BusinessDefinitionContent :business-definition="dataPoint.businessDefinition" />
          </ExpandableSection>

          <ExpandableSection
            title="Base Type"
            :is-expanded="expandedSubsections.baseType || false"
            @toggle="toggleSubsection('baseType')"
          >
            <TypeInfoContent :type-info="dataPoint.dataPointBaseType" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPoint.usedBy && dataPoint.usedBy.length > 0"
            title="Used By Frameworks"
            :is-expanded="expandedSubsections.usedBy || false"
            @toggle="toggleSubsection('usedBy')"
          >
            <FrameworksContent :frameworks="dataPoint.usedBy" />
          </ExpandableSection>

          <ExpandableSection
            v-if="dataPoint.constraints && dataPoint.constraints.length > 0"
            title="Constraints"
            :is-expanded="expandedSubsections.constraints || false"
            @toggle="toggleSubsection('constraints')"
          >
            <ConstraintsContent :constraints="dataPoint.constraints" />
          </ExpandableSection>
        </DataPointCard>
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
import DataPointCard from '@/components/resources/datapoints/DataPointCard.vue';
import ExpandableSection from '@/components/resources/datapoints/ExpandableSection.vue';
import BusinessDefinitionContent from '@/components/resources/datapoints/BusinessDefinitionContent.vue';
import TypeInfoContent from '@/components/resources/datapoints/TypeInfoContent.vue';
import FrameworksContent from '@/components/resources/datapoints/FrameworksContent.vue';
import ConstraintsContent from '@/components/resources/datapoints/ConstraintsContent.vue';

interface DataPointType {
  id: string;
  ref?: string;
}

interface Framework {
  id: string;
  ref?: string;
}

interface DataPoint {
  dataPointType: DataPointType;
  name: string;
  businessDefinition?: string;
  dataPointBaseType: DataPointType;
  usedBy?: Framework[];
  constraints?: string[];
}

interface Props {
  dataPointTypeId: string;
}

const props = defineProps<Props>();

const dataPoint = ref<DataPoint | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const expandedSubsections = ref<Record<string, boolean>>({});

const loadDataPoint = async () => {
  try {
    isLoading.value = true;
    error.value = null;

    const apiClientProvider = new UnauthenticatedApiClientProvider();
    const data = await apiClientProvider.specificationController.getDataPointTypeSpecification(props.dataPointTypeId);
    dataPoint.value = data.data;
  } catch (err) {
    console.error('Error loading data point:', err);
    error.value = err instanceof Error ? err.message : 'Unknown error occurred';
  } finally {
    isLoading.value = false;
  }
};

const toggleSubsection = (subsectionKey: string) => {
  expandedSubsections.value[subsectionKey] = !expandedSubsections.value[subsectionKey];
};

onMounted(() => {
  loadDataPoint();
});
</script>

<style scoped>
.datapoints-page {
  min-height: calc(100vh - 4rem);
  background-color: var(--surface-ground);
  padding-top: var(--spacing-xl);
}

.container {
  max-width: 1200px;
}
</style>
