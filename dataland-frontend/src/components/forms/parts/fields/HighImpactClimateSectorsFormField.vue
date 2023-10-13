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
        <FormKit type="group" name="applicableHighImpactClimateSector">
          <div v-for="selection of selections" :key="selection" class="bordered-box p-3 positionRelative">
            <em @click="removeItem(selection)" class="material-icons gray-closeIcon">close</em>
            <h4 class="gray-text fw-normal">{{ options.find((option) => option.value == selection).label }}</h4>
            <UploadFormHeader
              :label="`Sector ${selection} Energy Consumption`"
              :description="`Total energy consumption for high impact climate sector ${selection}`"
              :is-required="true"
            />
            <DataPointFormField
              unit="in GWh"
              :name="`NaceCode${selection}InGWh`"
              :placeholder="`Sector ${selection} Energy Consumption`"
              validation-label="Energy Consumption"
            />
          </div>
        </FormKit>
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
import DataPointFormField from "@/components/forms/parts/kpiSelection/DataPointFormField.vue";

export default defineComponent({
  name: "HighImpactClimateSectorsFormField",
  components: { DataPointFormField, MultiSelect, UploadFormHeader },
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
