<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" />
    <FreeTextFormElement :name="name" :display-name="displayName" :validation="validation" :placeholder="placeholder" />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import FreeTextFormElement from "@/components/forms/parts/elements/basic/FreeTextFormElement.vue";

export default defineComponent({
  name: "FreeTextFormField",
  components: { FreeTextFormElement, UploadFormHeader },
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
</script>
