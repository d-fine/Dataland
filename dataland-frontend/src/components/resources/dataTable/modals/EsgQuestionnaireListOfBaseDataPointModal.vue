<script setup lang="ts">
import { computed, inject, type Ref } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { type BaseDataPoint } from "@/utils/DataPoint";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

interface EsgQuestionnaireListOfBaseDataPointDialogData {
  label: string;
  input: Array<BaseDataPoint<string>>;
  descriptionColumnHeader: string;
  documentColumnHeader: string;
}

const dialogRef = inject<Ref<DynamicDialogInstance>>("dialogRef");
const dialogData = computed(() => {
  return dialogRef?.value?.data as EsgQuestionnaireListOfBaseDataPointDialogData;
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
        <DocumentLink
          :show-icon="!(data.dataSource == undefined)"
          :label="data.dataSource?.fileName"
          :download-name="data.dataSource?.fileName"
          :file-reference="data.dataSource?.fileReference"
        />
      </template>
    </Column>
  </DataTable>
</template>
