<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName!" :explanation="info!" :is-required="required" />
    <AddressFormElement :name="name" :display-name="displayName" :validation="validation" />
  </div>
</template>

<script lang="ts">
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import AddressFormElement from "@/components/forms/parts/elements/derived/AddressFormElement.vue";

export default defineComponent({
  name: "AddressFormField",
  components: { AddressFormElement, UploadFormHeader },
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
    required: {
      type: Boolean,
      default: false,
    },
  },
});
</script>
