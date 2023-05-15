<template>
  <Card class="bg-white">
    <template #title>Create a Company</template>
    <template #content>
      <div class="uploadFormWrapper">
        <FormKit
          :actions="false"
          type="form"
          id="createCompanyForm"
          @submit="postCompanyInformation"
          @submit-invalid="checkCustomInputs"
        >
          <h4>Name & location</h4>
          <UploadFormHeader
            :name="companyDataNames.companyName"
            :explanation="companyDataExplanations.companyName"
            :is-required="true"
          />
          <FormKit
            name="companyName"
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
              name="addAlternativeName"
              label="Add"
              class="p-button-text"
              icon="pi pi-plus"
            ></PrimeButton>
          </div>
          <FormKit
            name="alternativeName"
            v-model="enteredCompanyAlternativeName"
            type="text"
            placeholder="Company alternative name"
          />

          <template v-for="index in companyAlternativeNames.length" :key="index">
            <span class="form-list-item">
              {{ companyAlternativeNames[index - 1] }}
              <em @click="removeAlternativeName(index)" class="material-icons">close</em>
            </span>
          </template>

          <div class="next-to-each-other">
            <div>
              <UploadFormHeader
                :name="companyDataNames.headquarters"
                :explanation="companyDataExplanations.headquarters"
                :is-required="true"
              />
              <FormKit
                name="headquarters"
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
                :is-required="true"
              />
              <FormKit
                name="countryCode"
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
            name="headquartersPostalCode"
            v-model="headquartersPostalCode"
            type="text"
            :placeholder="companyDataNames.headquartersPostalCode"
          />

          <UploadFormHeader
            :name="companyDataNames.companyLegalForm"
            :explanation="companyDataExplanations.companyLegalForm"
          />
          <FormKit
            name="companyLegalForm"
            v-model="companyLegalForm"
            type="text"
            :placeholder="companyDataNames.companyLegalForm"
          />

          <UploadFormHeader :name="companyDataNames.website" :explanation="companyDataExplanations.website" />
          <FormKit name="website" v-model="website" type="text" :placeholder="companyDataNames.website" />

          <h4>Identifier</h4>

          <FormKit
            type="group"
            :config="{
              validationMessages: { identifierDoesNotExistValidator: 'There already exists a company with this ID' },
              validationRules: { identifierDoesNotExistValidator },
              validationVisibility: 'live',
            }"
          >
            <UploadFormHeader :name="companyDataNames.lei" :explanation="companyDataExplanations.lei" />
            <FormKit
              name="lei"
              v-model="lei"
              type="text"
              :placeholder="companyDataNames.lei"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.Lei}`"
            />

            <UploadFormHeader :name="companyDataNames.isin" :explanation="companyDataExplanations.isin" />
            <FormKit
              name="isin"
              v-model="isin"
              type="text"
              :placeholder="companyDataNames.isin"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.Isin}`"
            />

            <UploadFormHeader :name="companyDataNames.ticker" :explanation="companyDataExplanations.ticker" />
            <FormKit
              name="ticker"
              v-model="ticker"
              type="text"
              :placeholder="companyDataNames.ticker"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.Ticker}`"
            />

            <UploadFormHeader :name="companyDataNames.permId" :explanation="companyDataExplanations.permId" />
            <FormKit
              name="permId"
              v-model="permId"
              type="text"
              :placeholder="companyDataNames.permId"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.PermId}`"
            />

            <UploadFormHeader :name="companyDataNames.duns" :explanation="companyDataExplanations.duns" />
            <FormKit
              name="duns"
              v-model="duns"
              type="text"
              :placeholder="companyDataNames.duns"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.Duns}`"
            />

            <UploadFormHeader
              :name="companyDataNames.companyRegistrationNumber"
              :explanation="companyDataExplanations.companyRegistrationNumber"
            />
            <FormKit
              name="companyRegistrationNumber"
              v-model="companyRegistrationNumber"
              type="text"
              :placeholder="companyDataNames.companyRegistrationNumber"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.CompanyRegistrationNumber}`"
            />

            <UploadFormHeader :name="companyDataNames.vatNumber" :explanation="companyDataExplanations.vatNumber" />
            <FormKit
              name="vatNumber"
              v-model="vatNumber"
              type="text"
              :placeholder="companyDataNames.vatNumber"
              :validation="`identifierDoesNotExistValidator:${CompanyIdentifierIdentifierTypeEnum.VatNumber}`"
            />
          </FormKit>

          <h4>GICS classification</h4>

          <UploadFormHeader
            :name="companyDataNames.sector"
            :explanation="companyDataExplanations.sector"
            :is-required="true"
          />
          <FormKit
            name="sector"
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
          <SuccessMessage v-if="uploadSucceded" :message="message" :messageId="messageCounter" />
          <FailMessage v-else :message="message" :messageId="messageCounter" />
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
import {
  CompanyInformation,
  CompanyIdentifier,
  CompanyIdentifierIdentifierTypeEnum,
  StoredCompany,
} from "@clients/backend";
import { ApiClientProvider } from "@/services/ApiClients";
import PrimeButton from "primevue/button";
import { getAllCountryCodes } from "@/utils/CountryCodeConverter";
import { assertDefined } from "@/utils/TypeScriptUtils";
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import { checkCustomInputs } from "@/utils/validationsUtils";
import Tooltip from "primevue/tooltip";
import {
  companyDataNames,
  companyDataExplanations,
  gicsSectors,
} from "@/components/resources/frameworkDataSearch/ReferenceDataModelTranslations";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import { AxiosError } from "axios";
import { FormKitNode } from "@formkit/core";

export default defineComponent({
  name: "CreateCompany",
  components: {
    UploadFormHeader,
    Card,
    FormKit,
    PrimeButton,
    SuccessMessage,
    FailMessage,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["companyCreated"],
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
    vatNumber: "",
    sector: "",
    website: "",
    checkCustomInputs,
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
    CompanyIdentifierIdentifierTypeEnum,
  }),
  methods: {
    /**
     * Validates if there is already a company with an identifier of value of a FormKit input field
     * @param node the node corresponding the FormKit input field
     * @param identifierType the type of the identifier to check
     * @returns true if and only if there is no company with the in the node specified identifier of the specified type
     */
    async identifierDoesNotExistValidator(
      node: FormKitNode,
      identifierType: CompanyIdentifierIdentifierTypeEnum
    ): Promise<boolean> {
      const fetchedCompanies = (
        await (
          await new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).getCompanyDataControllerApi()
        ).getCompanies(node.value as string)
      ).data;
      return !fetchedCompanies.some((it: StoredCompany) =>
        it.companyInformation.identifiers.some(
          (id) => id.identifierType == identifierType && id.identifierValue == (node.value as string)
        )
      );
    },
    /**
     * Adds a CompanyIdentifier to the array of identifiers
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
      this.addIdentifier(CompanyIdentifierIdentifierTypeEnum.VatNumber, this.vatNumber);
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
     * @param index specifies the n-th alternative name to be removed
     */
    removeAlternativeName(index: number): void {
      this.companyAlternativeNames.splice(index - 1, 1);
    },
    /**
     * Builds a CompanyInformation object using the currently entered inputs and returns it
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
          const response = await companyDataControllerApi.postCompany(company);
          const newCompanyId = response.data.companyId;
          this.$emit("companyCreated", newCompanyId);
          this.$formkit.reset("createCompanyForm");
          this.companyAlternativeNames = new Array<string>();
          this.message = "New company has the ID: " + newCompanyId;
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
      }
    },
  },
});
</script>
