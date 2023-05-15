<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title
      ><span data-test="pageWrapperTitle"
        >{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Non-Financial Company/Service</span
      ></template
    >
    <template #content>
      <div v-if="waitingForData" class="inline-loading text-center">
        <p class="font-medium text-xl">Loading dataset to edit...</p>
        <i class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-show="!waitingForData" class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            :id="formId"
            @submit="postEuTaxonomyDataForNonFinancials"
            @submit-invalid="handleInvalidInput"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :modelValue="companyID"
              disabled="true"
            />
            <div class="uploadFormSection grid">
              <div class="col-3 p-3 topicLabel">
                <h4 id="reportingPeriod" class="anchor title">Reporting Period</h4>
              </div>
              <div class="col-9 formFields uploaded-files">
                <UploadFormHeader
                  :name="euTaxonomyKpiNameMappings.reportingPeriod"
                  :explanation="euTaxonomyKpiInfoMappings.reportingPeriod"
                />
                <div class="lg:col-6 md:col-6 col-12 p-0">
                  <Calendar
                    data-test="reportingPeriod"
                    v-model="reportingPeriod"
                    inputId="icon"
                    :showIcon="true"
                    view="year"
                    dateFormat="yy"
                  />
                </div>

                <FormKit type="hidden" :modelValue="reportingPeriodYear" name="reportingPeriod" />
              </div>
            </div>

            <div class="uploadFormSection grid">
              <FormKit type="group" name="data" label="data">
                <UploadReports
                  ref="UploadReports"
                  :referencedReportsForPrefill="templateDataset?.referencedReports"
                  @referenceableReportNamesChanged="handleChangeOfReferenceableReportNames"
                />

                <EuTaxonomyBasicInformation
                  :fiscalYearEndAsDate="fiscalYearEndAsDate"
                  :fiscalYearEnd="fiscalYearEnd"
                  @updateFiscalYearEndHandler="updateFiscalYearEndHandler"
                />

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="assurance" class="anchor title">Assurance</h4>
                  </div>

                  <!-- Level of assurance -->
                  <div data-test="assuranceSection" class="col-9 formFields">
                    <h3 class="mt-0">Assurance</h3>
                    <FormKit name="assurance" type="group">
                      <!-- Level of assurance -->
                      <div class="form-field">
                        <UploadFormHeader
                          :name="euTaxonomyKpiNameMappings.assurance ?? ''"
                          :explanation="euTaxonomyKpiInfoMappings.assurance ?? ''"
                          :is-required="true"
                        />
                        <div class="lg:col-4 md:col-6 col-12 p-0">
                          <FormKit
                            type="select"
                            name="assurance"
                            placeholder="Please choose..."
                            :validation-label="euTaxonomyKpiNameMappings.assurance ?? ''"
                            validation="required"
                            :options="assuranceData"
                          />
                        </div>
                      </div>
                      <!-- Assurance provider -->
                      <div class="form-field">
                        <UploadFormHeader
                          :name="euTaxonomyKpiNameMappings.provider ?? ''"
                          :explanation="euTaxonomyKpiInfoMappings.provider ?? ''"
                        />
                        <FormKit
                          type="text"
                          name="provider"
                          :placeholder="euTaxonomyKpiNameMappings.provider ?? ''"
                          :validation-label="euTaxonomyKpiNameMappings.provider ?? ''"
                        />
                      </div>

                      <!-- Data source -->
                      <div class="form-field">
                        <FormKit type="group" name="dataSource">
                          <h4 class="mt-0">Data source</h4>
                          <div class="next-to-each-other">
                            <div class="flex-1">
                              <UploadFormHeader
                                :name="euTaxonomyKpiNameMappings.report ?? ''"
                                :explanation="euTaxonomyKpiInfoMappings.report ?? ''"
                                :is-required="true"
                              />
                              <FormKit
                                type="select"
                                name="report"
                                placeholder="Select a report"
                                validation-label="Selecting a report"
                                validation="required"
                                :options="['None...', ...namesOfAllCompanyReportsForTheDataset]"
                              />
                            </div>
                            <div>
                              <UploadFormHeader
                                :name="euTaxonomyKpiNameMappings.page ?? ''"
                                :explanation="euTaxonomyKpiInfoMappings.page ?? ''"
                              />
                              <FormKit
                                outer-class="w-100"
                                type="number"
                                name="page"
                                placeholder="Page"
                                validation-label="Page"
                                validation="min:0"
                                step="1"
                                min="0"
                              />
                            </div>
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="capex" class="anchor title">CapEx</h4>
                  </div>

                  <!-- CapEx -->
                  <div data-test="capexSection" class="col-9 p-0">
                    <FormKit name="capex" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}CapEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                              :reportsName="namesOfAllCompanyReportsForTheDataset"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="opex" class="anchor title">OpEx</h4>
                  </div>

                  <!-- OpEx -->
                  <div data-test="opexSection" class="col-9 p-0">
                    <FormKit name="opex" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}OpEx`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                              :reportsName="namesOfAllCompanyReportsForTheDataset"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="revenue" class="anchor title">Revenue</h4>
                  </div>

                  <!-- Revenue -->
                  <div data-test="revenueSection" class="col-9 p-0">
                    <FormKit name="revenue" type="group">
                      <div
                        v-for="detailCashFlowType of euTaxonomyKPIsModel.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="euTaxonomyKPIsModel?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]"
                          type="group"
                        >
                          <div class="form-field">
                            <DataPointForm
                              :name="`${detailCashFlowType}Revenue`"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :valueType="detailCashFlowType === 'total' ? 'number' : 'percent'"
                              :reportsName="namesOfAllCompanyReportsForTheDataset"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </div>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <template v-if="postEuTaxonomyDataForNonFinancialsProcessed">
            <SuccessMessage
              v-if="postEuTaxonomyDataForNonFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailMessage v-else data-test="failedUploadMessage" :message="message" :messageId="messageCount" />
          </template>
          <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessMessage from "@/components/messages/SuccessMessage.vue";
import { FormKit } from "@formkit/vue";

import Calendar from "primevue/calendar";
import UploadFormHeader from "@/components/forms/parts/elements/basic/UploadFormHeader.vue";

import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
import FailMessage from "@/components/messages/FailMessage.vue";
import UploadReports from "@/components/forms/parts/UploadReports.vue";
import EuTaxonomyBasicInformation from "@/components/forms/parts/EuTaxonomyBasicInformation.vue";

import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeString } from "@/utils/StringHumanizer";
import { defineComponent, inject } from "vue";
import { useRoute } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";

import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyKPIsModel,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import {
  AssuranceDataAssuranceEnum,
  CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  DataMetaInformation,
  EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import { checkIfAllUploadedReportsAreReferencedInDataModel, checkCustomInputs } from "@/utils/validationsUtils";
import {
  convertValuesFromDecimalsToPercentages,
  convertValuesFromPercentagesToDecimals,
  ObjectType,
  updateObject,
} from "@/utils/updateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";
import { AxiosResponse } from "axios";
import DataPointForm from "@/components/forms/parts/kpiSelection/DataPointForm.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import { FormKitNode } from "@formkit/core";
import { formatAxiosErrorMessage } from "@/utils/AxiosErrorMessageFormatter";

export default defineComponent({
  name: "CreateEuTaxonomyForNonFinancials",
  components: {
    JumpLinksSection,
    SubmitButton,
    DataPointForm,
    Calendar,
    UploadFormHeader,
    UploadReports,
    EuTaxonomyBasicInformation,
    FailMessage,
    SubmitSideBar,
    Card,
    FormKit,
    SuccessMessage,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  emits: ["datasetCreated"],
  data: () => ({
    formId: "createEuTaxonomyForNonFinancialsForm",
    formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
    fiscalYearEndAsDate: null as Date | null,
    fiscalYearEnd: "",
    reportingPeriod: undefined as undefined | Date,
    onThisPageLinks: [
      { label: "Upload company reports", value: "uploadReports" },
      { label: "Basic information", value: "basicInformation" },
      { label: "Assurance", value: "assurance" },
      { label: "CapEx", value: "capex" },
      { label: "OpEx", value: "opex" },
      { label: "Revenue", value: "revenue" },
    ],
    route: useRoute(),
    editMode: false,
    waitingForData: false,
    formatBytesUserFriendly,
    checkCustomInputs,
    euTaxonomyKPIsModel,
    euTaxonomyKpiNameMappings,
    euTaxonomyKpiInfoMappings,
    assuranceData: {
      None: humanizeString(AssuranceDataAssuranceEnum.None),
      LimitedAssurance: humanizeString(AssuranceDataAssuranceEnum.LimitedAssurance),
      ReasonableAssurance: humanizeString(AssuranceDataAssuranceEnum.ReasonableAssurance),
    },

    postEuTaxonomyDataForNonFinancialsProcessed: false,
    messageCount: 0,
    postEuTaxonomyDataForNonFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
    humanizeString: humanizeString,
    message: "",
    namesOfAllCompanyReportsForTheDataset: [] as string[],
    templateDataset: undefined as undefined | EuTaxonomyDataForNonFinancials,
  }),
  computed: {
    reportingPeriodYear(): number {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear();
      }
      return 0;
    },
  },
  props: {
    companyID: {
      type: String,
    },
  },
  created() {
    const dataId = this.route.query.templateDataId;
    if (typeof dataId === "string" && dataId !== "") {
      this.editMode = true;
      void this.fetchTemplateData(dataId);
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }
  },

  methods: {
    /**
     * Loads the Dataset by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    async fetchTemplateData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getEuTaxonomyDataForNonFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId);
      const companyAssociatedEuTaxonomyData = dataResponse.data;
      if (companyAssociatedEuTaxonomyData.data?.fiscalYearEnd) {
        this.fiscalYearEndAsDate = new Date(companyAssociatedEuTaxonomyData.data.fiscalYearEnd);
      }
      if (companyAssociatedEuTaxonomyData?.reportingPeriod) {
        this.reportingPeriod = new Date(companyAssociatedEuTaxonomyData.reportingPeriod);
      }
      this.templateDataset = companyAssociatedEuTaxonomyData.data;
      const receivedFormInputsModel = convertValuesFromDecimalsToPercentages(
        companyAssociatedEuTaxonomyData as ObjectType
      );
      this.waitingForData = false;
      updateObject(this.formInputsModel, receivedFormInputsModel);
    },

    /**
     * Creates a new EuTaxonomy-Non-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForNonFinancials() {
      try {
        this.postEuTaxonomyDataForNonFinancialsProcessed = false;
        this.messageCount++;

        checkIfAllUploadedReportsAreReferencedInDataModel(
          this.formInputsModel.data as ObjectType,
          this.namesOfAllCompanyReportsForTheDataset
        );
        await (this.$refs.UploadReports.uploadFiles as () => Promise<void>)();

        const formInputsModelToSend = convertValuesFromPercentagesToDecimals(this.formInputsModel as ObjectType);
        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();
        this.postEuTaxonomyDataForNonFinancialsResponse =
          await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            formInputsModelToSend as CompanyAssociatedDataEuTaxonomyDataForNonFinancials
          );
        this.$emit("datasetCreated");
      } catch (error) {
        this.messageCount++;
        console.error(error);
        this.message = formatAxiosErrorMessage(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },

    /**
     * Updates the Fiscal Year End value
     * @param dateValue new date value
     */
    updateFiscalYearEndHandler(dateValue: Date) {
      this.fiscalYearEnd = getHyphenatedDate(dateValue);
      this.fiscalYearEndAsDate = dateValue;
    },

    /**
     * Handles invalid inputs and gives applicable error messages
     * @param node from which the input fields will be checked
     */
    handleInvalidInput(node: FormKitNode) {
      checkCustomInputs(node);
      this.message = `Sorry, not all fields are filled out correctly.`;
      this.postEuTaxonomyDataForNonFinancialsProcessed = true;
    },
    /**
     * Updates the local list of names of referenceable reports
     * @param reportNames new list of the referenceable reports' names
     */
    handleChangeOfReferenceableReportNames(reportNames: string[]) {
      this.namesOfAllCompanyReportsForTheDataset = reportNames;
    },
  },
});
</script>
