<template>
  <MultiSelectFormField
    name="riskPositions"
    label="Identified Risk"
    description="Which risks were specifically identified in the risk analysis?"
    :options="riskOptions"
    validation="required"
  />
  <FreeTextFormField
    name="specifiedComplaint"
    label="Specified Complaint"
    description="Please specify the complaint"
    validation="required"
  />
  <YesNoFormField
    name="measuresTaken"
    label="Addressing Measures"
    @update-yes-no-value="handleValueUpdate"
    description="Were measures taken to address the complaints?"
    validation="required"
  />
  <FreeTextFormField
    name="listedMeasures"
    label="Adressing Measures Details"
    v-if="addressingMeasures"
    description="Which measures have been taken to address the reported complaint?"
  />
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import { convertYesNoUndefinedToBoolean } from '@/utils/YesNoNa';

export default defineComponent({
  name: 'GrievanceMechanismAssessmentFormElement',
  data() {
    return {
      riskOptions: getDataset(DropdownDatasetIdentifier.RiskPositions),
      addressingMeasures: undefined as boolean | undefined,
    };
  },
  components: {
    MultiSelectFormField,
    YesNoFormField,
    FreeTextFormField,
  },
  methods: {
    /**
     * Handles the update of the YesNoValue
     * @param yesNoValue the updated value
     */
    handleValueUpdate(yesNoValue: string | undefined) {
      this.addressingMeasures = convertYesNoUndefinedToBoolean(yesNoValue);
    },
  },
});
</script>
