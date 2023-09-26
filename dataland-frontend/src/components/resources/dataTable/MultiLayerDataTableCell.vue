<template>
  <component :is="content.displayComponent" :content="content" />
</template>

<script lang="ts">
import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";
import StringDisplayComponent from "@/components/resources/dataTable/cells/StringDisplayComponent.vue";
import { defineComponent } from "vue";

export default defineComponent({
  name: "MultiLayerDataTableCell",
  components: { StringDisplayComponent },
  props: {
    content: {
      type: Object as () => AvailableDisplayValues,
      required: true,
    },
  },
});
</script>

<style scoped></style>
