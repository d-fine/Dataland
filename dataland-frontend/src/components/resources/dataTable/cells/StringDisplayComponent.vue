<template>
  <span v-if="content.displayValue">{{ content.displayValue }}</span>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponentName,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";

export default defineComponent({
  name: "StringDisplayComponent",
  props: {
    content: {
      type: Object as () => MLDTDisplayValue<MLDTDisplayComponentName.StringDisplayComponent>,
      required: true,
    },
  },
});
</script>
