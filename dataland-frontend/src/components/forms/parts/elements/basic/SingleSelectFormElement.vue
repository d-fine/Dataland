<template>
  <Dropdown
    :options="options"
    v-bind:model-value="selectedOption"
    @update:model-value="handleInputChange($event)"
    :placeholder="placeholder"
    :name="name"
    class="w-full md:w-14rem short"
    showClear
    option-label="label"
    option-value="value"
    :disabled="disabled"
  />
  <FormKit
    type="text"
    :disabled="disabled"
    :name="name"
    v-bind:model-value="selectedOption"
    @update:model-value="handleFormKitInputChange($event)"
    outer-class="hidden-input"
    :validation-label="validationLabel"
    :validation="validation"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { Dropdown },
  props: {
    ...DropdownOptionFormFieldProps,
    disabled: {
      type: Boolean,
      default: false,
    },
    modelValue: String,
  },
  data() {
    return {
      selectedOption: undefined as undefined | string,
    };
  },
  watch: {
    modelValue(newValue: string) {
      this.selectedOption = newValue;
    },
  },
  methods: {
    /**
     * Handler for changes in the dropdown component
     * @param newInput the new value in the dropdown
     */
    handleInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:model-value", this.selectedOption);
    },
    /**
     * Handler for changes in the formkit component (e.g. called if data got loaded)
     * @param newInput the new value in the field
     */
    handleFormKitInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:model-value", this.selectedOption);
    },
  },
});
</script>
