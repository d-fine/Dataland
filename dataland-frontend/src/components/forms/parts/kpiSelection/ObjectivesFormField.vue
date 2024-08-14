<template>
  <UploadFormSubcategoryHeader
    label="Substantial Contribution"
    description="Grade of the substantial contribution criterias fulfillment"
  />
  <div v-for="field in objectives" :key="field.name">
    <UploadFormHeader
      :label="'Substantial Contribution to ' + field.name"
      :description="'Taxonomy-aligned share substantially contributing to ' + field.description"
    />
    <FormKit
      type="text"
      :name="'substantialContributionTo' + field.key + 'InPercent'"
      :validation-label="field.name"
      validation="number|between:0,100"
      placeholder="Relative Value in %"
      outer-class="short"
    />
  </div>
  <UploadFormSubcategoryHeader
    propClass="header-separator"
    label="DNSH"
    description="Is no significant harm done to the criterion by this activity."
  />
  <div v-for="field in objectives" :key="field.name">
    <YesNoFormField
      :name="'dnshTo' + field.key"
      description=""
      :label="'Do no significant harm (dnsh) to ' + field.name"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import UploadFormSubcategoryHeader from '@/components/forms/parts/elements/basic/UploadFormSubcategoryHeader.vue';

export default defineComponent({
  name: 'ObjectivesFormField',
  components: { UploadFormHeader, YesNoFormField, UploadFormSubcategoryHeader },
  data() {
    return {
      objectives: [
        {
          name: 'Climate change mitigation',
          key: 'ClimateChangeMitigation',
          description: 'climate change mitigation',
        },
        {
          name: 'Climate change adaptation',
          key: 'ClimateChangeAdaptation',
          description: 'climate change adaptation',
        },
        {
          name: 'Sustainable use and protection of water and marine resources',
          key: 'SustainableUseAndProtectionOfWaterAndMarineResources',
          description: 'sustainable use and protection of water and marine resources',
        },
        {
          name: 'Transition to a circular economy',
          key: 'TransitionToACircularEconomy',
          description: 'circular economy',
        },
        {
          name: 'Pollution prevention and control',
          key: 'PollutionPreventionAndControl',
          description: 'pollution prevention and control',
        },
        {
          name: 'Protection and restoration of biodiversity and ecosystems',
          key: 'ProtectionAndRestorationOfBiodiversityAndEcosystems',
          description: 'protection and restoration of biodiversity and ecosystems',
        },
      ],
    };
  },
});
</script>
