<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - LkSG</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading LkSG data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedLksgData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postLkSGData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in lksgDataModel"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div
                  class="uploadFormSection grid"
                  v-for="subcategory in category.subcategories"
                  :key="subcategory.name"
                >
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="col-3 p-3 topicLabel">
                      <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                      <div :class="`p-badge badge-${category.color}`">
                        <span>{{ category.label.toUpperCase() }}</span>
                      </div>
                    </div>

                    <div class="col-9 formFields">
                      <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                        <component
                          v-if="field.showIf(companyAssociatedLksgData.data)"
                          :is="field.component"
                          :label="field.label"
                          :placeholder="field.placeholder"
                          :description="field.description"
                          :name="field.name"
                          :options="field.options"
                          :required="field.required"
                          :validation="field.validation"
                          :validation-label="field.validationLabel"
                          :data-test="field.name"
                          :shouldDisableCheckboxes="true"
                          @field-specific-documents-updated="
                            updateDocumentList(`${category.name}.${subcategory.name}.${field.name}`, $event)
                          "
                          :ref="field.name"
                        />
                      </FormKit>
                    </div>
                  </template>
                </div>
              </FormKit>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postLkSGDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in lksgDataModel" :key="category.name">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory.name">
                  <a
                    v-if="subcategoryVisibility.get(subcategory) ?? true"
                    @click="smoothScroll(`#${subcategory.name}`)"
                    >{{ subcategory.label }}</a
                  >
                </li>
              </ul>
            </li>
          </ul>
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>
<script lang="ts">
// @ts-nocheck
import { FormKit } from '@formkit/vue';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import { defineComponent, inject, computed } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import PrimeButton from 'primevue/button';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import Calendar from 'primevue/calendar';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { lksgDataModel } from '@/frameworks/lksg/UploadConfig';
import { type CompanyAssociatedDataLksgData, DataTypeEnum, type LksgData } from '@clients/backend';
import { useRoute } from 'vue-router';
import { checkCustomInputs } from '@/utils/ValidationUtils';
import NaceCodeFormField from '@/components/forms/parts/fields/NaceCodeFormField.vue';
import InputTextFormField from '@/components/forms/parts/fields/InputTextFormField.vue';
import FreeTextFormField from '@/components/forms/parts/fields/FreeTextFormField.vue';
import NumberFormField from '@/components/forms/parts/fields/NumberFormField.vue';
import DateFormField from '@/components/forms/parts/fields/DateFormField.vue';
import SingleSelectFormField from '@/components/forms/parts/fields/SingleSelectFormField.vue';
import MultiSelectFormField from '@/components/forms/parts/fields/MultiSelectFormField.vue';
import AddressFormField from '@/components/forms/parts/fields/AddressFormField.vue';
import RadioButtonsFormField from '@/components/forms/parts/fields/RadioButtonsFormField.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import YesNoNaFormField from '@/components/forms/parts/fields/YesNoNaFormField.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import LksgSubcontractingCompaniesFormField from '@/components/forms/parts/fields/LksgSubcontractingCompaniesFormField.vue';
import { objectDropNull } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, uploadFiles } from '@/utils/FileUploadUtils';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import { type Subcategory } from '@/utils/GenericFrameworkTypes';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import AmountWithCurrencyFormField from '@/components/forms/parts/fields/AmountWithCurrencyFormField.vue';
import BigDecimalBaseDataPointFormField from '@/components/forms/parts/fields/BigDecimalBaseDataPointFormField.vue';
import RiskAssessmentsFormField from '@/components/forms/parts/fields/RiskAssessmentsFormField.vue';
import GeneralViolationsAssessmentsFormField from '@/components/forms/parts/fields/GeneralViolationsAssessmentsFormField.vue';
import GrievanceMechanismAssessmentsFormField from '@/components/forms/parts/fields/GrievanceMechanismAssessmentsFormField.vue';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateLksgDataset',
  components: {
    SubmitButton,
    SubmitSideBar,
    UploadFormHeader,
    SuccessMessage,
    FailMessage,
    FormKit,
    Card,
    PrimeButton,
    Calendar,
    InputTextFormField,
    FreeTextFormField,
    NumberFormField,
    DateFormField,
    SingleSelectFormField,
    MultiSelectFormField,
    NaceCodeFormField,
    AddressFormField,
    RadioButtonsFormField,
    PercentageFormField,
    ProductionSitesFormField,
    RiskAssessmentsFormField,
    GeneralViolationsAssessmentsFormField,
    GrievanceMechanismAssessmentsFormField,
    MostImportantProductsFormField,
    ProcurementCategoriesFormField,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyDataPointFormField,
    AmountWithCurrencyFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
    BigDecimalBaseDataPointFormField,
    LksgSubcontractingCompaniesFormField,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createLkSGForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedLksgData: {} as CompanyAssociatedDataLksgData,
      lksgDataModel,
      route: useRoute(),
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postLkSGDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      fieldSpecificDocuments: new Map() as Map<string, DocumentToUpload>,
      listOfFilledKpis: [] as Array<string>,
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedLksgData.data?.general?.masterData?.dataDate;
        if (currentDate === undefined) {
          return '';
        } else {
          return currentDate.split('-')[0];
        }
      },
      set() {
        // IGNORED
      },
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.lksgDataModel, this.companyAssociatedLksgData.data);
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  created() {
    const dataId = this.route.query.templateDataId;
    if (dataId && typeof dataId === 'string') {
      void this.loadLKSGData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Builds an api to get and upload Lksg data
     * @returns the api
     */
    buildLksgDataApi(): PublicFrameworkDataApi<LksgData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.Lksg);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      } else return undefined;
    },

    /**
     * Loads the LkSG-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadLKSGData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const lksgDataControllerApi = this.buildLksgDataApi();
      const dataResponse = await lksgDataControllerApi!.getFrameworkData(dataId);
      const lksgResponseData = dataResponse.data;
      this.listOfFilledKpis = getFilledKpis(lksgResponseData.data);
      this.companyAssociatedLksgData = objectDropNull(lksgResponseData);
      this.waitingForData = false;
    },
    /**
     * Sends data to add LkSG data
     */
    async postLkSGData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.size > 0) {
          await uploadFiles(Array.from(this.fieldSpecificDocuments.values()), assertDefined(this.getKeycloakPromise));
        }
        const lksgDataControllerApi = this.buildLksgDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedLksgData.companyId,
          this.getKeycloakPromise
        );

        await lksgDataControllerApi!.postFrameworkData(this.companyAssociatedLksgData, isCompanyOwnerOrDataUploader);

        this.$emit('datasetCreated');
        this.dataDate = undefined;
        this.message = 'Upload successfully executed.';
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if ((error as Error).message) {
          this.message = formatAxiosErrorMessage(error as Error);
        } else {
          this.message =
            'An unexpected error occurred. Please try again or contact the support team if the issue persists.';
        }
        this.uploadSucceded = false;
      } finally {
        this.postLkSGDataProcessed = true;
      }
    },
    /**
     * updates the list of certificates that were uploaded in the corresponding formfields on change
     * @param fieldName the name of the formfield as a key
     * @param document the certificate as combined object of reference id and file content
     */
    updateDocumentList(fieldName: string, document: DocumentToUpload) {
      if (document) {
        this.fieldSpecificDocuments.set(fieldName, document);
      } else {
        this.fieldSpecificDocuments.delete(fieldName);
      }
    },
  },
  provide() {
    return {
      selectedProcurementCategories: computed(() => {
        return this.companyAssociatedLksgData.data?.general?.productionSpecificOwnOperations?.procurementCategories;
      }),
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
