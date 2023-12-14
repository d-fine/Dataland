<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - GDV</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading GDV data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedGdvData"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postGdvData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in gdvDataModel"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="" v-for="subcategory in category.subcategories" :key="subcategory">
                  <template v-if="subcategoryVisibility.get(subcategory) ?? true">
                    <div class="uploadFormSection grid">
                      <div class="col-3 p-3 topicLabel">
                        <h4 :id="subcategory.name" class="anchor title">{{ subcategory.label }}</h4>
                        <div :class="`p-badge badge-${category.color}`">
                          <span>{{ category.label.toUpperCase() }}</span>
                        </div>
                      </div>

                      <div class="col-9 formFields">
                        <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                          <component
                            v-if="field.showIf(companyAssociatedGdvData.data)"
                            :is="field.component"
                            :label="field.label"
                            :placeholder="field.placeholder"
                            :description="field.description"
                            :name="field.name"
                            :options="field.options"
                            :required="field.required"
                            :validation="field.validation"
                            :validation-label="field.validationLabel"
                            :reportingPeriod="yearOfDataDate"
                            :data-test="field.name"
                            :unit="field.unit"
                            @reportsUpdated="updateDocumentsList"
                            @field-specific-documents-updated="
                              updateDocumentsOnField(`${category.name}.${subcategory.name}.${field.name}`, $event)
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
          <div v-if="postGdvDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in gdvDataModel" :key="category">
              <ul>
                <li v-for="subcategory in category.subcategories" :key="subcategory">
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
import { FormKit } from "@formkit/vue";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import { defineComponent, inject, computed } from "vue";
import type Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Tooltip from "primevue/tooltip";
import PrimeButton from "primevue/button";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";
import YesNoFormField from "@/components/forms/parts/fields/YesNoFormField.vue";
import Calendar from "primevue/calendar";
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import { type CompanyAssociatedDataGdvData, type CompanyReport, DataTypeEnum, type GdvData } from "@clients/backend";
import { useRoute } from "vue-router";
import { checkCustomInputs } from "@/utils/ValidationsUtils";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import DateFormField from "@/components/forms/parts/fields/DateFormField.vue";
import SingleSelectFormField from "@/components/forms/parts/fields/SingleSelectFormField.vue";
import MultiSelectFormField from "@/components/forms/parts/fields/MultiSelectFormField.vue";
import AddressFormField from "@/components/forms/parts/fields/AddressFormField.vue";
import RadioButtonsFormField from "@/components/forms/parts/fields/RadioButtonsFormField.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
import YesNoNaFormField from "@/components/forms/parts/fields/YesNoNaFormField.vue";
import UploadReports from "@/components/forms/parts/UploadReports.vue";
import PercentageFormField from "@/components/forms/parts/fields/PercentageFormField.vue";
import ProductionSitesFormField from "@/components/forms/parts/fields/ProductionSitesFormField.vue";
import { objectDropNull, type ObjectType } from "@/utils/UpdateObjectUtils";
import { smoothScroll } from "@/utils/SmoothScroll";
import { type DocumentToUpload, getFileName } from "@/utils/FileUploadUtils";
import MostImportantProductsFormField from "@/components/forms/parts/fields/MostImportantProductsFormField.vue";
import { type Subcategory } from "@/utils/GenericFrameworkTypes";
import ProcurementCategoriesFormField from "@/components/forms/parts/fields/ProcurementCategoriesFormField.vue";
import { createSubcategoryVisibilityMap } from "@/utils/UploadFormUtils";
import HighImpactClimateSectorsFormField from "@/components/forms/parts/fields/HighImpactClimateSectorsFormField.vue";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";
import IntegerExtendedDataPointFormField from "@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue";
import BigDecimalExtendedDataPointFormField from "@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue";
import CurrencyDataPointFormField from "@/components/forms/parts/fields/CurrencyDataPointFormField.vue";
import YesNoExtendedDataPointFormField from "@/components/forms/parts/fields/YesNoExtendedDataPointFormField.vue";
import YesNoBaseDataPointFormField from "@/components/forms/parts/fields/YesNoBaseDataPointFormField.vue";
import YesNoNaBaseDataPointFormField from "@/components/forms/parts/fields/YesNoNaBaseDataPointFormField.vue";
import GdvYearlyDecimalTimeseriesDataFormField from "@/components/forms/parts/fields/GdvYearlyDecimalTimeseriesDataFormField.vue";
import { gdvDataModel } from "@/frameworks/gdv/UploadConfig";
import ListOfBaseDataPointsFormField from "@/components/forms/parts/fields/ListOfBaseDataPointsFormField.vue";
import { type FrameworkDataApi } from "@/utils/api/UnifiedFrameworkDataApi";
import { getFrontendFrameworkDefinition } from "@/frameworks/FrontendFrameworkRegistry";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateGdvDataset",
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
    GdvYearlyDecimalTimeseriesDataFormField,
    ListOfBaseDataPointsFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createGDVForm",
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedGdvData: {} as CompanyAssociatedDataGdvData,
      gdvDataModel: gdvDataModel,
      route: useRoute(),
      message: "",
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postGdvDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      documents: new Map() as Map<string, DocumentToUpload>,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      climateSectorsForPrefill: [] as Array<string>,
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedGdvData.data?.general?.masterData?.gueltigkeitsDatum;
        if (currentDate === undefined) {
          return "";
        } else {
          const currentDateSegments = currentDate.split("-");
          return currentDateSegments[0] ?? new Date().getFullYear();
        }
      },
      set() {
        // IGNORED
      },
    },
    namesOfAllCompanyReportsForTheDataset(): string[] {
      return getFileName(this.namesAndReferencesOfAllCompanyReportsForTheDataset);
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(this.gdvDataModel, this.companyAssociatedGdvData.data);
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
    if (dataId && typeof dataId === "string") {
      void this.loadGdvData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Loads the GDV-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadGdvData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
      const frameworkDefinition = getFrontendFrameworkDefinition(DataTypeEnum.Gdv);
      let gdvDataControllerApi: FrameworkDataApi<GdvData>;
      if (frameworkDefinition) {
        gdvDataControllerApi = frameworkDefinition?.getFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
        const dataResponse = await gdvDataControllerApi.getFrameworkData(dataId);
        const gdvResponseData = dataResponse.data;
        this.companyAssociatedGdvData = objectDropNull(gdvResponseData as ObjectType) as CompanyAssociatedDataGdvData;
      }

      this.waitingForData = false;
    },
    /**
     * Sends data to add GDV data
     */
    async postGdvData(): Promise<void> {
      this.messageCounter++;
      try {
        const apiClientProvider = new ApiClientProvider(assertDefined(this.getKeycloakPromise)());
        const frameworkDefinition = getFrontendFrameworkDefinition(DataTypeEnum.Gdv);
        let gdvDataControllerApi: FrameworkDataApi<GdvData>;
        if (frameworkDefinition) {
          gdvDataControllerApi = frameworkDefinition.getFrameworkApiClient(undefined, apiClientProvider.axiosInstance);
          await gdvDataControllerApi.postFrameworkData(this.companyAssociatedGdvData);
        }
        this.$emit("datasetCreated");
        this.dataDate = undefined;
        this.message = "Upload successfully executed.";
        this.uploadSucceded = true;
      } catch (error) {
        console.error(error);
        if (error.message) {
          this.message = formatAxiosErrorMessage(error as Error);
        } else {
          this.message =
            "An unexpected error occurred. Please try again or contact the support team if the issue persists.";
        }
        this.uploadSucceded = false;
      } finally {
        this.postGdvDataProcessed = true;
      }
    },
    /**
     * updates the list of documents that were uploaded
     * @param reportsNamesAndReferences repots names and references
     * @param reportsToUpload reports to upload
     */
    updateDocumentsList(reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]) {
      this.namesAndReferencesOfAllCompanyReportsForTheDataset = reportsNamesAndReferences;
      this.documents = new Map();
      if (reportsToUpload?.length) {
        reportsToUpload.forEach((document) => this.documents.set(document.file.name, document));
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
    };
  },
});
</script>
