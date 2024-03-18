<template>
  <SingleSelectFormField
    name="nameOfGeneralViolation"
    label="Identified Risk for Violation"
    description="Which risk positions of each violation were specifically identified?"
    :options="riskOptions"
  />
  <!-- todo check title -->
  <YesNoFormField
    name="nameCounteractingMeasures"
    label="Counteracting Measures"
    description="Have measures been taken to address these violations?"
  />
  <FreeTextFormField
    name="nameWhichCounteractingMeasures"
    label="Which Counteracting Measures"
    description="Please define the measures"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import { RiskPositionType } from "@clients/backend";

export default defineComponent({
  name: "GeneralViolationsAssessmentFormElement",
  data() {
    return {
      riskOptions: RiskPositionType,
    };
  },
  components: {
    SingleSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
});
</script>
