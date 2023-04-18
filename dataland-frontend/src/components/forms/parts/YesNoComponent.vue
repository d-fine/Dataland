<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" />
    <FormKit
      type="radio"
      :name="name"
      :validation-label="displayName"
      :options="radioButtonsOptions"
      :validation="validation"
      outer-class="yes-no-radio"
      inner-class="formkit-inner"
      input-class="formkit-input p-radiobutton"
    />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";

export default defineComponent({
  name: "YesNoComponent",
  components: { UploadFormHeader, FormKit },
  props: {
    name: {
      type: String,
      default: "",
    },
    info: {
      type: String,
      default: "",
    },
    displayName: {
      type: String,
      default: "",
    },
    radioButtonsOptions: {
      type: Array as () => Array<string>,
      default: () => ["Yes", "No"],
    },
    validation: {
      type: String,
      default: "",
    },
  },
});
</script>
