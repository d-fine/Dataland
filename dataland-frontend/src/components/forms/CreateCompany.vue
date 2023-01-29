<template>
  <Card>
    <template #title>Create a Company </template>
    <template #content>
      <!-- ToDo: styling of the page is off-->
      <div class="uploadFormWrapper">
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
            validation-label="Company Name"
          />
          <div class="flex align-items-center form-field-label">
            <UploadFormHeader
              :name="companyDataNames.companyAlternativeNames"
              :explanation="companyDataExplanations.companyAlternativeNames"
            />
            <PrimeButton
              :disabled="this.enteredCompanyAlternativeName === ''"
              @click="addCompanyAlternativeName"
              label="Add"
              class="p-button-text"
              icon="pi pi-plus"
            ></PrimeButton>
          </div>
          <FormKit v-model="enteredCompanyAlternativeName" type="text" placeholder="Company alternative name" />

          <template v-for="index in companyAlternativeNames.length" :key="index">
            <span class="form-list-item">
              {{ companyAlternativeNames[index - 1] }}
              <em @click="removeAlternativeName(index)" class="material-icons">close</em>
            </span>
            <!--            -->
            <!--            <div class="align-items-baseline">-->
            <!--              <div class="font-medium text-3l">{{ companyAlternativeNames[index - 1] }}</div>-->
            <!--              <PrimeButton @click="removeAlternativeName(index)" icon="pi pi-trash"></PrimeButton>-->
            <!--            </div>-->
          </template>

          <div class="next-to-each-other">
            <div>
              <UploadFormHeader
                :name="companyDataNames.headquarters"
                :explanation="companyDataExplanations.headquarters"
              />
              <FormKit
                v-model="headquarters"
                type="text"
                placeholder="City"
                validation="required"
                validation-label="Headquarters"
              />
            </div>
            <div>
              <UploadFormHeader
                :name="companyDataNames.countryCode"
                :explanation="companyDataExplanations.countryCode"
              />
              <!-- ToDo: the options are not searchable at the moment-->
              <FormKit
                v-model="countryCode"
                type="select"
                placeholder="Select"
                validation="required"
                validation-label="Country Code"
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

          <UploadFormHeader :name="companyDataNames.website" :explanation="companyDataExplanations.website" />
          <FormKit v-model="website" type="text" :placeholder="companyDataNames.website" />

          <h4>Identifier</h4>

          <!-- ToDo: there is no live update to check if an identifier is already in use-->
          <UploadFormHeader :name="companyDataNames.isin" :explanation="companyDataExplanations.isin" />
          <FormKit v-model="isin" type="text" :placeholder="companyDataNames.isin" />

          <UploadFormHeader :name="companyDataNames.ticker" :explanation="companyDataExplanations.ticker" />
          <FormKit v-model="ticker" type="text" :placeholder="companyDataNames.ticker" />

          <UploadFormHeader :name="companyDataNames.permId" :explanation="companyDataExplanations.permId" />
          <FormKit v-model="permId" type="text" :placeholder="companyDataNames.permId" />

          <UploadFormHeader :name="companyDataNames.duns" :explanation="companyDataExplanations.duns" />
          <FormKit v-model="duns" type="text" :placeholder="companyDataNames.duns" />

          <UploadFormHeader
            :name="companyDataNames.companyRegistrationNumber"
            :explanation="companyDataExplanations.companyRegistrationNumber"
          />
          <FormKit
            v-model="companyRegistrationNumber"
            type="text"
            :placeholder="companyDataNames.companyRegistrationNumber"
          />

          <h4>GICS classification</h4>

          <UploadFormHeader :name="companyDataNames.sector" :explanation="companyDataExplanations.sector" />
          <FormKit
            v-model="sector"
            type="select"
            placeholder="Please choose"
            validation="required"
            validation-label="Sector"
            :options="gicsSectors"
          />

          <PrimeButton type="submit" label="ADD COMPANY" name="addCompany" />
        </FormKit>
        <template v-if="postCompanyProcessed">
          <SuccessUpload v-if="uploadSucceded" :message="message" :messageId="messageCounter" />
          <FailedUpload v-else :message="message" :messageId="messageCounter" />
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
  gicsSectors,
} from "@/components/resources/frameworkDataSearch/ReferenceDataModelTranslations";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import { AxiosError } from "axios";

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
    companyRegistrationNumber: "",
    sector: "",
    website: "",
    identifiers: [] as Array<CompanyIdentifier>,
    enteredCompanyAlternativeName: "",
    allCountryCodes: getAllCountryCodes(),
    postCompanyProcessed: false,
    message: "",
    uploadSucceded: false,
    messageCounter: 0,
    companyDataExplanations,
    companyDataNames,
    gicsSectors,
  }),
  methods: {
    /**
     * Adds a CompanyIdentifier to the array of identifiers
     *
     * @param identifierType the type of the identifier as specified in CompanyIdentifierIdentifierTypeEnum
     * @param identifierValue the value of the identifier
     */
    addIdentifier(identifierType: CompanyIdentifierIdentifierTypeEnum, identifierValue: string): void {
      if (identifierValue !== "") {
        const newIdentifier = {
          identifierType: identifierType,
          identifierValue: identifierValue,
        } as CompanyIdentifier;
        this.identifiers.push(newIdentifier);
      }
    },
    /**
     * Creates a new array of identifiers using the currently existing values
     */
    collectIdentifiers(): void {
      this.identifiers = new Array<CompanyIdentifier>();
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Lei, this.lei);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Isin, this.isin);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Ticker, this.ticker);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.PermId, this.permId);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.Duns, this.duns);
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.CompanyRegistrationNumber, this.companyRegistrationNumber);
    },
    /**
     * Adds the value from the input field for company alternative names to the corresponding array.
     * Empty strings and duplicates are ignored and the input field value is reset.
     */
    addCompanyAlternativeName(): void {
      if (
        this.enteredCompanyAlternativeName !== "" &&
        !this.companyAlternativeNames.includes(this.enteredCompanyAlternativeName)
      ) {
        this.companyAlternativeNames.push(this.enteredCompanyAlternativeName);
      }
      this.enteredCompanyAlternativeName = "";
    },
    /**
     * Removes the n-th company alternative name from the corresponding array
     *
     * @param index specifies the n-th alternative name to be removed
     */
    removeAlternativeName(index: number): void {
      this.companyAlternativeNames.splice(index - 1, 1);
    },
    /**
     * Builds a CompanyInformation object using the currently entered inputs and returns it
     *
     * @returns the CompanyInformation object build
     */
    getCompanyInformation(): CompanyInformation {
      this.addCompanyAlternativeName();
      this.collectIdentifiers();
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
        website: this.website,
      } as CompanyInformation;
    },
    /**
     * Scrolls to the top of the page
     */
    toTop(): void {
      window.scrollTo({
        top: 0,
        left: 0,
        behavior: "smooth",
      });
    },
    /**
     * Posts the entered company information to the backend
     */
    async postCompanyInformation() {
      this.messageCounter++;
      try {
        const company = this.getCompanyInformation();
        if (this.identifiers.length === 0) {
          this.message = "Please specify at least one company identifier.";
          this.uploadSucceded = false;
        } else {
          const companyDataControllerApi = await new ApiClientProvider(
            assertDefined(this.getKeycloakPromise)()
          ).getCompanyDataControllerApi();
          await companyDataControllerApi.postCompany(company);
          this.$formkit.reset("createCompanyForm");
          this.companyAlternativeNames = new Array<string>();
          this.message = "Upload successfully executed.";
          this.uploadSucceded = true;
        }
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = "An error occurred: " + error.message;
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        }
        this.uploadSucceded = false;
      } finally {
        this.postCompanyProcessed = true;
        this.toTop();
      }
    },
  },
});
</script>
