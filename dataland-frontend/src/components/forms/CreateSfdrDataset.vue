<template>
  <Card class="col-12 page-wrapper-card p-3" style="background: var(--p-surface-50)">
    <template #title>New Dataset - SFDR</template>
    <template #content>
      <div class="separator" />
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedSfdrData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postSfdrData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <div class="uploadFormSection grid">
              <div class="col-3 p-3 topicLabel">
                <h4 id="reportingPeriod" class="anchor title">Reporting Period</h4>
              </div>
              <div class="col-9 form-field formFields uploaded-files">
                <UploadFormHeader
                  :label="'Reporting Period'"
                  :description="'The reporting period the dataset belongs to (e.g. a fiscal year).'"
                  :is-required="true"
                />
                <DatePicker
                  data-test="reportingPeriod"
                  v-model="reportingPeriod"
                  :updateModelType="'date'"
                  inputId="icon"
                  :showIcon="true"
                  view="year"
                  dateFormat="yy"
                  validation="required"
                />

                <FormKit type="hidden" :modelValue="reportingPeriodYear.toString()" name="reportingPeriod" />
              </div>
            </div>

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in sfdrDataModel"
                :key="category.name"
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
                      <Tag :value="category.label.toUpperCase()" severity="secondary" />
                    </div>

                    <div class="col-9 formFields">
                      <FormKit
                        v-for="field in subcategory.fields"
                        :key="field.name"
                        type="group"
                        :name="subcategory.name"
                      >
                        <component
                          v-if="field.showIf(companyAssociatedSfdrData.data)"
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
                          :unit="field.unit"
                          @reports-updated="updateDocumentsList"
                          @field-specific-documents-updated="
                            updateDocumentsOnField(`${category.name}.${subcategory.name}.${field.name}`, $event)
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
        <SubmitSideBar class="jumpLinks">
          <SubmitButton :formId="formId" />
          <div v-if="postSfdrDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in sfdrDataModel" :key="category.name">
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
import { FormKit } from '@formkit/vue';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import { defineComponent, inject, computed } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import Tooltip from 'primevue/tooltip';
import PrimeButton from 'primevue/button';
import Tag from 'primevue/tag';
import { type DocumentMetaInfo, DocumentMetaInfoDocumentCategoryEnum } from '@clients/documentmanager';
import DatePicker from 'primevue/datepicker';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import { sfdrDataModel } from '@/frameworks/sfdr/UploadConfig';
import { type CompanyAssociatedDataSfdrData, type CompanyReport, DataTypeEnum, type SfdrData } from '@clients/backend';
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from '@/utils/ValidationUtils';
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
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import { type ObjectType } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, getAvailableFileNames, uploadFiles } from '@/utils/FileUploadUtils';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import { type Subcategory } from '@/utils/GenericFrameworkTypes';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyExtendedDataPointFormField from '@/components/forms/parts/fields/CurrencyExtendedDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import BaseDataPointFormField from '@/components/forms/parts/elements/basic/BaseDataPointFormField.vue';
import { removeInferableDocumentFields } from '@/utils/DataPoint';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';
import DateExtendedDataPointFormField from '@/components/forms/parts/fields/DateExtendedDataPointFormField.vue';
import RadioButtonsExtendedDataPointFormField from '@/components/forms/parts/fields/RadioButtonsExtendedDataPointFormField.vue';

const referenceableReportsFieldId = 'referenceableReports';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateSfdrDataset',
  components: {
    BaseDataPointFormField,
    SubmitButton,
    SubmitSideBar,
    UploadFormHeader,
    DatePicker,
    SuccessMessage,
    FailMessage,
    FormKit,
    Card,
    Tag,
    PrimeButton,
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
    MostImportantProductsFormField,
    ProcurementCategoriesFormField,
    UploadReports,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyExtendedDataPointFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
    DateExtendedDataPointFormField,
    RadioButtonsExtendedDataPointFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createSFDRForm',
      reportingPeriod: undefined as undefined | Date,
      companyAssociatedSfdrData: {} as CompanyAssociatedDataSfdrData,
      sfdrDataModel,
      message: '',
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postSfdrDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      climateSectorsForPrefill: [] as Array<string>,
      listOfFilledKpis: [] as Array<string>,
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      fieldSpecificDocuments: new Map<string, DocumentToUpload[]>(),
    };
  },
  computed: {
    reportingPeriodYear(): number {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear();
      }
      return 0;
    },
    namesOfAllCompanyReportsForTheDataset(): string[] {
      return getAvailableFileNames(this.namesAndReferencesOfAllCompanyReportsForTheDataset);
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.sfdrDataModel, this.companyAssociatedSfdrData.data);
    },
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  created() {
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }
  },
  methods: {
    /**
     * Builds an api to get and upload Sfdr data
     * @returns the api
     */
    buildSfdrDataApi(): PublicFrameworkDataApi<SfdrData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.Sfdr);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
      } else return undefined;
    },

    /**
     * Sends data to add SFDR data
     */
    async postSfdrData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.get(referenceableReportsFieldId)?.length) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedSfdrData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset,
            this.namesAndReferencesOfAllCompanyReportsForTheDataset as ObjectType
          );
        }
        const documentsToUpload = Array.from(this.fieldSpecificDocuments.values()).flat();
        const referencedReports = this.companyAssociatedSfdrData.data?.general?.general?.referencedReports;
        const documentMetaInfoByReference = new Map<string, DocumentMetaInfo>(
          documentsToUpload.map((documentToUpload) => [
            documentToUpload.fileReference,
            {
              documentName: documentToUpload.fileNameWithoutSuffix,
              documentCategory: DocumentMetaInfoDocumentCategoryEnum.Other,
              companyIds: [this.companyID] as unknown as Set<string>,
              publicationDate:
                referencedReports?.[documentToUpload.fileNameWithoutSuffix]?.publicationDate ?? undefined,
              reportingPeriod: this.reportingPeriodYear.toString(),
            },
          ])
        );
        await uploadFiles(documentsToUpload, assertDefined(this.getKeycloakPromise), documentMetaInfoByReference);

        const sfdrDataControllerApi = this.buildSfdrDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedSfdrData.companyId,
          this.getKeycloakPromise
        );

        await sfdrDataControllerApi!.postFrameworkData(
          removeInferableDocumentFields(this.companyAssociatedSfdrData),
          isCompanyOwnerOrDataUploader
        );

        this.$emit('datasetCreated');
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
        this.postSfdrDataProcessed = true;
      }
    },
    /**
     * updates the list of documents that were uploaded
     * @param reportsNamesAndReferences repots names and references
     * @param reportsToUpload reports to upload
     */
    updateDocumentsList(reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]) {
      this.namesAndReferencesOfAllCompanyReportsForTheDataset = reportsNamesAndReferences;
      if (reportsToUpload.length) {
        this.fieldSpecificDocuments.set(referenceableReportsFieldId, reportsToUpload);
      } else {
        this.fieldSpecificDocuments.delete(referenceableReportsFieldId);
      }
    },
    /**
     * Updates the referenced document for a specific field
     * @param fieldId an identifier for the field
     * @param referencedDocument the documen that is referenced
     */
    updateDocumentsOnField(fieldId: string, referencedDocument: DocumentToUpload | undefined) {
      if (referencedDocument) {
        this.fieldSpecificDocuments.set(fieldId, [referencedDocument]);
      } else {
        this.fieldSpecificDocuments.delete(fieldId);
      }
    },
  },
  provide() {
    return {
      namesAndReferencesOfAllCompanyReportsForTheDataset: computed(() => {
        return this.namesAndReferencesOfAllCompanyReportsForTheDataset;
      }),
      referencedReportsForPrefill: computed(() => {
        return this.referencedReportsForPrefill;
      }),
      climateSectorsForPrefill: computed(() => {
        return this.climateSectorsForPrefill;
      }),
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
<style>
.jumpLinks {
  left: auto;
  right: 0;

  ul {
    margin: 0;
    padding: 0;

    li {
      list-style: none;
      margin: 0.5rem 0;

      a {
        color: var(--jumpLinks-color);
        text-decoration: none;

        &:hover {
          color: var(--jumpLinks-hover);
          cursor: pointer;
        }
      }
    }
  }
}

.separator {
  width: 100%;
  border-bottom: #e0dfde solid 1px;
  margin-top: 8px;
  margin-bottom: 24px;
}
</style>
