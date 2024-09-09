<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title>
      <span data-test="pageWrapperTitle"
        >{{ editMode ? 'Edit' : 'Create' }} EU Taxonomy Dataset for a Financial Company/Service</span
      >
    </template>
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
            @submit="postEuTaxonomyDataForFinancials"
            @submit-invalid="handleInvalidInput"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :modelValue="companyID"
            />
            <div class="uploadFormSection grid">
              <div class="col-3 p-3 topicLabel">
                <h4 id="reportingPeriod" class="anchor title">Reporting Period</h4>
              </div>
              <div class="col-9 formFields uploaded-files">
                <UploadFormHeader
                  data-test="reportingPeriodLabel"
                  :label="euTaxonomyKpiNameMappings.reportingPeriod"
                  :description="euTaxonomyKpiInfoMappings.reportingPeriod"
                  :is-required="true"
                />
                <div class="lg:col-6 md:col-6 col-12 p-0">
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

                <FormKit type="hidden" :modelValue="reportingPeriodYear.toString()" name="reportingPeriod" />
              </div>
              <FormKit type="group" name="data" label="data" validation-label="data" validation="required">
                <UploadReports
                  name="UploadReports"
                  ref="UploadReports"
                  :isMountedForEuTaxoFinancialsUploadPage="true"
                  :referencedReportsForPrefill="templateDataset?.referencedReports ?? undefined"
                  @reportsUpdated="updateReportsSelection"
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
                          :label="euTaxonomyKpiNameMappings.assurance ?? ''"
                          :description="euTaxonomyKpiInfoMappings.assurance ?? ''"
                          :is-required="true"
                        />
                        <div class="lg:col-4 md:col-6 col-12 p-0" data-test="assuranceDataSingleSelect">
                          <SingleSelectFormElement
                            name="value"
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
                          :label="euTaxonomyKpiNameMappings.provider ?? ''"
                          :description="euTaxonomyKpiInfoMappings.provider ?? ''"
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
                        <FormKit type="group" name="dataSource" ignore="true">
                          <h4 class="mt-0">Data source</h4>
                          <div class="next-to-each-other">
                            <div class="flex-1" data-test="companyReportsSingleSelect">
                              <UploadFormHeader
                                :label="euTaxonomyKpiNameMappings.report ?? ''"
                                :description="euTaxonomyKpiInfoMappings.report ?? ''"
                                :is-required="true"
                              />
                              <SingleSelectFormElement
                                ignore="true"
                                placeholder="Select a report"
                                validation-label="Selecting a report"
                                v-model="currentReportValue"
                                :options="[noReportLabel, ...namesOfAllCompanyReportsForTheDataset]"
                                allow-unknown-option
                              />
                            </div>
                            <div v-if="isValidFileName(isMounted, currentReportValue)" class="col-4">
                              <UploadFormHeader :label="'Page'" :description="pageNumberDescription" />
                              <FormKit
                                outer-class="w-100"
                                type="text"
                                name="page"
                                placeholder="Enter page"
                                v-model="pageForFileReference"
                                :validation-messages="{
                                  validatePageNumber: pageNumberValidationErrorMessage,
                                }"
                                :validation-rules="{ validatePageNumber }"
                                validation="validatePageNumber"
                                ignore="true"
                              />
                            </div>
                          </div>
                        </FormKit>
                        <FormKit type="group" name="dataSource" v-if="isValidFileName(isMounted, currentReportValue)">
                          <FormKit type="hidden" name="fileName" v-model="currentReportValue" />
                          <FormKit type="hidden" name="fileReference" :modelValue="fileReferenceAccordingToName" />
                          <FormKit
                            type="hidden"
                            name="page"
                            :validation-rules="{ validatePageNumber }"
                            validation="validatePageNumber"
                            v-model="pageForFileReference"
                          />
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
                        :label="euTaxonomyKpiNameMappings.financialServicesTypes ?? ''"
                        :description="euTaxonomyKpiInfoMappings.financialServicesTypes ?? ''"
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

                <FormKit name="kpiSections" type="group">
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

                    <FormKit :name="financialServiceOption.value" type="group">
                      <div v-if="financialServiceOption.value !== 'assetManagementKpis'" class="uploadFormSection">
                        <div
                          v-for="kpiType of euTaxonomyKPIsModel[financialServiceOption.value]"
                          :key="kpiType"
                          :data-test="kpiType"
                          class="uploadFormSection"
                        >
                          <div class="col-9 formFields">
                            <FormKit :name="kpiType" type="group">
                              <div class="form-field">
                                <DataPointFormWithToggle
                                  :name="kpiType ?? ''"
                                  :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                                  :kpiNameMappings="euTaxonomyKpiNameMappings"
                                  :reportsNameAndReferences="namesAndReferencesOfAllCompanyReportsForTheDataset"
                                />
                              </div>
                            </FormKit>
                          </div>
                        </div>
                      </div>
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
                                <DataPointFormWithToggle
                                  :name="kpiTypeEligibility ?? ''"
                                  :kpiInfoMappings="euTaxonomyKpiInfoMappings"
                                  :kpiNameMappings="euTaxonomyKpiNameMappings"
                                  :reportsNameAndReferences="namesAndReferencesOfAllCompanyReportsForTheDataset"
                                />
                              </div>
                            </FormKit>
                          </div>
                        </div>
                      </FormKit>
                    </FormKit>
                  </div>
                </FormKit>
              </FormKit>
            </div>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <template v-if="postEuTaxonomyDataForFinancialsProcessed">
            <SuccessMessage
              v-if="postEuTaxonomyDataForFinancialsResponse?.status === 200"
              msg="EU Taxonomy Data"
              :messageId="messageCount"
            />
            <FailMessage v-else :message="message" :messageId="messageCount" />
          </template>
          <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
        </SubmitSideBar>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
// @ts-nocheck
import SuccessMessage from '@/components/messages/SuccessMessage.vue';
import SubmitSideBar from '@/components/forms/parts/SubmitSideBar.vue';
import { FormKit } from '@formkit/vue';

import EuTaxonomyBasicInformation from '@/components/forms/parts/EuTaxonomyBasicInformation.vue';

import PrimeButton from 'primevue/button';
import MultiSelect from 'primevue/multiselect';
import UploadFormHeader from '@/components/forms/parts/elements/basic/UploadFormHeader.vue';
import Calendar from 'primevue/calendar';
import FailMessage from '@/components/messages/FailMessage.vue';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { ApiClientProvider } from '@/services/ApiClients';
import Card from 'primevue/card';
import { useRoute } from 'vue-router';
import { defineComponent, inject, nextTick } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import {
  checkIfAllUploadedReportsAreReferencedInDataModel,
  checkCustomInputs,
  validatePageNumber,
  PAGE_NUMBER_VALIDATION_ERROR_MESSAGE,
} from '@/utils/ValidationUtils';
import { getHyphenatedDate } from '@/utils/DataFormatUtils';
import {
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
  euTaxonomyKPIsModel,
  getKpiFieldNameForOneFinancialServiceType,
} from '@/components/forms/parts/kpiSelection/EuTaxonomyKPIsModel';
import {
  AssuranceDataPointValueEnum,
  type CompanyAssociatedDataEuTaxonomyDataForFinancials,
  type DataMetaInformation,
  DataTypeEnum,
  type EuTaxonomyDataForFinancials,
  EuTaxonomyDataForFinancialsFinancialServicesTypesEnum,
} from '@clients/backend';
import { type AxiosResponse } from 'axios';
import { type ObjectType, updateObject } from '@/utils/UpdateObjectUtils';
import JumpLinksSection from '@/components/forms/parts/JumpLinksSection.vue';
import SubmitButton from '@/components/forms/parts/SubmitButton.vue';
import { type FormKitNode } from '@formkit/core';
import UploadReports from '@/components/forms/parts/UploadReports.vue';
import { formatAxiosErrorMessage } from '@/utils/AxiosErrorMessageFormatter';
import DataPointFormWithToggle from '@/components/forms/parts/kpiSelection/DataPointFormWithToggle.vue';
import {
  uploadFiles,
  type DocumentToUpload,
  getAvailableFileNames,
  getFileReferenceByFileName,
  PAGE_NUMBER_DESCRIPTION,
} from '@/utils/FileUploadUtils';
import { isValidFileName, noReportLabel } from '@/utils/DataSource';
import SingleSelectFormElement from '@/components/forms/parts/elements/basic/SingleSelectFormElement.vue';
import { type ClickableLink } from '@/types/CustomPropTypes';
import { hasUserCompanyOwnerOrDataUploaderRole } from '@/utils/CompanyRolesUtils';

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },
  name: 'CreateEuTaxonomyForFinancials',
  components: {
    SingleSelectFormElement,
    JumpLinksSection,
    SubmitButton,
    SubmitSideBar,
    FailMessage,
    FormKit,
    SuccessMessage,
    UploadFormHeader,
    Card,
    UploadReports,
    EuTaxonomyBasicInformation,
    PrimeButton,
    Calendar,
    MultiSelect,
    DataPointFormWithToggle,
  },
  emits: ['datasetCreated'],
  data() {
    return {
      pageNumberDescription: PAGE_NUMBER_DESCRIPTION,
      pageForFileReference: undefined as string | undefined,
      pageNumberValidationErrorMessage: PAGE_NUMBER_VALIDATION_ERROR_MESSAGE,
      isMounted: false,
      formId: 'createEuTaxonomyForFinancialsForm',
      formInputsModel: {} as CompanyAssociatedDataEuTaxonomyDataForFinancials,
      fiscalYearEndAsDate: null as Date | null,
      fiscalYearEnd: '',
      currentReportValue: '' as string,
      reportingPeriod: undefined as undefined | Date,
      assuranceData: {
        None: humanizeStringOrNumber(AssuranceDataPointValueEnum.None),
        LimitedAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.LimitedAssurance),
        ReasonableAssurance: humanizeStringOrNumber(AssuranceDataPointValueEnum.ReasonableAssurance),
      },
      euTaxonomyKPIsModel,
      euTaxonomyKpiNameMappings,
      euTaxonomyKpiInfoMappings,
      route: useRoute(),
      waitingForData: false,
      editMode: false,
      noReportLabel: noReportLabel,
      postEuTaxonomyDataForFinancialsProcessed: false,
      messageCount: 0,
      postEuTaxonomyDataForFinancialsResponse: null as AxiosResponse<DataMetaInformation> | null,
      onThisPageLinksStart: [
        { label: 'Upload company reports', value: 'uploadReports' },
        { label: 'Basic information', value: 'basicInformation' },
        { label: 'Assurance', value: 'assurance' },
        { label: 'Add KPIs', value: 'addKpis' },
      ],
      onThisPageLinks: [] as ClickableLink[],
      financialServiceOptionsInDropdown: [
        {
          label: 'Credit Institution',
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.CreditInstitution
          ),
        },
        {
          label: 'Investment Firm',
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InvestmentFirm
          ),
        },
        {
          label: 'Insurance & Re-insurance',
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.InsuranceOrReinsurance
          ),
        },
        {
          label: 'Asset Management',
          value: getKpiFieldNameForOneFinancialServiceType(
            EuTaxonomyDataForFinancialsFinancialServicesTypesEnum.AssetManagement
          ),
        },
      ],
      selectedFinancialServiceOptions: [] as { label: string; value: string }[],
      confirmedSelectedFinancialServiceOptions: [] as { label: string; value: string }[],
      confirmedSelectedFinancialServiceTypes: [] as EuTaxonomyDataForFinancialsFinancialServicesTypesEnum[],
      message: '',
      namesAndReferencesOfAllCompanyReportsForTheDataset: {},
      templateDataset: undefined as undefined | EuTaxonomyDataForFinancials,
      isValidFileName: isValidFileName,
      documentsToUpload: [] as DocumentToUpload[],
    };
  },
  mounted() {
    setTimeout(() => (this.isMounted = true));
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
    fileReferenceAccordingToName(): string {
      return getFileReferenceByFileName(
        this.currentReportValue,
        this.namesAndReferencesOfAllCompanyReportsForTheDataset
      );
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
  created() {
    const dataId = this.route.query.templateDataId;
    if (typeof dataId === 'string' && dataId !== '') {
      this.editMode = true;
      this.fetchTemplateData(dataId);
    }
    if (this.reportingPeriod === undefined) {
      this.reportingPeriod = new Date();
    }

    this.onThisPageLinks = [...this.onThisPageLinksStart];
  },

  methods: {
    validatePageNumber,
    /**
     * Loads the Dataset by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     * @param dataId the id of the dataset to load
     */
    fetchTemplateData(dataId: string): void {
      this.waitingForData = true;

      new ApiClientProvider(assertDefined(this.getKeycloakPromise)())
        .getUnifiedFrameworkDataController(DataTypeEnum.EutaxonomyFinancials)
        .getFrameworkData(dataId)
        .then((resolvedPromise) => {
          const companyAssociatedEuTaxonomyData = resolvedPromise.data;
          if (companyAssociatedEuTaxonomyData?.reportingPeriod) {
            this.reportingPeriod = new Date(companyAssociatedEuTaxonomyData.reportingPeriod);
          }
          if (companyAssociatedEuTaxonomyData.data?.fiscalYearEnd) {
            this.fiscalYearEndAsDate = new Date(companyAssociatedEuTaxonomyData.data.fiscalYearEnd);
          }
          if (companyAssociatedEuTaxonomyData.data?.assurance?.dataSource?.fileName) {
            this.currentReportValue = companyAssociatedEuTaxonomyData.data.assurance.dataSource.fileName;
          }
          this.templateDataset = companyAssociatedEuTaxonomyData.data;

          this.extractFinancialServiceTypes(companyAssociatedEuTaxonomyData.data);

          (companyAssociatedEuTaxonomyData.data as ObjectType).kpiSections = this.extractKpis(
            companyAssociatedEuTaxonomyData.data as ObjectType
          );
          this.waitingForData = false;

          nextTick()
            .then(() => updateObject(this.formInputsModel as ObjectType, companyAssociatedEuTaxonomyData))
            .catch((e) => console.log(e));
        })
        .catch((e) => console.log(e));
    },

    /**
     * Parses received financial service types and eligibility KPIs data
     * @param receivedFormInputsModelData Received data
     * @returns An object with financial service types and eligibility KPIs which can be directly merged into the formInputsModel
     */
    extractKpis(receivedFormInputsModelData: ObjectType) {
      const financialServiceTypeKeys = Object.keys(euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType);
      return financialServiceTypeKeys
        .map((key: string) => {
          const financialServiceType = Object.values(euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType)[
            financialServiceTypeKeys.indexOf(key)
          ];
          const kpis = (receivedFormInputsModelData.eligibilityKpis as ObjectType)[financialServiceType];
          const eligibilityKpis = kpis ? { [financialServiceType]: kpis } : undefined;
          return { [key]: eligibilityKpis };
        })
        .map((item) => {
          const financialServiceType = Object.keys(item)[0];
          const kpis = receivedFormInputsModelData[financialServiceType] as ObjectType;
          if (kpis) {
            item[financialServiceType] = {
              ...item[financialServiceType],
              ...kpis,
            };
          }
          return item;
        })
        .filter((item) => Object.values(item)[0])
        .reduce((all, one) => ({ ...all, ...one }), []);
    },

    /**
     * Extracts which Financial Service Types are present in the dataset and adds the associated KPI components to
     * the form and list of links.
     * @param data EuTaxonomyDataForFinancials dataset that may include the different financialServicesTypes
     */
    extractFinancialServiceTypes(data?: EuTaxonomyDataForFinancials) {
      if (data?.financialServicesTypes) {
        const arrayWithCompanyKpiTypes = data?.financialServicesTypes;

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
    },

    /**
     * Confirms the list of kpis to be generated
     */
    confirmSelectedKPIs() {
      this.confirmedSelectedFinancialServiceOptions = this.selectedFinancialServiceOptions;
      this.onThisPageLinks = [...new Set(this.onThisPageLinksStart.concat(this.selectedFinancialServiceOptions))];
    },

    /**
     * Converts the kpisSection data to a request-friendly object
     * @param kpiSections Kpi Section data
     * @returns An object with KPIs comprised of financial service types and and object for eligibility KPIs
     */
    convertKpis(kpiSections: ObjectType) {
      const eligibilityKpis = Object.keys(kpiSections)
        .map((financialServiceTypeKey) => {
          const financialServiceType = euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType[
            financialServiceTypeKey
          ] as string;
          const section = kpiSections[financialServiceTypeKey] as ObjectType;
          const field = section[financialServiceType];
          return { [financialServiceType]: field };
        })
        .reduce((all, one) => ({ ...all, ...one }), []);

      const kpis = Object.keys(kpiSections)
        .filter((financialServiceTypeKey) => financialServiceTypeKey !== 'assetManagementKpis')
        .map((financialServiceTypeKey) => {
          const kpi = { [financialServiceTypeKey]: kpiSections[financialServiceTypeKey] };
          if (kpiSections[financialServiceTypeKey]) {
            const financialServiceType = (euTaxonomyKPIsModel.kpisFieldNameToFinancialServiceType as ObjectType)[
              financialServiceTypeKey
            ];
            delete (kpi[financialServiceTypeKey] as ObjectType)[financialServiceType as string];
          }
          return kpi;
        })
        .reduce((all, one) => ({ ...all, ...one }), []);

      return { eligibilityKpis, ...kpis };
    },

    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;

        // JSON.parse/stringify used to clone the formInputsModel in order to stop Proxy refreneces
        const clonedFormInputsModel = JSON.parse(
          JSON.stringify(this.formInputsModel)
        ) as unknown as CompanyAssociatedDataEuTaxonomyDataForFinancials;
        const kpiSections = (clonedFormInputsModel.data as ObjectType).kpiSections;
        delete (clonedFormInputsModel.data as ObjectType).kpiSections;
        (clonedFormInputsModel.data as ObjectType) = {
          ...(clonedFormInputsModel.data as ObjectType),
          ...this.convertKpis(kpiSections as ObjectType),
        };

        checkIfAllUploadedReportsAreReferencedInDataModel(
          this.formInputsModel.data as ObjectType,
          Object.keys(this.namesAndReferencesOfAllCompanyReportsForTheDataset)
        );
        await uploadFiles(this.documentsToUpload, assertDefined(this.getKeycloakPromise));
        const euTaxonomyDataForFinancialsControllerApi = new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getUnifiedFrameworkDataController(DataTypeEnum.EutaxonomyFinancials);

        const isCompanyOwnerOrDataUploader = await hasUserCompanyOwnerOrDataUploaderRole(
          this.formInputsModel.companyId,
          this.getKeycloakPromise
        );

        this.postEuTaxonomyDataForFinancialsResponse = await euTaxonomyDataForFinancialsControllerApi.postFrameworkData(
          clonedFormInputsModel,
          isCompanyOwnerOrDataUploader
        );

        this.$emit('datasetCreated');
      } catch (error) {
        this.messageCount++;
        console.error(error);
        this.message = formatAxiosErrorMessage(error as Error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },

    /**
     * Deletes the specified kpis section
     * @param value section name
     */
    removeKpisSection(value: string) {
      const filtered = this.confirmedSelectedFinancialServiceOptions.filter(
        (financialServiceOption: { label: string; value: string }) => financialServiceOption.value !== value
      );

      this.confirmedSelectedFinancialServiceOptions = filtered;
      this.selectedFinancialServiceOptions = filtered;

      this.onThisPageLinks = this.onThisPageLinks.filter((el: { label: string; value: string }) => el.value !== value);
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
      this.postEuTaxonomyDataForFinancialsProcessed = true;
    },
    /**
     * Sets the object containing the names of all stored and to-be-uploaded reports as keys, and their respective
     * fileReferences as values, and then sets the selection of reports that are to be uploaded.
     * @param reportsNamesAndReferences contains the names of all stored and to-be-uploaded reports as keys,
     * and their respective fileReferences as values
     * @param reportsToUpload contains the actual selection of reports that are to be uploaded
     */
    updateReportsSelection(reportsNamesAndReferences: object, reportsToUpload: DocumentToUpload[]) {
      this.namesAndReferencesOfAllCompanyReportsForTheDataset = reportsNamesAndReferences;
      this.documentsToUpload = [...reportsToUpload];
    },
  },
});
</script>
