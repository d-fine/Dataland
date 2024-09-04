<template>
  <RequestableDatasetsTable
    :metaInfoOfAvailableDatasets="metaInfoOfAvailableDatasets"
    :dataType="dataType"
    :companyId="companyId"
    @submittedAccessRequests="closeDialog"
  />
</template>

<script setup lang="ts">
import RequestableDatasetsTable from '@/components/resources/frameworkDataSearch/frameworkPanel/RequestableDatasetsTable.vue';
import { inject, type Ref, ref } from 'vue';
import { type DataMetaInformation } from '@clients/backend';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');

const dialogRefData = dialogRef?.value.data as {
  metaInfoOfAvailableDatasets: DataMetaInformation[];
  dataType: string;
  companyId: string;
};

const metaInfoOfAvailableDatasets = ref(dialogRefData.metaInfoOfAvailableDatasets);
const dataType = ref(dialogRefData.dataType);
const companyId = ref(dialogRefData.companyId);

/**
 * Closes the modal.
 */
function closeDialog(): void {
  dialogRef?.value.close();
}
</script>
