<template>
  <FormKit type="group" :name="name" :ignore="!isItActive">
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
      <div v-if="isItActive">
        <div>
          <div class="form-field border-none">
            <MultiSelectFormField
              label="Definition Product Type/Service"
              placeholder="Select"
              description="..."
              name="definitionProductTypeService"
              :options="['1', '2', '3']"
              innerClass="long"
            />
          </div>

          <div class="form-field border-none">
            <UploadFormHeader label="Order Volume per Procurement %" description="..." :is-required="false" />
            <FormKit
              type="text"
              name="orderVolume"
              :validation-label="validationLabel ?? label"
              validation="number"
              inner-class="long"
            />
          </div>
          <div class="form-field border-none">
            <MultiSelectFormField
              label="Select the countries"
              placeholder="Countries"
              description="..."
              name="suppliersPerCountryCode"
              :options="allCountry"
              optionLabel="label"
              optionValue="value"
              v-model="selectedCountrys"
              innerClass="long"
              @selectedValuesChanged="handleSelectedCountriesChanged"
            />
          </div>
          <div class="form-field border-none">
            <div class="flex justify-content-between">
              <UploadFormHeader label="Number of Direct Supliers" description="..." :is-required="false" />
            </div>
            <FormKit type="list" name="suppliersPerCountry" label="Suppliers Per Country" v-model="existingSuppliers">
              <FormKit type="group" v-for="el in selectedCountrys" :key="el.label">
                <div class="justify-content-between flex align-items-center">
                  <h5>{{ removeWordFromPhrase(`(${el.value})`, el.label) }}</h5>
                  <FormKit type="hidden" name="country" :modelValue="el.value" data-test="country" />
                  <div class="justify-content-end flex align-items-center">
                    <FormKit type="number" name="numberOfSuppliers" min="0" step="1" outer-class="my-0 mx-3" />
                    <PrimeButton icon="pi pi-times" rounded class="p-button-icon" />
                    <!--                  <em-->
                    <!--                      data-test="removeItemFromListOfSuppliersIds"-->
                    <!--                      @click="removeItemFromListOfSuppliers(id)"-->
                    <!--                      class="material-icons"-->
                    <!--                  >close</em>-->
                  </div>
                </div>
              </FormKit>
            </FormKit>
          </div>
        </div>
      </div>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import InputSwitch from "primevue/inputswitch";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import { DropdownDatasetIdentifier, getDataset } from "@/utils/PremadeDropdownDatasets";
import PrimeButton from "primevue/button";

export default defineComponent({
  name: "ProductCategoriesFormElement",
  components: {
    FormKit,
    UploadFormHeader,
    InputSwitch,
    MultiSelectFormField,
    PrimeButton,
  },
  data() {
    return {
      existingSuppliers: [] as Array<object>,

      isItActive: false,
      allCountry: getDataset(DropdownDatasetIdentifier.CountryCodes),
      selectedCountrys: [],
    };
  },
  props: FormFieldProps,
  methods: {
    /**
     * handle changes in selected Countrys
     * @param newVal - selected countrys new Value
     */
    handleSelectedCountriesChanged(newVal: []) {
      this.selectedCountrys = newVal;
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
