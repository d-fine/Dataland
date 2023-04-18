<template>
  <YesNoComponent
    :displayName="displayName"
    :info="info"
    :name="'fiscalYearDeviation'"
    :radioButtonsOptions="['Deviation', 'No Deviation']"
    required="required"
  />
</template>

<script lang="ts">
import { defineComponent } from "vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";

export default defineComponent({
  name: "FiscalYearDeviatingComponent",
  components: { YesNoComponent },
  props: {
    info: {
      type: String,
      default: "",
    },
    displayName: {
      type: String,
      default: "",
    },
  },
});
</script>
