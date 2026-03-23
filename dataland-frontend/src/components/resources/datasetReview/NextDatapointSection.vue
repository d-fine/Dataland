<template>
  <div>
    <section class="judge-modal__section judge-modal__section--centered" data-test="next-datapoint-section">
      <h3 class="judge-modal__section-title">Next datapoint</h3>

      <div class="judge-modal__toggle">
        <ToggleSwitch
            id="only-unreviewed-toggle"
            v-model="onlyShowUnreviewed"
            data-test="only-unreviewed-toggle"
        />
        <label for="only-unreviewed-toggle">
          Only show unreviewed
        </label>
      </div>

      <div class="judge-modal__next-select-container">
        <Select
            v-model="selectedNextDataPointTypeId"
            :options="options"
            optionLabel="label"
            optionValue="value"
            :filter="true"
            placeholder="Select next datapoint"
            :pt="{ root: { style: { flex: '1' } } }"
            data-test="next-datapoint-select"
        >
          <template #option="slotProps">
            <div class="judge-modal__next-option" :class="{ 'judge-modal__next-option--reviewed': slotProps.option.reviewed }">
              <i
                  v-if="slotProps.option.reviewed"
                  class="pi pi-check judge-modal__next-option-icon--reviewed"
                  aria-hidden="true"
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
        />
      </div>
    </section>

    <section v-if="patchError" class="judge-modal__section">
      <Message severity="error" data-test="judge-modal-patch-error">
        {{ patchError }}
      </Message>
    </section>
  </div>
</template>

<script setup lang="ts">
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import Message from 'primevue/message';
import ToggleSwitch from 'primevue/toggleswitch';
import type { NextDatapointOption } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

defineProps<{
  options: NextDatapointOption[];
  patchError?: string | null;
}>();

const emit = defineEmits<{
  goTo: [];
}>();

const onlyShowUnreviewed = defineModel<boolean>('onlyShowUnreviewed', { default: true });
const selectedNextDataPointTypeId = defineModel<string | null>('selectedNextDataPointTypeId', { default: null });
</script>

<style scoped lang="scss">
.judge-modal__section {
  padding: var(--spacing-xs);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__section--centered {
  justify-content: center;
}

.judge-modal__section-title {
  margin-top: 0;
  margin-bottom: var(--spacing-xs);
  white-space: nowrap;
}

.judge-modal__toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.judge-modal__next-select-container {
  display: flex;
  gap: var(--spacing-xs);
  margin-top: var(--spacing-sm);
}

.judge-modal__next-option {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.judge-modal__next-option--reviewed {
  opacity: 0.45;
}

.judge-modal__next-option-icon--reviewed {
  color: var(--p-green-500);
}
</style>

