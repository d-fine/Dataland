<template>
  <FormKit
    type="text"
    :name="name!"
    :validation-label="displayName!"
    :validation="validation!"
    :placeholder="placeholder"
    inner-class="short"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "InputTextFormElement",
  components: { FormKit },
  props: {
    name: {
      type: String,
      required: true,
    },
    displayName: {
      type: String,
      default: "",
    },
    validation: {
      type: String,
      default: "",
    },
    placeholder: {
      type: String,
      default: "",
    },
  },
});
</script>
