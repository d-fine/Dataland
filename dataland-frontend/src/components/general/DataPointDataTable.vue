<template>
  <div class="p-datatable p-component">
    <div class="p-datatable-wrapper overflow-auto">
      <table class="p-datatable-table" aria-label="Data point content">
        <tbody class="p-datatable-body">
          <tr
            v-if="
              dialogData.dataPointDisplay.value && dialogData.dataPointDisplay.value != ONLY_AUXILIARY_DATA_PROVIDED
            "
          >
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Value</span></th>
            <td>{{ dialogData.dataPointDisplay.value }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.quality">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Quality</span></th>
            <td>{{ humanizeStringOrNumber(dialogData.dataPointDisplay.quality) }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.dataSource">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentDownloadLink
                :document-download-info="{
                  downloadName:
                    dialogData.dataPointDisplay.dataSource.fileName ??
                    dialogData.dataPointDisplay.dataSource.fileReference,
                  fileReference: dialogData.dataPointDisplay.dataSource.fileReference,
                  page: dataSourceFirstPageInRange,
                  dataId: dialogData.dataId,
                  dataType: dialogData.dataType,
                }"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dataSourcePageRange">
            <th scope="row" class="headers-bg width-auto">
              <span class="table-left-label">{{ dataSourceHasMultiplePages ? 'Pages' : 'Page' }}</span>
            </th>
            <td>{{ dataSourcePageRange }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.comment">
            <th scope="row" class="headers-bg width-auto"><span class="table-left-label">Comment</span></th>
            <td>
              <RenderSanitizedMarkdownInput :text="dialogData.dataPointDisplay.comment" />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { type DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import DocumentDownloadLink from '@/components/resources/frameworkDataSearch/DocumentDownloadLink.vue';
import { ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants';
import { assertDefined } from '@/utils/TypeScriptUtils';
import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataPointDataTableRefProps } from '@/utils/Frameworks';
import { getPageInfo } from '@/components/resources/frameworkDataSearch/FileDownloadUtils.ts';

export default defineComponent({
  methods: {
    humanizeStringOrNumber,
  },

  components: { RenderSanitizedMarkdownInput, DocumentDownloadLink },
  inject: ['dialogRef'],
  name: 'DataPointDataTable',

  data: () => {
    return { ONLY_AUXILIARY_DATA_PROVIDED };
  },

  computed: {
    dialogData(): DataPointDataTableRefProps {
      return assertDefined(this.dialogRef as DynamicDialogInstance).data as DataPointDataTableRefProps;
    },

    dataSourceFirstPageInRange(): number | undefined {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).firstPageInRange;
    },

    dataSourcePageRange(): string {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).pageRange;
    },

    dataSourceHasMultiplePages(): boolean {
      return getPageInfo(this.dialogData.dataPointDisplay.dataSource).hasMultiplePages;
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
