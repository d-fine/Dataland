<template>
  <span class="inline-flex align-items-center justify-content-center border-round text-sm font-medium text-primary-600 bg-primary-50 px-2 py-1">
    {{ displayText }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  text: string;
  uppercase?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  uppercase: false,
});

const displayText = computed(() => 
  props.uppercase ? props.text.toUpperCase() : props.text
);
</script>