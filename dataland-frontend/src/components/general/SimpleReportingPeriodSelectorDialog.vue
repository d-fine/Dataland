<template>
  <div data-test="select-reporting-period-dialog">
    <h4 class="title">SELECT YEAR</h4>
    <div class="three-in-row" data-test="reporting-periods">
      <a
        v-for="(reportingPeriod, index) in reportingPeriods"
        :key="index"
        class="link"
        @click="onReportingPeriodSelection(reportingPeriod)"
      >
        {{ reportingPeriod }}
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = defineProps(['reportingPeriods']);
const reportingPeriods: Array<string> = props.reportingPeriods;
const emits = defineEmits(['selectedReportingPeriod']);

/**
 * Emits 'reportingPeriodSelection' event on button click
 * @param reportingPeriod selected reportingPeriod; sent as part of the emit event
 */
function onReportingPeriodSelection(reportingPeriod: string): void {
  emits('selectedReportingPeriod', reportingPeriod);
}
</script>
