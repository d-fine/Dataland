<template>
  <div>
    <Calendar
      inputId="icon"
      :showIcon="true"
      dateFormat="D, M dd, yy"
      :modelValue="dataForDisplay.data"
      :placeholder="props.context.placeholder"
      :maxDate="props.context.todayAsMax ? new Date() : undefined"
      @update:modelValue="handleModelUpdate"
    />
    <FormKitMessageDisplay :messages="props.context.messages" />
  </div>
</template>

<script setup lang="ts">
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import { type FormFieldContext } from "@/components/forms/v2/fields/Utils";
import Calendar from "primevue/calendar";
import FormKitMessageDisplay from "@/components/forms/v2/FormKitMessageDisplay.vue";
import { computed, type ComputedRef, watch } from "vue";

const props = defineProps<{
  context: FormFieldContext<string, { placeholder?: string; todayAsMax: boolean }>;
}>();

function handleModelUpdate(newValue: string | Date | string[] | Date[] | undefined) {
  const updatedValue = getHyphenatedDate(newValue);
  props.context.node.input(updatedValue);
}

type DisplayData = { data: Date | null; error: boolean };

const dataForDisplay: ComputedRef<DisplayData> = computed(() => {
  const rawInput: unknown = props.context._value;
  if (rawInput == null) {
    return { data: null, error: false };
  }

  if (typeof rawInput !== "string") {
    return { data: null, error: true };
  }

  const parsedTimestamp = Date.parse(`${rawInput}T00:00:00`);
  if (isNaN(parsedTimestamp)) {
    return { data: null, error: true };
  }
  return { data: new Date(parsedTimestamp), error: false };
});

watch(
  dataForDisplay,
  (newValue) => {
    if (newValue.error) {
      props.context.node.setErrors([
        `Received illegal date from source (${props.context._value}). Please select a new date to continue.`,
      ]);
    } else {
      props.context.node.clearErrors();
    }
  },
  {
    immediate: true,
  },
);
</script>
