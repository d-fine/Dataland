<template>
  <MultiSelectFormField
    name="nameOfRiskPosition"
    label="Identified Risk"
    description="Which risks were specifically identified in the risk analysis?"
    :options="riskOptions"
  />
  <FreeTextFormField
    name="nameOfSpecifiedComplaint"
    label="Specified Complaint"
    description="Please specify the complaint"
  />
  <YesNoFormField
    name="AddressingMeasures"
    label="Addressing Measures"
    description="Were measures taken to address the complaints?"
  />
  <FreeTextFormField
    name="nameWhichAdressingMeasures"
    label="Adressing Measures Details"
    description="Which measures have been taken to address the reported complaint?"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import { RiskPositionType } from "@clients/backend";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";

export default defineComponent({
  name: "GrievanceMechanismAssessmentFormElement",
  data() {
    return {
      riskOptions: RiskPositionType,
    };
  },
  components: {
    MultiSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
});
</script>
