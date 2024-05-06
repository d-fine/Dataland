<template>
  <div :class="containerClass">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <SingleSelectFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      :input-class="inputClass"
      :required="required"
      :class="containerClass"
      :ignore="ignore"
      :data-test="dataTest"
      :validation-messages="validationMessages"
      v-bind:model-value="selectedOption"
      @update:model-value="handleInputChange"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { type ComponentPropsOptions, defineComponent } from "vue";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { deepCopyObject, type ObjectType } from "@/utils/UpdateObjectUtils";

export default defineComponent({
  name: "SingleSelectFormField",
  components: { SingleSelectFormElement, UploadFormHeader },
  props: Object.assign(deepCopyObject(DropdownOptionFormFieldProps as ObjectType), {
    inputClass: { type: String, default: "long" },
    containerClass: { type: String, default: "form-field" },
    ignore: { type: Boolean, default: false },
    modelValue: String,
    dataTest: String,
    validationMessages: Object,
  }) as Readonly<ComponentPropsOptions>,
  emits: ["update:modelValue"],
  data() {
    return {
      selectedOption: null as null | string,
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
      this.$emit("update:modelValue", this.selectedOption);
    },
  },
});
</script>
