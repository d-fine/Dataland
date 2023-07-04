<template>
  <UploadFormHeader :label="label" :description="description" :is-required="required" />
  <MultiSelectFormElement
    :name="name"
    :validation="validation"
    :validation-label="validationLabel ?? label"
    :placeholder="placeholder"
    :options="options"
    :inner-class="innerClass"
    :optionLabel="optionLabel"
    @selectedValuesChanged="selectedValuesChanged"
  />
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";

import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormField",
  emits: ["selectedValuesChanged"],
  components: { MultiSelectFormElement, UploadFormHeader },
  props: {
    ...DropdownOptionFormFieldProps,
    optionLabel: {
      type: String,
    },
  },
  methods: {
    /**
     * handle changes in selected Countrys
     * @param newVal - selected countrys new Value
     */
    selectedValuesChanged(newVal) {
      this.$emit("selectedValuesChanged", newVal);
    },
  },
});
</script>
