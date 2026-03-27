<template>
  <div>
    <section
      data-test="next-datapoint-section"
      style="padding: var(--spacing-xs); display: flex; flex-direction: column; height: 100%"
    >
      <h3 style="margin-top: 0; white-space: nowrap">Next datapoint</h3>

      <div style="display: flex; align-items: center; gap: var(--spacing-xs)">
        <ToggleSwitch id="only-unreviewed-toggle" v-model="onlyShowUnreviewed" data-test="only-unreviewed-toggle" />
        <label for="only-unreviewed-toggle"> Only show unreviewed </label>
      </div>

      <div style="display: flex; gap: var(--spacing-xs); margin-top: var(--spacing-sm)">
        <Select
          v-model="selectedNextDataPointTypeId"
          :options="options"
          optionLabel="label"
          optionValue="dataPointTypeId"
          :filter="true"
          placeholder="Select next datapoint"
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
          :disabled="!selectedNextDataPointTypeId"
          data-test="go-to-datapoint-button"
          style="white-space: nowrap"
        />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import ToggleSwitch from 'primevue/toggleswitch';
import type { NextDataPointOption } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

defineProps<{
  options: NextDataPointOption[];
}>();

const emit = defineEmits<{
  goTo: [];
}>();

const onlyShowUnreviewed = defineModel<boolean>('onlyShowUnreviewed', { default: true });
const selectedNextDataPointTypeId = defineModel<string | null>('selectedNextDataPointTypeId', { default: null });
</script>
