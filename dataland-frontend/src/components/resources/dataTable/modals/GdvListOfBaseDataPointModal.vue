<script setup lang="ts">
import { computed, inject, type Ref } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";

import DataTable from "primevue/datatable";
import Column from "primevue/column";
import { type BaseDataPoint } from "@/utils/DataPoint";

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
    <Column field="value" header="Values" headerStyle="width: 15vw;"> </Column>
  </DataTable>
</template>
