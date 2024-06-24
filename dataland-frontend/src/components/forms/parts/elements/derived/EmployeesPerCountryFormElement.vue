<template>
  <UploadFormHeader label="Country" description="Please specify the country where you have employees" />
  <SingleSelectFormElement
    label="Country"
    name="country"
    validation-label="Country"
    :required="required"
    placeholder="Country"
    :options="allCountry"
    data-test="country"
    input-class="long normal-line-height"
    validation="required"
  />
  <NumberFormField
    :name="'numberOfEmployeesInHeadCount'"
    :validation-label="validationLabel"
    label="Number of employees in head count"
    description="Please specify the number of employees in head count"
    :validation="`integer`"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'numberOfEmployeesInFullTimeEquivalent'"
    :validation-label="validationLabel"
    label="Number of employees in full time equivalent"
    description="Please specify the number of employees in full time equivalent"
    :validation="validation"
    input-class="col-4 pr-0"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'EmployeesPerCountryFormElement',
  components: {
    UploadFormHeader,
    NumberFormField,
    SingleSelectFormElement,
  },
  data() {
    return {
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
    };
  },
  props: BaseFormFieldProps,
});
</script>
