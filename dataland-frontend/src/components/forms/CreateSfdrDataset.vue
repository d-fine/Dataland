<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - SFDR</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading SFDR data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
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
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" />

            <FormKit type="group" name="data" label="data">
              <FormKit
                type="group"
                v-for="category in sfdrDataModel"
                :key="category"
                :label="category.label"
                :name="category.name"
              >
                <div class="uploadFormSection grid" v-for="subcategory in category.subcategories" :key="subcategory">
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
                          v-if="field.showIf(companyAssociatedSfdrData.data)"
                          :is="field.component"
                          :label="field.label"
                          :placeholder="field.placeholder"
                          :description="field.description"
                          :name="field.name"
                          :options="field.options"
                          :required="field.required"
                          :certificateRequiredIfYes="field.certificateRequiredIfYes"
                          :validation="field.validation"
                          :validation-label="field.validationLabel"
                          :evidenceDesired="field.evidenceDesired"
                          :data-test="field.name"
                          :unit="field.unit"
                          @reportsUpdated="updateDocumentsList"
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
          <div v-if="postSfdrDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in sfdrDataModel" :key="category">
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
import { sfdrDataModel } from "@/components/resources/frameworkDataSearch/sfdr/SfdrDataModel";
import { AxiosError } from "axios";
import { type CompanyAssociatedDataSfdrData, type CompanyReport, DataTypeEnum } from "@clients/backend";
import { useRoute } from "vue-router";
import { checkCustomInputs, checkIfAllUploadedReportsAreReferencedInDataModel } from "@/utils/ValidationsUtils";
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
import DataPointFormField from "@/components/forms/parts/kpiSelection/DataPointFormField.vue";
import PercentageFormField from "@/components/forms/parts/fields/PercentageFormField.vue";
import ProductionSitesFormField from "@/components/forms/parts/fields/ProductionSitesFormField.vue";
import { objectDropNull, type ObjectType } from "@/utils/UpdateObjectUtils";
import { smoothScroll } from "@/utils/SmoothScroll";
import { type DocumentToUpload, uploadFiles } from "@/utils/FileUploadUtils";
import MostImportantProductsFormField from "@/components/forms/parts/fields/MostImportantProductsFormField.vue";
import { type Subcategory } from "@/utils/GenericFrameworkTypes";
import ProcurementCategoriesFormField from "@/components/forms/parts/fields/ProcurementCategoriesFormField.vue";
import { createSubcategoryVisibilityMap } from "@/utils/UploadFormUtils";
import HighImpactClimateSectorsFormField from "@/components/forms/parts/fields/HighImpactClimateSectorsFormField.vue";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateSfdrDataset",
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
    YesNoFormField,
    InputTextFormField,
    FreeTextFormField,
    NumberFormField,
    DataPointFormField,
    DateFormField,
    SingleSelectFormField,
    MultiSelectFormField,
    NaceCodeFormField,
    AddressFormField,
    RadioButtonsFormField,
    YesNoNaFormField,
    PercentageFormField,
    ProductionSitesFormField,
    MostImportantProductsFormField,
    ProcurementCategoriesFormField,
    UploadReports,
    HighImpactClimateSectorsFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createSFDRForm",
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedSfdrData: {} as CompanyAssociatedDataSfdrData,
      sfdrDataModel,
      route: useRoute(),
      message: "",
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postSfdrDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      documents: new Map() as Map<string, DocumentToUpload>,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesOfAllCompanyReportsForTheDataset: [] as string[],
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        const currentDate = this.companyAssociatedSfdrData.data?.general?.general?.fiscalYearEnd;
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
    const dataId = this.route.query.templateDataId;
    if (dataId && typeof dataId === "string") {
      void this.loadSfdrData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Loads the SFDR-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadSfdrData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const sfdrDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getUnifiedFrameworkDataController(DataTypeEnum.Sfdr);

      const dataResponse = await sfdrDataControllerApi.getFrameworkData(dataId);
      const sfdrResponseData = dataResponse.data;
      this.referencedReportsForPrefill = sfdrResponseData.data.general.general.referencedReports ?? {};
      this.companyAssociatedSfdrData = objectDropNull(sfdrResponseData as ObjectType) as CompanyAssociatedDataSfdrData;

      this.waitingForData = false;
    },
    /**
     * Sends data to add SFDR data
     */
    async postSfdrData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documents.size > 0) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedSfdrData.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset,
          );
          await uploadFiles(Array.from(this.documents.values()), assertDefined(this.getKeycloakPromise));
        }

        const sfdrDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getUnifiedFrameworkDataController(DataTypeEnum.Sfdr);
        await sfdrDataControllerApi.postFrameworkData(this.companyAssociatedSfdrData);
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
        this.postSfdrDataProcessed = true;
      }
    },
    /**
     * updates the list of documents that were uploaded
     * @param reportsNames repots names
     * @param reportsToUpload reports to upload
     */
    updateDocumentsList(reportsNames: string[], reportsToUpload: DocumentToUpload[]) {
      this.namesOfAllCompanyReportsForTheDataset = reportsNames;
      this.documents = new Map();
      reportsToUpload.forEach((document) => this.documents.set(document.file.name, document));
    },
  },
  provide() {
    return {
      namesOfAllCompanyReportsForTheDataset: computed(() => {
        return this.namesOfAllCompanyReportsForTheDataset;
      }),
      referencedReportsForPrefill: computed(() => {
        return this.referencedReportsForPrefill;
      }),
    };
  },
});
</script>
