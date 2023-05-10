<template>
  <FormKit
    type="radio"
    :name="name"
    :validation-label="validationLabel"
    :options="options"
    :validation="validation"
    :outer-class="{
      'yes-no-radio': true,
    }"
    :inner-class="{
      'formkit-inner': false,
    }"
    :input-class="{
      'formkit-input': false,
      'p-radiobutton': true,
    }"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "RadioButtonsFormElement",
  components: { FormKit },
  props: {
    name: {
      type: String,
      default: "",
    },
    options: {
      type: Array as () => Array<typeof Option>,
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
  },
});
</script>
