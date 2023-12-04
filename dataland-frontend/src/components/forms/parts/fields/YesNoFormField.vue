<template>
  <div :class="classes" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <RadioButtonsFormElement
      :name="name"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :options="HumanizedYesNo"
      @update:currentValue="emitUpdateCurrentValue"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import RadioButtonsFormElement from "@/components/forms/parts/elements/basic/RadioButtonsFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { HumanizedYesNo } from "@/utils/YesNoNa";

export default defineComponent({
  name: "YesNoFormField",
  computed: {
    HumanizedYesNo() {
      return HumanizedYesNo;
    },
  },
  components: { RadioButtonsFormElement, UploadFormHeader },
  props: {
    ...BaseFormFieldProps,
    radioButtonsDataTest: String,
    classes: {
      type: String,
      default: "form-field",
    },
  },
  methods: {
    /**
     * Emits an event when the currentValue has been changed
     * @param currentValue current value
     */
    emitUpdateCurrentValue(currentValue: string) {
      this.$emit("update:currentValue", currentValue);
    },
  },
});
</script>
