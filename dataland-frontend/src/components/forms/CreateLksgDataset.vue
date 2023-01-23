<template>
  <Card class="col-12">
    <template #title
      >New Dataset - LkSG
      <hr />
    </template>
    <template #content>
      <div class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="formInputsModel"
            :actions="false"
            type="form"
            id="createLkSGForm"
            @submit="postEuTaxonomyDataForFinancials"
            #default="{ state: { valid } }"
          >
            <div class="uploadFormSection grid">
              <div id="topicLabel" class="col-3 topicLabel">
                <h4 id="general" class="anchor title">General</h4>
                <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                <p>Please input all relevant basic information about the dataset</p>
              </div>

              <div id="formFields" class="col-9 formFields">
                <FormKit type="group" name="data" label="data">
                  <FormKit
                    type="date"
                    value="03-03-2018"
                    label="Birthday"
                    help="Enter your birth day"
                    validation="required"
                    validation-visibility="live"
                  />
                  <FormKit
                    type="radio"
                    name="lksgInScope"
                    label="LKSG in Scope"
                    :options="['Yes', 'No']"
                    :outer-class="{
                      'formkit-outer': false,
                      'yes-no-radio': true,
                    }"
                    :inner-class="{
                      'formkit-inner': false,
                    }"
                    :input-class="{
                      'formkit-input': false,
                      'p-radiobutton': true,
                    }"
                  />
                  <FormKit type="text" label="Company Legal Form" name="companyLegalForm" validation="required" />
                  <FormKit
                    type="number"
                    label="VAT Identification Number"
                    name="VATidentificationNumber"
                    validation="required|number"
                    step="1"
                  />
                  <FormKit
                    type="number"
                    label="Number Of Employees"
                    name="shareOfTemporaryWorkers"
                    placeholder="Value"
                    validation="required|number|between:0,100"
                    step="1"
                    :inner-class="{
                      short: true,
                    }"
                  />
                  <FormKit
                    type="number"
                    label="Share Of Temporary Workers"
                    name="shareOfTemporaryWorkers"
                    placeholder="Value %"
                    validation="required|number|between:0,100"
                    step="1"
                    :inner-class="{
                      short: true,
                    }"
                  />
                  <FormKit type="group" name="assurance" label="Assurance">
                    <div class="next-to-each-other">
                      <FormKit
                        type="number"
                        label="Total Revenue"
                        name="totalRevenue"
                        placeholder="Value"
                        validation="required|number"
                        step="1"
                      />
                      <FormKit type="select" label="&nbsp;" name="unit" placeholder="Unit" :options="['zł', 'USD']" />
                    </div>
                  </FormKit>
                  <FormKit
                    type="text"
                    label="Total Revenue Currency"
                    name="totalRevenueCurrency"
                    placeholder="Currency"
                    validation="required"
                    :inner-class="{
                      medium: true,
                    }"
                  />

                  <hr />

                  <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                    <template
                      v-for="fsType in [
                        'CreditInstitution',
                        'InsuranceOrReinsurance',
                        'AssetManagement',
                        'InvestmentFirm',
                      ]"
                      :key="fsType"
                    >
                      <div :name="fsType">
                        <FormKit type="group" :name="fsType">
                          <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                          <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                          <DataPointFormElement
                            name="taxonomyNonEligibleActivity"
                            label="Taxonomy Non Eligible Activity"
                          />
                          <DataPointFormElement name="derivatives" label="Derivatives" />
                          <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                          <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                        </FormKit>
                      </div>
                    </template>
                  </FormKit>
                </FormKit>
                <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
              </div>
            </div>
            <div class="uploadFormSection grid">
              <div id="topicLabel" class="col-3 topicLabel">
                <h4 id="childLabour" class="anchor title">Child Labour</h4>
                <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                <p>Please input all relevant basic information about the dataset</p>
              </div>

              <div id="formFields" class="col-9 formFields">
                <FormKit
                  type="text"
                  name="companyId"
                  label="Company ID"
                  placeholder="Company ID"
                  :model-value="companyID"
                  disabled="true"
                />
                <FormKit type="group" name="data" label="data">
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
                  <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                    <template
                      v-for="fsType in [
                        'CreditInstitution',
                        'InsuranceOrReinsurance',
                        'AssetManagement',
                        'InvestmentFirm',
                      ]"
                      :key="fsType"
                    >
                      <div :name="fsType">
                        <FormKit type="group" :name="fsType">
                          <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                          <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                          <DataPointFormElement
                            name="taxonomyNonEligibleActivity"
                            label="Taxonomy Non Eligible Activity"
                          />
                          <DataPointFormElement name="derivatives" label="Derivatives" />
                          <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                          <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                        </FormKit>
                      </div>
                    </template>
                  </FormKit>
                </FormKit>
                <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
              </div>
            </div>
            <div class="uploadFormSection grid">
              <div id="topicLabel" class="col-3 topicLabel">
                <h4 id="osh" class="anchor title">General</h4>
                <div class="p-badge badge-yellow"><span>OSH</span></div>
                <p>Please input all relevant basic information about the dataset</p>
              </div>

              <div id="formFields" class="col-9 formFields">
                <FormKit
                  type="date"
                  value="2011-01-01"
                  label="Birthday"
                  help="Enter your birth day"
                  validation="required|date_before:2010-01-01"
                  validation-visibility="live"
                />
                <FormKit
                  type="text"
                  name="companyId"
                  label="Company ID"
                  placeholder="Company ID"
                  :model-value="companyID"
                  disabled="true"
                />
                <FormKit type="group" name="data" label="data">
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
                  <FormKit type="group" name="eligibilityKpis" label="Eligibility KPIs">
                    <template
                      v-for="fsType in [
                        'CreditInstitution',
                        'InsuranceOrReinsurance',
                        'AssetManagement',
                        'InvestmentFirm',
                      ]"
                      :key="fsType"
                    >
                      <div :name="fsType">
                        <FormKit type="group" :name="fsType">
                          <h4>Eligibility KPIs ({{ humanizeString(fsType) }})</h4>
                          <DataPointFormElement name="taxonomyEligibleActivity" label="Taxonomy Eligible Activity" />
                          <DataPointFormElement
                            name="taxonomyNonEligibleActivity"
                            label="Taxonomy Non Eligible Activity"
                          />
                          <DataPointFormElement name="derivatives" label="Derivatives" />
                          <DataPointFormElement name="banksAndIssuers" label="Banks and Issuers" />
                          <DataPointFormElement name="investmentNonNfrd" label="Investment non Nfrd" />
                        </FormKit>
                      </div>
                    </template>
                  </FormKit>
                </FormKit>
                <FormKit type="submit" :disabled="!valid" label="Post EU-Taxonomy Dataset" name="postEUData" />
              </div>
            </div>
          </FormKit>
        </div>

        <div id="jumpLinks" class="col-3 text-left jumpLinks">
          <h4 id="topicTitles" class="title">On this page</h4>

          <ul>
            <li><a href="#general">General</a></li>
            <li><a href="#childLabour">Child labour</a></li>
            <li><a href="#forcedLabourSlaveryAndDebtBondage">Forced labour, slavery and debt bondage</a></li>
            <li><a href="#evidenceCertificatesAndAttestations">Evidence, certificates and attestations</a></li>
            <li><a href="#socialAndEmployeeMatters">Social and employee matters</a></li>
            <li><a href="#environment">Environment</a></li>
            <li><a href="#osh">OSH</a></li>
            <li><a href="#riskManagement">Risk management</a></li>
            <li><a href="#grievanceMechanism">Grievance mechanism</a></li>
            <li><a href="#codeOfConduct">Code of Conduct</a></li>
            <li><a href="#grievanceMechanism">Grievance mechanism</a></li>
            <li><a href="#freedomOfAssociation">Freedom of association</a></li>
            <li><a href="#humanRights">Human rights</a></li>
            <li><a href="#waste">Waste</a></li>
          </ul>
        </div>
      </div>

      <template v-if="postEuTaxonomyDataForFinancialsProcessed">
        <SuccessUpload
          v-if="postEuTaxonomyDataForFinancialsResponse"
          msg="EU Taxonomy Data"
          :data="postEuTaxonomyDataForFinancialsResponse.data"
          :messageCount="messageCount"
        />
        <FailedUpload v-else msg="EU Taxonomy Data" :messageCount="messageCount" />
      </template>
    </template>
  </Card>
</template>

<script lang="ts">
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import { FormKit } from "@formkit/vue";
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
  name: "CreateLksgDataset",
  components: { DataPointFormElement, FailedUpload, FormKit, SuccessUpload, Card },

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
    messageCount: 0,
    formInputsModel: {},
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  methods: {
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
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 300px;
}
</style>
