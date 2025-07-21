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
        :value="specificationData.dataPointType.id"
        type="monospace"
      />
      
      <SpecificationField
        label="Reference"
        :value="specificationData.dataPointType.ref"
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
      
      <div class="specification-field mb-3">
        <label class="field-label text-900 font-semibold block mb-1">Data Point Base Type:</label>
        <div class="field-value text-600">
          <div class="mb-2">
            <span class="font-family-monospace">{{ specificationData.dataPointBaseType.id }}</span>
          </div>
          <div class="mb-2">
            <button @click="handleLinkClick(specificationData.dataPointBaseType.ref)" class="text-blue-600 hover:text-blue-800 text-sm font-family-monospace bg-transparent border-none p-0 cursor-pointer">
              {{ specificationData.dataPointBaseType.ref }}
            </button>
          </div>
        </div>
      </div>
      
      <div class="specification-field mb-3">
        <label class="field-label text-900 font-semibold block mb-1">Used By:</label>
        <div class="field-value text-600">
          <div
            v-for="framework in specificationData.usedBy"
            :key="framework.id"
            class="mb-2"
          >
            <SpecificationBadge :text="framework.id" :uppercase="true" class="mr-2" />
            <button @click="handleLinkClick(framework.ref)" class="text-blue-600 hover:text-blue-800 text-sm font-family-monospace bg-transparent border-none p-0 cursor-pointer">
              {{ framework.ref }}
            </button>
          </div>
        </div>
      </div>

      <div v-if="specificationData.constraints !== null && specificationData.constraints !== undefined" class="specification-field mb-3">
        <label class="field-label text-900 font-semibold block mb-1">Constraints:</label>
        <pre class="field-value text-600 font-family-monospace bg-gray-50 p-2 border-round overflow-auto">{{ typeof specificationData.constraints === 'object' ? JSON.stringify(specificationData.constraints, null, 2) : specificationData.constraints }}</pre>
      </div>
    </template>
  </DocumentationPageTemplate>
</template>

<script setup lang="ts">
import DocumentationPageTemplate from '@/components/documentation/DocumentationPageTemplate.vue';
import SpecificationField from '@/components/documentation/SpecificationField.vue';
import SpecificationBadge from '@/components/documentation/SpecificationBadge.vue';
import { useDocumentationPage } from '@/composables/useDocumentationPage';
import { handleSpecificationLink } from '@/utils/linkHandler';
import type { DataPointTypeSpecification } from '@/types/documentation';

interface Props {
  dataPointTypeId: string;
}

const props = defineProps<Props>();

const { pageTitle, waitingForData, error, specificationData } = 
  useDocumentationPage<DataPointTypeSpecification>({
    pageType: 'data-point-types',
    entityId: props.dataPointTypeId,
  });

const handleLinkClick = (ref: string): void => {
  handleSpecificationLink(ref);
};
</script>

<style lang="scss" scoped>
@use '@/assets/scss/documentation';
</style>