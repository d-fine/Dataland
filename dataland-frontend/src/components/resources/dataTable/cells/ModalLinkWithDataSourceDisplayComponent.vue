<template>
  <div class="table-cell-alignment">
    <a
      @click="$dialog.open(content.displayValue.modalComponent, modalDataOptions)"
      class="link"
      data-test="activityLink"
      >{{ content.displayValue.label }}
      <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
    </a>

    <div
      v-if="
        content.displayValue.modalOptions?.data.dataPointDisplay.dataSource ||
        content.displayValue.modalOptions?.data.dataPointDisplay.quality ||
        content.displayValue.modalOptions?.data.dataPointDisplay.comment
      "
    >
      <a @click="$dialog.open(DataPointDataTable, modalDataSourceOptions)" class="link"
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
      return updatedDataModalOptions;
    },
    modalDataSourceOptions() {
      let updatedDataSourceModalOptions = this.content.displayValue.modalOptions;
      updatedDataSourceModalOptions!.props!.header = 'Data Source and Quality';
      return updatedDataSourceModalOptions;
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
