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
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "MultiSelectFormElement",
  components: { FormKit, MultiSelect, FormKitMessages },
  setup() {
    return {
      formkitMultiSelectFormElement: ref(),
    };
  },
  emits: ["update:selectedItemsBind"],
  computed: {
    selections: {
      get(): Array<DropdownOption> {
        // eslint-disable-next-line @typescript-eslint/no-unsafe-return
        return this.selectedItemsBind;
      },
      set(newValue: Array<DropdownOption>) {
        this.$emit("update:selectedItemsBind", newValue);
      },
    },
  },
  props: {
    ...MultiSelectFormProps,
    selectedItemsBind: {
      type: Array,
      default: () => [],
    },
  } as Readonly<ComponentPropsOptions>,
});
</script>
