<template>
  <span>{{ content.displayValue.label }}</span>
  <DocumentLink
    label=""
    :suffix="formattedPageNumber"
    :download-name="content.displayValue.fileName"
    :file-reference="content.displayValue.fileReference"
    show-icon
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DocumentLink from "@/components/resources/frameworkDataSearch/DocumentLink.vue";

export default defineComponent({
  name: "DocumentLinkDisplayComponent",
  components: { DocumentLink },
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.DataPointDisplayComponent>,
      required: true,
    },
  },
  computed: {
    formattedPageNumber() {
      if (this.content.displayValue.page || this.content.displayValue.page === 0) {
        return `page ${this.content.displayValue.page}`;
      } else {
        return "";
      }
    },
  },
});
</script>
