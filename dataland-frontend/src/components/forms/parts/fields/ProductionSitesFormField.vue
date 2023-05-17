<template>
  <div class="form-field">
    <UploadFormHeader :name="displayName" :explanation="info" :is-required="required" />
    <FormKit
      type="list"
      :name="name"
      :label="displayName"
      :validation="validation"
      :validation-label="validationLabel!"
      v-model="existingProductionSites"
    >
      <FormKit type="group" v-for="id in listOfProductionSiteIds" :key="id">
        <div data-test="productionSiteSection" class="productionSiteSection">
          <em
            data-test="removeItemFromListOfProductionSites"
            @click="removeItemFromListOfProductionSites(id)"
            class="material-icons close-section"
            >close</em
          >
          <ProductionSiteFormElement />
        </div>
      </FormKit>
      <PrimeButton
        data-test="ADD-NEW-Production-Site-button"
        label="ADD NEW Production Site"
        class="p-button-text"
        icon="pi pi-plus"
        @click="addNewProductionSite"
      />
    </FormKit>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import PrimeButton from "primevue/button";
import { defineComponent } from "vue";
import { LksgProductionSite } from "@clients/backend";
import ProductionSiteFormElement from "@/components/forms/parts/elements/derived/ProductionSiteFormElement.vue";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { FormFieldProps } from "@/components/forms/parts/fields/FormFieldProps";

export default defineComponent({
  name: "ProductionSitesFormField",
  props: FormFieldProps,
  data() {
    return {
      existingProductionSites: [] as LksgProductionSite[],
      listOfProductionSiteIds: [0] as number[],
      idCounter: 0,
    };
  },
  components: {
    UploadFormHeader,
    ProductionSiteFormElement,
    FormKit,
    PrimeButton,
  },
  mounted() {
    for (let i = 1; i < this.existingProductionSites.length; i++) {
      this.addNewProductionSite();
    }
  },
  methods: {
    /**
     * Adds a new Object to the ProductionSite array
     */
    addNewProductionSite() {
      this.idCounter++;
      this.listOfProductionSiteIds.push(this.idCounter);
    },

    /**
     * Remove Object from ProductionSite array
     * @param id - the id of the object in the array
     */
    removeItemFromListOfProductionSites(id: number) {
      this.listOfProductionSiteIds = this.listOfProductionSiteIds.filter((el) => el !== id);
    },
  },
});
</script>
