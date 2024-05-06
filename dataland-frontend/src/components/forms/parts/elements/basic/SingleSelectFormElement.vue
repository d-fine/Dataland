<template>
  <div :data-test="dataTest">
    <Dropdown
      :options="displayOptions"
      v-bind:model-value="selectedOption"
      @update:model-value="handleInputChange($event)"
      :placeholder="placeholder"
      :name="name"
      :showClear="!required"
      option-label="label"
      option-value="value"
      :class="'bottom-line ' + inputClass + ' ' + (!selectedOption ? ' no-selection' : '')"
      :disabled="disabled"
    />
    <!--  FormKit component only used to parse the selected value in nested FormKits  -->
    <FormKit
      type="text"
      :disabled="disabled"
      :name="name"
      v-bind:model-value="selectedOption"
      @update:model-value="handleFormKitInputChange($event)"
      :outer-class="{ 'hidden-input': true, 'formkit-outer': false }"
      :validation-label="validationLabel"
      :validation="validation"
      :ignore="ignore"
      :validation-messages="validationMessages"
    />
  </div>
</template>

<script lang="ts">
import { type ComponentPropsOptions, defineComponent } from "vue";
import Dropdown from "primevue/dropdown";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";
import { type DropdownOption } from "@/utils/PremadeDropdownDatasets";
import { isStringArray } from "@/utils/TypeScriptUtils";

type OptionType = string[] | DropdownOption[] | Record<string, string>;

/**
 * Converts from a liberally choosen amount of formats to specify options
 * to a unified DropDownOption interface for easier processing
 * @param options the input option in one of the desired formats
 * @returns the options converted to the unified format
 */
function convertOptionTypeToDropdownOptions(options: OptionType | null | undefined): DropdownOption[] {
  if (!options) return [];
  if (Array.isArray(options)) {
    if (isStringArray(options)) {
      return options.map((entry) => ({ value: entry, label: entry }));
    } else {
      return options;
    }
  } else {
    return Object.entries(options).map((pair) => ({ value: pair[0], label: pair[1] }));
  }
}

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { Dropdown },
  props: Object.assign(deepCopyObject(DropdownOptionFormFieldProps as ObjectType), {
    inputClass: { type: String, default: "long" },
    deselectRemovedOptionsOnShrinkage: {
      type: Boolean,
      default: true,
    },
    allowUnknownOption: {
      type: Boolean,
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    modelValue: String,
    ignore: {
      type: Boolean,
      default: false,
    },
    dataTest: String,
    validationMessages: Object,
  }) as Readonly<ComponentPropsOptions>,
  data() {
    return {
      selectedOption: this.modelValue as string | null,
    };
  },
  emits: ["update:modelValue"],
  watch: {
    modelValue(newValue: string) {
      this.selectedOption = newValue;
      this.deselectUnknownOptionsIfNotPermitted();
    },
    allowUnknownOption() {
      this.deselectUnknownOptionsIfNotPermitted();
    },
    options(newInput: OptionType, oldInput: OptionType) {
      if (!this.deselectRemovedOptionsOnShrinkage) return;

      const currentValueIsAllowedOnOldData = convertOptionTypeToDropdownOptions(oldInput).some(
        (it) => it.value == this.modelValue,
      );

      const currentValueIsAllowedOnNewData = convertOptionTypeToDropdownOptions(newInput).some(
        (it) => it.value == this.modelValue,
      );

      if (currentValueIsAllowedOnOldData && !currentValueIsAllowedOnNewData) {
        this.selectedOption = null;
      }
    },
  },
  computed: {
    convertedInputOptions(): DropdownOption[] {
      return convertOptionTypeToDropdownOptions(this.options as OptionType);
    },
    displayOptions(): DropdownOption[] {
      const returnOptions = [...this.convertedInputOptions];

      if (
        this.allowUnknownOption &&
        this.selectedOption &&
        !returnOptions.some((it) => it.value == this.selectedOption)
      ) {
        returnOptions.push({
          label: this.selectedOption,
          value: this.selectedOption,
        });
      }
      return returnOptions;
    },
  },
  methods: {
    /**
     * Handler for changes in the dropdown component
     * @param newInput the new value in the dropdown
     */
    handleInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:modelValue", this.selectedOption);
    },
    /**
     * Handler for changes in the formkit component (e.g. called if data got loaded)
     * @param newInput the new value in the field
     */
    handleFormKitInputChange(newInput: string) {
      this.selectedOption = newInput;
      this.$emit("update:modelValue", this.selectedOption);
    },

    /**
     * Deselects the current element if unknown options are not permitted
     * and the current selection is not in the list of allowed options
     */
    deselectUnknownOptionsIfNotPermitted() {
      if (!this.allowUnknownOption && !this.convertedInputOptions.some((it) => it.value == this.selectedOption)) {
        this.selectedOption = null;
      }
    },
  },
});
</script>

<style lang="scss" scoped>
.bottom-line {
  border-style: solid;
  border-width: 0 0 1px 0;
  border-color: $brown-lighter;
}
</style>
