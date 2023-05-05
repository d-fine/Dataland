<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <NaceCodeSelector v-model="selectedNaceCodes" />
    <FormKit
      type="list"
      :validation="validation!"
      :validation-label="validationLabel ?? displayName"
      :name="name"
      v-model="selectedNaceCodes"
      outer-class="hidden-input"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import NaceCodeSelector from "@/components/forms/parts/elements/derived/NaceCodeSelector.vue";

export default defineComponent({
  name: "NaceCodeFormField",
  components: { NaceCodeSelector, FormKit, UploadFormHeader },
  data() {
    return {
      selectedNaceCodes: [] as Array<string>,
    };
  },
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
    validationLabel: {
      type: String,
    },
    placeholder: {
      type: String,
      default: "",
    },
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
