<template>
  <div class="form-field-label">
    <h5>{{ name }}</h5>
    <em
      class="material-icons info-icon"
      aria-hidden="true"
      :title="name"
      v-tooltip.top="{
        value: explanation,
      }"
      >info</em
    >
  </div>
</template>

<script lang="ts">
import Tooltip from "primevue/tooltip";
import { defineComponent } from "vue";

export default defineComponent({
  name: "UploadFormHeader",
  directives: {
    tooltip: Tooltip,
  },
  props: {
    name: {
      type: String,
      required: true,
    },
    explanation: {
      type: String,
      required: true,
    },
  },
});

// TODO when setting the title of the <em /> to an empty string it is not displayed (at least in chrome)
// TODO i think this is preferable
</script>
