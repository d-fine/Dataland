<template>
  <div class="flex" v-if="content.displayComponentName == MLDTDisplayComponents.HighlightHiddenCellDisplay">
    <i class="pi pi-eye-slash pr-1 text-red-500" aria-hidden="true" data-test="hidden-icon" />
    <MultiLayerDataTableCell :content="content.displayValue.innerContents" />
  </div>
  <div v-if="content.displayComponentName == MLDTDisplayComponents.DataPointWrapperDisplayComponent">
    <DataPointWrapperDisplayComponent :content="content">
      <MultiLayerDataTableCell
        :content="content.displayValue.innerContents"
        v-if="content.displayValue.innerContents != MLDTDisplayObjectForEmptyString"
      />
      <template v-else>No data provided</template>
    </DataPointWrapperDisplayComponent>
  </div>
  <component v-else :is="content.displayComponentName" :content="content" />
</template>

<script lang="ts">
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  MLDTDisplayObjectForEmptyString,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
import DocumentLinkDisplayComponent from "@/components/resources/dataTable/cells/DocumentLinkDisplayComponent.vue";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import { defineComponent } from "vue";
import ModalLinkDisplayComponent from "@/components/resources/dataTable/cells/ModalLinkDisplayComponent.vue";
import DataPointDisplayComponent from "@/components/resources/dataTable/cells/DataPointDisplayComponent.vue";
import DataPointWrapperDisplayComponent from "@/components/resources/dataTable/cells/DataPointWrapperDisplayComponent.vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  computed: {
    MLDTDisplayObjectForEmptyString() {
      return MLDTDisplayObjectForEmptyString;
    },
    MLDTDisplayComponents() {
      return MLDTDisplayComponentName;
    },
  },
  components: {
    DataPointWrapperDisplayComponent,
    StringDisplayComponent,
    DocumentLinkDisplayComponent,
    ModalLinkDisplayComponent,
    DataPointDisplayComponent,
  },
  props: {
    content: {
      type: Object as () => AvailableMLDTDisplayObjectTypes,
      required: true,
    },
  },
});
</script>
