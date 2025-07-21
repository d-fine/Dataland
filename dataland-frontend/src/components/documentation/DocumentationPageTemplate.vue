<template>
  <BaseDocumentationLayout
    :page-title="pageTitle"
    :waiting-for-data="waitingForData"
    :error="error"
    :specification-data="specificationData"
  >
    <template #content="{ specificationData }">
      <SpecificationDetails
        :title="pageTitle"
        :specification-data="specificationData"
      >
        <template #fields="{ specificationData }">
          <slot name="fields" :specification-data="specificationData" />
        </template>
      </SpecificationDetails>

      <slot name="additional-sections" :specification-data="specificationData" />
    </template>
  </BaseDocumentationLayout>
</template>

<script setup lang="ts">
import BaseDocumentationLayout from './BaseDocumentationLayout.vue';
import SpecificationDetails from './SpecificationDetails.vue';

interface Props {
  pageTitle: string;
  waitingForData: boolean;
  error: string | null;
  specificationData: any;
}

defineProps<Props>();
</script>