<template>
  <Card class="bg-white d-infocard d-card mr-2">
    <template #title></template>
    <template #content>
      <div class="text-center">
        <slot> </slot>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import Card from "primevue/card";
import Tooltip from "primevue/tooltip";
import { humanizeString } from "@/utils/StringHumanizer";
import { defineComponent, PropType } from "vue";

export default defineComponent({
  name: "InfoCard",
  components: { Card },
  directives: {
    tooltip: Tooltip,
  },
  props: {
    title: {
      type: String,
    },
    value: {
      type: null as unknown as PropType<string | null>,
    },
    tooltipText: {
      type: String,
      default: "",
    },
  },
  computed: {
    humanizedValue() {
      const humanizedValue = humanizeString(this.value);
      return humanizedValue !== "" ? humanizedValue : "No data has been reported";
    },
  },
});
</script>
