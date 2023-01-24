<template>
  <AuthenticationWrapper>
    <TheHeader />
    <TheContent>
      <Card class="col-12">
        <template #title>
          <div class="grid">
          <BackButton class="col-2" />
          <span clas="col-2">New Dataset - Company</span>
          </div>
          <hr />
        </template>
        <template #content>
          <div style="flex-wrap: wrap">
            <div id="option1Container">
              <div id="topicLabel" class="col-3 text-left topicLabel">
                <h4 id="topicTitle" class="title">Option 01</h4>
                <p>Select the company for which you would like to add a new dataset.</p>
              </div>
              <div>SELECTOR PLACEHOLDER</div>
            </div>
            <div id="option2Container">
              <div id="topicLabel" class="col-3 text-left topicLabel">
                <h4 id="topicTitle" class="title">Option 02</h4>
                <p>
                  If you want to add a dataset for a new company, you first have to create the company. To create a new
                  company, all mandatory * fields must be filled.
                </p>
              </div>
              <div id="uploadForm" class="col-6 text-left uploadForm">
                <CreateCompany />
              </div>
            </div>
            <div id="jumpLinks" class="col-3 text-left jumpLinks">
              <h4 id="topicTitle" class="title">On this page</h4>

              <ul>
                <li><a href="#general">General</a></li>
                <li><a href="#childLabour">Child labour</a></li>
                <li><a href="#forcedLabourSlaveryAndDebtBondage">Forced labour, slavery and debt bondage</a></li>
                <li><a href="#evidenceCertificatesAndAttestations">Evidence, certificates and attestations</a></li>
                <li><a href="#support">Theme Support</a></li>
              </ul>
            </div>
          </div>
        </template>
      </Card>
    </TheContent>
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { convertCurrencyNumbersToNotationWithLetters } from "@/utils/CurrencyConverter";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import { FormKit } from "@formkit/vue";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import CreateCompany from "@/components/forms/CreateCompany.vue";

export default defineComponent({
  name: "CompanyInformation",
  components: {
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
      waitingForData: true,
    };
  },
  props: {
    companyID: {
      type: String,
      default: "loading",
    },
  },
  mounted() {
    void this.getCompanyInformation();
  },
  watch: {
    companyID() {
      void this.getCompanyInformation();
    },
  },
  methods: {
    async getCompanyInformation() {
      try {
        this.waitingForData = true;
        if (this.companyID != "loading") {
          const companyDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getCompanyDataControllerApi();
          const response = await companyDataControllerApi.getCompanyById(this.companyID);
          // this.companyInformation = response.data.companyInformation;
          this.waitingForData = false;
        }
      } catch (error) {
        console.error(error);
        // this.companyInformation = null;
      }
    },
    orderOfMagnitudeSuffix(value: number): string {
      return convertCurrencyNumbersToNotationWithLetters(value);
    },
  },
});
</script>
