<template>
  <div class="flex">
    <slot></slot>
    <a
      v-if="isAnyDataPointPropertyAvailableThatIsWorthShowingInModal"
      @click="$dialog.open(DataPointDataTable, modalOptions)"
      class="link"
    >
      <em class="pl-2 material-icons" aria-label="View datapoint details"> description </em>
    </a>
    <DocumentLink
      v-else-if="dataPointProperties.dataSource"
      label=""
      :download-name="dataPointProperties.dataSource.fileName ?? dataPointProperties.dataSource.fileReference"
      :file-reference="dataPointProperties.dataSource.fileReference"
      show-icon
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DataPointDataTable from "@/components/general/DataPointDataTable.vue";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";
import { type ExtendedDocumentReference, QualityOptions } from "@clients/backend";

export default defineComponent({
  name: "DataPointWrapperDisplayComponent",
  components: { DocumentLink },
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
          dataPointDisplay: this.dataPointProperties,
        },
      };
    },
    dataPointProperties() {
      const content = this.content.displayValue;
      return {
        quality: content.quality,
        dataSource: content.dataSource,
        comment: content.comment,
      };
    },
    isAnyDataPointPropertyAvailableThatIsWorthShowingInModal() {
      const dataSource = this.dataPointProperties.dataSource as ExtendedDocumentReference | undefined | null;
      const comment = this.dataPointProperties.comment;
      const quality = this.dataPointProperties.quality;
      return (
        comment != undefined || (quality != undefined && quality != QualityOptions.Na) || dataSource?.page != undefined
      );
    },
  },
});
</script>
