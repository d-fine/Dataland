<template>
  <section class="judge-modal__section" data-test="custom-datapoint-section">
    <div class="judge-modal__section-header-row">
      <h3 class="judge-modal__section-title">Custom datapoint</h3>
      <div class="judge-modal__toggle">
        <ToggleSwitch
            id="edit-mode-toggle"
            v-model="editModeEnabled"
            data-test="edit-mode-toggle"
        />
        <label for="edit-mode-toggle">
          JSON
        </label>
      </div>
    </div>

    <div class="judge-modal__custom-actions">
      <PrimeButton
          label="Copy original datapoint"
          variant="text"
          @click="emit('copyOriginal')"
          :disabled="!canCopyOriginal"
          data-test="copy-original-to-custom"
      />
      <PrimeButton
          label="Copy corrected datapoint"
          variant="text"
          @click="emit('copyCorrected')"
          :disabled="!canCopyCorrected"
          data-test="copy-corrected-to-custom"
      />
    </div>

    <!-- View mode: Display as editable form -->
    <div v-if="!editModeEnabled" class="judge-modal__form-table">
      <div class="judge-modal__form-row">
        <label for="custom-value-field" class="judge-modal__form-label">Value</label>
        <InputText
            id="custom-value-field"
            v-model="formData.value"
            class="judge-modal__form-field"
            placeholder="Select Value"
            data-test="custom-value-field"
        />
      </div>

      <div class="judge-modal__form-row">
        <label class="judge-modal__form-label">Quality</label>
        <Select
            v-model="formData.quality"
            :options="qualityOptions"
            option-label="label"
            option-value="value"
            placeholder="Select Quality"
            class="judge-modal__form-field"
            data-test="custom-quality-field"
        />
      </div>

      <div class="judge-modal__form-row">
        <label for="custom-document-field" class="judge-modal__form-label">Document</label>
        <InputText
            id="custom-document-field"
            v-model="formData.document"
            class="judge-modal__form-field"
            placeholder="Select Document"
            data-test="custom-document-field"
        />
      </div>

      <div class="judge-modal__form-row">
        <label for="custom-pages-field" class="judge-modal__form-label">Page(s)</label>
        <InputText
            id="custom-pages-field"
            v-model="formData.pages"
            class="judge-modal__form-field"
            placeholder="Select Page(s)"
            data-test="custom-pages-field"
        />
      </div>

      <div class="judge-modal__form-row">
        <label for="custom-comment-field" class="judge-modal__form-label">Comment</label>
        <Textarea
            id="custom-comment-field"
            v-model="formData.comment"
            class="judge-modal__form-field"
            placeholder="Write a comment"
            rows="2"
            data-test="custom-comment-field"
        />
      </div>
    </div>

    <!-- Edit mode: Display as JSON editor -->
    <div v-else class="judge-modal__json-editor">
      <label for="custom-json-textarea">
        Custom datapoint JSON
      </label>
      <Textarea
          id="custom-json-textarea"
          v-model="jsonValue"
          class="judge-modal__json-textarea"
          spellcheck="false"
          data-test="custom-json-textarea"
      />
    </div>

    <div class="judge-modal__section-actions">
      <PrimeButton
          label="ACCEPT CUSTOM"
          @click="emit('accept')"
          :disabled="acceptDisabled || !isCustomJsonValid"
          data-test="accept-custom-button"
      />
      <span v-if="editModeEnabled && !isCustomJsonValid && jsonValue.trim().length > 0">
        Custom JSON must be valid JSON.
      </span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import PrimeButton from 'primevue/button';
import Select from 'primevue/select';
import ToggleSwitch from 'primevue/toggleswitch';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { QualityOptions } from '@clients/backend';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import type { CustomFormData, DataPointSourceInfo, DataPointDetail } from '@/components/resources/datasetReview/JudgeDialogTypes.ts';

const DEFAULT_CUSTOM_JSON = JSON.stringify(
    { value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } },
    null,
    2
);

const qualityOptions = Object.values(QualityOptions).map((qualityOption) => ({
  label: humanizeStringOrNumber(qualityOption),
  value: qualityOption,
}));

const props = defineProps<{
  acceptDisabled?: boolean;
  canCopyOriginal?: boolean;
  canCopyCorrected?: boolean;
}>();

const emit = defineEmits<{
  accept: [];
  copyOriginal: [];
  copyCorrected: [];
}>();

const editModeEnabled = defineModel<boolean>('editModeEnabled', { default: false });
const jsonValue = defineModel<string>('json', {
  default: () => JSON.stringify({ value: null, quality: null, comment: null, dataSource: { fileName: null, page: null } }, null, 2),
});
const formData = defineModel<CustomFormData>('formData', {
  default: () => ({ value: '', quality: '', document: '', pages: '', comment: '' }),
});

const isCustomJsonValid = computed<boolean>(() => {
  if (!editModeEnabled.value) {
    const f = formData.value;
    return [f.value, f.quality, f.document, f.pages, f.comment].some((v) => v.trim().length > 0);
  }
  if (!jsonValue.value.trim()) return false;
  try {
    JSON.parse(jsonValue.value);
    return true;
  } catch {
    return false;
  }
});

function formDataToJson(): void {
  const { value, quality, comment, document, pages } = formData.value;

  const dataSource: DataPointSourceInfo = {
    ...(document ? { fileName: document } : {}),
    ...(pages ? { page: pages } : {}),
  };

  const data: DataPointDetail = {
    ...(value && { value }),
    ...(quality && { quality }),
    ...(comment && { comment }),
    ...(Object.keys(dataSource).length > 0 && { dataSource }),
  };

  jsonValue.value = Object.keys(data).length > 0 ? JSON.stringify(data, null, 2) : DEFAULT_CUSTOM_JSON;
}

function jsonToFormData(): void {
  try {
    const parsed = JSON.parse(jsonValue.value) as DataPointDetail;
    const toStr = (v: unknown): string => (v === null || v === undefined ? '' : String(v));
    formData.value = {
      value: toStr(parsed.value),
      quality: toStr(parsed.quality),
      document: toStr(parsed.dataSource?.fileName ?? parsed.dataSource?.fileReference),
      pages: toStr(parsed.dataSource?.page),
      comment: toStr(parsed.comment),
    };
  } catch {
    // invalid JSON, leave form as-is
  }
  jsonValue.value = DEFAULT_CUSTOM_JSON;
}

watch(editModeEnabled, (newVal) => {
  if (newVal) {
    formDataToJson();
  } else {
    jsonToFormData();
  }
});
</script>

<style scoped lang="scss">
.judge-modal__section {
  padding: var(--spacing-xs);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.judge-modal__section-title {
  margin-top: 0;
  margin-bottom: var(--spacing-xs);
  white-space: nowrap;
}

.judge-modal__section-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xs);

  > .judge-modal__section-title {
    margin-bottom: 0;
  }
}

.judge-modal__toggle {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
}

.judge-modal__custom-actions {
  display: flex;
  gap: 0.25rem;
  margin: 0;
}

.judge-modal__form-table {
  display: flex;
  flex-direction: column;
  gap: 0;
  flex: 1;
  min-height: 12rem;
  margin-top: var(--spacing-xs);
}

.judge-modal__form-row {
  display: flex;
  align-items: flex-start;
  gap: 0;
  padding: 0.1rem 0;
  flex-shrink: 0;
}

.judge-modal__form-label {
  width: 8rem;
  padding-right: var(--spacing-md);
  text-align: left;
  flex-shrink: 0;
}

.judge-modal__form-row:last-child .judge-modal__form-label {
  padding-top: 0.4rem;
}

.judge-modal__form-field {
  flex: 1;
}

.judge-modal__json-editor {
  margin-top: var(--spacing-xs);
  flex: 1;
  min-height: 12rem;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xxs);
}

.judge-modal__json-textarea {
  width: 100%;
  flex: 1;
  min-height: 0;
  height: 100%;
  overflow: auto;
}

.judge-modal__section-actions {
  margin-top: auto;
  padding-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}
</style>


