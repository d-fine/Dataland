<template>
  <AuthenticationWrapper>
    <TheHeader/>
    <TheContent>
      <BackButton id="backButton" label="CHOOSE COMPANY"/>
      <CompanyInformation :companyID="companyID"/>
      <Card class="col-12">
        <template #title>
          New Dataset - Framework
          <hr/>
        </template>
        <template #content>
          <div class="uploadFormWrapper">


            <div id="euTaxonomyContainer" class="grid">
              <div id="euTaxonomyLabel" class="col-3 text-left topicLabel">
                <h3>EU Taxonomy</h3>
                <p>Overiew of all existing and missing EU Taxonomy datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">
                <div id="eutaxonomyDataSetsContainer" class="mt-6">
                  todo
                </div>
              </div>
            </div>


            <div id="sfdrContainer" class="grid mt-6">
              <div id="sfdrLabel" class="col-3 text-left topicLabel">
                <h3>SFDR</h3>
                <p>Overiew of all existing and missing SFDR datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">
                <div id="sfdrDataSetsContainer" class="mt-6">
                  todo
                </div>
              </div>
            </div>


            <div id="lksgContainer" class="grid mt-6">
              <div id="lksgLabel" class="col-3 text-left topicLabel">
                <h3>LkSG</h3>
                <p>Overiew of all existing and missing LkSG datasets for this company.</p>
              </div>
              <div class="col-6 text-left uploadForm">

                <div id="lksgDataSetsContainer" class="mt-6">
                  todo
                </div>
              </div>
            </div>


          </div>
        </template>
      </Card>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import {ApiClientProvider} from "@/services/ApiClients";
import {convertCurrencyNumbersToNotationWithLetters} from "@/utils/CurrencyConverter";
import {defineComponent, inject, ref} from "vue";
import Keycloak from "keycloak-js";
import {assertDefined} from "@/utils/TypeScriptUtils";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import {FormKit} from "@formkit/vue";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import CreateCompany from "@/components/forms/CreateCompany.vue";
import CompaniesOnlySearchBar from "@/components/resources/companiesOnlySearch/CompaniesOnlySearchBar.vue";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";


export default defineComponent({
  name: "Choose Framework",
  components: {
    CompanyInformation,
    CompaniesOnlySearchBar,
    CreateCompany,
    AuthenticationWrapper,
    TheHeader,
    BackButton,
    TheContent,
    FormKit,
    Card,
    DataPointFormElement,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data() {
    return {
    };
  },
  props: {
    companyID: {
      type: String
    },
  },

  watch: {

  },
  methods: {
  },
});
</script>
