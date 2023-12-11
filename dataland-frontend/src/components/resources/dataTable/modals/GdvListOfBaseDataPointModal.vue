<script setup lang="ts">
import { computed, inject, type Ref } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { type BaseDataPoint } from "@/utils/DataPoint";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

interface GdvListOfBaseDataPointDialogData {
  label: string;
  input: Array<BaseDataPoint<string>>;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const dialogData = computed(() => {
  return dialogRef?.value?.data as GdvListOfBaseDataPointDialogData;
});

const tableData = computed(() => {
  return dialogData.value.input;
});
</script>

<template>
  <DataTable :value="tableData">
    <Column field="value" header="Certificate Name" headerStyle="width: 15vw;"> </Column>
    <Column header="Certificate" headerStyle="width: 15vw;">
      <template #body="{ data }">
        <DocumentLink
          label="Download Certificate"
          :download-name="data.dataSource.fileName"
          :file-reference="data.dataSource.fileReference"
          show-icon
        />
      </template>
    </Column>
  </DataTable>
</template>
