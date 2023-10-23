<template>
  <div class="flex" v-if="content.displayComponentName == MLDTDisplayComponents.HighlightHiddenCellDisplay">
    <i class="pi pi-eye-slash pr-1 text-red-500" aria-hidden="true" data-test="hidden-icon" />
    <MultiLayerDataTableCell :content="content.displayValue.innerContents" />
  </div>
  <component v-else :is="content.displayComponentName" :content="content" />
</template>

<script lang="ts">
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DocumentLinkDisplayComponent from "@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import DataPointDisplayComponent from "@/components/resources/dataTable/cells/DataPointDisplayComponent.vue";
import { defineComponent } from "vue";
import ModalLinkDisplayComponent from "@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  computed: {
    MLDTDisplayComponents() {
      return MLDTDisplayComponentName;
    },
  },
  components: {
    StringDisplayComponent,
    DocumentLinkDisplayComponent,
    DataPointDisplayComponent,
    ModalLinkDisplayComponent,
  },
  props: {
    content: {
      type: Object as () => AvailableMLDTDisplayObjectTypes,
      required: true,
    },
  },
});
</script>
