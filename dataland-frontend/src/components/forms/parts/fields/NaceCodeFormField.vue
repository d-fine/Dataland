<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <NaceCodeSelector v-model="selectedNaceCodes" />
    <!--
    Note: It is required to set the id of this div to the FormKit node Id to allow the checkCustomInputs methods
    in the validationUtils.ts file to scroll to this component when an error is detected. This is because the FormKit
    List type does not create a wrapper component on its own.
    -->
    <div :id="formKitNaceCodeInput?.node?.props?.id || undefined">
      <FormKit
        type="list"
        :validation="validation"
        :validation-label="validationLabel ?? displayName"
        :name="name"
        ref="formKitNaceCodeInput"
        v-model="selectedNaceCodes"
        outer-class="hidden-input"
      >
        <FormKitMessages />
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent, ref } from "vue";
import { FormKit, FormKitMessages } from "@formkit/vue";
import NaceCodeSelector from "@/components/forms/parts/elements/derived/NaceCodeSelector.vue";

export default defineComponent({
  name: "NaceCodeFormField",
  components: { NaceCodeSelector, FormKit, FormKitMessages, UploadFormHeader },
  setup() {
    return {
      formKitNaceCodeInput: ref(),
    };
  },
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
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
