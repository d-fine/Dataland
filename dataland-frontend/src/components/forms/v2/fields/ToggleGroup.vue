<template>
  <div class="mb-3 p-0 -ml-2" :class="dataPointVisible ? 'bordered-box' : ''">
    <InputSwitch :model-value="dataPointVisible" @update:model-value="onDataPointVisibleUiToggle" />
    <div v-if="dataPointVisible">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FormFieldContext } from "@/components/forms/v2/fields/Utils";
import InputSwitch from "primevue/inputswitch";
import { ref, watch } from "vue";
import { isObjectEmpty } from "@/utils/TypeScriptUtils";

const props = defineProps<{
  context: FormFieldContext<Record<string, unknown>, Record<string, never>>;
}>();

const dataPointVisible = ref(false);

function onDataPointVisibleUiToggle(newValue: boolean) {
  dataPointVisible.value = newValue;
  if (!newValue) {
    void props.context.node.input({});
  }
}

watch(
  props.context._value,
  (newValue) => {
    dataPointVisible.value = !isObjectEmpty(newValue);
  },
  { immediate: true },
);
</script>
