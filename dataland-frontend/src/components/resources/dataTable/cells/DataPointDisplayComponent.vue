<template>
  <span>{{ content.displayValue.label }}</span>
  <DocumentLink
    label=""
    :suffix="formattedPageNumber"
    :download-name="content.displayValue.reference.name"
    :reference="content.displayValue.reference.reference"
    show-icon
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  name: "DocumentLinkDisplayComponent",
  components: { DocumentLink },
  props: {
    content: {
      type: Object as () => MLDTDisplayValue<MLDTDisplayComponents.DataPointDisplayComponent>,
      required: true,
    },
  },
  computed: {
    formattedPageNumber() {
      if(this.content.displayValue.page || this.content.displayValue.page === 0) {
        return `page ${this.content.displayValue.page}`
      } else {
        return "";
      }
    },
  },
});
</script>
