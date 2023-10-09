<template>
  <div class="flex" v-if="content.displayComponent == MLDTDisplayComponents.HighlightHiddenCellDisplayComponent">
    <i class="pi pi-eye-slash pr-1 text-red-500" aria-hidden="true" data-test="hidden-icon" />
    <MultiLayerDataTableCell :content="content.displayValue.innerContents" />
  </div>
  <component v-else :is="content.displayComponent" :content="content" />
</template>

<script lang="ts">
import {
  type AvailableDisplayValues,
  MLDTDisplayComponents,
} from "@/components/resources/dataTable/MultiLayerDataTableCells";
import DocumentLinkDisplayComponent from "@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import { defineComponent } from "vue";
import ModalLinkDisplayComponent from "@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  computed: {
    MLDTDisplayComponents() {
      return MLDTDisplayComponents;
    },
  },
  components: { StringDisplayComponent, DocumentLinkDisplayComponent, ModalLinkDisplayComponent },
  props: {
    content: {
      type: Object as () => AvailableDisplayValues,
      required: true,
    },
  },
});
</script>
