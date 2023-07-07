<template>
  <div class="form-field" :data-test="name">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <MultiSelectFormElement
      :name="name"
      v-model="internalSelections"
      :validation="validation"
      :validation-label="validationLabel ?? label"
      :placeholder="placeholder"
      :options="options"
      :inner-class="innerClass"
      :optionValue="optionValue"
      :optionLabel="optionLabel"
      :ignore="ignore"
      @selectedValuesChanged="selectedValuesChanged"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { ComponentPropsOptions, defineComponent } from "vue";

import MultiSelectFormElement from "@/components/forms/parts/elements/basic/MultiSelectFormElement.vue";
import { MultiSelectFormProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormField",
  emits: ["selectedValuesChanged"],
  components: { MultiSelectFormElement, UploadFormHeader },
  data() {
    return {
      internalSelections: this.modelValue,
    };
  },
  props: { ...MultiSelectFormProps } as Readonly<ComponentPropsOptions>,
  methods: {
    /**
     * handle changes in selected Countries
     * @param newVal - selected Countries new Value
     */
    selectedValuesChanged(newVal) {
      this.$emit("selectedValuesChanged", newVal);
    },
  },
});
</script>
