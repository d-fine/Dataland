<template>
  <Card class="col-12 page-wrapper-card p-3">
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
            :id="formId"
            @submit="postEuTaxonomyDataForNonFinancials"
            @submit-invalid="checkCustomInputs"
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
                  :filesToUpload="filesToUpload"
                  :listOfUploadedReportsInfo="listOfUploadedReportsInfo"
                  :euTaxonomyKpiNameMappings="euTaxonomyKpiNameMappings"
                  :euTaxonomyKpiInfoMappings="euTaxonomyKpiInfoMappings"
                  :editMode="editMode"
                  @selectedFiles="onSelectedFilesHandler"
                  @removeReportFromFilesToUpload="removeReportFromFilesToUpload"
                  @removeReportFromUploadedReports="removeReportFromUploadedReports"
                  @updateReportDateHandler="updateReportDateHandler"
                />

                <BasicInformationFields
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
            <SuccessUpload
              v-if="postEuTaxonomyDataForNonFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailedUpload v-else data-test="failedUploadMessage" :message="message" :messageId="messageCount" />
          </template>
          <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";

import Calendar from "primevue/calendar";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";

import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
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
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyPseudoModelAndMappings,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyPseudoModelAndMappings";
import {
  AssuranceDataAssuranceEnum,
  CompanyAssociatedDataEuTaxonomyDataForNonFinancials,
  DataMetaInformation,
} from "@clients/backend";
import {
  checkIfAllUploadedReportsAreReferencedInDataModel,
  checkCustomInputs,
  checkIfThereAreNoDuplicateReportNames,
} from "@/utils/validationsUtils";
import { modifyObjectKeys, ObjectType, updateObject } from "@/utils/updateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import { ExtendedCompanyReport, ExtendedFile, WhichSetOfFiles } from "@/components/forms/Types";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";
import { calculateSha256HashFromFile } from "@/utils/GenericUtils";
import { AxiosError, AxiosResponse } from "axios";
import DataPointForm from "@/components/forms/parts/kpiSelection/DataPointForm.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import { FileUploadSelectEvent } from "primevue/fileupload";

export default defineComponent({
  name: "CreateEuTaxonomyForNonFinancials",
  components: {
    JumpLinksSection,
    SubmitButton,
    DataPointForm,
    Calendar,
    UploadFormHeader,
    UploadReports,
    BasicInformationFields,
    FailedUpload,
    SubmitSideBar,
    Card,
    FormKit,
    SuccessUpload,
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
    route: useRoute(),
    editMode: false,
    waitingForData: false,
    formatBytesUserFriendly,
    checkCustomInputs,
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
    postEuTaxonomyDataForNonFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
    humanizeString: humanizeString,
    message: "",
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
      const companyAssociatedEuTaxonomyData = dataResponse.data;
      if (companyAssociatedEuTaxonomyData.data?.fiscalYearEnd) {
        this.fiscalYearEndAsDate = new Date(companyAssociatedEuTaxonomyData.data.fiscalYearEnd);
      }
      if (companyAssociatedEuTaxonomyData?.reportingPeriod) {
        this.reportingPeriod = new Date(companyAssociatedEuTaxonomyData.reportingPeriod);
      }
      if (companyAssociatedEuTaxonomyData.data?.referencedReports) {
        const referencedReportsForDataId = companyAssociatedEuTaxonomyData.data.referencedReports;
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
        JSON.parse(JSON.stringify(companyAssociatedEuTaxonomyData)) as ObjectType,
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
        checkIfAllUploadedReportsAreReferencedInDataModel(
          this.formInputsModel.data as ObjectType,
          this.namesOfAllCompanyReportsForTheDataset
        );
        checkIfThereAreNoDuplicateReportNames(this.filesToUpload);
        const euTaxonomyDataForNonFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForNonFinancialsControllerApi();

        const documentUploadControllerControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getDocumentControllerApi();

        if (this.filesToUpload.length) {
          for (const file of this.filesToUpload) {
            const fileIsAlreadyInStorage = (await documentUploadControllerControllerApi.checkDocument(file.documentId))
              .data.documentExists;
            if (!fileIsAlreadyInStorage) {
              await documentUploadControllerControllerApi.postDocument(file); // TODO assure that hash by frontend equals the one from backend
            }
          }
        }

        await this.$nextTick();
        const formInputsModelToSend = modifyObjectKeys(
          JSON.parse(JSON.stringify(this.formInputsModel)) as ObjectType,
          "send"
        ); // TODO is the JSON stuff really needed? I have the feeling no!

        this.postEuTaxonomyDataForNonFinancialsResponse =
          await euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            formInputsModelToSend as CompanyAssociatedDataEuTaxonomyDataForNonFinancials
          );
        this.$emit("datasetCreated");
      } catch (error) {
        this.messageCount++;
        console.error(error);
        if (error instanceof AxiosError) {
          this.message =
            // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
            (error.response?.data.errors[0]?.summary as string) +
            // eslint-disable-next-line @typescript-eslint/no-unsafe-member-access
            (error.response?.data.errors[0]?.message as string);
        } else {
          this.message = (error as Error).message;
        }
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
    async onSelectedFilesHandler(event: FileUploadSelectEvent): void {
      this.filesToUpload = [
        ...completeInformationAboutSelectedFileWithAdditionalFields(
          event.files as Record<string, string>[],
          this.listOfUploadedReportsInfo
        ),
      ] as ExtendedFile[];
      this.filesToUpload = await Promise.all(
        this.filesToUpload.map(async (extendedFile) => {
          extendedFile.documentId = await calculateSha256HashFromFile(extendedFile);
          return extendedFile;
        })
      );
    },

    /**
     * Remove report from files uploaded
     *
     * @param fileToRemove File To Remove
     * @param fileRemoveCallback Callback function removes report from the ones selected in formKit
     * @param index Index number of the report
     */
    removeReportFromFilesToUpload(fileToRemove: ExtendedFile, fileRemoveCallback: (x: number) => void, index: number) {
      fileRemoveCallback(index);
      this.filesToUpload = this.filesToUpload.filter((el) => {
        return el.name !== fileToRemove.name;
      });
    },

    /**
     * Removes a report from the list of already uploaded reports while the user edits a dataset. That way it is no
     * longer included as referenced report after the edit it submitted.
     *
     * @param indexOfFileToRemove Index of the report that shall no longer be referenced by the dataset
     */
    removeReportFromUploadedReports(indexOfFileToRemove: number) {
      this.listOfUploadedReportsInfo.splice(indexOfFileToRemove, 1);
      this.filesToUpload = [
        ...completeInformationAboutSelectedFileWithAdditionalFields(
          this.filesToUpload as Record<string, string>[],
          this.listOfUploadedReportsInfo
        ),
      ] as ExtendedFile[];
    },
  },
});
</script>
