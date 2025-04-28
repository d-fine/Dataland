<template>
  <div v-if="dataPointDisplay && isDataCorrect" class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr>
            <th scope="row" class="headers-bg">Value</th>
            <td class="nowrap">{{ dataPointDisplay.value ?? '' }}</td>
          </tr>
          <tr v-if="dataPointDisplay.quality">
            <th scope="row" class="headers-bg">Quality</th>
            <td>{{ dataPointDisplay.quality }}</td>
          </tr>
          <tr v-if="dataPointDisplay.dataSource">
            <th scope="row" class="headers-bg">Data source</th>
            <td class="nowrap">
              <DocumentDownloadLink
                :document-download-info="{
                  downloadName: dataPointDisplay.dataSource.fileName ?? dataPointDisplay.dataSource.fileReference,
                  fileReference: dataPointDisplay.dataSource.fileReference,
                  page: dataSourcePage,
                }"
                :label="dataSourceLabel"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataSourcePages">
            <th scope="row" class="headers-bg">{{ dataSourcePagesRefersToMultiplePages ? 'Pages' : 'Page' }}</th>
            <td>{{ dataSourcePages }}</td>
          </tr>
          <tr v-if="dataPointDisplay.comment">
            <th scope="row" class="headers-bg">Comment</th>
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
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { type DataPointDisplay } from '@/utils/DataPoint';
import { getPageInfo } from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';

export default defineComponent({
  components: { DocumentDownloadLink },
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
      return dataSource.fileName;
    },

    dataSourcePage(): number | undefined {
      return getPageInfo(this.dataPointDisplay?.dataSource).firstPageInRange;
    },

    dataSourcePages(): string {
      return getPageInfo(this.dataPointDisplay?.dataSource).pageRange;
    },

    dataSourcePagesRefersToMultiplePages(): boolean {
      return getPageInfo(this.dataPointDisplay?.dataSource).hasMultiplePages;
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
