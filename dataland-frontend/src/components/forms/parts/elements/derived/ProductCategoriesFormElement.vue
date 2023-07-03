<template>
  <FormKit type="group" :name="name" :ignore="!isItActive">
    <div data-test="dataPointToggle" class="form-field vertical-middle">
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
    {{ selectedCountrys }}
    <div v-if="isItActive">
      <div>
        <MultiSelectFormField
          label="Definition Product Type/Service"
          placeholder="Select"
          description="..."
          name="definitionProductTypeService"
          :options="['1', '2', '3']"
          innerClass="long"
        />

        <div class="form-field">
          <UploadFormHeader label="Order Volume per Procurement %" description="..." :is-required="false" />
          <FormKit
            type="text"
            name="orderVolume"
            :validation-label="validationLabel ?? label"
            validation="number"
            inner-class="long"
          />
        </div>

        <MultiSelectFormField
          label="Select the countries"
          placeholder="Countries"
          description="..."
          name="suppliersPerCountry"
          :options="allCountry"
          optionLabel="label"
          optionValue="value"
          v-model="selectedCountrys"
          innerClass="long"
          @selectedValuesChanged="handleSelectedCountriesChanged"
        />

        <div class="form-field">
          <div class="flex justify-content-between">
            <UploadFormHeader label="Suppliers Per Country" description="..." :is-required="false" />
          </div>
          <FormKit type="list" name="suppliersPerCountry" label="Suppliers Per Country" v-model="existingSuppliers">
            <FormKit type="group" v-for="el in selectedCountrys" :key="el.label">
              <div class="next-to-each-other">
                <h5>{{ removeWordFromPhrase(`(${el.value})`, el.label) }}</h5>
                <FormKit type="text" name="country" :modelValue="el.value" data-test="country" />
                <FormKit
                  type="text"
                  name="numberOfSuppliers"
                  validation-label="Number Of Suppliers"
                  validation="number"
                  placeholder="Number Of Suppliers"
                />
                <em
                  data-test="removeItemFromListOfSuppliersIds"
                  @click="removeItemFromListOfSuppliers(id)"
                  class="material-icons mt-2 link"
                  >close</em
                >
              </div>
            </FormKit>
          </FormKit>
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

export default defineComponent({
  name: "ProductCategoriesFormElement",
  components: {
    FormKit,
    UploadFormHeader,
    InputSwitch,
    MultiSelectFormField,
  },
  data() {
    return {
      existingSuppliers: [] as Array<object>,
      listOfSuppliersIds: [] as number[],

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
     * Remove Object from Product array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfSuppliers(id: number) {
      this.listOfSuppliersIds = this.listOfSuppliersIds.filter((el) => el !== id);
    },

    /**
     * remove the word from the phrase
     * @param word - word to remove
     * @param phrase - phrase from which we remove
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
