<template>
  <div class="form-field">
    <div data-test="dataPointToggle" class="form-field border-none vertical-middle">
      <InputSwitch
        data-test="dataPointToggleButton"
        inputId="dataPointIsAvailableSwitch"
        @click="this.isItActive = false"
        v-model="isItActive"
      />
      <h5 data-test="dataPointToggleTitle" class="m-2">
        {{ label }}
      </h5>
    </div>
    <FormKit type="group" :name="name" v-if="isItActive">
      <div data-test="ProcurementCategoryFormElementContent">
        <div class="form-field border-none">
          <NaceCodeFormFieldBindData
            label="Definition Product Type/Service"
            description="Define the procured product types/services per category (own operations)"
            name="procuredProductTypesAndServicesNaceCodes"
            v-model:selectedNaceCodesBind="procuredProductTypesAndServicesNaceCodesValue"
          ></NaceCodeFormFieldBindData>
        </div>

        <div class="form-field border-none">
          <UploadFormHeader
            label="Order Volume per Procurement %"
            description="State your order volume per procurement category in the last fiscal year (percentage of total volume) (own operations)"
            :is-required="false"
          />
          <FormKit
            type="text"
            v-model="percentageOfTotalProcurementValue"
            name="percentageOfTotalProcurement"
            validation="number"
            inner-class="long"
          />
        </div>
        <div class="form-field border-none">
          <MultiSelectFormFieldBindData
            label="Sourcing Country per Category"
            placeholder="Countries"
            description="Name the sourcing countries per category (own operations)"
            name="suppliersPerCountryCode"
            :options="allCountry"
            optionLabel="label"
            :optionValue="false"
            v-model:selectedItemsBindInternal="selectedCountries"
            innerClass="long"
            :ignore="true"
          />
        </div>
        <div class="form-field border-none">
          <div class="flex justify-content-between">
            <UploadFormHeader
              v-if="selectedCountries.length > 0"
              :label="lksgDataModelTranslations.procurementCategories.directSuppliers"
              description="State the number of direct suppliers per procurement category and country (own operations)"
              :is-required="false"
              data-test="directSuppliersHeader"
            />
          </div>
          <FormKit
            type="group"
            name="numberOfSuppliersPerCountryCode"
            v-model="numberOfSuppliersPerCountryCodeValue"
            label="Suppliers Per Country"
          >
            <div v-for="el in selectedCountries" :key="el.label">
              <div class="justify-content-between flex align-items-center" data-test="supplierCountry">
                <h5>{{ removeWordFromPhrase(el.value, el.label) }}</h5>
                <div class="justify-content-end flex align-items-center">
                  <FormKit
                    type="number"
                    :name="el.value"
                    min="0"
                    step="1"
                    validation="required"
                    validation-label="Number of suppliers per country"
                    data-test="supplierCountryValue"
                    outer-class="my-0 mx-3"
                  />
                  <PrimeButton
                    icon="pi pi-times"
                    rounded
                    class="p-button-icon"
                    data-test="removeElementBtn"
                    @click="removeItemFromListOfSelectedCountries(el.value)"
                  />
                </div>
              </div>
            </div>
          </FormKit>
        </div>
      </div>
    </FormKit>
  </div>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import InputSwitch from "primevue/inputswitch";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
import PrimeButton from "primevue/button";
import NaceCodeFormFieldBindData from "@/components/forms/parts/fields/NaceCodeFormFieldBindData.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { LksgProcurementCategory } from "@clients/backend";
import { detailsCompanyDataTableColumnHeaders } from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";

export default defineComponent({
  name: "ProcurementCategoryFormElement",
  inject: {
    procurementCategories: {
      from: "procurementCategories",
      default: {} as { [key: string]: LksgProcurementCategory },
    },
  },
  components: {
    InputSwitch,
    FormKit,
    UploadFormHeader,
    MultiSelectFormFieldBindData,
    PrimeButton,
    NaceCodeFormFieldBindData,
  },
  props: BaseFormFieldProps,
  data() {
    return {
      isItActive: !!this.procurementCategories[this.name],
      procuredProductTypesAndServicesNaceCodesValue: [],
      percentageOfTotalProcurementValue: "",
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodes),
      selectedCountries: [],
      numberOfSuppliersPerCountryCodeValue: [],
      lksgDataModelTranslations: detailsCompanyDataTableColumnHeaders,
    };
  },
  watch: {
    preSelectedCountries(newValue: []) {
      this.selectedCountries = newValue;
    },
  },
  computed: {
    preSelectedCountries() {
      return this.allCountry.filter((el) =>
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access,no-prototype-builtins
        this.procurementCategories[this.name]?.numberOfSuppliersPerCountryCode?.hasOwnProperty(el.value)
      );
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
