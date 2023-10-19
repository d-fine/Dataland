<template>
  <table v-if="dataPointDisplay">
    <tr>
      <td>Value</td>
      <td>{{ dataPointDisplay.value }}</td>
    </tr>
    <tr>
      <td>Quality</td>
      <td>{{ dataPointDisplay.quality }}</td>
    </tr>
    <tr>
      <td>Data source</td>
      <td>
        <DocumentLink
          :label="dataSourceLabel"
          :download-name="dataPointDisplay.dataSource.fileName"
          :file-reference="dataPointDisplay.dataSource.fileReference"
          show-icon
        />
      </td>
    </tr>
    <tr v-if="dataPointDisplay.comment">
      <td>Comment</td>
      <td>{{ dataPointDisplay.comment }}</td>
    </tr>
  </table>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { type DynamicDialogInstance } from "primevue/dynamicdialogoptions";
import { type ExtendedDocumentReference } from "@clients/backend";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  components: { DocumentLink },
  inject: ["dialogRef"],
  name: "DataPointDataTable",
  data() {
    return {
      dataPointDisplay: undefined as DataPointDisplay | undefined,
    };
  },
  mounted() {
    const dialogRefToDisplay = this.dialogRef as DynamicDialogInstance;
    const dialogRefData = dialogRefToDisplay.data as {
      dataPointDisplay: DataPointDisplay;
    };
    console.log(dialogRefData.dataPointDisplay);
    this.dataPointDisplay = dialogRefData.dataPointDisplay;
  },
  computed: {
    dataSourceLabel() {
      return this.dataPointDisplay.dataSource.page
        ? `${this.dataPointDisplay.dataSource.fileName}, page ${this.dataPointDisplay.dataSource.page}`
        : this.dataPointDisplay.dataSource.fileName;
    },
  },
});

interface DataPointDisplay {
  value: string;
  quality: string;
  dataSource: ExtendedDocumentReference;
  comment?: string;
}
</script>
