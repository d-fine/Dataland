<script setup lang="ts">
import { computed, inject, type Ref } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import { type BaseDataPoint } from '@/utils/DataPoint';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { type DataMetaInformation } from '@clients/backend';

interface ListOfBaseDataPointDialogData {
  label: string;
  input: Array<BaseDataPoint<string>>;
  descriptionColumnHeader: string;
  documentColumnHeader: string;
  metaInfo: DataMetaInformation;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const dialogData = computed(() => {
  return dialogRef?.value?.data as ListOfBaseDataPointDialogData;
});

const tableData = computed(() => {
  return dialogData.value.input;
});
</script>

<template>
  <DataTable :value="tableData">
    <Column field="value" :header="dialogData.descriptionColumnHeader" headerStyle="width: 15vw;"> </Column>
    <Column :header="dialogData.documentColumnHeader" headerStyle="width: 15vw;">
      <template #body="{ data }">
        <DocumentDownloadLink
          :document-download-info="{
            downloadName: data.dataSource?.fileName,
            fileReference: data.dataSource?.fileReference,
            dataId: dialogData.metaInfo?.dataId,
            dataType: dialogData.metaInfo?.dataType,
          }"
          :label="data.dataSource?.fileName"
          :show-icon="!(data.dataSource == undefined)"
        />
      </template>
    </Column>
  </DataTable>
</template>
