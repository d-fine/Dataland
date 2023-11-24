<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table v-if="dataPointDisplay" class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <th class="headers-bg">Value</th>
            <td class="nowrap">{{ dataPointDisplay.value ?? "" }}</td>
          </tr>
          <tr v-if="dataPointDisplay.quality">
            <th class="headers-bg">Quality</th>
            <td>{{ dataPointDisplay.quality }}</td>
          </tr>
          <tr v-if="dataPointDisplay.dataSource">
            <th class="headers-bg">Data source</th>
            <td class="nowrap">
              <DocumentLink
                :label="dataSourceLabel"
                :download-name="dataPointDisplay.dataSource.fileName ?? dataPointDisplay.dataSource.fileReference"
                :file-reference="dataPointDisplay.dataSource.fileReference"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataPointDisplay.comment">
            <th class="headers-bg">Comment</th>
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
      type: Object as () => DataPointDisplay,
      require: true,
    },
  },
  computed: {
    dataSourceLabel() {
      return this.dataPointDisplay?.dataSource.page
        ? `${this.dataPointDisplay?.dataSource.fileName ?? ""}, page ${
            this.dataPointDisplay?.dataSource.page as number
          }`
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
      &.nowrap {
        white-space: nowrap;
      }
    }
    th {
      &.headers-bg {
        width: 2rem;
        padding-right: 1rem;
        font-weight: normal;
      }
    }
  }
}
</style>
