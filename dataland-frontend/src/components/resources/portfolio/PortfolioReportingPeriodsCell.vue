<template>
  <Button v-if="clickable" variant="link" @click="$emit('navigate')" :pt="buttonPt">
    <span v-tooltip.top="tooltipOptions">
      <template v-for="(period, index) in periods" :key="period.year">
        <span :class="{ 'non-sourceable-year': period.nonSourceable }">{{ period.year }}</span
        ><template v-if="index < periods.length - 1">, </template>
      </template>
    </span>
  </Button>
  <span v-else v-tooltip.top="tooltipOptions">
    <template v-if="periods.length === 0">No data available</template>
    <template v-else>
      <template v-for="(period, index) in periods" :key="period.year">
        <span :class="{ 'non-sourceable-year': period.nonSourceable }">{{ period.year }}</span
        ><template v-if="index < periods.length - 1">, </template>
      </template>
    </template>
  </span>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import Tooltip from 'primevue/tooltip';
import { computed } from 'vue';

const vTooltip = Tooltip;

/**
 * A single reporting period, potentially marked as non-sourceable (i.e. it will be displayed struck through).
 */
export interface ReportingPeriodEntry {
  year: string;
  nonSourceable: boolean;
}

const NON_SOURCEABLE_TOOLTIP_TEXT = 'If a year number is strikethrough, the report of this year is non-sourceable';

const props = defineProps<{
  periods: ReportingPeriodEntry[];
  clickable: boolean;
}>();

defineEmits<{
  navigate: [];
}>();

const buttonPt = {
  label: {
    style: 'font-weight: normal; text-align: left;',
  },
  root: {
    style: 'padding-left: 0;',
  },
};

const tooltipOptions = computed(() =>
  props.periods.some((period) => period.nonSourceable) ? { value: NON_SOURCEABLE_TOOLTIP_TEXT } : undefined
);
</script>

<style scoped>
.non-sourceable-year {
  text-decoration: line-through;
  text-decoration-thickness: 2px;
}
</style>
