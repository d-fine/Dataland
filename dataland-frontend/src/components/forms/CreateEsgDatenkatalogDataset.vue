<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - ESG Datenkatalog für Großunternehmen</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading ESG Datenkatalog data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedEsgDatenkatalogData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postEsgDatenkatalogData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in esgDatenkatalogDataModel"
                :key="category.name"
                :label="category.label"
                :name="category.name"
              >
                <div class="" v-for="subcategory in category.subcategories" :key="subcategory.name">
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="uploadFormSection grid">
                      <div class="col-3 p-3 topicLabel">
                        <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                        <div :class="`p-badge badge-${category.color}`">
                          <span>{{ category.label.toUpperCase() }}</span>
                        </div>
                      </div>

                      <div class="col-9 formFields">
                        <FormKit
                          v-for="field in subcategory.fields"
                          :key="field.name"
                          type="group"
                          :name="subcategory.name"
                        >
                          <component
                            v-if="field.showIf(companyAssociatedEsgDatenkatalogData.data)"
                            :is="field.component"
                            :label="field.label"
                            :placeholder="field.placeholder"
                            :description="field.description"
                            :name="field.name"
                            :options="field.options"
                            :required="field.required"
                            :validation="field.validation"
                            :validation-label="field.validationLabel"
                            :validationMessages="getValidationMessageForFirstQuestion(field)"
                            :reportingPeriod="yearOfDataDate"
                            :data-test="field.name"
                            :unit="field.unit"
                            :shouldDisableCheckboxes="true"
                            @field-specific-documents-updated="
                              updateDocumentList(`${category.name}.${subcategory.name}.${field.name}`, $event)
                            "
                            :ref="field.name"
                          />
                        </FormKit>
                      </div>
                    </div>
                  </template>
                </div>
              </FormKit>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postEsgDatenkatalogDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in esgDatenkatalogDataModel" :key="category.name">
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
import { computed, defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import Tooltip from 'primevue/tooltip';
import PrimeButton from 'primevue/button';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import YesNoFormField from '@/components/forms/parts/fields/YesNoFormField.vue';
import Calendar from 'primevue/calendar';
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import FailMessage from '@/components/messages/FailMessage.vue';
import {
  type CompanyAssociatedDataEsgDatenkatalogData,
  DataTypeEnum,
  type EsgDatenkatalogData,
} from '@clients/backend';
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
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import PercentageFormField from '@/components/forms/parts/fields/PercentageFormField.vue';
import ProductionSitesFormField from '@/components/forms/parts/fields/ProductionSitesFormField.vue';
import { objectDropNull } from '@/utils/UpdateObjectUtils';
import { smoothScroll } from '@/utils/SmoothScroll';
import { type DocumentToUpload, uploadFiles } from '@/utils/FileUploadUtils';
import MostImportantProductsFormField from '@/components/forms/parts/fields/MostImportantProductsFormField.vue';
import { type Field, type Subcategory } from '@/utils/GenericFrameworkTypes';
import ProcurementCategoriesFormField from '@/components/forms/parts/fields/ProcurementCategoriesFormField.vue';
import { createSubcategoryVisibilityMap } from '@/utils/UploadFormUtils';
import HighImpactClimateSectorsFormField from '@/components/forms/parts/fields/HighImpactClimateSectorsFormField.vue';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import YesNoExtendedDataPointFormField from '@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue';
import YesNoBaseDataPointFormField from '@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue';
import YesNoNaBaseDataPointFormField from '@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue';
import EsgDatenkatalogYearlyDecimalTimeseriesThreeYearDeltaDataFormField from '@/components/forms/parts/fields/EsgDatenkatalogYearlyDecimalTimeseriesThreeYearDeltaDataFormField.vue';
import EsgDatenkatalogYearlyDecimalTimeseriesThreeYearPastDataFormField from '@/components/forms/parts/fields/EsgDatenkatalogYearlyDecimalTimeseriesThreeYearPastDataFormField.vue';
import { esgDatenkatalogDataModel } from '@/frameworks/esg-datenkatalog/UploadConfig';
import ListOfBaseDataPointsFormField from '@/components/forms/parts/fields/ListOfBaseDataPointsFormField.vue';
import { getFilledKpis } from '@/utils/DataPoint';
import { getBasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkRegistry';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateEsgDatenkatalogDataset',
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
    MostImportantProductsFormField,
    ProcurementCategoriesFormField,
    UploadReports,
    HighImpactClimateSectorsFormField,
    IntegerExtendedDataPointFormField,
    BigDecimalExtendedDataPointFormField,
    CurrencyDataPointFormField,
    YesNoFormField,
    YesNoNaFormField,
    YesNoBaseDataPointFormField,
    YesNoNaBaseDataPointFormField,
    YesNoExtendedDataPointFormField,
    EsgDatenkatalogYearlyDecimalTimeseriesThreeYearDeltaDataFormField,
    EsgDatenkatalogYearlyDecimalTimeseriesThreeYearPastDataFormField,
    ListOfBaseDataPointsFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      formId: 'createEsgDatenkatalogForm',
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedEsgDatenkatalogData: {} as CompanyAssociatedDataEsgDatenkatalogData,
      esgDatenkatalogDataModel,
      route: useRoute(),
      message: '',
      listOfFilledKpis: [] as Array<string>,
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postEsgDatenkatalogDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      fieldSpecificDocuments: new Map() as Map<string, DocumentToUpload>,
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedEsgDatenkatalogData.data?.general?.masterData?.gueltigkeitsDatum;
        if (currentDate === undefined) {
          return '';
        } else {
          const currentDateSegments = currentDate!.split('-');
          return currentDateSegments[0] ?? new Date().getFullYear();
        }
      },
      set() {
        // IGNORED
      },
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(
        this.esgDatenkatalogDataModel,
        this.companyAssociatedEsgDatenkatalogData.data
      );
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
      void this.loadEsgDatenkatalogData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Builds an api to get and upload esg datenkatalog data
     * @returns the api
     */
    buildEsgDatenkatalogDataApi(): PublicFrameworkDataApi<EsgDatenkatalogData> | undefined {
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getBasePublicFrameworkDefinition(DataTypeEnum.EsgDatenkatalog);
      if (frameworkDefinition) {
        return frameworkDefinition.getPublicFrameworkApiClient(
          undefined,
          apiClientProvider.axiosInstance
        ) as PublicFrameworkDataApi<EsgDatenkatalogData>;
      } else return undefined;
    },

    /**
     * Loads the EsgDatenkatalog-Dataset identified by the provided dataId and pre-configures the form to contain
     * the data from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadEsgDatenkatalogData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const esgDatenkatalogDataControllerApi = this.buildEsgDatenkatalogDataApi();
      const dataResponse = await assertDefined(esgDatenkatalogDataControllerApi).getFrameworkData(dataId);
      const esgDatenkatalogResponseData = dataResponse.data;
      this.listOfFilledKpis = getFilledKpis(esgDatenkatalogResponseData.data);
      this.companyAssociatedEsgDatenkatalogData = objectDropNull(esgDatenkatalogResponseData);
      this.waitingForData = false;
    },
    /**
     * Sends data to add esg datenkatalog data
     */
    async postEsgDatenkatalogData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.fieldSpecificDocuments.size > 0) {
          await uploadFiles(Array.from(this.fieldSpecificDocuments.values()), assertDefined(this.getKeycloakPromise));
        }
        const esgDatenkatalogDataControllerApi = this.buildEsgDatenkatalogDataApi();

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.companyAssociatedEsgDatenkatalogData.companyId,
          this.getKeycloakPromise
        );

        await assertDefined(esgDatenkatalogDataControllerApi).postFrameworkData(
          this.companyAssociatedEsgDatenkatalogData,
          isCompanyOwnerOrDataUploader
        );

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
        this.postEsgDatenkatalogDataProcessed = true;
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
    /**
     * If the passed field is the first field of the esg datenkatalog frameworks first category and subcategory, a custom
     * validation message is returned for the "is"-validation for that field.
     * @param field that potentially could be the first field of the esg datenkatalog framework
     * @returns an object expected by FormKit in order to customize the validation message of a field
     */
    getValidationMessageForFirstQuestion(field: Field): { is: string } | undefined {
      if (field.name === esgDatenkatalogDataModel[0].subcategories[0].fields[0].name) {
        return { is: 'Sie müssen "Ja" wählen, um den Datensatz abschicken zu können.' };
      }
    },
  },
  provide() {
    return {
      listOfFilledKpis: computed(() => {
        return this.listOfFilledKpis;
      }),
    };
  },
});
</script>
