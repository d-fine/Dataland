<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="dataPointDisplay" class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr v-if="dataPointDisplay.value">
            <th class="headers-bg width-auto"><span class="table-left-label">Value</span></th>
            <td>{{ dataPointDisplay.value }}</td>
          </tr>
          <tr v-if="dataPointDisplay.quality">
            <th class="headers-bg width-auto"><span class="table-left-label">Quality</span></th>
            <td>{{ dataPointDisplay.quality }}</td>
          </tr>
          <tr v-if="dataPointDisplay.dataSource">
            <th class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentLink
                :label="dataSourceLabel"
                :download-name="dataPointDisplay.dataSource.fileName ?? dataPointDisplay.dataSource.fileReference"
                :file-reference="dataPointDisplay.dataSource.fileReference"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataPointDisplay.comment">
            <th class="headers-bg width-auto"><span class="table-left-label">Comment</span></th>
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
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { type DataPointDisplay } from "@/utils/DataPoint";

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
