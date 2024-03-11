<template>
  <!--  todo remove border-nones -->
  <div class="form-field border-none">
    <MultiSelectFormFieldBindData
      :label="label"
      placeholder="Countries"
      :description="description"
      :name="name"
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
        label="Subcontracting Companies Industries per Country"
        description="In which industries do the subcontracting companies in a country operate?"
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
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { BaseFormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import MultiSelectFormFieldBindData from "@/components/forms/parts/fields/MultiSelectFormFieldBindData.vue";
// import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import PrimeButton from "primevue/button";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import { getCountryNameFromCountryCode } from "@/utils/CountryCodeConverter";

export default defineComponent({
  name: "LksgSubcontractingCompaniesFormField",
  components: {
    FormKit,
    UploadFormHeader,
    MultiSelectFormFieldBindData,
    PrimeButton,
    // NaceCodeFormField, todo
  },
  props: BaseFormFieldProps,
  data() {
    return {
      allCountries: getDataset(DropdownDatasetIdentifier.CountryCodesIso2),
      selectedCountries: [] as { label: string; value: string }[],
      getCountryNameFromCountryCode,
    };
  },
  mounted() {
    this.selectedCountries = this.setPreSelectedCountries();
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
          element.value,
        ),
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
