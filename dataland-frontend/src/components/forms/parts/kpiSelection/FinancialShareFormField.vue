<template>
  <FormKit type="group" :name="name">
    <div class="mb-3 form-field">
      <UploadFormHeader :label="label" :description="description ?? ''" :is-required="required" />
      <div class="next-to-each-other">
        <FormKit
          type="text"
          name="relativeShareInPercent"
          validation-label="Relative Value"
          validation="number|between:0,100"
          placeholder="Relative Value in %"
          outer-class="short"
        />
        <FormKit type="group" name="absoluteShare">
          <FormKit
            type="text"
            name="amount"
            validation-label="Absolute Value"
            validation="number"
            placeholder="Absolute Value"
            outer-class="short"
          />
          <SingleSelectFormElement
            name="currency"
            validation="length:2,3"
            validation-label="Currency"
            placeholder="Currency"
            :options="countryCodeOptions"
          />
        </FormKit>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { FormKit } from "@formkit/vue";
import {
  BaseFormFieldProps,
  DropdownOptionFormFieldProps,
  FormFieldPropsWithPlaceholder,
} from "@/components/forms/parts/fields/FormFieldProps";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import SingleSelectFormElement from "@/components/forms/parts/elements/basic/SingleSelectFormElement.vue";

export default defineComponent({
  name: "FinancialShareFormField",
  components: { SingleSelectFormElement, FormKit, UploadFormHeader },
  data() {
    return {
      countryCodeOptions: getDataset(DropdownDatasetIdentifier.CurrencyCodes),
    };
  },
  props: {
    ...BaseFormFieldProps,
    evidenceDesired: {
      type: Boolean,
      default: false,
    },
    unit: {
      type: String,
    },
  },
});
</script>
