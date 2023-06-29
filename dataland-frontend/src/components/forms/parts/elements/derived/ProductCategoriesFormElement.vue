<template>
  <FormKit type="group" :name="name">
    <div class="flex align-items-center mt-3">
      <Checkbox inputId="productCategorieCheck" :binary="true" v-model="isItActive" />
      <label for="productCategorieCheck" class="ml-2"> {{ label }} </label>
    </div>

    <div v-if="isItActive" class="productSection">
      <em data-test="removeItemFromListOfProducts" @click="this.isItActive = false" class="material-icons close-section"
        >close</em
      >
      {{ existingRelatedCorporateSupplyChain }}
      <keep-alive>
        <div>
          <div class="form-field">
            <FreeTextFormField
              name="relatedCorporateSupplyChain"
              label="Definition Product Type Service"
              description="..."
              v-model="existingRelatedCorporateSupplyChain"
            />
          </div>

          <div class="form-field">
            <div class="flex justify-content-between">
              <UploadFormHeader label="Suppliers Per Country" description="..." :is-required="false" />

              <PrimeButton
                :disabled="false"
                label="Add"
                class="p-button-text"
                icon="pi pi-plus"
                @click="addNewSuppliers"
              ></PrimeButton>
            </div>
            <FormKit type="list" name="suppliersPerCountry" label="Suppliers Per Country" v-model="existingSuppliers">
              <FormKit type="group" v-for="id in listOfSuppliersIds" :key="id">
                <div class="next-to-each-other">
                  <FormKit
                    type="text"
                    name="country"
                    validation-label="Country"
                    validation="required"
                    placeholder="Country"
                    data-test="country"
                  />
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

          <div class="form-field">
            <UploadFormHeader label="Order Volume" description="..." :is-required="false" />
            <FormKit
              type="text"
              name="orderVolume"
              validation-label="validationLabel ?? label"
              validation="number"
              placeholder="placeholder"
              inner-class="long"
            />
          </div>
        </div>
      </keep-alive>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { defineComponent } from "vue";
import PrimeButton from "primevue/button";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";
import Checkbox from "primevue/checkbox";

export default defineComponent({
  name: "ProductFormElement",
  components: {
    FormKit,
    PrimeButton,
    Checkbox,
    FreeTextFormField,
    UploadFormHeader,
  },
  data() {
    return {
      existingRelatedCorporateSupplyChain: "",
      existingSuppliers: [] as Array<object>,
      listOfSuppliersIds: [0] as number[],
      idCounter: 0,
      isItActive: false,
    };
  },
  props: FormFieldProps,
  methods: {
    /**
     * Adds a new Object to the Suppliers array
     */
    addNewSuppliers() {
      this.idCounter++;
      this.listOfSuppliersIds.push(this.idCounter);
    },

    /**
     * Remove Object from Product array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfSuppliers(id: number) {
      this.listOfSuppliersIds = this.listOfSuppliersIds.filter((el) => el !== id);
    },
  },
});
</script>
