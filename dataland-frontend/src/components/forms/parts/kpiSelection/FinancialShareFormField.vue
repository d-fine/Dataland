<template>
  <FormKit type="group" :name="name">
    <div class="mb-3 form-field">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div class="next-to-each-other">
        <FormKit
          type="text"
          name="value"
          :validation-label="validationLabel ?? label"
          :validation="`number|${validation}`"
          placeholder="Value"
          outer-class="short"
        />
        <SingleSelectFormField
          validation="length:2,3"
          validation-label="Currency used in the report"
          placeholder="Currency used in the report"
          :options="countryCodeOptions"
          name="currency"
          label="Currency"
          description="The 3-letter alpha code that represents the currency used in the report."
        />
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import { DropdownOptionFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "FinancialShareFormField",
  components: { SingleSelectFormField, FormKit, UploadFormHeader },
  data() {
    return {
      countryCodeOptions: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
    };
  },
  props: DropdownOptionFormFieldProps,
});
</script>
