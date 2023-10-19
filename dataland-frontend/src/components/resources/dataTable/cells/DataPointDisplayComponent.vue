<template>
  <span>{{ content.displayValue.value }}</span>
  <a @click="$dialog.open(DataPointDataTable, modalOptions)" class="link"
    >Show meta information
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DataPointDataTable from "@/components/general/DataPointDataTable.vue";

export default defineComponent({
  name: "DocumentLinkDisplayComponent",
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>,
      required: true,
    },
  },
  computed: {
    DataPointDataTable() {
      return DataPointDataTable;
    },
    modalOptions() {
      return {
        props: {
          header: this.content.displayValue.fieldLabel,
          modal: true,
          dismissableMask: true,
        },
        data: {
          dataPointDisplay: this.convertedValueForModal,
        },
      };
    },
    convertedValueForModal() {
      const content = this.content.displayValue;
      return {
        value: content.value,
        quality: content.quality,
        dataSource: content.dataSource,
        // dataSource: content.page ? `${content.fileName}, page ${content.page}` : content.fileName,
        comment: content.comment,
      };
    },
  },
});
</script>
