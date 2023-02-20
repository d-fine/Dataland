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

                <div class="col-9 formFields uploaded-files">
                  <h4 class="mt-0">Select company reports</h4>
                  <FileUpload
                      name="demo[]"
                      url="./upload"
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
                        <PrimeButton @click="chooseCallback()" icon="pi pi-upload" class="m-0" label="UPLOAD REPORTS" />
                      </div>
                    </div>
                  </template>
                  <template #content="{ uploadedFiles, removeUploadedFileCallback }">
                    <div v-if="uploadedFiles.length > 0">
                        <div v-for="(file, index) of uploadedFiles" :key="file.name + file.type + file.size" class="flex w-full align-items-center file-upload-item">
                          <span class="font-semibold flex-1">{{ file.name }}</span>
                          <div class="mx-2 text-black-alpha-50">{{ formatSize(file.size) }}</div>
                          <Badge value="Completed" class="mt-3" severity="success" />
                          <PrimeButton icon="pi pi-times" @click="removeUploadedFileCallback(index)" class="p-button-rounded" />
                        </div>
                    </div>
                  </template>
                  </FileUpload>

                </div>

                <div v-if="files[0]?.name" class="col-9 formFields">
                  <h4 class="mt-0">{{ files[0].name }}</h4>
                  <div class="form-field">
                    <div class="lg:col-6 md:col-6 col-12">
                      <Calendar
                          data-test="lksgDataDate"
                          inputId="icon"
                          :showIcon="true"
                          dateFormat="D, M dd, yy"
                          :maxDate="new Date()"
                      />
                    </div>

                    <FormKit
                        type="text"
                        validation="required"
                        name="dataDate"
                        :outer-class="{ 'hidden-input': true }"
                    />
                  </div>
                </div>



                  <!-- rest -->

                  <FormKit
                    type="select"
                    name="financialServicesTypes"
                    multiple
                    validation="required"
                    label="Financial Services Types"
                    placeholder="Please choose"
                    :options="{
                      CreditInstitution: humanizeString('CreditInstitution'),
                      InsuranceOrReinsurance: humanizeString('InsuranceOrReinsurance'),
                      AssetManagement: humanizeString('AssetManagement'),
                      InvestmentFirm: humanizeString('InvestmentFirm'),
                    }"
                    help="Select all that apply by holding command (macOS) or control (PC)."
                  />
                  <FormKit type="group" name="assurance" label="Assurance">
                    <FormKit
                      type="select"
                      name="assurance"
                      label="Assurance"
                      placeholder="Please choose"
                      :options="{
                        None: humanizeString('None'),
                        LimitedAssurance: humanizeString('LimitedAssurance'),
                        ReasonableAssurance: humanizeString('ReasonableAssurance'),
                      }"
                    />
                  </FormKit>
                  <FormKit
                    type="radio"
                    name="reportingObligation"
                    label="Reporting Obligation"
                    :options="['Yes', 'No']"
                    :outer-class="{
                      'formkit-outer': false,
                    }"
                    :inner-class="{
                      'formkit-inner': false,
                    }"
                    :input-class="{
                      'formkit-input': false,
                      'p-radiobutton:': true,
                    }"
                  />
              </div>
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
import FileUpload from 'primevue/fileupload';
import PrimeButton from "primevue/button";
import { UPLOAD_MAX_FILE_SIZE_IN_BYTES } from "@/utils/Constants";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import Calendar from "primevue/calendar";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import { humanizeString } from "@/utils/StringHumanizer";
import { ApiClientProvider } from "@/services/ApiClients";
import Card from "primevue/card";
import DataPointFormElement from "@/components/forms/DataPointFormElement.vue";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
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
  },

  data: () => ({
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
    maxFileSize: UPLOAD_MAX_FILE_SIZE_IN_BYTES,
    messageCount: 0,
    formInputsModel: {},
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    files: [],
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
    /**
     * Creates a new EuTaxonomy-Financials framework entry for the current company
     * with the data entered in the form by using the Dataland API
     */
    async postEuTaxonomyDataForFinancials(): Promise<void> {
      try {
        this.postEuTaxonomyDataForFinancialsProcessed = false;
        this.messageCount++;
        const euTaxonomyDataForFinancialsControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getEuTaxonomyDataForFinancialsControllerApi();
        this.postEuTaxonomyDataForFinancialsResponse =
          await euTaxonomyDataForFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForFinancials(
            this.formInputsModel
          );
        this.$formkit.reset("createEuTaxonomyForFinancialsForm");
      } catch (error) {
        this.postEuTaxonomyDataForFinancialsResponse = null;
        console.error(error);
      } finally {
        this.postEuTaxonomyDataForFinancialsProcessed = true;
      }
    },

    onSelectedFiles(event) {
      console.log('event', event)
      this.files = event.files;
    },

    formatSize(bytes) {
      if (bytes === 0) {
        return '0 B';
      }
      let k = 1000,
          dm = 3,
          sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
          i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
    },

  },
});
</script>
