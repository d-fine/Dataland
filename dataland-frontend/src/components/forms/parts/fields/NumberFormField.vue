<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" />
    <NumberFormElement :name="name" :display-name="displayName" :validation="validation" :placeholder="placeholder" />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import NumberFormElement from "@/components/forms/parts/elements/basic/NumberFormElement.vue";

export default defineComponent({
  name: "NumberFormField",
  components: { NumberFormElement, UploadFormHeader },
  props: {
    name: {
      type: String,
      required: true,
    },
    info: {
      type: String,
      default: "",
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
