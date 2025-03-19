<template>
  <div v-if="dataPointDisplay && isDataCorrect" class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <th class="headers-bg">Value</th>
            <td class="nowrap">{{ dataPointDisplay.value ?? '' }}</td>
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
                :page="dataSourcePage"
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
  <template v-else>No data provided</template>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import { type DataPointDisplay } from '@/utils/DataPoint';

export default defineComponent({
  components: { DocumentLink },
  name: 'DataPointDataTableInModal',
  props: {
    dataPointDisplay: {
      type: Object as () => DataPointDisplay,
      require: true,
    },
  },
  computed: {
    isDataCorrect() {
      return (
        (!!this.dataPointDisplay?.value && this.dataPointDisplay?.value !== 'No data provided') ||
        (!!this.dataPointDisplay?.comment && this.dataPointDisplay?.comment !== '')
      );
    },
    dataSourceLabel(): string | undefined {
      const dataSource = this.dataPointDisplay?.dataSource;
      if (!dataSource || !dataSource.fileName) return undefined;
      if ('page' in dataSource) {
        return `${dataSource.fileName}, page ${dataSource.page}`;
      }
      return dataSource.fileName;
    },
    dataSourcePage(): number | undefined {
      const dataSource = this.dataPointDisplay?.dataSource;
      if (dataSource && 'page' in dataSource) {
        return Number(dataSource.page?.split('-')[0]);
      } else return undefined;
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
