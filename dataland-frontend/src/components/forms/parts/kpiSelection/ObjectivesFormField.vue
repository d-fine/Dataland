<template>
  <p class="p-0 mx-0 mt-0 mb-3">Substantial Contribution</p>
  <FormKit type="group" name="substantialContributionCriteria">
    <div v-for="field in objectives" :key="field.long_name">
      <UploadFormHeader :label="field.long_name" description="" :is-required="false" />
      <FormKit
        type="text"
        :name="field.key"
        :validation-label="field.long_name"
        validation="number|between:0,100"
        placeholder="Relative Value in %"
        outer-class="short"
      />
    </div>
  </FormKit>
  <p class="header-separator">DNSH</p>
  <FormKit type="group" name="dnshCriteria">
    <div v-for="field in objectives" :key="field.long_name">
      <YesNoFormField :name="field.key" description="" :label="field.name" />
    </div>
  </FormKit>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

export default defineComponent({
  name: "ObjectivesFormField",
  components: { UploadFormHeader, YesNoFormField },
  data() {
    return {
      objectives: [
        {
          name: "Climate mitigation",
          long_name: "Climate change mitigation",
          key: "climateMitigation",
        },
        {
          name: "Climate adaptation",
          long_name: "Climate change adaptation",
          key: "climateAdaptation",
        },
        {
          name: "Water",
          long_name: "Sustainable use and protection of water and marine resources",
          key: "water",
        },
        {
          name: "Circular economy",
          long_name: "Transition to a circular economy",
          key: "circularEconomy",
        },
        {
          name: "Pollution prevention",
          long_name: "Pollution prevention and control",
          key: "pollutionPrevention",
        },
        {
          name: "Biodiversity",
          long_name: "Protection and restoration of biodiversity and ecosystems",
          key: "biodiversity",
        },
      ],
    };
  },
});
</script>
