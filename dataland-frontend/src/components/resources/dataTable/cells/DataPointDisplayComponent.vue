<template>
  <a @click="$dialog.open(DataPointDataTable, modalOptions)" class="link"
    >{{ content.displayValue?.value ?? NO_DATA_PROVIDED() }}
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
import { NO_DATA_PROVIDED } from "@/utils/Constants";

export default defineComponent({
  name: "DataPointDisplayComponent",
  methods: {
    /**
     * Builds the no data provided string
     * @returns the no data provided string
     */
    NO_DATA_PROVIDED() {
      return NO_DATA_PROVIDED;
    },
  },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>,
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
        value: content.value,
        quality: content.quality,
        dataSource: content.dataSource,
        comment: content.comment,
      };
    },
  },
});
</script>
