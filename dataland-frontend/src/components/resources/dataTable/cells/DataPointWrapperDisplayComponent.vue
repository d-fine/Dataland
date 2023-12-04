<template>
  <div class="flex">
    <slot></slot>
    <a @click="$dialog.open(DataPointDataTable, modalOptions)" class="link"
      ><em class="pl-2 material-icons" aria-label="View datapoint details"> description </em>
    </a>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DataPointDataTable from "@/components/general/DataPointDataTable.vue";

export default defineComponent({
  name: "DataPointWrapperDisplayComponent",
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointWrapperDisplayComponent>,
      required: true,
    },
  },
  data() {
    return {
      DataPointDataTable,
    };
  },
  computed: {
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
        quality: content.quality,
        dataSource: content.dataSource,
        comment: content.comment,
      };
    },
  },
});
</script>
