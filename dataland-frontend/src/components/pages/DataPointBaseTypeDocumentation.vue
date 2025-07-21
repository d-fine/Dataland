<template>
  <DocumentationPageTemplate
    :page-title="pageTitle"
    :waiting-for-data="waitingForData"
    :error="error"
    :specification-data="specificationData"
  >
    <template #fields="{ specificationData }">
      <SpecificationField
        label="ID"
        :value="specificationData.dataPointBaseType.id"
        type="monospace"
      />
      
      <SpecificationField
        label="Reference"
        :value="specificationData.dataPointBaseType.ref"
        type="link"
        :clickable="true"
      />
      
      <SpecificationField
        label="Name"
        :value="specificationData.name"
        type="text"
      />
      
      <div class="specification-field mb-3">
        <label class="field-label text-900 font-semibold block mb-1">Business Definition:</label>
        <p class="field-value text-600 line-height-3">{{ specificationData.businessDefinition }}</p>
      </div>
      
      <SpecificationField
        label="Validated By"
        :value="specificationData.validatedBy"
        type="monospace"
      />
    </template>

    <template #additional-sections="{ specificationData }">
      <UsedBySection
        title="Used By Data Point Types"
        :items="specificationData.usedBy"
        empty-message="No data point types use this base type"
      />

      <SchemaRenderer
        title="Example Structure"
        :data="specificationData.example"
        :show-raw-label="false"
      />
    </template>
  </DocumentationPageTemplate>
</template>

<script setup lang="ts">
import DocumentationPageTemplate from '@/components/documentation/DocumentationPageTemplate.vue';
import SpecificationField from '@/components/documentation/SpecificationField.vue';
import UsedBySection from '@/components/documentation/UsedBySection.vue';
import SchemaRenderer from '@/components/documentation/SchemaRenderer.vue';
import { useDocumentationPage } from '@/composables/useDocumentationPage';
import type { DataPointBaseTypeSpecification } from '@/types/documentation';

interface Props {
  baseTypeId: string;
}

const props = defineProps<Props>();

const { pageTitle, waitingForData, error, specificationData } = 
  useDocumentationPage<DataPointBaseTypeSpecification>({
    pageType: 'data-point-base-types',
    entityId: props.baseTypeId,
  });
</script>

<style lang="scss" scoped>
@use '@/assets/scss/documentation';
</style>