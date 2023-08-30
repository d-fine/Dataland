<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title
      ><span data-test="pageWrapperTitle">
        {{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Non-Financial Company/Service</span
      ></template
    >
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading Eu Taxonomy For Non Financials data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedEuTaxonomyDataForNonFinancials"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postEuTaxonomyForNonFinancialsData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" disabled="true" />
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
                <div class="lg:col-4 md:col-6 col-12 pl-0">
                  <Calendar
                    data-test="reportingPeriod"
                    v-model="reportingPeriod"
                    inputId="icon"
                    :showIcon="true"
                    view="year"
                    dateFormat="yy"
                    validation="required"
                  />
                </div>

                <FormKit type="hidden" :modelValue="reportingPeriodYear" name="reportingPeriod" />
              </div>
            </div>

            <FormKit type="group" name="data" label="data">
              <div
                v-for="category in euTaxonomyForNonFinancialsDataModel"
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
                    <!--//TODO do we need the field.fields -->
                    <div class="col-9 formFields">
                      <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                          <component
                            v-if="field.showIf(companyAssociatedEuTaxonomyDataForNonFinancials.data)"
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
              </div>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postEuTaxonomyForNonFinancialsDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else data-test="failedUploadMessage" :message="message" :messageId="messageCounter" />
          </div>

          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li v-for="category in euTaxonomyForNonFinancialsDataModel" :key="category">
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
import { euTaxonomyForNonFinancialsDataModel } from "@/components/resources/frameworkDataSearch/euTaxonomy/EuTaxonomyForNonFinancialsDataModel.ts";
import { type CompanyAssociatedDataEuTaxonomyDataForNonFinancials, type CompanyReport } from "@clients/backend";
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
import FinancialShareFormField from "@/components/forms/parts/kpiSelection/FinancialShareFormField.vue";
import AlignedActivitiesFormField from "@/components/forms/parts/kpiSelection/AlignedActivitiesFormField.vue";
import NonAlignedActivitiesFormField from "@/components/forms/parts/kpiSelection/NonAlignedActivitiesFormField.vue";
import AssuranceFormField from "@/components/forms/parts/kpiSelection/AssuranceFormField.vue";
import PercentageFormField from "@/components/forms/parts/fields/PercentageFormField.vue";
import InputSwitch from "primevue/inputswitch";
import { objectDropNull, type ObjectType } from "@/utils/UpdateObjectUtils";
import { smoothScroll } from "@/utils/SmoothScroll";
import { type DocumentToUpload, uploadFiles } from "@/utils/FileUploadUtils";
import { type Subcategory } from "@/utils/GenericFrameworkTypes";
import { createSubcategoryVisibilityMap } from "@/utils/UploadFormUtils";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";
export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateEuTaxonomyForNonFinancials",
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
    InputSwitch,
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
    UploadReports,
    FinancialShareFormField,
    AlignedActivitiesFormField,
    AssuranceFormField,
    NonAlignedActivitiesFormField,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createEuTaxonomyForNonFinancialsForm",
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedEuTaxonomyDataForNonFinancials: {} as CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
      euTaxonomyForNonFinancialsDataModel,
      route: useRoute(),
      message: "",
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postEuTaxonomyForNonFinancialsDataProcessed: false,
      messageCounter: 0,
      checkCustomInputs,
      documents: new Map() as Map<string, DocumentToUpload>,
      referencedReportsForPrefill: {} as { [key: string]: CompanyReport },
      namesOfAllCompanyReportsForTheDataset: [] as string[],
      reportingPeriod: undefined as undefined | Date,
      editMode: false,
    };
  },
  computed: {
    reportingPeriodYear(): number {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear();
      }
      return 0;
    },
    subcategoryVisibility(): Map<Subcategory, boolean> {
      return createSubcategoryVisibilityMap(
        this.euTaxonomyForNonFinancialsDataModel,
        this.companyAssociatedEuTaxonomyDataForNonFinancials.data,
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
    if (dataId && typeof dataId === "string" && dataId !== "") {
      this.editMode = true;
      void this.loadEuTaxonomyForNonFinancialsData(dataId);
    } else {
      this.waitingForData = false;
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }
  },
  methods: {
    /**
     * Loads the EuTaxonomyForNonFinancials-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadEuTaxonomyForNonFinancialsData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const euTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getEuTaxonomyDataForNonFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId);
      const euTaxonomyForNonFinancialsResponseData = dataResponse.data;
      if (euTaxonomyForNonFinancialsResponseData?.reportingPeriod) {
        this.reportingPeriod = new Date(euTaxonomyForNonFinancialsResponseData.reportingPeriod);
      }
      this.referencedReportsForPrefill = euTaxonomyForNonFinancialsResponseData.data.general?.referencedReports ?? {};
      this.companyAssociatedEuTaxonomyDataForNonFinancials = objectDropNull(
        euTaxonomyForNonFinancialsResponseData as ObjectType,
      ) as CompanyAssociatedDataEuTaxonomyDataForNonFinancials;

      this.waitingForData = false;
    },
    /**
     * Sends data to add EuTaxonomyForNonFinancials data
     */
    async postEuTaxonomyForNonFinancialsData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documents.size > 0) {
          checkIfAllUploadedReportsAreReferencedInDataModel(
            this.companyAssociatedEuTaxonomyDataForNonFinancials.data as ObjectType,
            this.namesOfAllCompanyReportsForTheDataset,
          );

          await uploadFiles(Array.from(this.documents.values()), assertDefined(this.getKeycloakPromise));
        }

        const euTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        await euTaxonomyForNonFinancialsDataControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
          this.companyAssociatedEuTaxonomyDataForNonFinancials,
        );
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
        this.postEuTaxonomyForNonFinancialsDataProcessed = true;
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
