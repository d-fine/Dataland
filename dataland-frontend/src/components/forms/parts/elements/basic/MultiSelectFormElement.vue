<template>
  <MultiSelect
    v-model="selections"
    :options="options"
    :placeholder="placeholder"
    :show-toggle-all="false"
    :class="innerClass"
    :optionValue="optionValue"
    :optionLabel="optionLabel"
    :maxSelectedLabels="3"
  />
  <!--
    Note: It is required to set the id of this div to the FormKit node Id to allow the checkCustomInputs methods
    in the validationUtils.ts file to scroll to this component when an error is detected. This is because the FormKit
    List type does not create a wrapper component on its own.
  -->
  <div :id="formkitMultiSelectFormElement?.node?.props?.id || undefined">
    <FormKit
      type="list"
      ref="formkitMultiSelectFormElement"
      :validation-label="validationLabel"
      :validation="validation"
      :name="name"
      v-model="selections"
      outer-class="hidden-input"
      :ignore="ignore"
    >
      <FormKitMessages />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { ComponentPropsOptions, defineComponent, ref } from "vue";
import { FormKit, FormKitMessages } from "@formkit/vue";
import MultiSelect from "primevue/multiselect";
import { MultiSelectFormProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "MultiSelectFormElement",
  emits: ["selectedValuesChanged"],
  components: { FormKit, MultiSelect, FormKitMessages },
  setup() {
    return {
      formkitMultiSelectFormElement: ref(),
    };
  },
  data() {
    return {
      selections: [] as string[],
    };
  },
  watch: {
    selections(newVal) {
      this.$emit("selectedValuesChanged", newVal);
    },
    modelValue(newVal: []) {
      this.selections = newVal;
    },
  },
  props: { ...MultiSelectFormProps } as Readonly<ComponentPropsOptions>,
});
</script>
