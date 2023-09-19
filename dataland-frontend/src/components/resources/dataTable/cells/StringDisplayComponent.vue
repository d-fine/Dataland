<template>
  <span v-if="content.displayValue">{{ content.displayValue }}</span>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import {
  type MLDTDisplayComponents,
  type MLDTDisplayValue,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";

export default defineComponent({
  name: "StringDisplayComponent",
  props: {
    content: {
      type: Object as () => MLDTDisplayValue<MLDTDisplayComponents.StringDisplayComponent>,
      required: true,
    },
  },
});
</script>
