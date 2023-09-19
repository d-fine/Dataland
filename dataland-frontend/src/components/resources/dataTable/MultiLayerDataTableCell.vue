<template>
  <component :is="content.displayComponent" :content="content" />
</template>

<script lang="ts">
import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import DocumentLinkDisplayComponent from "@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue";
import ModalLinkDisplayComponent from "@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  components: { DocumentLinkDisplayComponent, ModalLinkDisplayComponent, StringDisplayComponent },
  props: {
    content: {
      type: Object as () => AvailableDisplayValues,
      required: true,
    },
  },
});
</script>

<style scoped></style>
