<template>
  <div :class="label ? null : 'mt-2'">
    <div v-if="label" class="form-field-label">
      <h5>{{ label }}</h5>
      <span class="asterisk" v-if="isRequired">*</span>
      <em
        v-if="description"
        class="material-icons info-icon"
        aria-hidden="true"
        :title="label"
        v-tooltip.top="{ value: description }"
      >
        info
      </em>
    </div>
  </div>
</template>

<script lang="ts">
import Tooltip from 'primevue/tooltip';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'UploadFormHeader',
  directives: {
    tooltip: Tooltip,
  },
  props: {
    label: {
      type: String,
      required: true,
    },
    description: {
      type: String,
      required: true,
    },
    isRequired: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
