<template>
  <FormKit
    type="select"
    :validation-label="validationLabel"
    :validation="validation"
    :placeholder="placeholder"
    :name="name"
    :options="options"
    :inner-class="innerClass"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { DropdownOption } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "SingleSelectFormElement",
  components: { FormKit },
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
