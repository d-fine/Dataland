<template>
  <div class="form-field">
    <MultiSelectFormFieldBindData
      :label="label"
      placeholder="Countries"
      :description="description"
      :options="allCountries"
      optionLabel="label"
      v-model:selectedItemsBindInternal="selectedCountries"
      inputClass="long"
    />
    <FormKit type="group" :name="name" v-model="industriesPerCountry">
      <div v-for="(element, index) in selectedCountries" :key="element.label">
        <div v-if="index > 0" class="h-2rem" />
        <div class="w-12 flex justify-content-start align-items-baseline" data-test="subcontractingCountriesIndustries">
          <PrimeButton
            icon="pi pi-times"
            rounded
            class="p-button-icon"
            data-test="removeElementBtn"
            @click="removeItemFromListOfSelectedCountries(element.value)"
          />
          <NaceCodeFormField
            class="border-none w-full"
            :label="`Subcontracting Companies Industries in ${getCountryNameFromCountryCode(element.value)}`"
            :description="`In which industries do the subcontracting companies in ${getCountryNameFromCountryCode(element.value)} operate?`"
            :name="element.value"
            :shouldDisableCheckboxes="true"
          />
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
// @ts-nocheck
import { defineComponent } from 'vue';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import { FormKit } from '@formkit/vue';
import MultiSelectFormFieldBindData from '@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import PrimeButton from 'primevue/button';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';

export default defineComponent({
  name: 'LksgSubcontractingCompaniesFormField',
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
          this.allCountries.find((country) => country.value == countryCode)
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
      this.selectedCountries = this.selectedCountries.filter((element) => countryCode !== element.value);
    },
  },
});
</script>
