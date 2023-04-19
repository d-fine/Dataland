<template>
  <Card class="col-12 page-wrapper-card">
    <template #title
      ><span data-test="pageWrapperTitle"
        >{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Non-Financial Company/Service</span
      ></template
    >
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="CreateEuTaxonomyForNonFinancialsForm"
            @submit="postEuTaxonomyDataForNonFinancials"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :model-value="companyID"
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
                <div class="md:col-6 col-12 p-0">
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
                  :filesToUpload="filesToUpload"
                  :listOfUploadedReportsInfo="listOfUploadedReportsInfo"
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :editMode="editMode"
                  @selectedFiles="onSelectedFilesHandler"
                  @removeReportFromFilesToUpload="removeReportFromFilesToUpload"
                  @updateReportDateHandler="updateReportDateHandler"
                />

                <BasicInformationFields
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
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
                        />
                        <div class="lg:col-4 md:col-6 col-12 p-0">
                          <FormKit
                            type="select"
                            name="assurance"
                            placeholder="Please chose..."
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
                              />
                              <FormKit
                                type="select"
                                name="report"
                                placeholder="Select a report"
                                validation-label="Select a report"
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
                        v-for="detailCashFlowType of euTaxonomyPseudoModelAndMappings.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="
                            euTaxonomyPseudoModelAndMappings?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]
                          "
                          type="group"
                        >
                          <div class="form-field">
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}CapEx`] ?? "" }}
                            </h3>
                            <KPIfieldSet
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
                        v-for="detailCashFlowType of euTaxonomyPseudoModelAndMappings.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="
                            euTaxonomyPseudoModelAndMappings?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]
                          "
                          type="group"
                        >
                          <div class="form-field">
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}OpEx`] ?? "" }}
                            </h3>
                            <KPIfieldSet
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
                        v-for="detailCashFlowType of euTaxonomyPseudoModelAndMappings.euTaxonomyDetailsPerCashFlowType"
                        :key="detailCashFlowType"
                        :data-test="detailCashFlowType"
                        class="formFields"
                      >
                        <FormKit
                          :name="
                            euTaxonomyPseudoModelAndMappings?.euTaxonomyDetailsPerCashFlowFilesNames[detailCashFlowType]
                          "
                          type="group"
                        >
                          <div class="form-field">
                            <h3>
                              {{ euTaxonomyKpiNameMappings[`${detailCashFlowType}Revenue`] ?? "" }}
                            </h3>
                            <KPIfieldSet
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

              <!--------- SUBMIT --------->

              <div class="uploadFormSection grid">
                <div class="col-3"></div>

                <div class="col-9">
                  <PrimeButton data-test="submitButton" type="submit" label="SUBMIT FORM" />
                </div>
              </div>
            </div>
          </FormKit>
          <template v-if="postEuTaxonomyDataForNonFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForNonFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :message="`New data has dataId: ${postEuTaxonomyDataForNonFinancialsResponse.data.dataId}`"
              :messageId="messageCount"
            />
            <FailedUpload v-else msg="EU Taxonomy Data" :messageId="messageCount" />
          </template>
        </div>
        <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";

import Calendar from "primevue/calendar";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import PrimeButton from "primevue/button";

import KPIfieldSet from "@/components/forms/parts/kpiSelection/KPIfieldSet.vue";

import FailedUpload from "@/components/messages/FailedUpload.vue";
import UploadReports from "@/components/forms/parts/UploadReports.vue";
import BasicInformationFields from "@/components/forms/parts/BasicInformationFields.vue";

import Card from "primevue/card";
import { ApiClientProvider } from "@/services/ApiClients";
import { humanizeString } from "@/utils/StringHumanizer";
import { defineComponent, inject } from "vue";
import { useRoute } from "vue-router";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import {
  completeInformationAboutSelectedFileWithAdditionalFields,
  updatePropertyFilesUploaded,
} from "@/utils/EuTaxonomyUtils";

import {
  euTaxonomyPseudoModelAndMappings,
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyPseudoModelAndMappings";
import { CompanyAssociatedDataEuTaxonomyDataForNonFinancials } from "@clients/backend";
import { checkCustomInputs } from "@/utils/ValidationsUtils";
import { modifyObjectKeys, ObjectType, updateObject } from "@/utils/UpdateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { ExtendedCompanyReport, ExtendedFile, WhichSetOfFiles } from "@/components/forms/Types";
import { AssuranceDataAssuranceEnum } from "@clients/backend";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";

export default defineComponent({
  name: "CreateEuTaxonomyForNonFinancials",
  components: {
    JumpLinksSection,
    Calendar,
    UploadFormHeader,
    PrimeButton,
    UploadReports,
    BasicInformationFields,
    KPIfieldSet,
    FailedUpload,
    Card,
    FormKit,
    SuccessUpload,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  data: () => ({
    formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
    fiscalYearEndAsDate: null as Date | null,
    fiscalYearEnd: "",
    reportingPeriod: new Date(),
    filesToUpload: [] as ExtendedFile[],
    listOfUploadedReportsInfo: [] as ExtendedCompanyReport[],
    onThisPageLinks: [
      { label: "Upload company reports", value: "uploadReports" },
      { label: "Basic information", value: "basicInformation" },
      { label: "Assurance", value: "assurance" },
      { label: "CapEx", value: "capex" },
      { label: "OpEx", value: "opex" },
      { label: "Revenue", value: "revenue" },
    ],
    elementPosition: 0,
    route: useRoute(),
    editMode: false,
    waitingForData: false,
    checkCustomInputs,
    formatBytesUserFriendly,
    updatePropertyFilesUploaded,
    euTaxonomyPseudoModelAndMappings,
    euTaxonomyKpiNameMappings,
    euTaxonomyKpiInfoMappings,
    assuranceData: {
      None: humanizeString(AssuranceDataAssuranceEnum.None),
      LimitedAssurance: humanizeString(AssuranceDataAssuranceEnum.LimitedAssurance),
      ReasonableAssurance: humanizeString(AssuranceDataAssuranceEnum.ReasonableAssurance),
    },

    postEuTaxonomyDataForNonFinancialsProcessed: false,
    messageCount: 0,
    postEuTaxonomyDataForNonFinancialsResponse: null,
    humanizeString: humanizeString,
  }),
  computed: {
    namesOfAllCompanyReportsForTheDataset(): string[] {
      const namesFromFilesToUpload = this.filesToUpload.map((el) => el.name.split(".")[0]);
      const namesFromListOfUploadedReports = this.listOfUploadedReportsInfo.map((el) => el.name.split(".")[0]);
      return [...new Set([...namesFromFilesToUpload, ...namesFromListOfUploadedReports])];
    },
    reportingPeriodYear(): number {
      return this.reportingPeriod.getFullYear();
    },
  },
  props: {
    companyID: {
      type: String,
    },
  },
  mounted() {
    const dataId = this.route.query.templateDataId;
    if (typeof dataId === "string" && dataId !== "") {
      this.editMode = true;
      void this.loadEuData(dataId);
    }
  },

  methods: {
    /**
     * Loads the Dataset by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     *
     * @param dataId the id of the dataset to load
     */
    async loadEuData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getEuTaxonomyDataForNonFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyDataForNonFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId);
      const dataResponseData = dataResponse.data;
      if (dataResponseData.data?.fiscalYearEnd) {
        this.fiscalYearEndAsDate = new Date(dataResponseData.data.fiscalYearEnd);
      }
      if (dataResponseData?.reportingPeriod) {
        this.reportingPeriod = new Date(dataResponseData.reportingPeriod);
      }
      if (dataResponseData.data?.referencedReports) {
        const referencedReportsForDataId = dataResponseData.data.referencedReports;
        for (const key in referencedReportsForDataId) {
          this.listOfUploadedReportsInfo.push({
            name: key,
            reference: referencedReportsForDataId[key].reference,
            currency: referencedReportsForDataId[key].currency,
            reportDate: referencedReportsForDataId[key].reportDate,
            isGroupLevel: referencedReportsForDataId[key].isGroupLevel,
            reportDateAsDate: referencedReportsForDataId[key].reportDate
              ? new Date(referencedReportsForDataId[key].reportDate as string)
              : "",
          });
        }
      }
      const receivedFormInputsModel = modifyObjectKeys(
        JSON.parse(JSON.stringify(dataResponseData)) as ObjectType,
        "receive"
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
        let allFilesWasUploadedSuccessful = true;

        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();

        const documentUploadControllerControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentUploadController();

        if (this.filesToUpload.length) {
          for (let index = 0; index < this.filesToUpload.length; index++) {
            const uploadFileSuccessful = await documentUploadControllerControllerApi.postDocument(
              this.filesToUpload[index]
            );
            if (!uploadFileSuccessful) {
              allFilesWasUploadedSuccessful = false;
              break;
            } else if (uploadFileSuccessful) {
              this.updatePropertyFilesUploaded(
                index,
                "documentId",
                uploadFileSuccessful.data.documentId,
                this.filesToUpload
              );
            }
          }
        }

        if (allFilesWasUploadedSuccessful) {
          await this.$nextTick();
          const formInputsModelToSend = modifyObjectKeys(
            JSON.parse(JSON.stringify(this.formInputsModel)) as ObjectType,
            "send"
          );
          this.postEuTaxonomyDataForNonFinancialsResponse =
            await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
              formInputsModelToSend
            );
        }
        this.$formkit.reset("CreateEuTaxonomyForNonFinancialsForm");
        // eslint-disable-next-line @typescript-eslint/no-unsafe-call
        this.$refs.UploadReports.clearAllSelectedFiles();
        this.fiscalYearEndAsDate = null;
        this.filesToUpload = [];
        this.listOfUploadedReportsInfo = [];
      } catch (error) {
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForNonFinancialsProcessed = true;
      }
    },

    /**
     * Updates the Fiscal Year End value
     *
     * @param dateValue new date value
     */
    updateFiscalYearEndHandler(dateValue: Date) {
      this.fiscalYearEnd = getHyphenatedDate(dateValue);
      this.fiscalYearEndAsDate = dateValue;
    },

    /**
     * Updates the date of a single report file
     *
     * @param index file to update
     * @param dateValue new date value
     * @param whichSetOfFiles which set of files will be edited
     */
    updateReportDateHandler(index: number, dateValue: Date, whichSetOfFiles: WhichSetOfFiles) {
      const updatedSetOfFiles = this.updatePropertyFilesUploaded(
        index,
        "reportDateAsDate",
        dateValue,
        this[whichSetOfFiles]
      );
      this[whichSetOfFiles] = [...updatedSetOfFiles];
    },

    /**
     * Add files to object filesToUpload
     *
     * @param event full event object containing the files
     * @param event.originalEvent event information
     * @param event.files files
     */
    onSelectedFilesHandler(event: { files: Record<string, string>[]; originalEvent: Event }): void {
      if (event.files.length) {
        this.filesToUpload = [
          ...completeInformationAboutSelectedFileWithAdditionalFields(event.files, this.listOfUploadedReportsInfo),
        ] as ExtendedFile[];
      } else {
        return;
      }
    },

    /**
     * Remove report from files uploaded
     *
     * @param fileToRemove File To Remove
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param index Index number of the report
     */
    removeReportFromFilesToUpload(
      fileToRemove: Record<string, string>,
      fileRemoveCallback: (x: number) => void,
      index: number
    ) {
      fileRemoveCallback(index);
      this.filesToUpload = this.filesToUpload.filter((el) => {
        return el.name !== fileToRemove.name;
      });
    },
  },
});
</script>
