<template>
  <a @click="$dialog.open(content.displayValue.modalComponent, modalDataOptions)" class="link"
    >{{ content.displayValue.label }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
  <div style="display: flex; flex-direction: column">
    <div
      v-if="
        content.displayValue.modalOptions?.data.dataSource ||
        content.displayValue.modalOptions?.data.quality ||
        content.displayValue.modalOptions?.data.comment
      "
    >
      <a @click="$dialog.open(DataPointDataTable, modalDataOptions)" class="link"
        >{{ 'Data Source' }}
        <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
      </a>
    </div>
    <div v-else style="display: flex">
      <p>No source or quality provided</p>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { type DataMetaInformation } from '@clients/backend';
import DataPointDataTable from '@/components/general/DataPointDataTable.vue';

export default defineComponent({
  name: 'ModalLinkWithDataSourceDisplayComponent',
  computed: {
    DataPointDataTable() {
      return DataPointDataTable;
    },
    modalDataOptions() {
      let updatedDataModalOptions = this.content.displayValue.modalOptions;
      updatedDataModalOptions!.data.metaInfo = this.metaInfo;
      updatedDataModalOptions!.data.dataPointDisplay = {
        quality: updatedDataModalOptions?.data.quality,
        dataSource: updatedDataModalOptions?.data.dataSource,
        comment: updatedDataModalOptions?.data.comment,
      };
      return updatedDataModalOptions;
    },
  },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkWithDataSourceDisplayComponent>,
      required: true,
    },
    metaInfo: {
      type: Object as () => DataMetaInformation,
      required: true,
    },
  },
});
</script>
