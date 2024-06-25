<template>
  <div :class="'grid ' + bgClass">
    <div class="col-12 pl-4 pr-4">
      <slot></slot>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'MarginWrapper',
  props: {
    bgClass: {
      type: String,
      default: '',
    },
  },
});
</script>
