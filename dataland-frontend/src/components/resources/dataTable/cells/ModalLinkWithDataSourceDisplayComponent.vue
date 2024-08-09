<template>
  <a @click="$dialog.open(content.displayValue.modalComponent, modalDataOptions)" class="link"
    >{{ content.displayValue.label }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
  <a @click="$dialog.open(DataPointDataTable, convertedValueForModal)" class="link"
    >{{ 'Data Source' }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
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

    convertedValueForModal() {
      const content = this.content.displayValue.modalOptions;
      let dataModalOptionsWithSource = this.content.displayValue.modalOptions;
      dataModalOptionsWithSource!.data.dataPointDisplay = {
        quality: content.source.quality,
        dataSource: content.source.dataSource,
        comment: content.source.comment,
      };
      return dataModalOptionsWithSource;
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
