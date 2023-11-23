<template>
  <div class="form-field">
    <UploadFormHeader :label="label" :description="description" :is-required="required" />
    <div>
      <MultiSelect
        v-model="selections"
        :options="options"
        :placeholder="label"
        option-label="label"
        option-value="value"
        :show-toggle-all="false"
        :class="inputClass"
      />
      <div class="grid mt-3">
        <FormKit type="group" name="applicableHighImpactClimateSectors">
          <div
            v-for="selection of selections"
            data-test="applicableHighImpactClimateSector"
            :key="selection"
            class="bordered-box p-3 positionRelative col-12"
          >
            <em @click="removeItem(selection)" class="material-icons gray-closeIcon">close</em>
            <h4 class="gray-text fw-normal">{{ options.find((option) => option.value == selection).label }}</h4>
            <div class="next-to-each-other">
              <div class="col-6">
                <UploadFormHeader
                  :label="`Sector ${selection} Energy Consumption`"
                  :description="`Total energy consumption for high impact climate sector ${selection}`"
                  :is-required="false"
                />
                <BigDecimalExtendedDataPointFormField
                  unit="GWh"
                  :name="`NaceCode${selection}InGWh`"
                  :placeholder="`Sector ${selection} Energy Consumption`"
                  validation-label="Energy Consumption"
                />
              </div>
              <div class="col-6" style="border-left: 1px solid #c3c3c3">
                <UploadFormHeader
                  :label="`Sector ${selection} Relative Energy Consumption`"
                  :description="`Energy consumption for high impact climate sector ${selection} per revenue`"
                  :is-required="false"
                />
                <BigDecimalExtendedDataPointFormField
                  unit="GWh / €M revenue"
                  :name="`NaceCode${selection}InGWhPerMillionEURRevenue`"
                  :placeholder="`Sector ${selection} Relative Energy Consumption`"
                  validation-label="Relative Energy Consumption"
                />
              </div>
            </div>
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
import { optionsForHighImpactClimateSectors } from "@/types/HighImpactClimateSectors";
import BigDecimalExtendedDataPointFormField from "@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue";

export default defineComponent({
  name: "HighImpactClimateSectorsFormField",
  inject: {
    injectClimateSectors: {
      from: "climateSectorsForPrefill",
      default: [] as string[],
    },
  },
  components: { BigDecimalExtendedDataPointFormField, MultiSelect, UploadFormHeader },
  props: BaseFormFieldProps,
  data() {
    return {
      options: optionsForHighImpactClimateSectors,
      selections: [],
    };
  },
  mounted() {
    this.selections = this.injectClimateSectors;
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
