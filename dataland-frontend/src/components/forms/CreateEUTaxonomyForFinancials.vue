<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>
      <span data-test="pageWrapperTitle"
        >{{ editMode ? "Edit" : "Create" }} EU Taxonomy Dataset for a Financial Company/Service</span
      >
    </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            :id="formId"
            @submit="postEuTaxonomyDataForFinancials"
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
                  data-test="reportingPeriodLabel"
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
              <FormKit type="group" name="data" label="data" validation-label="data" validation="required">
                <UploadReports
                  ref="UploadReports"
                  :editMode="editMode"
                  :dataset="templateDataset"
                  @referenceable-files-changed="referenceableFilesChanged"
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
                    <h4 id="addKpis" class="anchor title">Add KPIs</h4>
                    <p>Select at least one services type to add the related section of KPIs.</p>
                  </div>

                  <!-- Add KPIs -->
                  <div class="col-9 formFields">
                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader
                        data-test="selectKPIsLabel"
                        :name="euTaxonomyKpiNameMappings.financialServicesTypes ?? ''"
                        :explanation="euTaxonomyKpiInfoMappings.financialServicesTypes ?? ''"
                        :is-required="true"
                      />

                      <MultiSelect
                        v-model="selectedFinancialServiceOptions"
                        :options="financialServiceOptionsInDropdown"
                        data-test="MultiSelectfinancialServicesTypes"
                        name="MultiSelectfinancialServicesTypes"
                        optionLabel="label"
                        validation-label="Services Types"
                        validation="required"
                        placeholder="Select..."
                        class="mb-3"
                      />
                      <ul v-if="selectedFinancialServiceOptions.length">
                        <li :key="index" v-for="(financialServiceOption, index) of selectedFinancialServiceOptions">
                          {{ financialServiceOption.label }}
                        </li>
                      </ul>

                      <PrimeButton
                        @click="confirmSelectedKPIs"
                        data-test="addKpisButton"
                        :label="selectedFinancialServiceOptions.length ? 'UPDATE KPIS' : 'ADD RELATED KPIS'"
                      />
                      <FormKit
                        :modelValue="confirmedSelectedFinancialServiceTypes"
                        type="text"
                        validationLabel="Choosing a Financials Services Type and adding KPIs for it "
                        validation="required"
                        name="financialServicesTypes"
                        :outer-class="{ 'hidden-input': true }"
                      />
                    </div>
                  </div>
                </div>

                <div
                  v-for="financialServiceOption of confirmedSelectedFinancialServiceOptions"
                  :key="financialServiceOption"
                  :data-test="financialServiceOption.value"
                  class="uploadFormSection"
                >
                  <div class="flex w-full">
                    <div class="p-3 topicLabel">
                      <h3 :id="financialServiceOption.value" class="anchor title">
                        {{ financialServiceOption.label }}
                      </h3>
                    </div>

                    <PrimeButton
                      @click="removeKpisSection(financialServiceOption.value)"
                      label="REMOVE THIS SECTION"
                      data-test="removeSectionButton"
                      class="p-button-text ml-auto"
                      icon="pi pi-trash"
                    ></PrimeButton>
                  </div>

                  <FormKit
                    v-if="financialServiceOption.value !== 'assetManagementKpis'"
                    :name="financialServiceOption.value"
                    type="group"
                  >
                    <div
                      v-for="kpiType of euTaxonomyKPIsModel[financialServiceOption.value]"
                      :key="kpiType"
                      :data-test="kpiType"
                      class="uploadFormSection"
                    >
                      <div class="col-9 formFields">
                        <FormKit :name="kpiType" type="group">
                          <div class="form-field">
                            <DataPointForm
                              :name="kpiType ?? ''"
                              :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                              :kpiNameMappings="euTaxonomyKpiNameMappings"
                              :reportsName="namesOfAllCompanyReportsForTheDataset"
                            />
                          </div>
                        </FormKit>
                      </div>
                    </div>
                  </FormKit>

                  <FormKit name="eligibilityKpis" type="group">
                    <FormKit
                      :name="euTaxonomyKPIsModel?.kpisFieldNameToFinancialServiceType[financialServiceOption.value]"
                      type="group"
                    >
                      <div
                        v-for="kpiTypeEligibility of euTaxonomyKPIsModel.eligibilityKpis"
                        :key="kpiTypeEligibility"
                        :data-test="kpiTypeEligibility"
                        class="uploadFormSection"
                      >
                        <div class="col-9 formFields">
                          <FormKit :name="kpiTypeEligibility" type="group">
                            <div class="form-field">
                              <DataPointForm
                                :name="kpiTypeEligibility ?? ''"
                                :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                                :kpiNameMappings="euTaxonomyKpiNameMappings"
                                :reportsName="namesOfAllCompanyReportsForTheDataset"
                              />
                            </div>
                          </FormKit>
                        </div>
                      </div>
                    </FormKit>
                  </FormKit>
                </div>
              </FormKit>
            </div>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <template v-if="postEuTaxonomyDataForFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailedUpload v-else :message="message" :messageId="messageCount" />
          </template>
          <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";
import { FormKit } from "@formkit/vue";

import BasicInformationFields from "@/components/forms/parts/BasicInformationFields.vue";

import PrimeButton from "primevue/button";
import MultiSelect from "primevue/multiselect";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import { useRoute } from "vue-router";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { checkIfAllUploadedReportsAreReferencedInDataModel, checkCustomInputs } from "@/utils/validationsUtils";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyKPIsModel,
  getKpiFieldNameForOneFinancialServiceType,
} from "@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel";
import {
  AssuranceDataAssuranceEnum,
  CompanyAssociatedDataEuTaxonomyDataForFinancials,
  DataMetaInformation,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
  EuTaxonomyDataForNonFinancials,
} from "@clients/backend";
import { AxiosError, AxiosResponse } from "axios";
import { modifyObjectKeys, ObjectType, updateObject } from "@/utils/updateObjectUtils";
import { formatBytesUserFriendly } from "@/utils/NumberConversionUtils";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";
import DataPointForm from "@/components/forms/parts/kpiSelection/DataPointForm.vue";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import { FormKitNode } from "@formkit/core";
import UploadReports from "@/components/forms/parts/UploadReports.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateEuTaxonomyForFinancials",
  components: {
    JumpLinksSection,
    SubmitButton,
    SubmitSideBar,
    FailedUpload,
    FormKit,
    SuccessUpload,
    UploadFormHeader,
    Card,

    UploadReports,
    BasicInformationFields,

    PrimeButton,
    Calendar,
    MultiSelect,
    DataPointForm,
  },
  emits: ["datasetCreated"],
  data() {
    return {
      formId: "createEuTaxonomyForFinancialsForm",
      formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForFinancials,
      fiscalYearEndAsDate: null as Date | null,
      fiscalYearEnd: "",
      reportingPeriod: undefined as undefined | Date,
      assuranceData: {
        None: humanizeString(AssuranceDataAssuranceEnum.None),
        LimitedAssurance: humanizeString(AssuranceDataAssuranceEnum.LimitedAssurance),
        ReasonableAssurance: humanizeString(AssuranceDataAssuranceEnum.ReasonableAssurance),
      },
      euTaxonomyKPIsModel,
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      formatBytesUserFriendly,
      checkCustomInputs,
      route: useRoute(),
      waitingForData: false,
      editMode: false,

      postEuTaxonomyDataForFinancialsProcessed: false,
      messageCount: 0,
      postEuTaxonomyDataForFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
      humanizeString: humanizeString,
      onThisPageLinksStart: [
        { label: "Upload company reports", value: "uploadReports" },
        { label: "Basic information", value: "basicInformation" },
        { label: "Assurance", value: "assurance" },
        { label: "Add KPIs", value: "addKpis" },
      ],
      onThisPageLinks: [] as { label: string; value: string }[],
      financialServiceOptionsInDropdown: [
        {
          label: "Credit Institution",
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution
          ),
        },
        {
          label: "Investment Firm",
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm
          ),
        },
        {
          label: "Insurance & Re-insurance",
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
          ),
        },
        {
          label: "Asset Management",
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement
          ),
        },
      ],
      selectedFinancialServiceOptions: [] as { label: string; value: string }[],
      confirmedSelectedFinancialServiceOptions: [] as { label: string; value: string }[],
      confirmedSelectedFinancialServiceTypes: [] as string[], // TODO we could try to use a list of FinancialServiceTypeEnums here
      message: "",
      namesOfAllCompanyReportsForTheDataset: [] as string[],
      templateDataset: undefined as undefined | EuTaxonomyDataForNonFinancials,
    };
  },
  computed: {
    reportingPeriodYear(): number {
      if (this.reportingPeriod) {
        return this.reportingPeriod.getFullYear();
      }
      return 0;
    },
  },
  watch: {
    confirmedSelectedFinancialServiceOptions: function (newValue: { label: string; value: string }[]) {
      this.confirmedSelectedFinancialServiceTypes = newValue.map(
        (financialServiceOption: { label: string; value: string }): string => {
          return euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType[financialServiceOption.value] as string;
        }
      );
    },
  },

  props: {
    companyID: {
      type: String,
      required: true,
    },
  },
  mounted() {
    const dataId = this.route.query.templateDataId;
    if (typeof dataId === "string" && dataId !== "") {
      this.editMode = true;
      void this.loadEuData(dataId);
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }

    this.onThisPageLinks = [...this.onThisPageLinksStart];
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
      const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getEuTaxonomyDataForFinancialsControllerApi();

      const dataResponse =
        await euTaxonomyDataForFinancialsControllerApi.getCompanyAssociatedEuTaxonomyDataForFinancials(dataId);
      const companyAssociatedEuTaxonomyData = dataResponse.data;
      if (companyAssociatedEuTaxonomyData?.reportingPeriod) {
        this.reportingPeriod = new Date(companyAssociatedEuTaxonomyData.reportingPeriod);
      }
      if (companyAssociatedEuTaxonomyData.data?.fiscalYearEnd) {
        this.fiscalYearEndAsDate = new Date(companyAssociatedEuTaxonomyData.data.fiscalYearEnd);
      }
      this.templateDataset = companyAssociatedEuTaxonomyData.data;
      if (companyAssociatedEuTaxonomyData.data?.financialServicesTypes) {
        // types of company financial services
        const arrayWithCompanyKpiTypes = companyAssociatedEuTaxonomyData.data?.financialServicesTypes;
        // all types of financial services

        this.selectedFinancialServiceOptions = this.financialServiceOptionsInDropdown.filter(
          (financialServiceOption: { label: string; value: string }) => {
            return arrayWithCompanyKpiTypes?.includes(
              euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType[
                financialServiceOption.value
              ] as EuTaxonomyDataForFinancialsFinancialServicesTypesEnum
            );
          }
        );
        this.confirmSelectedKPIs();
      }

      const receivedFormInputsModel = modifyObjectKeys(companyAssociatedEuTaxonomyData as ObjectType, "receive");
      this.waitingForData = false;

      await this.$nextTick(); // TODO check if this is neccessary
      updateObject(this.formInputsModel, receivedFormInputsModel);
    },

    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;

        checkIfAllUploadedReportsAreReferencedInDataModel(
          this.formInputsModel.data as ObjectType,
          this.namesOfAllCompanyReportsForTheDataset
        );
        // TODO dont throw an error but use validation???

        await (this.$refs.UploadReports.uploadFiles as () => Promise<void>)();

        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();

        await this.$nextTick();
        const formInputsModelToSend = modifyObjectKeys(this.formInputsModel as ObjectType, "send");
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            formInputsModelToSend as CompanyAssociatedDataEuTaxonomyDataForFinancials
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
            (error.response?.data.errors[0]?.message as string); //TODO: fix types
        } else {
          this.message = (error as Error).message;
        }
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },

    /**
     * Confirms the list of kpis to be generated
     *
     */
    confirmSelectedKPIs() {
      this.confirmedSelectedFinancialServiceOptions = this.selectedFinancialServiceOptions;
      this.onThisPageLinks = [...new Set(this.onThisPageLinksStart.concat(this.selectedFinancialServiceOptions))];
    },

    /**
     * Deletes the specified kpis section
     *
     * @param value section name
     */
    removeKpisSection(value: string) {
      this.confirmedSelectedFinancialServiceOptions = this.confirmedSelectedFinancialServiceOptions.filter(
        (financialServiceOption: { label: string; value: string }) => financialServiceOption.value !== value
      );
      this.selectedFinancialServiceOptions = this.confirmedSelectedFinancialServiceOptions;
      this.onThisPageLinks = this.onThisPageLinks.filter((el: { label: string; value: string }) => el.value !== value);
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
     * Handles invalid inputs and gives applicable error messages
     *
     * @param node from which the input fields will be checked
     */
    handleInvalidInput(node: FormKitNode) {
      checkCustomInputs(node);
      this.message = `Sorry, not all fields are filled out correctly.`;
      this.postEuTaxonomyDataForFinancialsProcessed = true;
    },
    /**
     * Updates the local list of names of referenceable files
     *
     * @param reportsFilenames new list of referenceable files
     */
    referenceableFilesChanged(reportsFilenames: string[]) {
      this.namesOfAllCompanyReportsForTheDataset = reportsFilenames;
    },
  },
});
</script>
