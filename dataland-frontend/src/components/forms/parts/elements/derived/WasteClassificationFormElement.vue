<template>
  <SingleSelectFormField
    name="wasteClassification"
    label="Waste Classification"
    description="Which waste classification applies here?"
    :options="wasteClassifications"
    placeholder="Select Waste Classification"
    validation="required"
    data-test="wasteClassification"
  />
  <InputTextFormField
    name="typeOfWaste"
    description="Please state type of waste."
    label="Type Of waste"
    validation="required"
  />
  <NumberFormField
    :name="'totalAmountOfWasteInTonnes'"
    :validation-label="validationLabel"
    label="Total amount of waste in tonnes"
    description="Please specify the total amount of waste in tonnes"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteRecycleOrReuseInTonnes'"
    :validation-label="validationLabel"
    label="Recycled or reused waste in tonnes"
    description="Please specify the recycled or reused waste in tonnes"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteDisposalInTonnes'"
    :validation-label="validationLabel"
    label="Waste disposal in tonnes"
    description="Please specify the waste disposal in tonnes"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'totalAmountOfWasteInCubicMeters'"
    :validation-label="validationLabel"
    label="Total amount of waste in m³"
    description="Please specify the total amount of waste in m³"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteRecycleOrReuseInCubicMeters'"
    :validation-label="validationLabel"
    label="Recycled or reused waste in m³"
    description="Please specify the recycled or reused waste in m³"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteDisposalInCubicMeters'"
    :validation-label="validationLabel"
    label="Waste disposal in m³"
    description="Please specify the waste disposal in m³"
    :validation="validation"
    input-class="col-4 pr-0"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';

export default defineComponent({
  name: 'WasteClassificationFormElement',
  data() {
    return {
      wasteClassifications: getDataset(DropdownDatasetIdentifier.WasteClassifications),
    };
  },
  props: BaseFormFieldProps,
  components: {
    InputTextFormField,
    SingleSelectFormField,
    NumberFormField,
  },
  methods: {},
});
</script>
