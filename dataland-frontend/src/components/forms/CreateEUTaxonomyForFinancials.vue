<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>Create EU Taxonomy Dataset for a Financial Company/Service</template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createEuTaxonomyForFinancialsForm"
            @submit="postEuTaxonomyDataForFinancials"
            #default="{ state: { valid } }"
          >
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :model-value="companyID"
              disabled="true"
            />
            <FormKit type="group" name="data" label="data">
              <div class="uploadFormSection grid">
                <div class="col-3 p-3 topicLabel">
                  <h4 id="general" class="anchor title">Upload company reports</h4>
                  <p>Please upload all relevant reports for this dataset in the PDF format.</p>
                </div>
                <!-- Select company reports -->
                <div class="col-9 formFields uploaded-files">
                  <h3 class="mt-0">Select company reports</h3>
                  <FileUpload
                    ref="fileUpload"
                    name="fileUpload"
                    accept=".pdf, .xlsx"
                    @select="onSelectedFiles"
                    :maxFileSize="maxFileSize"
                    invalidFileSizeMessage="{0}: Invalid file size, file size should be smaller than {1}."
                    :fileLimit="1"
                    :auto="true"
                  >
                    <template #header="{ chooseCallback }">
                      <div class="flex flex-wrap justify-content-between align-items-center flex-1 gap-2">
                        <div class="flex gap-2">
                          <PrimeButton
                            @click="chooseCallback()"
                            icon="pi pi-upload"
                            class="m-0"
                            label="UPLOAD REPORTS"
                          />
                        </div>
                      </div>
                    </template>
                    <template #content="{ uploadedFiles, removeUploadedFileCallback }">
                      <div v-if="uploadedFiles.length > 0">
                        <div
                          v-for="(file, index) of uploadedFiles"
                          :key="file.name + file.type + file.size"
                          class="flex w-full align-items-center file-upload-item"
                        >
                          <span class="font-semibold flex-1">{{ file.name }}</span>
                          <div class="mx-2 text-black-alpha-50">{{ formatSize(file.size) }}</div>
                          <PrimeButton
                            icon="pi pi-times"
                            @click="onRemoveTemplatingFile(file, removeUploadedFileCallback, index)"
                            class="p-button-rounded"
                          />
                        </div>
                      </div>
                    </template>
                  </FileUpload>
                </div>

                <div class="uploadFormSection">
                  <FormKit name="referencedReports" type="group">
                    <!-- Select company reports -->
                    <div v-for="file of files" :key="file.name" class="col-9 formFields">
                      <div class="form-field-label">
                        <h3 class="mt-0">{{ file.name }}</h3>
                      </div>
                      <FormKit :name="file.name" type="group">
                        <!-- Date of the report -->
                        <div class="form-field">
                          <UploadFormHeader
                            :name="euTaxonomyKpiNameMappings.fiscalYearEnd"
                            :explanation="euTaxonomyKpiInfoMappings.fiscalYearEnd"
                          />
                          <div class="lg:col-6 md:col-6 col-12 p-0">
                            <Calendar
                              data-test="dateOfTheReport"
                              inputId="icon"
                              :showIcon="true"
                              dateFormat="D, M dd, yy"
                              :maxDate="new Date()"
                            />
                          </div>

                          <FormKit
                            type="text"
                            validation="required"
                            name="dateOfTheReport"
                            :outer-class="{ 'hidden-input': true }"
                          />
                        </div>

                        <!-- Currency used in the report -->
                        <div class="form-field" data-test="currencyUsedInTheReport">
                          <UploadFormHeader
                            :name="euTaxonomyKpiNameMappings.currency"
                            :explanation="euTaxonomyKpiInfoMappings.currency"
                          />
                          <div class="lg:col-6 md:col-6 col-12 p-0">
                            <FormKit
                              type="text"
                              name="currencyUsedInTheReport"
                              validation="required"
                              validation-label="Currency used in the report"
                              placeholder="Currency used in the report"
                            />
                          </div>
                        </div>
                        <!-- Integrated report is on a group level -->
                        <div class="form-field">
                          <CheckBoxCustom
                            name="groupLevelIntegratedReport"
                            :explanation="euTaxonomyKpiInfoMappings.groupLevelIntegratedReport"
                            :displayName="euTaxonomyKpiNameMappings.groupLevelIntegratedReport"
                          />
                        </div>
                      </FormKit>
                    </div>
                  </FormKit>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Basic information</h4>
                  </div>
                  <!-- Basic information -->
                  <div class="col-9 formFields">
                    <h3 class="mt-0">Basic information</h3>

                    <YesNoComponent
                      :displayName="euTaxonomyKpiNameMappings.fiscalYearDeviation"
                      :info="euTaxonomyKpiInfoMappings.fiscalYearDeviation"
                      :name="'fiscalYearDeviation'"
                      :radioButtonsOptions="['Deviation', 'NoDeviation']"
                      required="required"
                    />

                    <!-- The date the fiscal year ends -->
                    <div class="form-field">
                      <UploadFormHeader
                        :name="euTaxonomyKpiNameMappings.fiscalYearEnd"
                        :explanation="euTaxonomyKpiInfoMappings.fiscalYearEnd"
                      />
                      <div class="lg:col-6 md:col-6 col-12 p-0">
                        <Calendar
                          inputId="fiscalYearEnd"
                          v-model="fiscalYearEnd"
                          :showIcon="true"
                          dateFormat="D, M dd, yy"
                        />
                      </div>

                      <FormKit
                        type="text"
                        validation="required"
                        name="fiscalYearEnd"
                        v-model="convertedFiscalYearEnd"
                        :outer-class="{ 'hidden-input': true }"
                      />
                    </div>

                    <!-- Scope of entities -->
                    <div class="form-field">
                      <YesNoComponent
                        :displayName="euTaxonomyKpiNameMappings.scopeOfEntities"
                        :info="euTaxonomyKpiInfoMappings.scopeOfEntities"
                        :name="'scopeOfEntities'"
                      />
                    </div>

                    <!-- EU Taxonomy activity level reporting -->
                    <div class="form-field">
                      <YesNoComponent
                        :displayName="euTaxonomyKpiNameMappings.activityLevelReporting"
                        :info="euTaxonomyKpiInfoMappings.activityLevelReporting"
                        :name="'activityLevelReporting'"
                      />
                    </div>

                    <!-- Number of employees -->
                    <div class="form-field">
                      <UploadFormHeader
                        :name="euTaxonomyKpiNameMappings.numberOfEmployees"
                        :explanation="euTaxonomyKpiInfoMappings.numberOfEmployees"
                      />
                      <div class="lg:col-4 md:col-4 col-6 p-0">
                        <FormKit
                          type="number"
                          name="numberOfEmployees"
                          validation-label="Number of employees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                        />
                      </div>
                    </div>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Assurance</h4>
                  </div>

                  <!-- Level of assurance -->
                  <div class="col-9 formFields">
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

                        <h3 class="mt-0">Assurance</h3>
                        <UploadFormHeader name="Data source" explanation="Data source" />
                        <div class="next-to-each-other">
                          <FormKit
                            outer-class="flex-1"
                            type="select"
                            name="report"
                            placeholder="Select a report"
                            validation-label="Select a report"
                            validation="required"
                            :options="['None...', ...(files.map(el => el.name))]"
                          />
                          <FormKit
                              outer-class="w-100"
                              type="number"
                              name="page"
                              placeholder="Page"
                              validation-label="Page"
                          />
                          <FormKit
                              outer-class="short"
                              type="text"
                              name="tagName"
                              placeholder="Tag Name"
                              validation-label="Tag Name"
                          />
                        </div>
                      </div>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">Add KPIs</h4>
                    <p>Select at least one company type to add the related section of KPIs.</p>
                  </div>

                  <!-- Add KPIs -->
                  <div class="col-9 formFields">
                    <!-- Data source -->
                    <div class="form-field">
                      <UploadFormHeader name="Company type *" explanation="Company type *" />

                      <MultiSelect
                        v-model="selectedKPIs"
                        :options="kpisModel"
                        name="companyType"
                        optionLabel="label"
                        validation-label="Company type"
                        validation="required"
                        placeholder="Select Brands"
                        class="mb-3"
                      />
                      <ul v-if="selectedKPIs.length">
                        <li v-for="(file, index) of selectedKPIs">{{ file.label }}</li>
                      </ul>

                      <PrimeButton @click="confirmeSelectedKPIs" class="m-0" label="ADD RELATED KPIS" />
                    </div>
                  </div>
                </div>

                <div v-for="(copanyType, index) of confirmedSelectedKPIs" :key="copanyType" class="uploadFormSection">
                  <FormKit :name="copanyType.value" type="group">
                    <div class="flex w-full">
                      <h2>{{ copanyType.label }}</h2>

                      <PrimeButton
                        @click="removeKpisSection(copanyType.value)"
                        label="REMOVE THIS SECTION"
                        class="p-button-text ml-auto"
                        icon="pi pi-trash"
                      ></PrimeButton>
                    </div>

                    <div
                      v-for="kpiType of Object.entries(euTaxonomyKPIsModel[copanyType.value])"
                      :key="JSON.stringify(kpiType)"
                      class="uploadFormSection"
                    >
                      <FormKit :name="kpiType[0]" type="group">
                        <div class="col-3 p-3 topicLabel">
                          <h4 id="general" class="anchor title">{{ kpiType[0] }}</h4>
                        </div>

                        <div v-for="(kpi, index) of kpiType[1]" :key="index" class="col-9 formFields">
                          <FormKit :name="kpi" type="group">
                            <h3 class="mt-0">{{ kpi }}</h3>
                            <div class="form-field">
                              <KPIfieldsSet name="xxxxx" />
                            </div>
                          </FormKit>
                        </div>
                      </FormKit>
                    </div>

                    {{ Object.keys(euTaxonomyKPIsModel[copanyType.value]) }}
                  </FormKit>
                </div>

                <!--------- SUBMIT --------->

                <div class="uploadFormSection grid">
                  <div class="col-3"></div>

                  <div class="col-9">
                    <PrimeButton data-test="submitButton" type="submit" label="ADD DATA" />
                  </div>
                </div>
              </div>

              <!-- rest -->
              <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                <template
                  v-for="fsType in ['CreditInstitution', 'InsuranceOrReinsurance', 'AssetManagement', 'InvestmentFirm']"
                  :key="fsType"
                >
                  <div :name="fsType">
                    <FormKit type="group" :name="fsType">
                      <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                      <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                      <DataPointFormElement name="taxonomyNonEligibleActivity" label="Taxonomy Non Eligible Activity" />
                      <DataPointFormElement name="derivatives" label="Derivatives" />
                      <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                      <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                    </FormKit>
                  </div>
                </template>
              </FormKit>
              <FormKit type="group" name="creditInstitutionKpis" label="Credit Institution KPIs">
                <h4>Credit Institution KPIs</h4>
                <div name="creditInstitutionKpis">
                  <DataPointFormElement name="tradingPortfolio" label="Trading Portfolio" />
                  <DataPointFormElement name="interbankLoans" label="Interbank Loans" />
                  <DataPointFormElement
                    name="tradingPortfolioAndInterbankLoans"
                    label="Trading Portfolio and Interbank Loans (combined)"
                  />
                  <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
                </div>
              </FormKit>
              <FormKit type="group" name="insuranceKpis" label="Insurance KPIs">
                <h4>Insurance KPIs</h4>
                <DataPointFormElement
                  name="taxonomyEligibleNonLifeInsuranceActivities"
                  label="Taxonomy Eligible non Life Insurance Activities"
                />
              </FormKit>
              <FormKit type="group" name="investmentFirmKpis" label="Investment Firm KPIs">
                <h4>Investment Firm KPIs</h4>
                <DataPointFormElement name="greenAssetRatio" label="Green asset ratio" />
              </FormKit>
              <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
            </FormKit>
          </FormKit>
          <template v-if="postEuTaxonomyDataForFinancialsProcessed">
            <SuccessUpload
              v-if="postEuTaxonomyDataForFinancialsResponse"
              msg="EU Taxonomy Data"
              :message="postEuTaxonomyDataForFinancialsResponse.data"
              :messageId="messageCount"
            />
            <FailedUpload v-else msg="EU Taxonomy Data" :messageId="messageCount" />
          </template>
        </div>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";
import FileUpload from "primevue/fileupload";
import PrimeButton from "primevue/button";
import MultiSelect from "primevue/multiselect";
import KPIfieldsSet from "@/components/forms/parts/kpiSelection/KPIfieldsSet.vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject, ref } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import {
  euTaxonomyKPIsModel,
  euTaxonomyKpiInfoMappings,
  euTaxonomyKpiNameMappings,
} from "@/components/forms/parts/kpiSelection/euTaxonomyKPIsModel.js";
import CheckBoxCustom from "@/components/forms/parts/CheckBoxCustom.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
      fileUpload: ref<typeof FileUpload>(),
    };
  },
  name: "CreateEUTaxonomyForFinancials",
  components: {
    DataPointFormElement,
    FailedUpload,
    FormKit,
    SuccessUpload,
    UploadFormHeader,
    Card,
    FileUpload,
    PrimeButton,
    Calendar,
    MultiSelect,
    YesNoComponent,
    KPIfieldsSet,
    CheckBoxCustom,
  },

  data: () => ({
    formInputsModel: {},
    files: [],
    fiscalYearEnd: "",
    convertedFiscalYearEnd: "",
    assuranceData: [
      { label: "None", value: "None" },
      { label: "LimitedAssurance", value: "LimitedAssurance" },
      { label: "ReasonableAssurance", value: "ReasonableAssurance" },
    ],
    maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,

    innerClass: {
      "formkit-inner": false,
      "p-inputwrapper": true,
    },
    inputClass: {
      "formkit-input": false,
      "p-inputtext": true,
      "w-full": true,
    },
    postEuTaxonomyDataForFinancialsProcessed: false,
    messageCount: 0,
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    euTaxonomyKPIsModel,
    euTaxonomyKpiNameMappings,
    euTaxonomyKpiInfoMappings,
    kpisModel: [
      { label: "Industrial Companies KPIs", value: "industrialCompanies" },
      { label: "Financial Companies KPIs", value: "financialCompanies" },
      { label: "Credit Insitution KPIs", value: "creditInstitution" },
      { label: "Green Asset Ratio", value: "greenAssetRatio" },
      { label: "Insurance & Re-insurance KPIs", value: "insuranceReinsurance" },
    ],
    selectedKPIs: [],
    confirmedSelectedKPIs: [],
  }),
  watch: {
    fiscalYearEnd: function (newValue: Date) {
      if (newValue) {
        this.convertedFiscalYearEnd = this.converteDateFormat(newValue);
      } else {
        this.convertedFiscalYearEnd = "";
      }
    },
  },
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
    converteDateFormat(date) {
      return `${date.getFullYear()}-${("0" + (date.getMonth() + 1).toString()).slice(-2)}-${(
        "0" + date.getDate().toString()
      ).slice(-2)}`;
    },
    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */

    postEuTaxonomyDataForFinancials() {
      console.log("------>", this.formInputsModel);
    },

    // async postEuTaxonomyDataForFinancials(): Promise<void> {
    //   try {
    //     this.postEuTaxonomyDataForFinancialsProcessed = false;
    //     this.messageCount++;
    //     const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
    //       assertDefined(this.getKeycloakPromise)()
    //     ).getEuTaxonomyDataForFinancialsControllerApi();
    //     this.postEuTaxonomyDataForFinancialsResponse =
    //       await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
    //         this.formInputsModel
    //       );
    //     this.$formkit.reset("createEuTaxonomyForFinancialsForm");
    //   } catch (error) {
    //     this.postEuTaxonomyDataForFinancialsResponse = null;
    //     console.error(error);
    //   } finally {
    //     this.postEuTaxonomyDataForFinancialsProcessed = true;
    //   }
    // },

    onSelectedFiles(event) {
      console.log("event", event);
      this.files = [...this.files, ...event.files];
      console.log("this.files", this.files);
    },

    onRemoveTemplatingFile(file, fileRemoveCallback: void, index: number) {
      fileRemoveCallback(index);
      this.files = this.files.filter((el) => {
        return el.name !== file.name;
      });
    },

    confirmeSelectedKPIs() {
      console.log("confirmedSelectedKPIs");
      this.confirmedSelectedKPIs = this.selectedKPIs;
    },

    removeKpisSection(value: string) {
      this.confirmedSelectedKPIs = this.confirmedSelectedKPIs.filter(
        (el: { label: string; value: string }) => el.value !== value
      );
      this.selectedKPIs = this.confirmedSelectedKPIs;
    },

    formatSize(bytes: number) {
      if (bytes === 0) {
        return "0 B";
      }
      const k = 1000,
        dm = 3,
        sizes = ["B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"],
        i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)).toString() + " " + sizes[i];
    },
  },
});
</script>
