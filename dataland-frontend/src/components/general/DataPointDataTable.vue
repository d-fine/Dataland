<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="dataPointDisplay" class="p-datatable-table">
        <tbody class="p-datatable-body">
          <tr>
            <td class="headers-bg width-auto"><span class="table-left-label">Value</span></td>
            <td>{{ dataPointDisplay.value }}</td>
          </tr>
          <tr>
            <td class="headers-bg width-auto"><span class="table-left-label">Quality</span></td>
            <td>{{ dataPointDisplay.quality }}</td>
          </tr>
          <tr>
            <td class="headers-bg width-auto"><span class="table-left-label">Data source</span></td>
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
            <td class="headers-bg width-auto"><span class="table-left-label">Comment</span></td>
            <td>{{ dataPointDisplay.comment }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
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

<style scoped lang="scss">
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
}
.width-auto {
  width: auto;
}
</style>
