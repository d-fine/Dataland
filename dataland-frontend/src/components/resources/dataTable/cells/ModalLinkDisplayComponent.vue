<template>
  <a
    v-if="content?.displayValue?.modalOptions?.data.listOfRowContents.length"
    @click="$dialog.open(content.displayValue.modalComponent, content.displayValue.modalOptions)"
    class="link"
    >{{ content.displayValue.label }}
    <em class="pl-2 material-icons" aria-hidden="true" title=""> dataset </em>
  </a>
  <span v-else></span>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
export default defineComponent({
  name: "ModalLinkDisplayComponentName",
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.ModalLinkDisplayComponentName>,
      required: true,
    },
  },
});
</script>
