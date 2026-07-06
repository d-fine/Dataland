<template>
  <div style="display: flex; flex-direction: column; gap: var(--spacing-sm)">
    <section data-test="pre-approval-section" style="padding: var(--spacing-xs); display: flex; flex-direction: column">
      <div
        style="
          display: flex;
          align-items: center;
          gap: var(--spacing-sm);
          width: 100%;
          margin-bottom: var(--spacing-sm);
        "
      >
        <h3 style="margin-top: 0; margin-bottom: 0; white-space: nowrap">Pre-Approval:</h3>
        <span
          :style="{
            fontSize: 'var(--font-size-sm)',
            whiteSpace: 'nowrap',
            backgroundColor: preApprovalVerdictBadge.background,
            color: preApprovalVerdictBadge.color,
          }"
          data-test="pre-approval-verdict-badge"
        >
          {{ preApprovalVerdictBadge.label }}
        </span>
        <button
          v-if="preApprovalChecklist"
          type="button"
          class="p-link"
          aria-label="Pre-approval info"
          style="background: none; border: none; padding: 0; cursor: pointer; display: inline-flex; align-items: center"
          @click="isInfoDialogOpen = true"
        >
          <em class="material-icons ml-2" aria-hidden="true">info</em>
        </button>
      </div>

      <div
        v-if="preApprovalChecklist"
        style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-xxs) var(--spacing-xs)"
      >
        <div
          v-for="item in preApprovalChecklist"
          :key="item.label"
          style="padding: var(--spacing-xxs) var(--spacing-xs)"
        >
          <span>{{ item.label }}</span>
          <span
            :class="[item.passed ? 'pi pi-check' : 'pi pi-times', 'ml-2']"
            :style="{ color: item.passed ? 'var(--p-green-600)' : 'var(--p-red-600)' }"
            aria-hidden="true"
          />
        </div>
      </div>
    </section>

    <section
      data-test="next-datapoint-section"
      style="padding: var(--spacing-xs); display: flex; flex-direction: column"
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
          id="next-datapoint-select"
          aria-label="Next data point"
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
              <span :data-test="`next-datapoint-option-${slotProps.option.dataPointTypeId}`">
                {{ slotProps.option.label }}
              </span>
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
  </div>

  <PrimeDialog
    v-model:visible="isInfoDialogOpen"
    :dismissable-mask="true"
    :modal="true"
    header="Pre-Approval Checks"
    style="max-width: 35rem; width: 100vw"
  >
    <p>For pre-approval of a KPI all four pre-approval pre-conditions need to be true:</p>
    <p>
      <strong>"QA accepted"</strong> is true if the verdict of the DALAI-bot review is "QA accepted" (i.e. not "QA
      Inconclusive", "QA Rejected" or "Mixed Verdicts").
    </p>
    <p>
      <strong>"Not an exempted field"</strong> is true if the KPI is not on the list of fields that are exempted from
      automatic preapproval.
    </p>
    <p>
      <strong>"Randomly selected for preapproval"</strong> is true if the KPI was selected to be preapproved by random
      sampling (probability of pre-approval is currently {{ displaySamplingProbability }} %).
    </p>
    <p>
      <strong>"Nonsignificant deviation"</strong> is true if either there is no data for the previous year (previous
      year data not on Dataland or null), or the data from the previous year does not significantly deviate from this
      year's reported data. Significant means more than 50 % deviation for numerical fields, more than 5 for integer
      fields or "Yes" -> "No"/ "No" -> "Yes" for Yes/No fields.
    </p>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import Select from 'primevue/select';
import ToggleSwitch from 'primevue/toggleswitch';
import { computed, ref } from 'vue';
import type { NextDataPointOption } from '@/types/JudgeDialogTypes.ts';
import type { PreApprovalCheckResults } from '@clients/qaservice';
import { usePreApprovalConfigQuery } from '@/api-queries/qa-service/pre-approval/usePreApprovalConfigQuery.ts';

const isInfoDialogOpen = ref(false);

const props = defineProps<{
  options: NextDataPointOption[];
  onlyShowUnreviewed: boolean;
  selectedNextDataPointTypeId: string | null;
  preApprovalCheckResults: PreApprovalCheckResults | null;
}>();

// ===== Pre-Approval table data =====

const preApprovalChecklist = computed(() => {
  const results = props.preApprovalCheckResults;
  if (!results) return null;
  return [
    { label: 'All QA reports accepted:', passed: results.areAllQaReportsAccepted },
    { label: 'Not an exempted field:', passed: results.dataPointEligible },
    { label: 'Randomly selected for pre-approval:', passed: results.passesRandomSampling },
    { label: 'Nonsignificant deviation:', passed: results.passesSignificanceCheck },
  ];
});

// ===== Pre-Approval badge data =====

const preApprovalVerdictBadge = computed<{ label: string; background: string; color: string }>(() => {
  if (!preApprovalChecklist.value)
    return { label: 'PRE-APPROVAL OBJECT NOT FOUND', background: 'var(--p-yellow-100)', color: 'var(--p-yellow-700)' };
  const allPass = preApprovalChecklist.value.every((item) => item.passed);
  return allPass
      ? { label: 'PRE-APPROVED', background: 'var(--p-green-100)', color: 'var(--p-green-700)' }
      : { label: 'MANUAL REVIEW', background: 'var(--p-red-100)', color: 'var(--p-red-700)' };
});

// ===== Pre-Approval config data =====

const { data: preApprovalConfig } = usePreApprovalConfigQuery();
const samplingProbability = computed(() => preApprovalConfig.value?.samplingProbability ?? undefined);
const displaySamplingProbability = computed(() =>
  samplingProbability.value === undefined ? 'unknown' : (1 - samplingProbability.value) * 100
);

// ===== Next data point data =====

const emit = defineEmits<{
  (e: 'update:onlyShowUnreviewed', value: boolean): void;
  (e: 'update:selectedNextDataPointTypeId', value: string): void;
  (e: 'goTo'): void;
}>();
</script>
