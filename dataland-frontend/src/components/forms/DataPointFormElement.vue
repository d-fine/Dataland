<template>
  <FormKit type="group" :name="name">
    <FormKit
      type="text"
      name="value"
      validation="number"
      :label="label"
      :inner-class="innerClass"
      :input-class="inputClass"
    />
    <FormKit type="hidden" name="quality" value="Estimated" />
  </FormKit>
</template>

<script>
import { FormKit } from "@formkit/vue";
export default {
  component: { FormKit },
  name: "DataPointFormElement",
  data: () => ({
    innerClass: {
      "formkit-inner": false,
      "p-inputwrapper": true,
    },
    inputClass: {
      "formkit-input": false,
      "p-inputtext": true,
      "w-full": true,
    },
  }),
  props: {
    name: {
      type: String,
    },
    label: {
      type: String,
    },
  },
};
</script>

<style scoped></style>
