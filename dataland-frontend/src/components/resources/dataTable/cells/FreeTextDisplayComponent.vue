<template>
  <span class="preserve-line-wrap" v-if="content.displayValue">{{ content.displayValue }}</span>
</template>

<style scoped>
.preserve-line-wrap {
  white-space: pre-wrap;
}
</style>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayObject,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

export default defineComponent({
  name: "FreeTextDisplayComponent",
  props: {
    content: {
      type: Object as () => MLDTDisplayObject<MLDTDisplayComponentName.FreeTextDisplayComponent>,
      required: true,
    },
  },
});
</script>
