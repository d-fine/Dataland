<template>
  <Card class="col-12 page-wrapper-card p-3">
    <template #title> New Dataset - LkSG </template>
    <template #content>
      <div v-show="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading LkSG data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-show="!waitingForData" class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="lkSGDataModel"
            :actions="false"
            type="form"
            :id="formId"
            :name="formId"
            @submit="postLkSGData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID" disabled="true" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" disabled="true" />
            <FormKit type="group" name="data" label="data">
              <FormKit type="group" name="social" label="social">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">
                      {{ lksgSubAreasNameMappings._general }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                    <p>Please input all relevant basic information about the dataset</p>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="general" :label="lksgSubAreasNameMappings._general">
                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.dataDate"
                          :explanation="lksgKpisInfoMappings.dataDate"
                          :is-required="true"
                        />
                        <div class="lg:col-4 md:col-6 col-12">
                          <Calendar
                            data-test="lksgDataDate"
                            inputId="icon"
                            v-model="dataDate"
                            :showIcon="true"
                            dateFormat="D, M dd, yy"
                            :maxDate="new Date()"
                          />
                        </div>

                        <FormKit
                          type="text"
                          :validation-label="lksgKpisNameMappings.dataDate"
                          validation="required"
                          name="dataDate"
                          v-model="convertedDataDate"
                          :outer-class="{ 'hidden-input': true }"
                        />
                      </div>

                      <div class="form-field" data-test="lksgInScope">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.lksgInScope"
                          :explanation="lksgKpisInfoMappings.lksgInScope"
                          :is-required="true"
                        />
                        <FormKit
                          type="radio"
                          :validation-label="lksgKpisNameMappings.lksgInScope"
                          name="lksgInScope"
                          :options="['Yes', 'No']"
                          :outer-class="{
                            'yes-no-radio': true,
                          }"
                          :inner-class="{
                            'formkit-inner': false,
                          }"
                          :input-class="{
                            'formkit-input': false,
                            'p-radiobutton': true,
                          }"
                          validation="required"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.vatIdentificationNumber"
                          :explanation="lksgKpisInfoMappings.vatIdentificationNumber"
                          :is-required="true"
                        />
                        <FormKit
                          type="text"
                          :validation-label="lksgKpisNameMappings.vatIdentificationNumber"
                          validation="required|length:3"
                          name="vatIdentificationNumber"
                          :inner-class="{ short: true }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.numberOfEmployees"
                          :explanation="lksgKpisInfoMappings.numberOfEmployees"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          name="numberOfEmployees"
                          :validation-label="lksgKpisNameMappings.numberOfEmployees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                          :inner-class="{ short: true }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.shareOfTemporaryWorkers"
                          :explanation="lksgKpisInfoMappings.shareOfTemporaryWorkers"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          name="shareOfTemporaryWorkers"
                          :validation-label="lksgKpisNameMappings.shareOfTemporaryWorkers"
                          placeholder="Value %"
                          step="0.01"
                          min="0"
                          validation="required|number|between:0,100"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.totalRevenue"
                          :explanation="lksgKpisInfoMappings.totalRevenue"
                          :is-required="true"
                        />
                        <FormKit
                          type="number"
                          min="0"
                          :validation-label="lksgKpisNameMappings.totalRevenue"
                          validation="required|number|min:0"
                          name="totalRevenue"
                          placeholder="Value"
                          step="1"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.totalRevenueCurrency"
                          :explanation="lksgKpisInfoMappings.totalRevenueCurrency"
                          :is-required="true"
                        />
                        <FormKit
                          type="text"
                          name="totalRevenueCurrency"
                          :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                          placeholder="Currency"
                          validation="required"
                          :inner-class="{
                            short: true,
                          }"
                        />
                      </div>

                      <div class="form-field" data-test="IsYourCompanyManufacturingCompany">
                        <UploadFormHeader
                          :name="'Is your company a manufacturing company?'"
                          :explanation="lksgKpisInfoMappings.listOfProductionSites"
                          :is-required="true"
                        />
                        <FormKit
                          type="radio"
                          :ignore="true"
                          id="IsYourCompanyManufacturingCompany"
                          name="IsYourCompanyManufacturingCompany"
                          :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                          :options="['Yes', 'No']"
                          v-model="isYourCompanyManufacturingCompany"
                          validation="required"
                          :outer-class="{
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
                      </div>

                      <FormKit
                        type="list"
                        v-if="isYourCompanyManufacturingCompany !== 'No'"
                        :validation-label="lksgKpisNameMappings.totalRevenueCurrency"
                        name="listOfProductionSites"
                        label="listOfProductionSites"
                      >
                        <FormKit type="group" v-for="(item, index) in listOfProductionSites" :key="item.id">
                          <div
                            data-test="productionSiteSection"
                            class="productionSiteSection"
                            :class="isYourCompanyManufacturingCompany === 'No' ? 'p-disabled' : ''"
                          >
                            <em
                              data-test="removeItemFromlistOfProductionSites"
                              @click="removeItemFromlistOfProductionSites(item.id)"
                              class="material-icons close-section"
                              >close</em
                            >

                            <div class="form-field">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.productionSiteName"
                                :explanation="lksgKpisInfoMappings.productionSiteName"
                                :is-required="true"
                              />
                              <FormKit
                                type="text"
                                :validation-label="lksgKpisNameMappings.productionSiteName"
                                name="name"
                                validation="required"
                              />
                            </div>

                            <div class="form-field" data-test="isInHouseProductionOrIsContractProcessing">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.inHouseProductionOrContractProcessing"
                                :explanation="lksgKpisInfoMappings.inHouseProductionOrContractProcessing"
                                :is-required="true"
                              />
                              <FormKit
                                type="radio"
                                name="isInHouseProductionOrIsContractProcessing"
                                :validation-label="lksgKpisNameMappings.inHouseProductionOrContractProcessing"
                                :options="isInHouseProductionOrContractProcessingMap"
                                validation="required"
                                :outer-class="{
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
                            </div>

                            <div class="form-field">
                              <UploadFormHeader
                                :name="lksgKpisNameMappings.addressesOfProductionSites"
                                :explanation="lksgKpisInfoMappings.addressesOfProductionSites"
                                :is-required="true"
                              />

                              <FormKit
                                type="text"
                                name="streetAndHouseNumber"
                                validation="required"
                                :validation-label="lksgKpisNameMappings.addressesOfProductionSites"
                                placeholder="Street, House number"
                              />
                              <div class="next-to-each-other">
                                <FormKit
                                  type="select"
                                  name="country"
                                  validation-label="Country"
                                  validation="required"
                                  placeholder="Country"
                                  :options="allCountry"
                                />
                                <FormKit
                                  type="text"
                                  name="city"
                                  validation-label="City"
                                  validation="required"
                                  placeholder="City"
                                />
                                <FormKit
                                  type="text"
                                  validation="required"
                                  validation-label="Postcode"
                                  name="postalCode"
                                  placeholder="postalCode"
                                />
                              </div>
                            </div>

                            <div class="form-field">
                              <div class="form-field-label">
                                <h5>List Of Goods Or Services</h5>
                                <em
                                  class="material-icons info-icon"
                                  aria-hidden="true"
                                  title="listOfGoodsOrServices"
                                  v-tooltip.top="{
                                    value: lksgKpisInfoMappings['listOfGoodsOrServices']
                                      ? lksgKpisInfoMappings['listOfGoodsOrServices']
                                      : '',
                                  }"
                                  >info</em
                                >
                                <PrimeButton
                                  :disabled="listOfProductionSites[index].listOfGoodsOrServicesString === ''"
                                  @click="addNewItemsTolistOfProductionSites(index)"
                                  label="Add"
                                  class="p-button-text"
                                  icon="pi pi-plus"
                                ></PrimeButton>
                              </div>
                              <FormKit
                                data-test="listOfGoodsOrServices"
                                type="text"
                                :ignore="true"
                                v-model="listOfProductionSites[index].listOfGoodsOrServicesString"
                                placeholder="Add comma (,) for more than one value"
                              />
                              <FormKit
                                v-model="listOfProductionSites[index].listOfGoodsOrServices"
                                type="list"
                                label="list of goods or services"
                                name="listOfGoodsOrServices"
                              />
                              <div class="">
                                <span
                                  class="form-list-item"
                                  :key="element"
                                  v-for="element in item.listOfGoodsOrServices"
                                >
                                  {{ element }}
                                  <em
                                    @click="removeItemFromlistOfGoodsOrServices(index, element)"
                                    class="material-icons"
                                    >close</em
                                  >
                                </span>
                              </div>
                            </div>
                          </div>
                        </FormKit>
                        <PrimeButton
                          data-test="ADD-NEW-Production-Site-button"
                          label="ADD NEW Production Site"
                          class="p-button-text"
                          :disabled="isYourCompanyManufacturingCompany === 'No'"
                          icon="pi pi-plus"
                          @click="addNewProductionSite"
                        />
                      </FormKit>
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="childLabour" class="anchor title">
                      {{ lksgSubAreasNameMappings.childLabour }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="childLabour">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employeeUnder18']"
                        :info="lksgKpisInfoMappings['employeeUnder18']"
                        :name="'employeeUnder18'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employeeUnder15']"
                        :info="lksgKpisInfoMappings['employeeUnder15']"
                        :name="'employeeUnder15'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employeeUnder18Apprentices']"
                        :info="lksgKpisInfoMappings['employeeUnder18Apprentices']"
                        :name="'employeeUnder18Apprentices'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePrevention']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePrevention']"
                        :name="'employmentUnderLocalMinimumAgePrevention'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionEmploymentContracts']
                        "
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionEmploymentContracts']"
                        :name="'employmentUnderLocalMinimumAgePreventionEmploymentContracts'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionJobDescription']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionJobDescription']"
                        :name="'employmentUnderLocalMinimumAgePreventionJobDescription'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionIdentityDocuments']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionIdentityDocuments']"
                        :name="'employmentUnderLocalMinimumAgePreventionIdentityDocuments'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionTraining']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionTraining']"
                        :name="'employmentUnderLocalMinimumAgePreventionTraining'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge']
                        "
                        :info="
                          lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge']
                        "
                        :name="'employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="forcedLabourSlaveryAndDebtBondage" class="anchor title">
                      {{ lksgSubAreasNameMappings.forcedLabourSlaveryAndDebtBondage }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="forcedLabourSlaveryAndDebtBondage">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPrevention']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPrevention']"
                        :name="'forcedLabourAndSlaveryPrevention'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionEmploymentContracts']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionEmploymentContracts']"
                        :name="'forcedLabourAndSlaveryPreventionEmploymentContracts'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionIdentityDocuments']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionIdentityDocuments']"
                        :name="'forcedLabourAndSlaveryPreventionIdentityDocuments'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionFreeMovement']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionFreeMovement']"
                        :name="'forcedLabourAndSlaveryPreventionFreeMovement'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets']
                        "
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets']"
                        :name="'forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionTraining']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionTraining']"
                        :name="'forcedLabourAndSlaveryPreventionTraining'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['documentedWorkingHoursAndWages']"
                        :info="lksgKpisInfoMappings['documentedWorkingHoursAndWages']"
                        :name="'documentedWorkingHoursAndWages'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['adequateLivingWage']"
                        :info="lksgKpisInfoMappings['adequateLivingWage']"
                        :name="'adequateLivingWage'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['regularWagesProcessFlow']"
                        :info="lksgKpisInfoMappings['regularWagesProcessFlow']"
                        :name="'regularWagesProcessFlow'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['fixedHourlyWages']"
                        :info="lksgKpisInfoMappings['fixedHourlyWages']"
                        :name="'fixedHourlyWages'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="evidenceCertificatesAndAttestations" class="anchor title">
                      {{ lksgSubAreasNameMappings.evidenceCertificatesAndAttestations }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="evidenceCertificatesAndAttestations">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['iso26000']"
                        :info="lksgKpisInfoMappings['iso26000']"
                        :name="'iso26000'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['sa8000Certification']"
                        :info="lksgKpisInfoMappings['sa8000Certification']"
                        :name="'sa8000Certification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['smetaSocialAuditConcept']"
                        :info="lksgKpisInfoMappings['smetaSocialAuditConcept']"
                        :name="'smetaSocialAuditConcept'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['betterWorkProgramCertificate']"
                        :info="lksgKpisInfoMappings['betterWorkProgramCertificate']"
                        :name="'betterWorkProgramCertificate'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['iso45001Certification']"
                        :info="lksgKpisInfoMappings['iso45001Certification']"
                        :name="'iso45001Certification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['iso14000Certification']"
                        :info="lksgKpisInfoMappings['iso14000Certification']"
                        :name="'iso14000Certification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['emasCertification']"
                        :info="lksgKpisInfoMappings['emasCertification']"
                        :name="'emasCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['iso37001Certification']"
                        :info="lksgKpisInfoMappings['iso37001Certification']"
                        :name="'iso37001Certification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['iso37301Certification']"
                        :info="lksgKpisInfoMappings['iso37301Certification']"
                        :name="'iso37301Certification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['riskManagementSystemCertification']"
                        :info="lksgKpisInfoMappings['riskManagementSystemCertification']"
                        :name="'riskManagementSystemCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['amforiBsciAuditReport']"
                        :info="lksgKpisInfoMappings['amforiBsciAuditReport']"
                        :name="'amforiBsciAuditReport'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['initiativeClauseSocialCertification']"
                        :info="lksgKpisInfoMappings['initiativeClauseSocialCertification']"
                        :name="'initiativeClauseSocialCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['responsibleBusinessAssociationCertification']"
                        :info="lksgKpisInfoMappings['responsibleBusinessAssociationCertification']"
                        :name="'responsibleBusinessAssociationCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['fairLabourAssociationCertification']"
                        :info="lksgKpisInfoMappings['fairLabourAssociationCertification']"
                        :name="'fairLabourAssociationCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['fairWorkingConditionsPolicy']"
                        :info="lksgKpisInfoMappings['fairWorkingConditionsPolicy']"
                        :name="'fairWorkingConditionsPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['fairAndEthicalRecruitmentPolicy']"
                        :info="lksgKpisInfoMappings['fairAndEthicalRecruitmentPolicy']"
                        :name="'fairAndEthicalRecruitmentPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['equalOpportunitiesAndNondiscriminationPolicy']"
                        :info="lksgKpisInfoMappings['equalOpportunitiesAndNondiscriminationPolicy']"
                        :name="'equalOpportunitiesAndNondiscriminationPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['healthAndSafetyPolicy']"
                        :info="lksgKpisInfoMappings['healthAndSafetyPolicy']"
                        :name="'healthAndSafetyPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['complaintsAndGrievancesPolicy']"
                        :info="lksgKpisInfoMappings['complaintsAndGrievancesPolicy']"
                        :name="'complaintsAndGrievancesPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['forcedLabourPolicy']"
                        :info="lksgKpisInfoMappings['forcedLabourPolicy']"
                        :name="'forcedLabourPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['childLabourPolicy']"
                        :info="lksgKpisInfoMappings['childLabourPolicy']"
                        :name="'childLabourPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['environmentalImpactPolicy']"
                        :info="lksgKpisInfoMappings['environmentalImpactPolicy']"
                        :name="'environmentalImpactPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['supplierCodeOfConduct']"
                        :info="lksgKpisInfoMappings['supplierCodeOfConduct']"
                        :name="'supplierCodeOfConduct'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="grievanceMechanism" class="anchor title">
                      {{ lksgSubAreasNameMappings.grievanceMechanism }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="grievanceMechanism">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['grievanceHandlingMechanism']"
                        :info="lksgKpisInfoMappings['grievanceHandlingMechanism']"
                        :name="'grievanceHandlingMechanism'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['grievanceHandlingMechanismUsedForReporting']"
                        :info="lksgKpisInfoMappings['grievanceHandlingMechanismUsedForReporting']"
                        :name="'grievanceHandlingMechanismUsedForReporting'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['legalProceedings']"
                        :info="lksgKpisInfoMappings['legalProceedings']"
                        :name="'legalProceedings'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 p-3 topicLabel">
                    <h4 id="osh" class="anchor title">{{ lksgSubAreasNameMappings.osh }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="osh">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshMonitoring']"
                        :info="lksgKpisInfoMappings['oshMonitoring']"
                        :name="'oshMonitoring'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicy']"
                        :info="lksgKpisInfoMappings['oshPolicy']"
                        :name="'oshPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyPersonalProtectiveEquipment']"
                        :info="lksgKpisInfoMappings['oshPolicyPersonalProtectiveEquipment']"
                        :name="'oshPolicyPersonalProtectiveEquipment'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyMachineSafety']"
                        :info="lksgKpisInfoMappings['oshPolicyMachineSafety']"
                        :name="'oshPolicyMachineSafety'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyDisasterBehaviouralResponse']"
                        :info="lksgKpisInfoMappings['oshPolicyDisasterBehaviouralResponse']"
                        :name="'oshPolicyDisasterBehaviouralResponse'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyAccidentsBehaviouralResponse']"
                        :info="lksgKpisInfoMappings['oshPolicyAccidentsBehaviouralResponse']"
                        :name="'oshPolicyAccidentsBehaviouralResponse'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyWorkplaceErgonomics']"
                        :info="lksgKpisInfoMappings['oshPolicyWorkplaceErgonomics']"
                        :name="'oshPolicyWorkplaceErgonomics'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyHandlingChemicalsAndOtherHazardousSubstances']"
                        :info="lksgKpisInfoMappings['oshPolicyHandlingChemicalsAndOtherHazardousSubstances']"
                        :name="'oshPolicyHandlingChemicalsAndOtherHazardousSubstances'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyFireProtection']"
                        :info="lksgKpisInfoMappings['oshPolicyFireProtection']"
                        :name="'oshPolicyFireProtection'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyWorkingHours']"
                        :info="lksgKpisInfoMappings['oshPolicyWorkingHours']"
                        :name="'oshPolicyWorkingHours'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyTrainingAddressed']"
                        :info="lksgKpisInfoMappings['oshPolicyTrainingAddressed']"
                        :name="'oshPolicyTrainingAddressed'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshPolicyTraining']"
                        :info="lksgKpisInfoMappings['oshPolicyTraining']"
                        :name="'oshPolicyTraining'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshManagementSystem']"
                        :info="lksgKpisInfoMappings['oshManagementSystem']"
                        :name="'oshManagementSystem'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshManagementSystemInternationalCertification']"
                        :info="lksgKpisInfoMappings['oshManagementSystemInternationalCertification']"
                        :name="'oshManagementSystemInternationalCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshManagementSystemNationalCertification']"
                        :info="lksgKpisInfoMappings['oshManagementSystemNationalCertification']"
                        :name="'oshManagementSystemNationalCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['workplaceAccidentsUnder10']"
                        :info="lksgKpisInfoMappings['workplaceAccidentsUnder10']"
                        :name="'workplaceAccidentsUnder10'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['oshTraining']"
                        :info="lksgKpisInfoMappings['oshTraining']"
                        :name="'oshTraining'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="freedomOfAssociation" class="anchor title">
                      {{ lksgSubAreasNameMappings.freedomOfAssociation }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="freedomOfAssociation">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['freedomOfAssociation']"
                        :info="lksgKpisInfoMappings['freedomOfAssociation']"
                        :name="'freedomOfAssociation'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['discriminationForTradeUnionMembers']"
                        :info="lksgKpisInfoMappings['discriminationForTradeUnionMembers']"
                        :name="'discriminationForTradeUnionMembers'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['freedomOfOperationForTradeUnion']"
                        :info="lksgKpisInfoMappings['freedomOfOperationForTradeUnion']"
                        :name="'freedomOfOperationForTradeUnion'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['freedomOfAssociationTraining']"
                        :info="lksgKpisInfoMappings['freedomOfAssociationTraining']"
                        :name="'freedomOfAssociationTraining'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['worksCouncil']"
                        :info="lksgKpisInfoMappings['worksCouncil']"
                        :name="'worksCouncil'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="humanRights" class="anchor title">
                      {{ lksgSubAreasNameMappings.humanRights }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="humanRights">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['diversityAndInclusionRole']"
                        :info="lksgKpisInfoMappings['diversityAndInclusionRole']"
                        :name="'diversityAndInclusionRole'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['preventionOfMistreatments']"
                        :info="lksgKpisInfoMappings['preventionOfMistreatments']"
                        :name="'preventionOfMistreatments'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['equalOpportunitiesOfficer']"
                        :info="lksgKpisInfoMappings['equalOpportunitiesOfficer']"
                        :name="'equalOpportunitiesOfficer'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['riskOfHarmfulPollution']"
                        :info="lksgKpisInfoMappings['riskOfHarmfulPollution']"
                        :name="'riskOfHarmfulPollution'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['unlawfulEvictionAndTakingOfLand']"
                        :info="lksgKpisInfoMappings['unlawfulEvictionAndTakingOfLand']"
                        :name="'unlawfulEvictionAndTakingOfLand'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['useOfPrivatePublicSecurityForces']"
                        :info="lksgKpisInfoMappings['useOfPrivatePublicSecurityForces']"
                        :name="'useOfPrivatePublicSecurityForces'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights']
                        "
                        :info="lksgKpisInfoMappings['useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights']"
                        :name="'useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights'"
                      />
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <FormKit type="group" name="governance" label="governance">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="socialAndEmployeeMatters" class="anchor title">
                      {{ lksgSubAreasNameMappings.socialAndEmployeeMatters }}
                    </h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="socialAndEmployeeMatters">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['responsibilitiesForFairWorkingConditions']"
                        :info="lksgKpisInfoMappings['responsibilitiesForFairWorkingConditions']"
                        :name="'responsibilitiesForFairWorkingConditions'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="environment" class="anchor title">
                      {{ lksgSubAreasNameMappings.environment }}
                    </h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="environment">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['responsibilitiesForTheEnvironment']"
                        :info="lksgKpisInfoMappings['responsibilitiesForTheEnvironment']"
                        :name="'responsibilitiesForTheEnvironment'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="osh_governance" class="anchor title">{{ lksgSubAreasNameMappings.osh }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="osh">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['responsibilitiesForOccupationalSafety']"
                        :info="lksgKpisInfoMappings['responsibilitiesForOccupationalSafety']"
                        :name="'responsibilitiesForOccupationalSafety'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="riskManagement" class="anchor title">
                      {{ lksgSubAreasNameMappings.riskManagement }}
                    </h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="riskManagement">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['riskManagementSystem']"
                        :info="lksgKpisInfoMappings['riskManagementSystem']"
                        :name="'riskManagementSystem'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 p-3 topicLabel">
                    <h4 id="codeOfConduct" class="anchor title">
                      {{ lksgSubAreasNameMappings.codeOfConduct }}
                    </h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="codeOfConduct">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['codeOfConduct']"
                        :info="lksgKpisInfoMappings['codeOfConduct']"
                        :name="'codeOfConduct'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['codeOfConductRiskManagementTopics']"
                        :info="lksgKpisInfoMappings['codeOfConductRiskManagementTopics']"
                        :name="'codeOfConductRiskManagementTopics'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['codeOfConductTraining']"
                        :info="lksgKpisInfoMappings['codeOfConductTraining']"
                        :name="'codeOfConductTraining'"
                      />
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <FormKit type="group" name="environmental" label="environmental">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="waste" class="anchor title">{{ lksgSubAreasNameMappings.waste }}</h4>
                    <div class="p-badge badge-green"><span>ENVIRONMENTAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="waste">
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryWasteHandling']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryWasteHandling']"
                        :name="'mercuryAndMercuryWasteHandling'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryWasteHandlingPolicy']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryWasteHandlingPolicy']"
                        :name="'mercuryAndMercuryWasteHandlingPolicy'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['chemicalHandling']"
                        :info="lksgKpisInfoMappings['chemicalHandling']"
                        :name="'chemicalHandling'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['environmentalManagementSystem']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystem']"
                        :name="'environmentalManagementSystem'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['environmentalManagementSystemInternationalCertification']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystemInternationalCertification']"
                        :name="'environmentalManagementSystemInternationalCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['environmentalManagementSystemNationalCertification']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystemNationalCertification']"
                        :name="'environmentalManagementSystemNationalCertification'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['legalRestrictedWaste']"
                        :info="lksgKpisInfoMappings['legalRestrictedWaste']"
                        :name="'legalRestrictedWaste'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['legalRestrictedWasteProcesses']"
                        :info="lksgKpisInfoMappings['legalRestrictedWasteProcesses']"
                        :name="'legalRestrictedWasteProcesses'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandling']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandling']"
                        :name="'mercuryAddedProductsHandling'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandlingRiskOfExposure']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandlingRiskOfExposure']"
                        :name="'mercuryAddedProductsHandlingRiskOfExposure'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandlingRiskOfDisposal']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandlingRiskOfDisposal']"
                        :name="'mercuryAddedProductsHandlingRiskOfDisposal'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryCompoundsProductionAndUse']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryCompoundsProductionAndUse']"
                        :name="'mercuryAndMercuryCompoundsProductionAndUse'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure']"
                        :name="'mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUse']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUse']"
                        :name="'persistentOrganicPollutantsProductionAndUse'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskOfExposure']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskOfExposure']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskOfExposure'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskOfDisposal']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskOfDisposal']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskOfDisposal'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseTransboundaryMovements']
                        "
                        :info="
                          lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseTransboundaryMovements']
                        "
                        :name="'persistentOrganicPollutantsProductionAndUseTransboundaryMovements'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskForImportingState']
                        "
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskForImportingState']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskForImportingState'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein']
                        "
                        :info="lksgKpisInfoMappings['hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein']"
                        :name="'hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein'"
                      />
                      <YesNoComponent
                        :displayName="
                          lksgKpisNameMappings['hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein']
                        "
                        :info="lksgKpisInfoMappings['hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein']"
                        :name="'hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['hazardousWasteDisposal']"
                        :info="lksgKpisInfoMappings['hazardousWasteDisposal']"
                        :name="'hazardousWasteDisposal'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['hazardousWasteDisposalRiskOfImport']"
                        :info="lksgKpisInfoMappings['hazardousWasteDisposalRiskOfImport']"
                        :name="'hazardousWasteDisposalRiskOfImport'"
                      />
                      <YesNoComponent
                        :displayName="lksgKpisNameMappings['hazardousAndOtherWasteImport']"
                        :info="lksgKpisInfoMappings['hazardousAndOtherWasteImport']"
                        :name="'hazardousAndOtherWasteImport'"
                      />
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </FormKit>
          </FormKit>
        </div>
        <SubmitSideBar>
          <SubmitButton :formId="formId" />
          <div v-if="postLkSGDataProcessed">
            <SuccessUpload v-if="uploadSucceded" :messageId="messageCounter" />
            <FailedUpload v-else :message="message" :messageId="messageCounter" />
          </div>
          <h4 id="topicTitles" class="title pt-3">On this page</h4>
          <ul>
            <li><a @click="smoothScroll('#general')">General</a></li>
            <li><a @click="smoothScroll('#childLabour')">Child labour</a></li>
            <li>
              <a @click="smoothScroll('#forcedLabourSlaveryAndDebtBondage')">Forced labour, slavery and debt bondage</a>
            </li>
            <li>
              <a @click="smoothScroll('#evidenceCertificatesAndAttestations')"
                >Evidence, certificates and attestations</a
              >
            </li>
            <li><a @click="smoothScroll('#grievanceMechanism')">Grievance mechanism</a></li>
            <li><a @click="smoothScroll('#osh')">OSH</a></li>
            <li><a @click="smoothScroll('#freedomOfAssociation')">Freedom of association</a></li>
            <li><a @click="smoothScroll('#humanRights')">Human rights</a></li>
            <li><a @click="smoothScroll('#socialAndEmployeeMatters')">Social and employee matters</a></li>
            <li><a @click="smoothScroll('#environment')">Environment</a></li>
            <li><a @click="smoothScroll('#riskManagement')">Risk management</a></li>
            <li><a @click="smoothScroll('#codeOfConduct')">Code of Conduct</a></li>
            <li><a @click="smoothScroll('#waste')">Waste</a></li>
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
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import Tooltip from "primevue/tooltip";
import PrimeButton from "primevue/button";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import Calendar from "primevue/calendar";
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import {
  lksgKpisInfoMappings,
  lksgKpisNameMappings,
  lksgSubAreasNameMappings,
} from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { AxiosError } from "axios";
import { humanizeString } from "@/utils/StringHumanizer";
import { CompanyAssociatedDataLksgData, InHouseProductionOrContractProcessing } from "@clients/backend";
import { useRoute } from "vue-router";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import { smoothScroll } from "@/utils/smoothScroll";
import { checkCustomInputs } from "@/utils/validationsUtils";
import SubmitButton from "@/components/forms/parts/SubmitButton.vue";
import SubmitSideBar from "@/components/forms/parts/SubmitSideBar.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: {
    SubmitButton,
    SubmitSideBar,
    UploadFormHeader,
    SuccessUpload,
    FailedUpload,
    FormKit,
    Card,
    PrimeButton,
    YesNoComponent,
    Calendar,
  },
  directives: {
    tooltip: Tooltip,
  },

  data() {
    return {
      formId: "createLkSGForm",
      isYourCompanyManufacturingCompany: "No",
      listOfProductionSites: [
        {
          id: 0,
          listOfGoodsOrServices: [] as string[],
          listOfGoodsOrServicesString: "",
        },
      ],
      idCounter: 0,
      allCountry: getAllCountryNamesWithCodes(),
      waitingForData: false,
      dataDate: undefined as Date | undefined,
      lkSGDataModel: {} as CompanyAssociatedDataLksgData,
      route: useRoute(),
      message: "",
      uploadSucceded: false,
      postLkSGDataProcessed: false,
      messageCounter: 0,
      lksgKpisInfoMappings,
      lksgKpisNameMappings,
      lksgSubAreasNameMappings,
      isInHouseProductionOrContractProcessingMap: Object.fromEntries(
        new Map<string, string>([
          [
            InHouseProductionOrContractProcessing.InHouseProduction,
            humanizeString(InHouseProductionOrContractProcessing.InHouseProduction),
          ],
          [
            InHouseProductionOrContractProcessing.ContractProcessing,
            humanizeString(InHouseProductionOrContractProcessing.ContractProcessing),
          ],
        ])
      ),
      smoothScroll,
      checkCustomInputs,
      updatingData: false,
    };
  },
  computed: {
    yearOfDataDate: {
      get(): string {
        return this.dataDate?.getFullYear()?.toString() || "";
      },
      set() {
        // IGNORED
      },
    },
    convertedDataDate: {
      get(): string {
        if (this.dataDate) {
          return getHyphenatedDate(this.dataDate);
        } else {
          return "";
        }
      },
      set() {
        // IGNORED
      },
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
    if (dataId !== undefined && typeof dataId === "string" && dataId !== "") {
      void this.loadLKSGData(dataId);
    }
  },
  methods: {
    /**
     * Loads the LKGS-Dataset identified by the provided dataId and pre-configures the form to contain the data
     * from the dataset
     *
     * @param dataId the id of the dataset to load
     */
    async loadLKSGData(dataId: string): Promise<void> {
      this.waitingForData = true;
      const lkSGDataControllerApi = await new ApiClientProvider(
        assertDefined(this.getKeycloakPromise)()
      ).getLksgDataControllerApi();

      const dataResponse = await lkSGDataControllerApi.getCompanyAssociatedLksgData(dataId);
      const lksgDataset = dataResponse.data;
      const numberOfProductionSites = lksgDataset.data?.social?.general?.listOfProductionSites?.length || 0;
      if (numberOfProductionSites > 0) {
        this.isYourCompanyManufacturingCompany = "Yes";
        const productionSites = assertDefined(lksgDataset.data?.social?.general?.listOfProductionSites);
        this.listOfProductionSites = [];
        this.idCounter = numberOfProductionSites;
        for (let i = 0; i < numberOfProductionSites; i++) {
          this.listOfProductionSites.push({
            id: i,
            listOfGoodsOrServices: productionSites[i].listOfGoodsOrServices || [],
            listOfGoodsOrServicesString: "",
          });
        }
      }
      const dataDateFromDataset = lksgDataset.data?.social?.general?.dataDate;
      if (dataDateFromDataset) {
        this.dataDate = new Date(dataDateFromDataset);
      }
      this.lkSGDataModel = lksgDataset;
      this.waitingForData = false;
    },
    /**
     * Sends data to add LkSG data
     */
    async postLkSGData(): Promise<void> {
      this.messageCounter++;
      try {
        const lkSGDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getLksgDataControllerApi();
        await lkSGDataControllerApi.postCompanyAssociatedLksgData(this.lkSGDataModel);
        this.$formkit.reset(this.formId);
        this.isYourCompanyManufacturingCompany = "No";
        this.listOfProductionSites = [
          {
            id: 0,
            listOfGoodsOrServices: [],
            listOfGoodsOrServicesString: "",
          },
        ];
        this.idCounter = 0;
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
        this.postLkSGDataProcessed = true;
      }
    },

    /**
     * Adds a new Object to the ProductionSite array
     */
    addNewProductionSite() {
      this.idCounter++;
      this.listOfProductionSites.push({
        id: this.idCounter,
        listOfGoodsOrServices: [],
        listOfGoodsOrServicesString: "",
      });
    },

    /**
     * Remove Object from ProductionSite array
     *
     * @param id - the id of the object in the array
     */
    removeItemFromlistOfProductionSites(id: number) {
      this.listOfProductionSites = this.listOfProductionSites.filter((el) => el.id !== id);
    },

    /**
     * Adds a new item to the list of Production Sites Goods Or Services
     *
     * @param index - index of the element in the listOfProductionSites array
     */
    addNewItemsTolistOfProductionSites(index: number) {
      const items = this.listOfProductionSites[index].listOfGoodsOrServicesString.split(";").map((item) => item.trim());
      this.listOfProductionSites[index].listOfGoodsOrServices = [
        ...this.listOfProductionSites[index].listOfGoodsOrServices,
        ...items,
      ];
      this.listOfProductionSites[index].listOfGoodsOrServicesString = "";
    },

    /**
     * Remove item from list of Production Sites Goods Or Services
     *
     * @param index - index of the element in the listOfProductionSites array
     * @param item - which item is to be deleted
     */
    removeItemFromlistOfGoodsOrServices(index: number, item: string) {
      this.listOfProductionSites[index].listOfGoodsOrServices = this.listOfProductionSites[
        index
      ].listOfGoodsOrServices.filter((el) => el !== item);
    },
  },
});
</script>
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 100px;
}
</style>
