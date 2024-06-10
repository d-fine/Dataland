<template>
  <div>
    <Dropdown
      :options="[{ label: 'A', value: 'ABCDEFG' }]"
      :model-value="dataForDisplay.file"
      @update:model-value="handleFileChange"
      :show-clear="true"
      placeholder="Please select a report."
      option-label="label"
      option-value="value"
    />
    <InputNumber :model-value="dataForDisplay.page" @update:model-value="handlePageChange" />
    <FormKitMessageDisplay :messages="props.context.messages" />
  </div>
</template>

<script setup lang="ts">
import InputNumber from "primevue/inputnumber";
import Dropdown from "primevue/dropdown";
import { type FormFieldContext } from "@/components/forms/v2/fields/Utils";
import FormKitMessageDisplay from "@/components/forms/v2/FormKitMessageDisplay.vue";
import { computed } from "vue";
import { type ExtendedDocumentReference } from "@clients/backend";

const props = defineProps<{
  context: FormFieldContext<ExtendedDocumentReference, {}>;
}>();

const dataForDisplay = computed<{ file: string | null; page: number | null; error: boolean }>(() => {
  const rawInput = props.context._value;
  if (rawInput == null) {
    return { file: null, page: null, error: false };
  }
  return { file: rawInput.fileName, page: rawInput.page, error: false };
});

function sendUpdate(selectedFile: string | null, selectedPage: number | null) {
  if (selectedFile == null) {
    void props.context.node.input(null);
    return;
  }
  const newDocumentReference: ExtendedDocumentReference = {
    fileName: selectedFile,
    fileReference: `Reference to ${selectedFile}`,
    page: selectedPage,
  };
  void props.context.node.input(newDocumentReference);
}

function handleFileChange(newFile: string | null) {
  sendUpdate(newFile, dataForDisplay.value.page);
}

function handlePageChange(newPage: number | null) {
  sendUpdate(dataForDisplay.value.file, newPage);
}
</script>
