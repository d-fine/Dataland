<template>
  <DocumentationPageTemplate
    :page-title="pageTitle"
    :waiting-for-data="waitingForData"
    :error="error"
    :specification-data="specificationData"
  >
    <template #fields="{ specificationData }">
      <SpecificationField
        label="Framework ID"
        :value="specificationData.framework.id"
        type="text"
      />
      
      <SpecificationField
        label="Framework Reference"
        :value="specificationData.framework.ref"
        type="monospace"
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
        label="Referenced Report JSON Path"
        :value="specificationData.referencedReportJsonPath"
        type="monospace"
      />
    </template>

    <template #additional-sections="{ specificationData }">
      <SchemaRenderer
        title="Schema Structure"
        :data="specificationData.schema"
        @link-click="handleLinkClick"
      />
    </template>
  </DocumentationPageTemplate>
</template>

<script setup lang="ts">
import DocumentationPageTemplate from '@/components/documentation/DocumentationPageTemplate.vue';
import SpecificationField from '@/components/documentation/SpecificationField.vue';
import SchemaRenderer from '@/components/documentation/SchemaRenderer.vue';
import { useDocumentationPage } from '@/composables/useDocumentationPage';
import { handleSpecificationLink } from '@/utils/linkHandler';
import type { SfdrFrameworkSpecification } from '@/types/documentation';

interface Props {
  frameworkId: string;
}

const props = defineProps<Props>();

const { pageTitle, waitingForData, error, specificationData } = 
  useDocumentationPage<SfdrFrameworkSpecification>({
    pageType: 'frameworks',
    entityId: props.frameworkId,
  });

const handleLinkClick = (ref: string): void => {
  handleSpecificationLink(ref);
};
</script>

<style lang="scss" scoped>
@use '@/assets/scss/documentation';
</style>