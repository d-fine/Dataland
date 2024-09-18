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
            :label="companyDataNames.companyName"
            :description="companyDataExplanations.companyName"
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
              :label="companyDataNames.companyAlternativeNames"
              :description="companyDataExplanations.companyAlternativeNames"
            />
            <PrimeButton
              :disabled="enteredCompanyAlternativeName === ''"
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
                :label="companyDataNames.headquarters"
                :description="companyDataExplanations.headquarters"
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
              <SingleSelectFormField
                container-class=""
                name="countryCode"
                v-model="countryCode"
                placeholder="Select"
                validation="required"
                validation-label="Country Code"
                :options="allCountryCodes.map((it) => ({ value: it, label: it }))"
                :label="companyDataNames.countryCode"
                :description="companyDataExplanations.countryCode"
                required
              />
            </div>
          </div>

          <UploadFormHeader
            :label="companyDataNames.headquartersPostalCode"
            :description="companyDataExplanations.headquartersPostalCode"
          />
          <FormKit
            name="headquartersPostalCode"
            v-model="headquartersPostalCode"
            type="text"
            :placeholder="companyDataNames.headquartersPostalCode"
          />

          <UploadFormHeader
            :label="companyDataNames.companyLegalForm"
            :description="companyDataExplanations.companyLegalForm"
          />
          <FormKit
            name="companyLegalForm"
            v-model="companyLegalForm"
            type="text"
            :placeholder="companyDataNames.companyLegalForm"
          />

          <UploadFormHeader :label="companyDataNames.website" :description="companyDataExplanations.website" />
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
            <UploadFormHeader :label="companyDataNames.lei" :description="companyDataExplanations.lei" />
            <FormKit
              name="lei"
              v-model="lei"
              type="text"
              :placeholder="companyDataNames.lei"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.Lei}`"
            />

            <UploadFormHeader :label="companyDataNames.isin" :description="companyDataExplanations.isin" />
            <FormKit
              name="isin"
              v-model="isin"
              type="text"
              :placeholder="companyDataNames.isin"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.Isin}`"
            />

            <UploadFormHeader :label="companyDataNames.ticker" :description="companyDataExplanations.ticker" />
            <FormKit
              name="ticker"
              v-model="ticker"
              type="text"
              :placeholder="companyDataNames.ticker"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.Ticker}`"
            />

            <UploadFormHeader :label="companyDataNames.permId" :description="companyDataExplanations.permId" />
            <FormKit
              name="permId"
              v-model="permId"
              type="text"
              :placeholder="companyDataNames.permId"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.PermId}`"
            />

            <UploadFormHeader :label="companyDataNames.duns" :description="companyDataExplanations.duns" />
            <FormKit
              name="duns"
              v-model="duns"
              type="text"
              :placeholder="companyDataNames.duns"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.Duns}`"
            />

            <UploadFormHeader
              :label="companyDataNames.companyRegistrationNumber"
              :description="companyDataExplanations.companyRegistrationNumber"
            />
            <FormKit
              name="companyRegistrationNumber"
              v-model="companyRegistrationNumber"
              type="text"
              :placeholder="companyDataNames.companyRegistrationNumber"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.CompanyRegistrationNumber}`"
            />

            <UploadFormHeader :label="companyDataNames.vatNumber" :description="companyDataExplanations.vatNumber" />
            <FormKit
              name="vatNumber"
              v-model="vatNumber"
              type="text"
              :placeholder="companyDataNames.vatNumber"
              :validation="`identifierDoesNotExistValidator:${IdentifierType.VatNumber}`"
            />
          </FormKit>

          <h4>GICS classification</h4>

          <SingleSelectFormField
            :label="companyDataNames.sector"
            :description="companyDataExplanations.sector"
            name="sector"
            v-model="sector"
            placeholder="Please choose"
            :options="gicsSectors.map((it) => ({ value: it, label: it }))"
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
import { FormKit } from '@formkit/vue';
import Card from 'primevue/card';
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { type CompanyInformation, IdentifierType } from '@clients/backend';
import { ApiClientProvider } from '@/services/ApiClients';
import PrimeButton from 'primevue/button';
import { getAllCountryCodes } from '@/utils/CountryCodeConverter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { checkCustomInputs } from '@/utils/ValidationUtils';
import Tooltip from 'primevue/tooltip';
import {
  companyDataNames,
  companyDataExplanations,
  gicsSectors,
} from '@/components/resources/frameworkDataSearch/ReferenceDataModelTranslations';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import { AxiosError } from 'axios';
import { type FormKitNode } from '@formkit/core';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';

export default defineComponent({
  name: 'CreateCompany',
  components: {
    SingleSelectFormField,
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
  emits: ['companyCreated'],
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  data: () => ({
    companyName: '',
    companyAlternativeNames: [] as Array<string>,
    companyLegalForm: '',
    headquarters: '',
    headquartersPostalCode: '',
    countryCode: null as null | string,
    lei: '',
    isin: '',
    ticker: '',
    permId: '',
    duns: '',
    companyRegistrationNumber: '',
    vatNumber: '',
    sector: null as null | string,
    website: '',
    checkCustomInputs,
    identifiers: {} as { [key: string]: Array<string> },
    enteredCompanyAlternativeName: '',
    allCountryCodes: getAllCountryCodes(),
    postCompanyProcessed: false,
    message: '',
    uploadSucceded: false,
    messageCounter: 0,
    companyDataExplanations,
    companyDataNames,
    gicsSectors,
    IdentifierType,
  }),
  methods: {
    /**
     * Validates if there is already a company with an identifier of value of a FormKit input field
     * @param node the node corresponding the FormKit input field
     * @param identifierType the type of the identifier to check
     * @returns true if and only if there is no company with the in the node specified identifier of the specified type
     */
    async identifierDoesNotExistValidator(node: FormKitNode, identifierType: IdentifierType): Promise<boolean> {
      try {
        await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).backendClients.companyDataController.existsIdentifier(identifierType, node.value as string);
        return false;
      } catch (error) {
        if (error instanceof AxiosError && error.response?.status == 404) {
          return true;
        }
        throw error;
      }
    },
    /**
     * Adds a CompanyIdentifier to the array of identifiers
     * @param identifierType the type of the identifier as specified in IdentifierType
     * @param identifierValue the value of the identifier
     */
    setIdentifier(identifierType: IdentifierType, identifierValue: string): void {
      if (identifierValue !== '') {
        this.identifiers[identifierType] = [identifierValue];
      }
    },
    /**
     * Creates a new array of identifiers using the currently existing values
     */
    collectIdentifiers(): void {
      this.identifiers = {};
      this.setIdentifier(IdentifierType.Lei, this.lei);
      this.setIdentifier(IdentifierType.Isin, this.isin);
      this.setIdentifier(IdentifierType.Ticker, this.ticker);
      this.setIdentifier(IdentifierType.PermId, this.permId);
      this.setIdentifier(IdentifierType.Duns, this.duns);
      this.setIdentifier(IdentifierType.CompanyRegistrationNumber, this.companyRegistrationNumber);
      this.setIdentifier(IdentifierType.VatNumber, this.vatNumber);
    },
    /**
     * Adds the value from the input field for company alternative names to the corresponding array.
     * Empty strings and duplicates are ignored and the input field value is reset.
     */
    addCompanyAlternativeName(): void {
      if (
        this.enteredCompanyAlternativeName !== '' &&
        !this.companyAlternativeNames.includes(this.enteredCompanyAlternativeName)
      ) {
        this.companyAlternativeNames.push(this.enteredCompanyAlternativeName);
      }
      this.enteredCompanyAlternativeName = '';
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
        countryCode: assertDefined(this.countryCode),
        isTeaserCompany: false,
        website: this.website,
      };
    },
    /**
     * Posts the entered company information to the backend
     */
    async postCompanyInformation() {
      this.messageCounter++;
      try {
        const company = this.getCompanyInformation();
        const hasAtLeastOneIdentifier = Object.values(this.identifiers).some((it) => it.length > 0);
        if (!hasAtLeastOneIdentifier) {
          this.message = 'Please specify at least one company identifier.';
          this.uploadSucceded = false;
        } else {
          const companyDataControllerApi = new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
            .backendClients.companyDataController;
          const response = await companyDataControllerApi.postCompany(company);
          const newCompanyId = response.data.companyId;
          this.$emit('companyCreated', newCompanyId);
          this.$formkit.reset('createCompanyForm');
          this.companyAlternativeNames = new Array<string>();
          this.message = 'New company has the ID: ' + newCompanyId;
          this.uploadSucceded = true;
        }
      } catch (error) {
        console.error(error);
        if (error instanceof AxiosError) {
          this.message = 'An error occurred: ' + error.message;
        } else {
          this.message =
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
        this.uploadSucceded = false;
      } finally {
        this.postCompanyProcessed = true;
      }
    },
  },
});
</script>
