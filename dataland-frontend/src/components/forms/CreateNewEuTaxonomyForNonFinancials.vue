<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>New Dataset - New Eu Taxonomy For Non Financials</template>
    <template #content>
      <div v-if="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading New Eu Taxonomy For Non Financials data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-else class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedNewEuTaxonomyDataForNonFinancials"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postNewEuTaxonomyForNonFinancialsData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" disabled="true" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" disabled="true" />

            <FormKit type="group" name="data" label="data">
              <div
                v-for="category in euTaxonomyForNonFinancialsDataModel"
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
                      <SubcategoryToggleWrapper :subcategoryName="subcategory.name">
                        <FormKit v-for="field in subcategory.fields" :key="field" type="group" :name="subcategory.name">
                          <component
                            v-if="field.showIf(companyAssociatedNewEuTaxonomyDataForNonFinancials.data)"
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
                      </SubcategoryToggleWrapper>
                    </div>
                  </template>
                </div>
              </div>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postNewEuTaxonomyForNonFinancialsDataProcessed">
            <SuccessMessage v-if="uploadSucceded" :messageId="messageCounter" />
            <FailMessage v-else :message="message" :messageId="messageCounter" />
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
import { AxiosError } from "axios";
import { type CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials, type CompanyReport } from "@clients/backend";
import { useRoute } from "vue-router";
import { checkCustomInputs } from "@/utils/ValidationsUtils";
import NaceCodeFormField from "@/components/forms/parts/fields/NaceCodeFormField.vue";
import InputTextFormField from "@/components/forms/parts/fields/InputTextFormField.vue";
import FreeTextFormField from "@/components/forms/parts/fields/FreeTextFormField.vue";
import NumberFormField from "@/components/forms/parts/fields/NumberFormField.vue";
import SubcategoryToggleWrapper from "@/components/forms/parts/kpiSelection/SubcategoryToggleWrapper.vue";
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
export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateNewEuTaxonomyForNonFinancials",
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
    SubcategoryToggleWrapper,
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
      formId: "createNewEuTaxonomyForNonFinancialsForm",
      waitingForData: true,
      dataDate: undefined as Date | undefined,
      companyAssociatedNewEuTaxonomyDataForNonFinancials: {} as CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials,
      euTaxonomyForNonFinancialsDataModel,
      route: useRoute(),
      message: "",
      smoothScroll: smoothScroll,
      uploadSucceded: false,
      postNewEuTaxonomyForNonFinancialsDataProcessed: false,
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
        const currentDate = this.companyAssociatedNewEuTaxonomyDataForNonFinancials.data?.general?.fiscalYearEnd;
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
      return createSubcategoryVisibilityMap(
        this.euTaxonomyForNonFinancialsDataModel,
        this.companyAssociatedNewEuTaxonomyDataForNonFinancials.data,
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
    if (dataId && typeof dataId === "string") {
      void this.loadNewEuTaxonomyForNonFinancialsData(dataId);
    } else {
      this.waitingForData = false;
    }
  },
  methods: {
    /**
     * Loads the NewEuTaxonomyForNonFinancials-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async loadNewEuTaxonomyForNonFinancialsData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const newEuTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)(),
      ).getNewEutaxonomyDataForNonFinancialsControllerApi();

      const dataResponse =
        await newEuTaxonomyForNonFinancialsDataControllerApi.getCompanyAssociatedNewEuTaxonomyDataForNonFinancials(
          dataId,
        );
      const newEuTaxonomyForNonFinancialsResponseData = dataResponse.data;
      this.referencedReportsForPrefill =
        newEuTaxonomyForNonFinancialsResponseData.data.general?.referencedReports ?? {};
      this.companyAssociatedNewEuTaxonomyDataForNonFinancials = objectDropNull(
        newEuTaxonomyForNonFinancialsResponseData as ObjectType,
      ) as CompanyAssociatedDataNewEuTaxonomyDataForNonFinancials;

      this.waitingForData = false;
    },
    /**
     * Sends data to add NewEuTaxonomyForNonFinancials data
     */
    async postNewEuTaxonomyForNonFinancialsData(): Promise<void> {
      this.messageCounter++;
      try {
        if (this.documents.size > 0) {
          await uploadFiles(Array.from(this.documents.values()), assertDefined(this.getKeycloakPromise));
        }

        const newEuTaxonomyForNonFinancialsDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)(),
        ).getNewEutaxonomyDataForNonFinancialsControllerApi();
        await newEuTaxonomyForNonFinancialsDataControllerApi.postCompanyAssociatedNewEuTaxonomyDataForNonFinancials(
          this.companyAssociatedNewEuTaxonomyDataForNonFinancials,
        );
        this.$emit("datasetCreated");
        this.dataDate = undefined;
        this.message = "Upload successfully executed.";
        this.uploadSucceded = true;
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
        this.postNewEuTaxonomyForNonFinancialsDataProcessed = true;
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
