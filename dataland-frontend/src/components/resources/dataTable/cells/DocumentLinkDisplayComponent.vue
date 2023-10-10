<template>
  <DocumentLink
    :label="content.displayValue.label"
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
      type: Object as () => MLDTDisplayValue<MLDTDisplayComponents.DocumentLinkDisplayComponent>,
      required: true,
    },
  },
});
</script>
