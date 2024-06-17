<template>
  <div :class="[propClass, 'form-field-label']">
    <p class="p-0 mx-0 mt-0 mb-3">{{ label }}</p>
    <span class="asterisk" v-if="isRequired">*</span>
    <em
      v-if="description !== ''"
      class="material-icons info-icon"
      aria-hidden="true"
      :title="label"
      v-tooltip.top="{
        value: description,
      }"
      >info</em
    >
  </div>
</template>

<script lang="ts">
import Tooltip from 'primevue/tooltip';
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'UploadFormSubcategoryHeader',
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
    propClass: {
      type: String,
      default: '',
    },
  },
});
</script>
