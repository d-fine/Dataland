<template>
  <div class="form-field">
    <div data-test="dataPointToggle" class="form-field border-none vertical-middle">
      <InputSwitch data-test="dataPointToggleButton" inputId="dataPointIsAvailableSwitch" v-model="isActive" />
      <h5 data-test="dataPointToggleTitle" class="m-2">
        {{ label }}
      </h5>
    </div>
    <FormKit type="group" :name="name" v-if="isActive">
      <div data-test="ProcurementCategoryFormElementContent">
        <div class="form-field border-none">
          <NaceCodeFormField
            label="Procured Products/Services"
            description="Define the procured product types/services per category (own operations)"
            name="procuredProductTypesAndServicesNaceCodes"
            v-model:selectedNaceCodesBind="procuredProductTypesAndServicesNaceCodesValue"
            :shouldDisableCheckboxes="true"
          />
        </div>

        <div class="form-field border-none">
          <PercentageFormField
            label="Order Volume"
            description="State your order volume per procurement category in the last fiscal year (percentage of total volume) (own operations)"
            :is-required="false"
            v-model:percentageFieldValueBind="shareOfTotalProcurementInPercent"
            name="shareOfTotalProcurementInPercent"
            validation="between:0,100"
          />
        </div>
        <div class="form-field border-none">
          <MultiSelectFormFieldBindData
            label="Sourcing Countries"
            placeholder="Countries"
            description="Name the sourcing countries per procurement category (own operations)"
            name="suppliersPerCountryCode"
            :options="allCountries"
            optionLabel="label"
            v-model:selectedItemsBindInternal="selectedCountries"
            inputClass="long"
          />
        </div>
        <div class="form-field border-none">
          <div class="flex justify-content-between">
            <UploadFormHeader
              v-if="selectedCountries.length > 0"
              label="Number of Direct Suppliers"
              description="State the number of direct suppliers per procurement category and country (own operations)"
              :is-required="false"
              data-test="directSuppliersHeader"
            />
          </div>
          <FormKit type="group" name="numberOfSuppliersPerCountryCode" label="Suppliers Per Country">
            <div v-for="el in selectedCountries" :key="el.label">
              <div class="justify-content-between flex align-items-center" data-test="supplierCountry">
                <h5>{{ getCountryNameFromCountryCode(el.value) }}</h5>
                <div class="justify-content-end flex align-items-center">
                  <FormKit
                    type="number"
                    :name="el.value"
                    min="1"
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
// @ts-nocheck
import { defineComponent } from 'vue';
import InputSwitch from 'primevue/inputswitch';
import { BaseFormFieldProps } from '@/components/forms/parts/fields/FormFieldProps';
import { FormKit } from '@formkit/vue';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import MultiSelectFormFieldBindData from '@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import PrimeButton from 'primevue/button';
import { DropdownDatasetIdentifier, getDataset } from '@/utils/PremadeDropdownDatasets';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter';
import { type LksgProcurementCategory } from '@clients/backend';

export default defineComponent({
  name: 'ProcurementCategoryFormElement',
  inject: {
    selectedProcurementCategories: {
      from: 'selectedProcurementCategories',
      default: {} as { [key: string]: LksgProcurementCategory },
    },
  },
  components: {
    InputSwitch,
    FormKit,
    UploadFormHeader,
    MultiSelectFormFieldBindData,
    PrimeButton,
    NaceCodeFormField,
    PercentageFormField,
  },
  props: BaseFormFieldProps,
  data() {
    return {
      isActive: !!this.selectedProcurementCategories?.[this.name],
      procuredProductTypesAndServicesNaceCodesValue: [],
      shareOfTotalProcurementInPercent: '',
      allCountries: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      selectedCountries: [] as { label: string; value: string }[],
      getCountryNameFromCountryCode,
    };
  },
  mounted() {
    if (this.isActive) {
      this.selectedCountries = this.setPreSelectedCountries();
    }
  },
  methods: {
    /**
     * Sets the selectedCountries according to the value of which countries have been selected
     * @returns Pre Selected Countries
     */
    setPreSelectedCountries() {
      return this.allCountries.filter((element) =>
        // eslint-disable-next-line @typescript-eslint/no-unsafe-argument,@typescript-eslint/no-unsafe-call,@typescript-eslint/no-unsafe-member-access,no-prototype-builtins,
        Object.keys(this.selectedProcurementCategories?.[this.name]?.numberOfSuppliersPerCountryCode)?.includes(
          element.value
        )
      );
    },
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
