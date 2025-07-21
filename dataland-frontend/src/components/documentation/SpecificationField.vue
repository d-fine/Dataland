<template>
  <div class="specification-field mb-3">
    <label class="field-label text-900 font-semibold block mb-1">{{ label }}:</label>
    <component 
      :is="fieldComponent" 
      v-bind="fieldProps"
      :class="fieldClasses"
    >
      {{ displayValue }}
    </component>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { handleSpecificationLink } from '@/utils/linkHandler';

interface Props {
  label: string;
  value: string;
  type?: 'text' | 'link' | 'monospace';
  clickable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  clickable: false,
});

const fieldComponent = computed(() => {
  return props.clickable ? 'button' : 'span';
});

const fieldClasses = computed(() => {
  const baseClasses = 'field-value text-600';
  const typeClasses = {
    monospace: 'font-family-monospace',
    link: 'text-blue-600 hover:text-blue-800',
    text: '',
  };
  
  const clickableClasses = props.clickable 
    ? 'bg-transparent border-none p-0 cursor-pointer' 
    : '';

  return `${baseClasses} ${typeClasses[props.type]} ${clickableClasses}`.trim();
});

const fieldProps = computed(() => {
  if (props.clickable) {
    return {
      onClick: () => handleSpecificationLink(props.value),
    };
  }
  return {};
});

const displayValue = computed(() => props.value);
</script>