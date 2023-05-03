<template>
  <div class="form-field">
    <InputTextFormField
      name="productionSiteName"
      :info="lksgKpisInfoMappings.productionSiteName"
      :display-name="lksgKpisNameMappings.productionSiteName"
      validation="required"
    />
  </div>

  <div class="form-field">
    <AddressFormField
      name="addressesOfProductionSites"
      :info="lksgKpisInfoMappings.addressesOfProductionSites"
      :display-name="lksgKpisNameMappings.addressesOfProductionSites"
      validation="required"
    />
  </div>

  <div class="form-field">
    <div class="form-field-label">
      <h5>List Of Goods Or Services</h5>
      <em
        class="material-icons info-icon"
        aria-hidden="true"
        title="listOfGoodsOrServices"
        v-tooltip.top="{
          value: lksgKpisInfoMappings['listOfGoodsOrServices'] ? lksgKpisInfoMappings['listOfGoodsOrServices'] : '',
        }"
        >info</em
      >
      <PrimeButton
        :disabled="listOfGoodsOrServicesString === ''"
        @click="addNewItemsToListOfGoodsOrServices()"
        label="Add"
        class="p-button-text"
        icon="pi pi-plus"
      ></PrimeButton>
    </div>
    <FormKit
      data-test="listOfGoodsOrServices"
      type="text"
      :ignore="true"
      v-model="listOfGoodsOrServicesString"
      placeholder="Add comma (,) for more than one value"
    />
    <FormKit
      v-model="productionSite.listOfGoodsOrServices"
      type="list"
      label="list of goods or services"
      name="listOfGoodsOrServices"
    />
    <div class="">
      <span class="form-list-item" :key="element" v-for="element in item.listOfGoodsOrServices">
        {{ element }}
        <em @click="removeItemFromListOfGoodsOrServices(element)" class="material-icons">close</em>
      </span>
    </div>
  </div>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import {
  lksgKpisInfoMappings,
  lksgKpisNameMappings,
} from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import { defineComponent } from "vue";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import { LksgProductionSite } from "@clients/backend";

export default defineComponent({
  name: "ProductionSiteElement",
  computed: {
    lksgKpisNameMappings() {
      return lksgKpisNameMappings;
    },
    lksgKpisInfoMappings() {
      return lksgKpisInfoMappings;
    },
  },
  data() {
    return {
      allCountry: getAllCountryNamesWithCodes(),
      productionSite: Object as LksgProductionSite,
      listOfGoodsOrServicesString: "",
    };
  },
  components: {
    InputTextFormField,
    AddressFormField,
    FormKit,
  },
  methods: {
    /**
     * Remove item from list of Production Sites Goods Or Services
     *
     * @param element - the item to be deleted
     */
    removeItemFromListOfGoodsOrServices(element: string) {
      if (this.productionSite.listOfGoodsOrServices) {
        this.productionSite.listOfGoodsOrServices = this.productionSite.listOfGoodsOrServices.filter(
          (el) => el !== element
        );
      }
    },

    /**
     * Adds a new item to the list of Production Sites Goods Or Services
     */
    addNewItemsToListOfGoodsOrServices() {
      const items = this.listOfGoodsOrServicesString.split(",").map((element) => element.trim());
      this.productionSite.listOfGoodsOrServices = [...this.productionSite.listOfGoodsOrServices, ...items];
    },
  },
});
</script>
