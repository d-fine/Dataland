<template>
  <section
    data-test="next-datapoint-section"
    style="padding: var(--spacing-xs); display: flex; flex-direction: column; height: 100%"
  >
    <h3 style="margin-top: 0; white-space: nowrap">Next data point</h3>

    <div style="display: flex; align-items: center; gap: var(--spacing-xs)">
      <ToggleSwitch
        id="only-unreviewed-toggle"
        :modelValue="props.onlyShowUnreviewed"
        @update:modelValue="emit('update:onlyShowUnreviewed', $event)"
        data-test="only-unreviewed-toggle"
      />
      <label for="only-unreviewed-toggle"> Only show unreviewed </label>
    </div>

    <div style="display: flex; gap: var(--spacing-xs); margin-top: var(--spacing-sm)">
      <Select
        :modelValue="props.selectedNextDataPointTypeId"
        @update:modelValue="emit('update:selectedNextDataPointTypeId', $event)"
        :options="props.options"
        option-label="label"
        option-value="dataPointTypeId"
        :filter="true"
        placeholder="Select next data point"
        :pt="{ root: { style: { flex: '1' } } }"
        data-test="next-datapoint-select"
      >
        <template #option="slotProps">
          <div :style="slotProps.option.reviewed ? 'color: var(--p-gray-300);' : ''">
            <i
              v-if="slotProps.option.reviewed"
              class="pi pi-check"
              aria-hidden="true"
              :style="{ color: 'var(--p-gray-300)', marginRight: 'var(--spacing-xs)' }"
            ></i>
            <span>{{ slotProps.option.label }}</span>
          </div>
        </template>
      </Select>

      <PrimeButton
        label="GO TO"
        @click="emit('goTo')"
        :disabled="!props.selectedNextDataPointTypeId"
        data-test="go-to-datapoint-button"
        style="white-space: nowrap"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import ToggleSwitch from 'primevue/toggleswitch';
import type { NextDataPointOption } from '@/types/JudgeDialogTypes.ts';

const props = defineProps<{
  options: NextDataPointOption[];
  onlyShowUnreviewed: boolean;
  selectedNextDataPointTypeId: string | null;
}>();

const emit = defineEmits<{
  (e: 'update:onlyShowUnreviewed', value: boolean): void;
  (e: 'update:selectedNextDataPointTypeId', value: string): void;
  (e: 'goTo'): void;
}>();
</script>
