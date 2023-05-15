<template>
  <MultiSelect
    v-model="selections"
    :options="options"
    :placeholder="placeholder"
    option-label="label"
    option-value="value"
    :show-toggle-all="false"
    :class="innerClass"
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
    >
      <FormKitMessages />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from "vue";
import { FormKit, FormKitMessages } from "@formkit/vue";
import MultiSelect from "primevue/multiselect";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "MultiSelectFormElement",
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
  props: {
    name: {
      type: String,
      required: true,
    },
    validation: {
      type: String,
      default: "",
    },
    validationLabel: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<DropdownOption>,
      required: true,
    },
    innerClass: {
      type: String,
      default: "",
    },
  },
});
</script>
