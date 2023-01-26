<template>
  <Card class="col-5 col-offset-1">
    <template #title>Create a Company </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <FormKit
          v-model="formInputsModel"
          :actions="false"
          type="form"
          id="createCompanyForm"
          @submit="postCompanyInformation"
        >
          <h4>Name & location</h4>
          <UploadFormHeader :name="companyDataNames.companyName" :explanation="companyDataExplanations.companyName" />
          <FormKit
            v-model="companyName"
            type="text"
            :placeholder="companyDataNames.companyName"
            validation="required"
          />
          <div class="grid align-items-center">
            <div>
              <div class="grid align-items-center">
                <UploadFormHeader :name="companyDataNames.companyAlternativeNames" :explanation="companyDataExplanations.companyAlternativeNames" />
                <PrimeButton @click="addCompanyAlternativeName" label="ADD" class="p-button-text" icon="pi pi-plus"></PrimeButton>
              </div>
              <FormKit v-model="enteredCompanyAlternativeName" type="text" placeholder="Company alternative name" />
            </div>

            <!--PrimeButton @click="addCompanyAlternativeName" icon="pi pi-plus"></PrimeButton-->
          </div>

          <FormKit v-model="companyAlternativeNames" type="list" name="companyAlternativeNames">
            <template v-for="index in companyAlternativeNames.length" :key="index">
              <div class="grid align-items-baseline">
                <FormKit type="text" />
                <PrimeButton @click="removeAlternativeName(index)" icon="pi pi-trash"></PrimeButton>
              </div>
            </template>
          </FormKit>

          <div class="grid align-items-center">
            <div>
              <UploadFormHeader
                :name="companyDataNames.headquarters"
                :explanation="companyDataExplanations.headquarters"
              />
              <FormKit v-model="headquarters" type="text" placeholder="City" validation="required" />
            </div>
            <div>
              <UploadFormHeader
                :name="companyDataNames.countryCode"
                :explanation="companyDataExplanations.countryCode"
              />
              <FormKit
                v-model="countryCode"
                type="select"
                placeholder="Select"
                validation="required"
                :options="allCountryCodes"
              />
            </div>
          </div>

          <UploadFormHeader
            :name="companyDataNames.headquartersPostalCode"
            :explanation="companyDataExplanations.headquartersPostalCode"
          />
          <FormKit
            v-model="headquartersPostalCode"
            type="text"
            :placeholder="companyDataNames.headquartersPostalCode"
          />

          <UploadFormHeader
            :name="companyDataNames.companyLegalForm"
            :explanation="companyDataExplanations.companyLegalForm"
          />
          <FormKit v-model="companyLegalForm" type="text" :placeholder="companyDataNames.companyLegalForm" />

          <h4>Identifier</h4>

          <UploadFormHeader :name="companyDataNames.isin" :explanation="companyDataExplanations.isin" />
          <FormKit v-model="isin" type="text" :placeholder="companyDataNames.isin" />

          <UploadFormHeader :name="companyDataNames.ticker" :explanation="companyDataExplanations.ticker" />
          <FormKit v-model="ticker" type="text" :placeholder="companyDataNames.ticker" />

          <UploadFormHeader :name="companyDataNames.permId" :explanation="companyDataExplanations.permId" />
          <FormKit v-model="permId" type="text" :placeholder="companyDataNames.permId" />

          <UploadFormHeader :name="companyDataNames.duns" :explanation="companyDataExplanations.duns" />
          <FormKit v-model="duns" type="text" :placeholder="companyDataNames.duns" />

          <h4>GICS classification</h4>

          <UploadFormHeader :name="companyDataNames.sector" :explanation="companyDataExplanations.sector" />
          <FormKit
            v-model="sector"
            type="select"
            placeholder="Please choose"
            :options="gicsSectors"
          />

          <FormKit type="submit" label="ADD COMPANY" name="addCompany" />
        </FormKit>
        <template v-if="postCompanyProcessed">
          <SuccessUpload
            v-if="postCompanyResponse"
            msg="company"
            :messageCount="messageCount"
            :data="postCompanyResponse.data"
          />
          <FailedUpload v-else msg="company" :messageCount="messageCount" />
        </template>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import Card from "primevue/card";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { CompanyInformation, CompanyIdentifier, CompanyIdentifierIdentifierTypeEnum } from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import PrimeButton from "primevue/button";
import { getAllCountryCodes } from "@/utils/CountryCodeConverter";
import { assertDefined } from "@/utils/TypeScriptUtils";
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import Tooltip from "primevue/tooltip";
import {
  companyDataNames,
  companyDataExplanations,
  gicsSectors
} from "@/components/resources/frameworkDataSearch/ReferenceDataModelTranslations";
import UploadFormHeader from "@/utils/UploadFormHeader.vue";

export default defineComponent({
  name: "CreateCompany",
  components: {
    UploadFormHeader,
    Card,
    FormKit,
    PrimeButton,
    SuccessUpload,
    FailedUpload,
  },
  directives: {
    tooltip: Tooltip,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  data: () => ({
    //ToDo tooltips are missing
    contentTest: "This is the content",
    companyName: "",
    companyAlternativeNames: [] as Array<string>,
    companyLegalForm: "",
    headquarters: "",
    headquartersPostalCode: "",
    countryCode: "",
    lei: "",
    isin: "",
    ticker: "",
    permId: "",
    duns: "",
    identifiers: [] as Array<CompanyIdentifier>,
    sector: "",
    enteredCompanyAlternativeName: "",
    allCountryCodes: getAllCountryCodes(),
    postCompanyResponse: null,
    messageCount: 0,
    postCompanyProcessed: false,
    companyDataExplanations,
    companyDataNames,
    gicsSectors,
  }),
  methods: {
    addIdentifier(identifierType: CompanyIdentifierIdentifierTypeEnum, identifierValue: string): void {
      if (identifierValue !== "") {
        const newIdentifier = {
          identifierType: identifierType,
          identifierValue: identifierValue,
        } as CompanyIdentifier;
        this.identifiers.push(newIdentifier);
      }
    },
    addCompanyAlternativeName(): void {
      //ToDo change the companyAlternativeNames to a set to avoid duplicate entries
      if (this.enteredCompanyAlternativeName !== "") {
        this.companyAlternativeNames.push(this.enteredCompanyAlternativeName);
        this.enteredCompanyAlternativeName = "";
      }
    },
    removeAlternativeName(index: number): void {
      this.companyAlternativeNames.splice(index - 1, 1);
    },
    getCompanyInformation(): CompanyInformation {
      this.addCompanyAlternativeName();
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Lei, this.lei);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Isin, this.isin);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Ticker, this.ticker);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.PermId, this.permId);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Duns, this.duns);
      return {
        companyName: this.companyName,
        companyAlternativeNames: this.companyAlternativeNames,
        companyLegalForm: this.companyLegalForm,
        headquarters: this.headquarters,
        headquartersPostalCode: this.headquartersPostalCode,
        sector: this.sector,
        identifiers: this.identifiers,
        countryCode: this.countryCode,
        isTeaserCompany: false,
      } as CompanyInformation;
    },
    async postCompanyInformation() {
      //ToDo displaying of error/success was transferred from original script should be replaced by a more sensible version
      try {
        this.messageCount++;
        console.log("Posting data");
        const company = this.getCompanyInformation();
        console.log("Constructed Company Object:");
        console.log(company);
        const companyDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getCompanyDataControllerApi();
        this.postCompanyResponse = await companyDataControllerApi.postCompany(company);
        this.$formkit.reset("createCompanyForm");
        //ToDo after submission the boxes showing the alternative names get emptied but not removed
        this.companyAlternativeNames = new Array<string>();
      } catch (error) {
        console.error(error);
        this.postCompanyResponse = null;
      } finally {
        this.postCompanyProcessed = true;
      }
    },
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 300px;
}
.formkit-icon {
  max-width: 5em;
}
</style>
