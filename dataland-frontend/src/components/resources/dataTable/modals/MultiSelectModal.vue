<script lang="ts">
import { defineComponent } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
interface DialogRefData {
  label: string;
  values: Array<string>;
}
export default defineComponent({
  name: 'MultiSelectModal',
  inject: ['dialogRef'],
  components: { DataTable, Column },
  computed: {
    dialogRefData(): DialogRefData {
      const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
      return dialogRefToDisplay.data as DialogRefData;
    },
    listOfRowContents(): Array<{ value: string }> {
      return this.dialogRefData.values.map((it) => ({ value: it }));
    },
  },
});
</script>

<template>
  <DataTable :value="listOfRowContents">
    <Column field="value" header="Values" headerStyle="width: 15vw;"> </Column>
  </DataTable>
</template>
