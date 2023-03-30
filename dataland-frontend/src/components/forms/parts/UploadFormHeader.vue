<template>
  <div class="form-field-label">
    <h5>{{ name }}</h5>
    <div class="asterisk" v-if="isRequired"> * </div>
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
    name: String,
    explanation: String,
    isRequired: {
      type: Boolean,
      default: false
    },
  },
});
</script>
