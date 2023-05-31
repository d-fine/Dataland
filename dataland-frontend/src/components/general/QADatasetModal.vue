<template>
  <div>
    <pre id="dataset-container">{{ datasetAsJson }}</pre>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import {DynamicDialogInstance} from "primevue/dynamicdialogoptions";

export default defineComponent( {
  inject: ["dialogRef"],
  name: "QADatasetModal",
  data() {
    return {
      dataSetToReview: null as unknown as object,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      dataSetToReview: object;
    };
    this.dataSetToReview = dialogRefData.dataSetToReview;
  },
  computed: {
    datasetAsJson(): string {
      return JSON.stringify(this.dataSetToReview, null, 2);
    },
  },
});
</script>