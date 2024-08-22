<template>
  <FreeTextFormField
    name="pollutionType"
    label="Pollution Type"
    description="Please describe the pollution type"
    validation="required"
  />
  <NumberFormField
    :name="'emissionInKilograms'"
    validation-label="emission in kilograms"
    label="Mass of pollutant in kg"
    description="Please specify the mass of the pollutant in kg"
    validation="min:0"
    input-class="col-4 pr-0"
  />
  <SingleSelectFormField
    name="releaseMedium"
    label="Release Medium"
    description="Please specify in which medium the pollutant was released"
    :options="releaseMedium"
    validation="required"
    data-test="relaseMedium"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'PollutionEmissionFormElement',
  data() {
    return {
      releaseMedium: getDataset(DropdownDatasetIdentifier.ReleaseMedium),
    };
  },
  props: BaseFormFieldProps,
  components: {
    NumberFormField,
    SingleSelectFormField,
    FreeTextFormField,
  },
  methods: {},
});
</script>
