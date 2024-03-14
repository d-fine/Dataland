<template>
  <SingleSelectFormField
    name="nameOfRiskPosition"
    label="Identified Risk"
    description="Which risks were specifically identified in the risk analysis?"
    :options="riskOptions"
  />
  <YesNoFormField
    name="nameCounteractingMeasures"
    label="Counteracting Measures"
    description="Have measures been defined to counteract the risk?"
  />
  <FreeTextFormField
    name="nameWhichCounteractingMeasures"
    label="Which Counteracting Measures"
    description="Which measures have been applied to counteract the risk?"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import { RiskPositionType } from "@clients/backend";

export default defineComponent({
  name: "RiskAssessmentFormElement",
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
