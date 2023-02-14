<template>
  <div class="form-field">
    <UploadFormHeader :name="kpisNameMappings[name]" :explanation="kpisInfoMappings[name]" />
    <FormKit
      type="radio"
      :name="name"
      :validation-label="kpisNameMappings[name]"
      :options="['Yes', 'No']"
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
import { kpisNameMappings, kpisInfoMappings } from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  name: "YesNoComponent",
  components: { UploadFormHeader, FormKit },
  data: () => ({
    kpisNameMappings,
    kpisInfoMappings,
  }),
  props: {
    name: {
      type: String,
    },
  },
});
</script>
