<template>
  <div class="form-field">
    <!-- TODO card around this -->
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <div>
      <MultiSelect
        v-model="selections"
        :options="options"
        :placeholder="label"
        option-label="label"
        option-value="value"
        :show-toggle-all="false"
        :class="innerClass"
      />
      <div class="grid2 mt-3">
        <div v-for="selection of selections" :key="selection" class="bordered-box p-3 positionRelative">
          <em @click="removeItem(selection)" class="material-icons gray-closeIcon">close</em>
          <h4 class="gray-text fw-normal">{{ options.find((option) => option.value == selection).label }}</h4>
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
              validation="number|required"
            />
            <span class="middle-next-to-field">in GWh</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import MultiSelect from "primevue/multiselect";
import { naceCodeTree } from "@/components/forms/parts/elements/derived/NaceCodeTree";

export default defineComponent({
  name: "HighImpactClimateSectorsFormField",
  components: { MultiSelect, UploadFormHeader },
  props: BaseFormFieldProps,
  data() {
    return {
      options: naceCodeTree
        .filter((sector) => ["A", "B", "C", "D", "E", "F", "G", "H", "L"].includes(sector.key))
        .map((sector) => ({ label: sector.label, value: sector.key })),
      selections: [] as string[],
    };
  },
  methods: {
    /**
     * Unselected item from selections
     * @param selection item to unselect
     */
    removeItem(selection: string) {
      this.selections = this.selections.filter((el: string) => el !== selection);
    },
  },
});
</script>

<style scoped></style>
