<template>
  <div>
    <div class="form-field border-none">
      <NaceCodeFormField
        label="Definition Product Type/Service"
        description="Define the procured product types/services per category (own operations)"
        name="definitionProductTypeService"
      ></NaceCodeFormField>
    </div>

    <div class="form-field border-none">
      <UploadFormHeader
        label="Order Volume per Procurement %"
        description="State your order volume per procurement category in the last fiscal year (percentage of total volume) (own operations)"
        :is-required="false"
      />
      <FormKit type="text" name="orderVolume" validation="number" inner-class="long" />
    </div>
    <div class="form-field border-none">
      <MultiSelectFormField
        label="Sourcing Country per Category"
        placeholder="Countries"
        description="Name the sourcing countries per category (own operations)"
        name="suppliersPerCountryCode"
        :options="allCountry"
        optionLabel="label"
        v-model="selectedCountries"
        innerClass="long"
        :ignore="true"
        @selectedValuesChanged="handleSelectedCountriesChanged"
      />
    </div>
    <div class="form-field border-none">
      <div class="flex justify-content-between">
        <UploadFormHeader
          v-if="selectedCountries.length > 0"
          label="Number of Direct Supliers"
          description="State the number of direct suppliers per procurement category and country (own operations)"
          :is-required="false"
        />
      </div>
      <FormKit
        type="group"
        name="numberOfSuppliersPerCountryCode"
        label="Suppliers Per Country"
        v-model="existingSuppliers"
      >
        <div v-for="el in selectedCountries" :key="el.label">
          <div class="justify-content-between flex align-items-center">
            <h5>{{ removeWordFromPhrase(`(${el.value})`, el.label) }}</h5>
            <!--            <FormKit type="text" :name="el.value" :modelValue="el.value" data-test="country" />-->
            <div class="justify-content-end flex align-items-center">
              <FormKit
                type="number"
                :name="el.value"
                min="0"
                step="1"
                validation="required"
                validation-label="Number of suppliers per country"
                outer-class="my-0 mx-3"
              />
              <PrimeButton
                icon="pi pi-times"
                rounded
                class="p-button-icon"
                @click="removeItemFromListOfSelectedCountries(el.value)"
              />
            </div>
          </div>
        </div>
      </FormKit>
    </div>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "ProcurementCategoryFormElementFields",
  components: {
    FormKit,
    UploadFormHeader,
    MultiSelectFormField,
    PrimeButton,
    NaceCodeFormField,
  },
  data() {
    return {
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodes),
      selectedCountries: [],
    };
  },
  methods: {
    /**
     * handle changes in selected Countries
     * @param newVal - selected Countries new Value
     */
    handleSelectedCountriesChanged(newVal: []) {
      this.selectedCountries = newVal;
    },

    /**
     * Remove item from selected countries list
     * @param countryCode - Country code to be removed
     */
    removeItemFromListOfSelectedCountries(countryCode: string) {
      this.selectedCountries = this.selectedCountries.filter((el) => countryCode !== el.value);
    },

    /**
     * remove the word from the phrase
     * @param word - word to remove
     * @param phrase - phrase from which we remove
     * @returns phrase without the removed word
     */
    removeWordFromPhrase(word: string, phrase: string): string {
      const splittedPhrase: [string] = phrase.split(" ");
      for (let i = splittedPhrase.length - 1; i >= 0; i--) {
        if (splittedPhrase[i].includes(word)) {
          splittedPhrase.splice(i, 1);
          break;
        }
      }
      return splittedPhrase.join(" ");
    },
  },
});
</script>
