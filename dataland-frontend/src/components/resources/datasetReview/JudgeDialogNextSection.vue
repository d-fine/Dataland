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
            backgroundColor: props.preApprovalVerdictBadge.background,
            color: props.preApprovalVerdictBadge.color,
          }"
          data-test="pre-approval-verdict-badge"
        >
          {{ props.preApprovalVerdictBadge.label }}
        </span>
        <button
          v-if="props.isPreApprovalCheckResultsNotNull"
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
        v-if="props.isPreApprovalCheckResultsNotNull"
        style="display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-xxs) var(--spacing-xs)"
      >
        <div style="padding: var(--spacing-xxs) var(--spacing-xs)">
          <span>All QA reports accepted:</span>
          <span
            :class="[props.preApprovalCheckResults!.areAllQaReportsAccepted ? 'pi pi-check' : 'pi pi-times', 'ml-2']"
            :style="{
              color: props.preApprovalCheckResults!.areAllQaReportsAccepted ? 'var(--p-green-600)' : 'var(--p-red-600)',
            }"
            aria-hidden="true"
          />
        </div>
        <div style="padding: var(--spacing-xxs) var(--spacing-xs)">
          <span>Not an exempted field:</span>
          <span
            :class="[props.preApprovalCheckResults!.isDataPointEligible ? 'pi pi-check' : 'pi pi-times', 'ml-2']"
            :style="{
              color: props.preApprovalCheckResults!.isDataPointEligible ? 'var(--p-green-600)' : 'var(--p-red-600)',
            }"
            aria-hidden="true"
          />
        </div>
        <div style="padding: var(--spacing-xxs) var(--spacing-xs)">
          <span>Randomly selected for pre-approval:</span>
          <span
            :class="[props.preApprovalCheckResults!.passesRandomSampling ? 'pi pi-check' : 'pi pi-times', 'ml-2']"
            :style="{
              color: props.preApprovalCheckResults!.passesRandomSampling ? 'var(--p-green-600)' : 'var(--p-red-600)',
            }"
            aria-hidden="true"
          />
        </div>
        <div style="padding: var(--spacing-xxs) var(--spacing-xs)">
          <span>Nonsignificant deviation:</span>
          <span
            :class="[props.preApprovalCheckResults!.passesSignificanceCheck ? 'pi pi-check' : 'pi pi-times', 'ml-2']"
            :style="{
              color: props.preApprovalCheckResults!.passesSignificanceCheck ? 'var(--p-green-600)' : 'var(--p-red-600)',
            }"
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
    <p>
      The <strong>QA accepted</strong> field is true if and only if the QA was accepted. It is then not in the states
      "QA Inconclusive", "QA Rejected" or "Mixed Verdicts".
    </p>
    <p>
      The <strong>Not an exempted field</strong> is true if and only if the KPI is not on the list of fields that are
      exempted from automatic preapproval.
    </p>
    <p>
      The <strong>Randomly selected for preapproval</strong> field is true if and only if the KPI was selected to be
      preapproved by random sampling. It rejects a datapoint for pre-approval with a probability of
      {{ preApprovalConfig?.samplingProbability ?? 'unknown' }}.
    </p>
    <p>
      The <strong>Nonsignificant deviation</strong> field is true if and only if either there is no data for the
      previous year, or the data from the previous year does not significantly deviate from this year's reported data.
    </p>
  </PrimeDialog>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import PrimeDialog from 'primevue/dialog';
import Select from 'primevue/select';
import ToggleSwitch from 'primevue/toggleswitch';
import { ref } from 'vue';
import type { NextDataPointOption } from '@/types/JudgeDialogTypes.ts';
import type { PreApprovalCheckResults } from '@clients/qaservice';
import { usePreApprovalConfigQuery } from '@/api-queries/qa-service/dataset-judgement/usePreApprovalConfigQuery.ts';

const isInfoDialogOpen = ref(false);

const props = defineProps<{
  options: NextDataPointOption[];
  onlyShowUnreviewed: boolean;
  selectedNextDataPointTypeId: string | null;
  preApprovalVerdictBadge: { label: string; background: string; color: string };
  preApprovalCheckResults: PreApprovalCheckResults | null;
  isPreApprovalCheckResultsNotNull: boolean;
}>();

const { data: preApprovalConfig } = usePreApprovalConfigQuery();

const emit = defineEmits<{
  (e: 'update:onlyShowUnreviewed', value: boolean): void;
  (e: 'update:selectedNextDataPointTypeId', value: string): void;
  (e: 'goTo'): void;
}>();
</script>
