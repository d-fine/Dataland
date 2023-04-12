<template>
  <div class="form-field" v-if="displayed === 'Yes'">
    <UploadFormHeader :name="displayName" :explanation="info" />
    <FormKit
      type="radio"
      :name="name"
      :validation-label="displayName"
      :options="radioButtonsOptions"
      :validation="required"
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
    required: {
      type: String,
      default: "",
    },
    displayed: {
      type: String,
      default: "Yes",
    },
  },
});
</script>
