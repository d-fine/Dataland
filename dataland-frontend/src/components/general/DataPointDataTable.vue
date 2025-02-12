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
            <th class="headers-bg width-auto"><span class="table-left-label">Value</span></th>
            <td>{{ dialogData.dataPointDisplay.value }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.quality">
            <th class="headers-bg width-auto"><span class="table-left-label">Quality</span></th>
            <td>{{ humanizeStringOrNumber(dialogData.dataPointDisplay.quality) }}</td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.dataSource">
            <th class="headers-bg width-auto"><span class="table-left-label">Data source</span></th>
            <td>
              <DocumentLink
                :label="dataSourceLabel"
                :download-name="
                  dialogData.dataPointDisplay.dataSource.fileName ??
                  dialogData.dataPointDisplay.dataSource.fileReference
                "
                :file-reference="dialogData.dataPointDisplay.dataSource.fileReference"
                :data-id="dialogData.dataId"
                :data-type="dialogData.dataType"
                show-icon
              />
            </td>
          </tr>
          <tr v-if="dialogData.dataPointDisplay.comment">
            <th class="headers-bg width-auto"><span class="table-left-label">Comment</span></th>
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
import DocumentLink from '@/components/resources/frameworkDataSearch/DocumentLink.vue';
import { ONLY_AUXILIARY_DATA_PROVIDED } from '@/utils/Constants';
import { assertDefined } from '@/utils/TypeScriptUtils';
import RenderSanitizedMarkdownInput from '@/components/general/RenderSanitizedMarkdownInput.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { type DataPointDataTableRefProps } from '@/utils/Frameworks';

export default defineComponent({
  methods: {
    humanizeStringOrNumber,
  },
  components: { RenderSanitizedMarkdownInput, DocumentLink },
  inject: ['dialogRef'],
  name: 'DataPointDataTable',
  data: () => {
    return { ONLY_AUXILIARY_DATA_PROVIDED };
  },
  computed: {
    dialogData(): DataPointDataTableRefProps {
      return assertDefined(this.dialogRef as DynamicDialogInstance).data as DataPointDataTableRefProps;
    },
    dataSourceLabel(): string {
      const dataSource = this.dialogData.dataPointDisplay.dataSource;
      if (!dataSource) return '';
      if ('page' in dataSource) {
        return dataSource.page === null
          ? `${dataSource.fileName}`
          : `${dataSource.fileName},
         page(s) ${dataSource.page}`;
      } else {
        return dataSource.fileName ?? '';
      }
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
