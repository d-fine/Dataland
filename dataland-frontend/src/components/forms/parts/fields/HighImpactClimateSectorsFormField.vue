<template>
  <div class="form-field">
    <!-- TODO card around this -->
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <div :class="innerClass">
      <MultiSelect
        v-model="selections"
        :options="options"
        :placeholder="label"
        option-label="label"
        option-value="value"
        :show-toggle-all="false"
      />
      <div v-for="selection of selections" :key="selection">
        <h4>{{ options.find((option) => option.value == selection).label }}</h4>
        <UploadFormHeader
            :label="`Sector ${selection} Energy Consumption`"
            :description="`Total energy consumption for high impact climate sector ${selection}`"
            :is-required="true"
        />
        <div class="next-to-each-other">
          <FormKit
            type="text"
            :name="`sector${selection}EnergyConsumption`"
            :placeholder="`Sector ${selection} Energy Consumption`"
            validation-label="Energy Consumption"
            validation="number"
          />
          <span>in GWh</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import MultiSelect from "primevue/multiselect";
import { naceCodeTree } from "@/components/forms/parts/elements/derived/NaceCodeTree";

export default defineComponent({
  name: "HighImpactClimateSectorsFormField",
  components: { NumberFormField, MultiSelect, UploadFormHeader },
  props: BaseFormFieldProps,
  data() {
    return {
      options: naceCodeTree
        .filter((sector) => ["A", "B", "C", "D", "E", "F", "G", "H", "L"].includes(sector.key))
        .map((sector) => ({ label: sector.label, value: sector.key })),
      selections: [] as string[],
    };
  },
});
</script>

<style scoped></style>
