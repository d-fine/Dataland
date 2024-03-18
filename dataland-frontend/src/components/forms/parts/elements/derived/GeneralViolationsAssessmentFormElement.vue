<template>
  <SingleSelectFormField
    name="nameOfGeneralViolation"
    label="Human Rights or Environmental Violations Definition"
    description="Please select the risk position of the violation."
    :options="riskOptions"
    placeholder="Select Risk"
  />
  <YesNoFormField
    name="nameCounteractingMeasures"
    label="Counteracting Measures"
    description="Have measures been taken to address this violation?"
  />
  <FreeTextFormField
    name="nameWhichCounteractingMeasures"
    label="Which Counteracting Measures"
    description="Which measures have been applied to counteract this violation?"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";

export default defineComponent({
  name: "GeneralViolationsAssessmentFormElement",
  data() {
    return {
      riskOptions: getDataset(DropdownDatasetIdentifier.RiskPositions),
    };
  },
  components: {
    SingleSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
});
</script>
