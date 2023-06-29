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
            <FormKit type="group" name="suppliersPerCountry" label="Suppliers Per Country">
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
              </div>

              <PrimeButton :disabled="false" label="Add" class="p-button-text" icon="pi pi-plus"></PrimeButton>

              <div class="form-field">
                <FormKit
                  type="text"
                  name="orderVolume"
                  validation-label="validationLabel ?? label"
                  validation="number"
                  placeholder="placeholder"
                  inner-class="long"
                />
              </div>
            </FormKit>
          </div>
        </div>
      </keep-alive>
    </div>
  </FormKit>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
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
  },
  data() {
    return {
      existingRelatedCorporateSupplyChain: "",
      isItActive: false,
    };
  },
  props: FormFieldProps,
  methods: {},
});
</script>
