<template>
  <UploadFormSubcategoryHeader
    label="Substantial Contribution"
    description="Grade of the substantial contribution criterias fulfillment"
  />
  <div v-for="field in objectives" :key="field.name">
    <UploadFormHeader :label="'Substantial Contribution to '+field.name" description="" :is-required="false" />
    <FormKit
      type="text"
      :name="'substantialContributionTo' + field.key"
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
    <YesNoFormField :name="'dnshTo' + field.key" description="" :label="'Do no significant harm (dnsh) to '+field.name" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import UploadFormSubcategoryHeader from "@/components/forms/parts/elements/basic/UploadFormSubcategoryHeader.vue";

export default defineComponent({
  name: "ObjectivesFormField",
  components: { UploadFormHeader, YesNoFormField, UploadFormSubcategoryHeader },
  data() {
    return {
      objectives: [
        {
          name: "Climate mitigation mitigation",
          key: "ClimateChangeMitigation",
        },
        {
          name: "Climate adaptation adaptation",
          key: "ClimateChangeAdaption",
        },
        {
          name: "Sustainable use and protection of water and marine resources",
          key: "SustainableUseAndProtectionOfWaterAndMarineResources",
        },
        {
          name: "Transition to a circular economy",
          key: "TransitionToACircularEconomy",
        },
        {
          name: "Pollution prevention and control",
          key: "PollutionPreventionAndControl",
        },
        {
          name: "Protection and restoration of biodiversity and ecosystems",
          key: "ProtectionAndRestorationOfBiodiversityAndEcosystems",
        },
      ],
    };
  },
});
</script>
