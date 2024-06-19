<template>
  <SingleSelectFormField
    name="wasteClassification"
    label="Waste Classification"
    description="Which waste classification applies here?"
    :options="wasteClassifications"
    placeholder="Select Waste Classification"
    validation="required"
  />
  <InputTextFormField
    name="typeOfWaste"
    description="Please state type of waste."
    label="Type Of waste"
    validation="required"
  />
  <NumberFormField
    :name="'totalAmountInTons'"
    :validation-label="validationLabel"
    label="Total amount of waste in tons"
    description="Please specify the total amount of waste in tons"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteRecycleOrReuseInTons'"
    :validation-label="validationLabel"
    label="Recycled or reused waste in tons"
    description="Please specify the recycled or reused waste in tons"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'wasteDisposalInTons'"
    :validation-label="validationLabel"
    label="Waste disposal in tons"
    description="Please specify the waste disposal in tons"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <NumberFormField
    :name="'totalAmountInCubicMeters'"
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
import { defineComponent } from "vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "WasteClassificationFormElement",
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
