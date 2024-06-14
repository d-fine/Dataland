<template>
  <div class="flex">
    <a
      v-if="dataPointProperties.dataSource"
      @click="$dialog.open(DataPointDataTable, modalOptions)"
      class="link"
    >
      <slot></slot>
      <em class="pl-2 material-icons" aria-label="View datapoint details"> dataset </em>
    </a>
    <div v-else><slot></slot></div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DataPointDataTable from "@/components/general/DataPointDataTable.vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { type DataMetaInformation } from "@clients/backend";

export default defineComponent({
  name: "DataPointWrapperDisplayComponent",
  components: { DocumentLink },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointWrapperDisplayComponent>,
      required: true,
    },
    metaInfo: {
      type: Object as () => DataMetaInformation,
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
          dataPointDisplay: this.dataPointProperties,
          dataId: this.metaInfo.dataId,
          dataType: this.metaInfo.dataType,
        },
      };
    },
    dataPointProperties() {
      const content = this.content.displayValue;
      let valueOption = undefined;
      if (content.innerContents.displayComponentName == MLDTDisplayComponentName.StringDisplayComponent) {
        valueOption = content.innerContents.displayValue;
      }
      return {
        value: valueOption,
        quality: content.quality,
        dataSource: content.dataSource,
        comment: content.comment,
      };
    },
  },
});
</script>
