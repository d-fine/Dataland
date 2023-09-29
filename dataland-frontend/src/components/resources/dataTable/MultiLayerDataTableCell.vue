<template>
  <component :is="content.displayComponent" :content="content" />
</template>

<script lang="ts">
import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";
import DocumentLinkDisplayComponent from "@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import { defineComponent } from "vue";
import ModalLinkDisplayComponent from "@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  components: { StringDisplayComponent, DocumentLinkDisplayComponent, ModalLinkDisplayComponent },
  props: {
    content: {
      type: Object as () => AvailableDisplayValues,
      required: true,
    },
  },
});
</script>
