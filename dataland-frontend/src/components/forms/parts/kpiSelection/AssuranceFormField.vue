<template>
  <FormKit type="group" :name="name">
    <div class="mb-3 form-field">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div>
        <h2 style="color: #3e1046">AssuranceFormField.vue</h2>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";

export default defineComponent({
  name: "AssuranceFormField",
  components: { SingleSelectFormElement, FormKit, UploadFormHeader },
  data() {
    return {
      countryCodeOptions: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
    };
  },
  props: DropdownOptionFormFieldProps,
});
</script>
