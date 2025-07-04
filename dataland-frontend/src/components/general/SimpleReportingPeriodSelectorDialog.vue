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
<style scoped>
.link {
  color: var(--main-color);
  background: transparent;
  border: transparent;
  cursor: pointer;
  display: flex;

  &:hover {
    color: hsl(from var(--main-color) h s calc(l - 20));
    text-decoration: underline;
  }

  &:active {
    color: hsl(from var(--main-color) h s calc(l + 10));
  }

  &:focus {
    box-shadow: 0 0 0 0.2rem var(--btn-focus-border-color);
  }
}
</style>
