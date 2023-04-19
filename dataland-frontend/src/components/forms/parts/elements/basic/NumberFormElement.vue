<template>
  <FormKit
    type="number"
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
  name: "NumberFormElement",
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

// TODO what about props for e.g. min and step size
</script>
