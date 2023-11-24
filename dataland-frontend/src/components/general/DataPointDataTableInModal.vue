<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="dataPointDisplay" class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <td class="headers-bg">Value</td>
            <td nowrap>{{ dataPointDisplay.value ?? "" }}</td>
          </tr>
          <tr v-if="dataPointDisplay.quality">
            <td class="headers-bg">Quality</td>
            <td>{{ dataPointDisplay.quality }}</td>
          </tr>
          <tr v-if="dataPointDisplay.dataSource">
            <td class="headers-bg">Data source</td>
            <td nowrap>
              <DocumentLink
                :label="dataSourceLabel"
                :download-name="dataPointDisplay.dataSource.fileName ?? dataPointDisplay.dataSource.fileReference"
                :file-reference="dataPointDisplay.dataSource.fileReference"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataPointDisplay.comment">
            <td class="headers-bg">Comment</td>
            <td>{{ dataPointDisplay.comment }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { type DataPointDisplay } from "@/utils/DataPoint";

export default defineComponent({
  components: { DocumentLink },
  name: "DataPointDataTableInModal",
  props: {
    dataPointDisplay: {
      type: Object as DataPointDisplay,
      require: true,
    },
  },
  computed: {
    dataSourceLabel() {
      return this.dataPointDisplay?.dataSource.page
        ? `${this.dataPointDisplay?.dataSource.fileName}, page ${this.dataPointDisplay?.dataSource.page}`
        : this.dataPointDisplay?.dataSource.fileName;
    },
  },
});
</script>

<style scoped lang="scss">
.p-datatable-table {
  border-spacing: 0;
  border-collapse: collapse;
  tr {
    border-bottom: 1px solid #e3e2df;
    border-top: 1px solid #e3e2df;
    &:first-child {
      border: none;
    }
    &:last-child {
      border: none;
    }
    td {
      padding: 0.5rem;
      border: none;
      &.headers-bg {
        width: 2rem;
        padding-right: 1rem;
      }
    }
  }
}
</style>
