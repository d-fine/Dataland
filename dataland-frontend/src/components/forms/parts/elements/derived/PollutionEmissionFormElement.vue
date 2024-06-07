<template>
  <FreeTextFormField
    name="pollutionType"
    label="Pollution Type"
    description="Please describe the pollution type"
    validation="required"
  />
  <NumberFormField
    :name="'emissionInKilograms'"
    :validation-label="validationLabel"
    label="Mass of pollutant in kg"
    description="Please specify the mass of the pollutant in kg"
    :validation="validation"
    input-class="col-4 pr-0"
  />
  <MultiSelectFormField
    name="releaseMedium"
    label="Release Medium"
    description="Please specify in which medium the pollutant was released"
    :options="riskOptions"
    validation="required"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { convertYesNoUndefinedToBoolean } from "@/utils/YesNoNa";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";

export default defineComponent({
  name: "GrievanceMechanismAssessmentFormElement",
  data() {
    return {
      riskOptions: getDataset(DropdownDatasetIdentifier.ReleaseMedium),
    };
  },
  components: {
    NumberFormField,
    MultiSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
  methods: {},
});
</script>
