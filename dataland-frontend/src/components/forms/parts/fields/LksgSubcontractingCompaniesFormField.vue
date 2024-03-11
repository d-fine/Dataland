<template>
  <!--  todo remove border-nones -->
  <div class="form-field border-none">
    <MultiSelectFormFieldBindData
      :label="label"
      placeholder="Countries"
      :description="description"
      :options="allCountries"
      optionLabel="label"
      v-model:selectedItemsBindInternal="selectedCountries"
      inputClass="long"
    />
  </div>
  <div class="form-field">
    <FormKit type="group" :name="name" v-model="industriesPerCountry">
      <div v-for="(el, index) in selectedCountries" :key="el.label">
        <div v-if="index > 0" class="h-2rem" />
        <div class="w-12 flex justify-content-start align-items-baseline" data-test="subcontractingCountriesIndustries">
          <PrimeButton
            icon="pi pi-times"
            rounded
            class="p-button-icon"
            data-test="removeElementBtn"
            @click="removeItemFromListOfSelectedCountries(el.value)"
          />
          <NaceCodeFormField
            class="border-none w-full"
            :label="`Subcontracting Companies Industries in ${getCountryNameFromCountryCode(el.value)}`"
            :description="`In which industries do the subcontracting companies in ${getCountryNameFromCountryCode(el.value)} operate?`"
            :name="el.value"
          />
          <!-- todo which nace codes / enable checkboxes -->
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { FormKit } from "@formkit/vue";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import PrimeButton from "primevue/button";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";

export default defineComponent({
  name: "LksgSubcontractingCompaniesFormField",
  components: {
    FormKit,
    MultiSelectFormFieldBindData,
    PrimeButton,
    NaceCodeFormField,
  },
  props: BaseFormFieldProps,
  data() {
    return {
      industriesPerCountry: undefined as { [p: string]: string[] } | undefined,
      allCountries: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      selectedCountries: [] as { label: string; value: string }[],
      getCountryNameFromCountryCode,
    };
  },
  watch: {
    industriesPerCountry() {
      if (this.industriesPerCountry == undefined) {
        return;
      }
      if (Object.keys(this.industriesPerCountry).length > this.selectedCountries.length) {
        this.selectedCountries = Object.keys(this.industriesPerCountry).map((countryCode) =>
          this.allCountries.find((country) => country.value == countryCode),
        );
      }
    },
  },
  methods: {
    /**
     * Remove item from selected countries list
     * @param countryCode - Country code to be removed
     */
    removeItemFromListOfSelectedCountries(countryCode: string) {
      this.selectedCountries = this.selectedCountries.filter((el) => countryCode !== el.value);
    },
  },
});
</script>
