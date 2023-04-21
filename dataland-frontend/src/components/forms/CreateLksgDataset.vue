<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>New Dataset - LkSG</template>
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
            id="createLkSGForm"
            name="createLkSGForm"
            @submit="postLkSGData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :modelValue="companyID" disabled="true" />
            <FormKit type="hidden" name="reportingPeriod" v-model="yearOfDataDate" disabled="true" />
            <FormKit type="group" name="data" label="data">
              <FormKit type="group" name="social" label="social">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">{{ lksgSubAreasNameMappings._general }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                    <p>Please input all relevant basic information about the dataset</p>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="general" :label="lksgSubAreasNameMappings._general">
                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpisNameMappings.dataDate"
                          :explanation="lksgKpisInfoMappings.dataDate"
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
                    <h4 id="childLabour" class="anchor title">{{ lksgSubAreasNameMappings.childLabour }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="childLabour">
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employeeUnder18']"
                        :info="lksgKpisInfoMappings['employeeUnder18']"
                        :name="'employeeUnder18'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employeeUnder15']"
                        :info="lksgKpisInfoMappings['employeeUnder15']"
                        :name="'employeeUnder15'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employeeUnder18Apprentices']"
                        :info="lksgKpisInfoMappings['employeeUnder18Apprentices']"
                        :name="'employeeUnder18Apprentices'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePrevention']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePrevention']"
                        :name="'employmentUnderLocalMinimumAgePrevention'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionEmploymentContracts']
                        "
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionEmploymentContracts']"
                        :name="'employmentUnderLocalMinimumAgePreventionEmploymentContracts'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionJobDescription']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionJobDescription']"
                        :name="'employmentUnderLocalMinimumAgePreventionJobDescription'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionIdentityDocuments']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionIdentityDocuments']"
                        :name="'employmentUnderLocalMinimumAgePreventionIdentityDocuments'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['employmentUnderLocalMinimumAgePreventionTraining']"
                        :info="lksgKpisInfoMappings['employmentUnderLocalMinimumAgePreventionTraining']"
                        :name="'employmentUnderLocalMinimumAgePreventionTraining'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPrevention']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPrevention']"
                        :name="'forcedLabourAndSlaveryPrevention'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionEmploymentContracts']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionEmploymentContracts']"
                        :name="'forcedLabourAndSlaveryPreventionEmploymentContracts'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionIdentityDocuments']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionIdentityDocuments']"
                        :name="'forcedLabourAndSlaveryPreventionIdentityDocuments'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionFreeMovement']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionFreeMovement']"
                        :name="'forcedLabourAndSlaveryPreventionFreeMovement'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets']
                        "
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets']"
                        :name="'forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourAndSlaveryPreventionTraining']"
                        :info="lksgKpisInfoMappings['forcedLabourAndSlaveryPreventionTraining']"
                        :name="'forcedLabourAndSlaveryPreventionTraining'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['documentedWorkingHoursAndWages']"
                        :info="lksgKpisInfoMappings['documentedWorkingHoursAndWages']"
                        :name="'documentedWorkingHoursAndWages'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['adequateLivingWage']"
                        :info="lksgKpisInfoMappings['adequateLivingWage']"
                        :name="'adequateLivingWage'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['regularWagesProcessFlow']"
                        :info="lksgKpisInfoMappings['regularWagesProcessFlow']"
                        :name="'regularWagesProcessFlow'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['iso26000']"
                        :info="lksgKpisInfoMappings['iso26000']"
                        :name="'iso26000'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['sa8000Certification']"
                        :info="lksgKpisInfoMappings['sa8000Certification']"
                        :name="'sa8000Certification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['smetaSocialAuditConcept']"
                        :info="lksgKpisInfoMappings['smetaSocialAuditConcept']"
                        :name="'smetaSocialAuditConcept'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['betterWorkProgramCertificate']"
                        :info="lksgKpisInfoMappings['betterWorkProgramCertificate']"
                        :name="'betterWorkProgramCertificate'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['iso45001Certification']"
                        :info="lksgKpisInfoMappings['iso45001Certification']"
                        :name="'iso45001Certification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['iso14000Certification']"
                        :info="lksgKpisInfoMappings['iso14000Certification']"
                        :name="'iso14000Certification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['emasCertification']"
                        :info="lksgKpisInfoMappings['emasCertification']"
                        :name="'emasCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['iso37001Certification']"
                        :info="lksgKpisInfoMappings['iso37001Certification']"
                        :name="'iso37001Certification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['iso37301Certification']"
                        :info="lksgKpisInfoMappings['iso37301Certification']"
                        :name="'iso37301Certification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['riskManagementSystemCertification']"
                        :info="lksgKpisInfoMappings['riskManagementSystemCertification']"
                        :name="'riskManagementSystemCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['amforiBsciAuditReport']"
                        :info="lksgKpisInfoMappings['amforiBsciAuditReport']"
                        :name="'amforiBsciAuditReport'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['initiativeClauseSocialCertification']"
                        :info="lksgKpisInfoMappings['initiativeClauseSocialCertification']"
                        :name="'initiativeClauseSocialCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['responsibleBusinessAssociationCertification']"
                        :info="lksgKpisInfoMappings['responsibleBusinessAssociationCertification']"
                        :name="'responsibleBusinessAssociationCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['fairLabourAssociationCertification']"
                        :info="lksgKpisInfoMappings['fairLabourAssociationCertification']"
                        :name="'fairLabourAssociationCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['fairWorkingConditionsPolicy']"
                        :info="lksgKpisInfoMappings['fairWorkingConditionsPolicy']"
                        :name="'fairWorkingConditionsPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['fairAndEthicalRecruitmentPolicy']"
                        :info="lksgKpisInfoMappings['fairAndEthicalRecruitmentPolicy']"
                        :name="'fairAndEthicalRecruitmentPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['equalOpportunitiesAndNondiscriminationPolicy']"
                        :info="lksgKpisInfoMappings['equalOpportunitiesAndNondiscriminationPolicy']"
                        :name="'equalOpportunitiesAndNondiscriminationPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['healthAndSafetyPolicy']"
                        :info="lksgKpisInfoMappings['healthAndSafetyPolicy']"
                        :name="'healthAndSafetyPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['complaintsAndGrievancesPolicy']"
                        :info="lksgKpisInfoMappings['complaintsAndGrievancesPolicy']"
                        :name="'complaintsAndGrievancesPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['forcedLabourPolicy']"
                        :info="lksgKpisInfoMappings['forcedLabourPolicy']"
                        :name="'forcedLabourPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['childLabourPolicy']"
                        :info="lksgKpisInfoMappings['childLabourPolicy']"
                        :name="'childLabourPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['environmentalImpactPolicy']"
                        :info="lksgKpisInfoMappings['environmentalImpactPolicy']"
                        :name="'environmentalImpactPolicy'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['grievanceHandlingMechanism']"
                        :info="lksgKpisInfoMappings['grievanceHandlingMechanism']"
                        :name="'grievanceHandlingMechanism'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['grievanceHandlingMechanismUsedForReporting']"
                        :info="lksgKpisInfoMappings['grievanceHandlingMechanismUsedForReporting']"
                        :name="'grievanceHandlingMechanismUsedForReporting'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshMonitoring']"
                        :info="lksgKpisInfoMappings['oshMonitoring']"
                        :name="'oshMonitoring'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicy']"
                        :info="lksgKpisInfoMappings['oshPolicy']"
                        :name="'oshPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyPersonalProtectiveEquipment']"
                        :info="lksgKpisInfoMappings['oshPolicyPersonalProtectiveEquipment']"
                        :name="'oshPolicyPersonalProtectiveEquipment'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyMachineSafety']"
                        :info="lksgKpisInfoMappings['oshPolicyMachineSafety']"
                        :name="'oshPolicyMachineSafety'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyDisasterBehaviouralResponse']"
                        :info="lksgKpisInfoMappings['oshPolicyDisasterBehaviouralResponse']"
                        :name="'oshPolicyDisasterBehaviouralResponse'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyAccidentsBehaviouralResponse']"
                        :info="lksgKpisInfoMappings['oshPolicyAccidentsBehaviouralResponse']"
                        :name="'oshPolicyAccidentsBehaviouralResponse'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyWorkplaceErgonomics']"
                        :info="lksgKpisInfoMappings['oshPolicyWorkplaceErgonomics']"
                        :name="'oshPolicyWorkplaceErgonomics'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyHandlingChemicalsAndOtherHazardousSubstances']"
                        :info="lksgKpisInfoMappings['oshPolicyHandlingChemicalsAndOtherHazardousSubstances']"
                        :name="'oshPolicyHandlingChemicalsAndOtherHazardousSubstances'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyFireProtection']"
                        :info="lksgKpisInfoMappings['oshPolicyFireProtection']"
                        :name="'oshPolicyFireProtection'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyWorkingHours']"
                        :info="lksgKpisInfoMappings['oshPolicyWorkingHours']"
                        :name="'oshPolicyWorkingHours'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyTrainingAddressed']"
                        :info="lksgKpisInfoMappings['oshPolicyTrainingAddressed']"
                        :name="'oshPolicyTrainingAddressed'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshPolicyTraining']"
                        :info="lksgKpisInfoMappings['oshPolicyTraining']"
                        :name="'oshPolicyTraining'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshManagementSystem']"
                        :info="lksgKpisInfoMappings['oshManagementSystem']"
                        :name="'oshManagementSystem'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshManagementSystemInternationalCertification']"
                        :info="lksgKpisInfoMappings['oshManagementSystemInternationalCertification']"
                        :name="'oshManagementSystemInternationalCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['oshManagementSystemNationalCertification']"
                        :info="lksgKpisInfoMappings['oshManagementSystemNationalCertification']"
                        :name="'oshManagementSystemNationalCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['workplaceAccidentsUnder10']"
                        :info="lksgKpisInfoMappings['workplaceAccidentsUnder10']"
                        :name="'workplaceAccidentsUnder10'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['freedomOfAssociation']"
                        :info="lksgKpisInfoMappings['freedomOfAssociation']"
                        :name="'freedomOfAssociation'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['discriminationForTradeUnionMembers']"
                        :info="lksgKpisInfoMappings['discriminationForTradeUnionMembers']"
                        :name="'discriminationForTradeUnionMembers'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['freedomOfOperationForTradeUnion']"
                        :info="lksgKpisInfoMappings['freedomOfOperationForTradeUnion']"
                        :name="'freedomOfOperationForTradeUnion'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['freedomOfAssociationTraining']"
                        :info="lksgKpisInfoMappings['freedomOfAssociationTraining']"
                        :name="'freedomOfAssociationTraining'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['worksCouncil']"
                        :info="lksgKpisInfoMappings['worksCouncil']"
                        :name="'worksCouncil'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="humanRights" class="anchor title">{{ lksgSubAreasNameMappings.humanRights }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="humanRights">
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['diversityAndInclusionRole']"
                        :info="lksgKpisInfoMappings['diversityAndInclusionRole']"
                        :name="'diversityAndInclusionRole'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['preventionOfMistreatments']"
                        :info="lksgKpisInfoMappings['preventionOfMistreatments']"
                        :name="'preventionOfMistreatments'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['equalOpportunitiesOfficer']"
                        :info="lksgKpisInfoMappings['equalOpportunitiesOfficer']"
                        :name="'equalOpportunitiesOfficer'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['riskOfHarmfulPollution']"
                        :info="lksgKpisInfoMappings['riskOfHarmfulPollution']"
                        :name="'riskOfHarmfulPollution'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['unlawfulEvictionAndTakingOfLand']"
                        :info="lksgKpisInfoMappings['unlawfulEvictionAndTakingOfLand']"
                        :name="'unlawfulEvictionAndTakingOfLand'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['useOfPrivatePublicSecurityForces']"
                        :info="lksgKpisInfoMappings['useOfPrivatePublicSecurityForces']"
                        :name="'useOfPrivatePublicSecurityForces'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['responsibilitiesForFairWorkingConditions']"
                        :info="lksgKpisInfoMappings['responsibilitiesForFairWorkingConditions']"
                        :name="'responsibilitiesForFairWorkingConditions'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="environment" class="anchor title">{{ lksgSubAreasNameMappings.environment }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="environment">
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['responsibilitiesForOccupationalSafety']"
                        :info="lksgKpisInfoMappings['responsibilitiesForOccupationalSafety']"
                        :name="'responsibilitiesForOccupationalSafety'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="riskManagement" class="anchor title">{{ lksgSubAreasNameMappings.riskManagement }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="riskManagement">
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['riskManagementSystem']"
                        :info="lksgKpisInfoMappings['riskManagementSystem']"
                        :name="'riskManagementSystem'"
                      />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 p-3 topicLabel">
                    <h4 id="codeOfConduct" class="anchor title">{{ lksgSubAreasNameMappings.codeOfConduct }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="codeOfConduct">
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['codeOfConduct']"
                        :info="lksgKpisInfoMappings['codeOfConduct']"
                        :name="'codeOfConduct'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['codeOfConductRiskManagementTopics']"
                        :info="lksgKpisInfoMappings['codeOfConductRiskManagementTopics']"
                        :name="'codeOfConductRiskManagementTopics'"
                      />
                      <RadioButtonsGroup
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
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryWasteHandling']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryWasteHandling']"
                        :name="'mercuryAndMercuryWasteHandling'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryWasteHandlingPolicy']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryWasteHandlingPolicy']"
                        :name="'mercuryAndMercuryWasteHandlingPolicy'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['chemicalHandling']"
                        :info="lksgKpisInfoMappings['chemicalHandling']"
                        :name="'chemicalHandling'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['environmentalManagementSystem']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystem']"
                        :name="'environmentalManagementSystem'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['environmentalManagementSystemInternationalCertification']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystemInternationalCertification']"
                        :name="'environmentalManagementSystemInternationalCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['environmentalManagementSystemNationalCertification']"
                        :info="lksgKpisInfoMappings['environmentalManagementSystemNationalCertification']"
                        :name="'environmentalManagementSystemNationalCertification'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['legalRestrictedWaste']"
                        :info="lksgKpisInfoMappings['legalRestrictedWaste']"
                        :name="'legalRestrictedWaste'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['legalRestrictedWasteProcesses']"
                        :info="lksgKpisInfoMappings['legalRestrictedWasteProcesses']"
                        :name="'legalRestrictedWasteProcesses'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandling']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandling']"
                        :name="'mercuryAddedProductsHandling'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandlingRiskOfExposure']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandlingRiskOfExposure']"
                        :name="'mercuryAddedProductsHandlingRiskOfExposure'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAddedProductsHandlingRiskOfDisposal']"
                        :info="lksgKpisInfoMappings['mercuryAddedProductsHandlingRiskOfDisposal']"
                        :name="'mercuryAddedProductsHandlingRiskOfDisposal'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryCompoundsProductionAndUse']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryCompoundsProductionAndUse']"
                        :name="'mercuryAndMercuryCompoundsProductionAndUse'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure']"
                        :info="lksgKpisInfoMappings['mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure']"
                        :name="'mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUse']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUse']"
                        :name="'persistentOrganicPollutantsProductionAndUse'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskOfExposure']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskOfExposure']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskOfExposure'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskOfDisposal']"
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskOfDisposal']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskOfDisposal'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseTransboundaryMovements']
                        "
                        :info="
                          lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseTransboundaryMovements']
                        "
                        :name="'persistentOrganicPollutantsProductionAndUseTransboundaryMovements'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['persistentOrganicPollutantsProductionAndUseRiskForImportingState']
                        "
                        :info="lksgKpisInfoMappings['persistentOrganicPollutantsProductionAndUseRiskForImportingState']"
                        :name="'persistentOrganicPollutantsProductionAndUseRiskForImportingState'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein']
                        "
                        :info="lksgKpisInfoMappings['hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein']"
                        :name="'hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein'"
                      />
                      <RadioButtonsGroup
                        :displayName="
                          lksgKpisNameMappings['hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein']
                        "
                        :info="lksgKpisInfoMappings['hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein']"
                        :name="'hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['hazardousWasteDisposal']"
                        :info="lksgKpisInfoMappings['hazardousWasteDisposal']"
                        :name="'hazardousWasteDisposal'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['hazardousWasteDisposalRiskOfImport']"
                        :info="lksgKpisInfoMappings['hazardousWasteDisposalRiskOfImport']"
                        :name="'hazardousWasteDisposalRiskOfImport'"
                      />
                      <RadioButtonsGroup
                        :displayName="lksgKpisNameMappings['hazardousAndOtherWasteImport']"
                        :info="lksgKpisInfoMappings['hazardousAndOtherWasteImport']"
                        :name="'hazardousAndOtherWasteImport'"
                      />
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </FormKit>

            <!--------- SUBMIT --------->

            <div class="uploadFormSection grid">
              <div class="col-3"></div>

              <div class="col-9">
                <PrimeButton
                  data-test="submitButton"
                  type="submit"
                  :label="this.updatingData ? 'UPDATE DATA' : 'ADD DATA'"
                />
              </div>
            </div>
          </FormKit>

          <div v-if="postLkSGDataProcessed">
            <SuccessUpload v-if="uploadSucceded" :messageId="messageCounter" />
            <FailedUpload v-else :message="message" :messageId="messageCounter" />
          </div>
        </div>
        <JumpLinksSection :onThisPageLinks="onThisPageLinks" />
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
import RadioButtonsGroup from "@/components/forms/parts/RadioButtonsGroup.vue";
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
import { checkCustomInputs } from "@/utils/ValidationsUtils";
import JumpLinksSection from "@/components/forms/parts/JumpLinksSection.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: {
    JumpLinksSection,
    UploadFormHeader,
    SuccessUpload,
    FailedUpload,
    FormKit,
    Card,
    PrimeButton,
    RadioButtonsGroup,
    Calendar,
  },
  directives: {
    tooltip: Tooltip,
  },
  emits: ["datasetCreated"],
  data() {
    return {
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
      onThisPageLinks: [
        { label: "General", value: "general" },
        { label: "Child labour", value: "childLabour" },
        { label: "Forced labour, slavery and debt bondage", value: "forcedLabourSlaveryAndDebtBondage" },
        { label: "Evidence, certificates and attestations", value: "evidenceCertificatesAndAttestations" },
        { label: "Grievance mechanism", value: "grievanceMechanism" },
        { label: "OSH", value: "osh" },
        { label: "Freedom of association", value: "freedomOfAssociation" },
        { label: "Human rights", value: "humanRights" },
        { label: "Social and employee matters", value: "socialAndEmployeeMatters" },
        { label: "Environment", value: "environment" },
        { label: "Risk management", value: "riskManagement" },
        { label: "Waste", value: "waste" },
      ],
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
        this.$emit("datasetCreated");
        this.$formkit.reset("createLkSGForm");
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
