<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" />
    <FormKit
      type="radio"
      :name="name"
      :validation-label="displayName"
      :options="radioButtonsOptions"
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
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { Option } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "RadioButtonsForm",
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
      type: Array as () => Array<Option>,
      required: true,
    },
    validation: {
      type: String,
      default: "",
    },
  },
});
</script>
