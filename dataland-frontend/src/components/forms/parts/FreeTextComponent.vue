<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" />
    <FormKit
      type="text"
      :name="name!"
      :validation-label="displayName!"
      :validation="validation!"
      :placeholder="placeholder"
      inner-class="short"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "FreeTextComponent",
  components: { UploadFormHeader, FormKit },
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
