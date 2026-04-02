<template>
  <section
    data-test="custom-datapoint-section"
    style="padding: var(--spacing-xs); display: flex; flex-direction: column; height: 100%"
  >
    <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--spacing-xs)">
      <div style="display: flex; align-items: center; gap: var(--spacing-xxs)">
        <h3 style="margin-top: 0; margin-bottom: 0; white-space: nowrap">Custom datapoint</h3>
        <span
          v-if="isAccepted"
          class="pi pi-check text-green-500 ml-2 text-xl font-bold"
          data-test="accepted-check-custom-section"
          aria-label="Accepted"
        />
      </div>
      <div style="display: flex; align-items: center; gap: var(--spacing-xs)">
        <ToggleSwitch id="edit-mode-toggle" v-model="editModeEnabled" data-test="edit-mode-toggle" />
        <label for="edit-mode-toggle"> JSON </label>
      </div>
    </div>

    <div style="white-space: nowrap">
      <PrimeButton
        label="Copy original datapoint"
        variant="text"
        size="small"
        @click="emit('copyOriginal')"
        :disabled="!canCopyOriginal"
        data-test="copy-original-to-custom"
      />
      <PrimeButton
        label="Copy corrected datapoint"
        variant="text"
        size="small"
        @click="emit('copyCorrected')"
        :disabled="!canCopyCorrected"
        data-test="copy-corrected-to-custom"
      />
    </div>

    <!-- View mode: Display as editable form -->
    <div v-if="!editModeEnabled" class="p-datatable p-component">
      <div class="p-datatable-wrapper">
        <table
          class="p-datatable-table judge-modal__datatable"
          aria-label="Data point content"
          style="width: 100%; table-layout: fixed"
        >
          <tbody class="p-datatable-body">
            <tr>
              <th scope="row" class="headers-bg">Value</th>
              <td>
                <InputText
                  id="custom-value-field"
                  v-model="formData.value"
                  size="small"
                  fluid
                  placeholder="Enter Value"
                  data-test="custom-value-field"
                />
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Quality</th>
              <td>
                <Select
                  v-model="formData.quality"
                  :options="qualityOptions"
                  option-label="label"
                  option-value="value"
                  size="small"
                  fluid
                  placeholder="Select Quality"
                  data-test="custom-quality-field"
                />
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Document</th>
              <td>
                <Select
                  v-model="formData.document"
                  :options="availableDocuments"
                  option-label="label"
                  option-value="value"
                  size="small"
                  fluid
                  placeholder="Select Document"
                  data-test="custom-document-field"
                />
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Page(s)</th>
              <td>
                <InputText
                  id="custom-pages-field"
                  v-model="formData.pages"
                  size="small"
                  fluid
                  placeholder="Enter Page(s)"
                  data-test="custom-pages-field"
                />
              </td>
            </tr>
            <tr>
              <th scope="row" class="headers-bg">Comment</th>
              <td>
                <InputText
                  id="custom-comment-field"
                  v-model="formData.comment"
                  size="small"
                  fluid
                  placeholder="Write a comment"
                  data-test="custom-comment-field"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Edit mode: Display as JSON editor -->
    <div v-else style="display: flex; flex-direction: column">
      <Textarea
        id="custom-json-textarea"
        v-model="jsonValue"
        size="small"
        spellcheck="false"
        data-test="custom-json-textarea"
        style="height: 10.8rem; overflow: auto; resize: none"
      />
    </div>

    <div style="margin-top: auto; padding-top: var(--spacing-xs)">
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
import type { CustomFormData, DocumentOption } from '@/types/JudgeDialogTypes.ts';
import {
  DEFAULT_CUSTOM_JSON,
  parseDataPointJsonToFormData,
  parseFormDataToDataPointJson,
} from '@/utils/JudgeDialogUtils.ts';

const qualityOptions = Object.values(QualityOptions).map((qualityOption) => ({
  label: qualityOption,
  value: qualityOption,
}));

const props = defineProps<{
  acceptDisabled?: boolean;
  canCopyOriginal?: boolean;
  canCopyCorrected?: boolean;
  availableDocuments?: DocumentOption[];
  isAccepted?: boolean;
}>();

const emit = defineEmits<{
  accept: [];
  copyOriginal: [];
  copyCorrected: [];
}>();

const editModeEnabled = defineModel<boolean>('editModeEnabled', { default: false });
const jsonValue = defineModel<string>('json', {
  default: () => DEFAULT_CUSTOM_JSON,
});
const formData = defineModel<CustomFormData>('formData', {
  default: () => DEFAULT_CUSTOM_JSON,
});

const selectedDocumentOption = computed<DocumentOption | null>(
  () => props.availableDocuments?.find((doc) => doc.value === formData.value.document) ?? null
);

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

/**
 * Converts the form data into the JSON structure expected by the backend and updates the jsonValue.
 * @returns nothing, updates jsonValue in place.
 */
function formDataToJson(): void {
  jsonValue.value = parseFormDataToDataPointJson(formData.value, selectedDocumentOption.value);
}

/**
 * Parses the JSON from jsonValue and updates the form data accordingly. If the JSON is invalid, the form data is left unchanged.
 */
function jsonToFormData(): void {
  const parsed = parseDataPointJsonToFormData(jsonValue.value);
  if (parsed !== null) {
    formData.value = parsed;
  }
}

watch(
  () => formData.value.quality,
  (newQuality) => {
    if (newQuality === QualityOptions.NoDataFound) {
      formData.value = { ...formData.value, value: '', document: '', pages: '', comment: '' };
    }
  }
);

watch(editModeEnabled, (newVal) => {
  if (newVal) {
    formDataToJson();
  } else {
    jsonToFormData();
  }
});
</script>

<style scoped lang="scss">
.judge-modal__datatable {
  tr {
    th {
      width: var(--spacing-xxxxxl);
      padding: var(--spacing-xxs) var(--spacing-xs);
      vertical-align: middle;
    }

    td {
      padding: var(--spacing-none) var(--spacing-none);
      vertical-align: middle;
      font-size: var(--font-size-xs);
      width: 100%;
      max-width: 0;
      overflow: hidden;
    }
  }
}
</style>
