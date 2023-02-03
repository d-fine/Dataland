<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>New Dataset - LkSG </template>
    <template #content>
      <div class="grid uploadFormWrapper">
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
            <FormKit
              type="hidden"
              name="companyId"
              label="Company ID"
              placeholder="Company ID"
              :model-value="companyID"
              disabled="true"
            />
            <FormKit type="group" name="data" label="data">
              <FormKit type="group" name="social" label="social">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="general" class="anchor title">{{ lksgSubAreaNameMappings._general }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                    <p>Please input all relevant basic information about the dataset</p>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="general" :label="lksgSubAreaNameMappings._general">
                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpiNameMappings.dataDate"
                          :explanation="lksgKpiInfoMappings.dataDate"
                        />
                        <Calendar
                          data-test="lksgDataDate"
                          inputId="icon"
                          v-model="dataDate"
                          :showIcon="true"
                          dateFormat="D, M dd, yy"
                          :maxDate="new Date()"
                        />

                        <FormKit
                          type="text"
                          validation="required"
                          name="dataDate"
                          v-model="convertedDataDate"
                          :outer-class="{ 'hidden-input': true }"
                        />
                      </div>

                      <div class="form-field" data-test="lksgInScope">
                        <UploadFormHeader
                          :name="lksgKpiNameMappings.lksgInScope"
                          :explanation="lksgKpiInfoMappings.lksgInScope"
                        />
                        <FormKit
                          type="radio"
                          :validation-label="lksgKpiNameMappings.lksgInScope"
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
                          :name="lksgKpiNameMappings.vatIdentificationNumber"
                          :explanation="lksgKpiInfoMappings.vatIdentificationNumber"
                        />
                        <FormKit
                          type="text"
                          :validation-label="lksgKpiNameMappings.vatIdentificationNumber"
                          validation="required|length:3"
                          name="vatIdentificationNumber"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpiNameMappings.numberOfEmployees"
                          :explanation="lksgKpiInfoMappings.numberOfEmployees"
                        />
                        <FormKit
                          type="number"
                          name="numberOfEmployees"
                          :validation-label="lksgKpiNameMappings.numberOfEmployees"
                          placeholder="Value"
                          validation="required|number"
                          step="1"
                          min="0"
                          :inner-class="{ short: true }"
                        />
                      </div>

                      <div class="form-field">
                        <UploadFormHeader
                          :name="lksgKpiNameMappings.shareOfTemporaryWorkers"
                          :explanation="lksgKpiInfoMappings.shareOfTemporaryWorkers"
                        />
                        <FormKit
                          type="number"
                          name="shareOfTemporaryWorkers"
                          :validation-label="lksgKpiNameMappings.shareOfTemporaryWorkers"
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
                          :name="lksgKpiNameMappings.totalRevenue"
                          :explanation="lksgKpiInfoMappings.totalRevenue"
                        />
                        <FormKit
                          type="number"
                          min="0"
                          :validation-label="lksgKpiNameMappings.totalRevenue"
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
                          :name="lksgKpiNameMappings.totalRevenueCurrency"
                          :explanation="lksgKpiInfoMappings.totalRevenueCurrency"
                        />
                        <FormKit
                          type="text"
                          name="totalRevenueCurrency"
                          :validation-label="lksgKpiNameMappings.totalRevenueCurrency"
                          placeholder="Currency"
                          validation="required"
                          :inner-class="{
                            medium: true,
                          }"
                        />
                      </div>

                      <div class="form-field" data-test="IsYourCompanyManufacturingCompany">
                        <UploadFormHeader
                          :name="'Is your company a manufacturing company?'"
                          :explanation="lksgKpiInfoMappings.listOfProductionSites"
                        />
                        <FormKit
                          type="radio"
                          :ignore="true"
                          id="IsYourCompanyManufacturingCompany"
                          name="IsYourCompanyManufacturingCompany"
                          :validation-label="lksgKpiNameMappings.totalRevenueCurrency"
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
                        :validation-label="lksgKpiNameMappings.totalRevenueCurrency"
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
                                :name="lksgKpiNameMappings.productionSiteName"
                                :explanation="lksgKpiInfoMappings.productionSiteName"
                              />
                              <FormKit
                                type="text"
                                :validation-label="lksgKpiNameMappings.productionSiteName"
                                name="name"
                                validation="required"
                              />
                            </div>

                            <div class="form-field" data-test="isInHouseProductionOrIsContractProcessing">
                              <UploadFormHeader
                                :name="lksgKpiNameMappings.inHouseProductionOrContractProcessing"
                                :explanation="lksgKpiInfoMappings.inHouseProductionOrContractProcessing"
                              />
                              <FormKit
                                type="radio"
                                name="isInHouseProductionOrIsContractProcessing"
                                :validation-label="lksgKpiNameMappings.inHouseProductionOrContractProcessing"
                                :options="['In-house Production', 'Contract Processing']"
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
                                :name="lksgKpiNameMappings.addressesOfProductionSites"
                                :explanation="lksgKpiInfoMappings.addressesOfProductionSites"
                              />

                              <FormKit
                                type="text"
                                name="streetAndHouseNumber"
                                validation="required"
                                :validation-label="lksgKpiNameMappings.addressesOfProductionSites"
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
                                    value: lksgKpiInfoMappings['listOfGoodsOrServices']
                                      ? lksgKpiInfoMappings['listOfGoodsOrServices']
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
                    <h4 id="childLabour" class="anchor title">{{ lksgSubAreaNameMappings.childLabour }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="childLabour">
                      <YesNoComponent :name="'employeeUnder18'" />
                      <YesNoComponent :name="'employeeUnder15'" />
                      <YesNoComponent :name="'employeeUnder18Apprentices'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePrevention'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePreventionEmploymentContracts'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePreventionJobDescription'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePreventionIdentityDocuments'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePreventionTraining'" />
                      <YesNoComponent :name="'employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="forcedLabourSlaveryAndDebtBondage" class="anchor title">
                      {{ lksgSubAreaNameMappings.forcedLabourSlaveryAndDebtBondage }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="forcedLabourSlaveryAndDebtBondage">
                      <YesNoComponent :name="'forcedLabourAndSlaveryPrevention'" />
                      <YesNoComponent :name="'forcedLabourAndSlaveryPreventionEmploymentContracts'" />
                      <YesNoComponent :name="'forcedLabourAndSlaveryPreventionIdentityDocuments'" />
                      <YesNoComponent :name="'forcedLabourAndSlaveryPreventionFreeMovement'" />
                      <YesNoComponent :name="'forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets'" />
                      <YesNoComponent :name="'forcedLabourAndSlaveryPreventionTraining'" />
                      <YesNoComponent :name="'documentedWorkingHoursAndWages'" />
                      <YesNoComponent :name="'adequateLivingWage'" />
                      <YesNoComponent :name="'regularWagesProcessFlow'" />
                      <YesNoComponent :name="'fixedHourlyWages'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="evidenceCertificatesAndAttestations" class="anchor title">
                      {{ lksgSubAreaNameMappings.evidenceCertificatesAndAttestations }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="evidenceCertificatesAndAttestations">
                      <YesNoComponent :name="'iso26000'" />
                      <YesNoComponent :name="'sa8000Certification'" />
                      <YesNoComponent :name="'smetaSocialAuditConcept'" />
                      <YesNoComponent :name="'betterWorkProgramCertificate'" />
                      <YesNoComponent :name="'iso45001Certification'" />
                      <YesNoComponent :name="'iso14000Certification'" />
                      <YesNoComponent :name="'emasCertification'" />
                      <YesNoComponent :name="'iso37001Certification'" />
                      <YesNoComponent :name="'iso37301Certification'" />
                      <YesNoComponent :name="'riskManagementSystemCertification'" />
                      <YesNoComponent :name="'amforiBsciAuditReport'" />
                      <YesNoComponent :name="'initiativeClauseSocialCertification'" />
                      <YesNoComponent :name="'responsibleBusinessAssociationCertification'" />
                      <YesNoComponent :name="'fairLabourAssociationCertification'" />
                      <YesNoComponent :name="'fairWorkingConditionsPolicy'" />
                      <YesNoComponent :name="'fairAndEthicalRecruitmentPolicy'" />
                      <YesNoComponent :name="'equalOpportunitiesAndNondiscriminationPolicy'" />
                      <YesNoComponent :name="'healthAndSafetyPolicy'" />
                      <YesNoComponent :name="'complaintsAndGrievancesPolicy'" />
                      <YesNoComponent :name="'forcedLabourPolicy'" />
                      <YesNoComponent :name="'childLabourPolicy'" />
                      <YesNoComponent :name="'environmentalImpactPolicy'" />
                      <YesNoComponent :name="'supplierCodeOfConduct'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="grievanceMechanism" class="anchor title">
                      {{ lksgSubAreaNameMappings.grievanceMechanism }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="grievanceMechanism">
                      <YesNoComponent :name="'grievanceHandlingMechanism'" />
                      <YesNoComponent :name="'grievanceHandlingMechanismUsedForReporting'" />
                      <YesNoComponent :name="'legalProceedings'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 p-3 topicLabel">
                    <h4 id="osh" class="anchor title">{{ lksgSubAreaNameMappings.osh }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="osh">
                      <YesNoComponent :name="'oshMonitoring'" />
                      <YesNoComponent :name="'oshPolicy'" />
                      <YesNoComponent :name="'oshPolicyPersonalProtectiveEquipment'" />
                      <YesNoComponent :name="'oshPolicyMachineSafety'" />
                      <YesNoComponent :name="'oshPolicyDisasterBehaviouralResponse'" />
                      <YesNoComponent :name="'oshPolicyAccidentsBehaviouralResponse'" />
                      <YesNoComponent :name="'oshPolicyWorkplaceErgonomics'" />
                      <YesNoComponent :name="'oshPolicyHandlingChemicalsAndOtherHazardousSubstances'" />
                      <YesNoComponent :name="'oshPolicyFireProtection'" />
                      <YesNoComponent :name="'oshPolicyWorkingHours'" />
                      <YesNoComponent :name="'oshPolicyTrainingAddressed'" />
                      <YesNoComponent :name="'oshPolicyTraining'" />
                      <YesNoComponent :name="'oshManagementSystem'" />
                      <YesNoComponent :name="'oshManagementSystemInternationalCertification'" />
                      <YesNoComponent :name="'oshManagementSystemNationalCertification'" />
                      <YesNoComponent :name="'workplaceAccidentsUnder10'" />
                      <YesNoComponent :name="'oshTraining'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="freedomOfAssociation" class="anchor title">
                      {{ lksgSubAreaNameMappings.freedomOfAssociation }}
                    </h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="freedomOfAssociation">
                      <YesNoComponent :name="'freedomOfAssociation'" />
                      <YesNoComponent :name="'discriminationForTradeUnionMembers'" />
                      <YesNoComponent :name="'freedomOfOperationForTradeUnion'" />
                      <YesNoComponent :name="'freedomOfAssociationTraining'" />
                      <YesNoComponent :name="'worksCouncil'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="humanRights" class="anchor title">{{ lksgSubAreaNameMappings.humanRights }}</h4>
                    <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="humanRights">
                      <YesNoComponent :name="'diversityAndInclusionRole'" />
                      <YesNoComponent :name="'preventionOfMistreatments'" />
                      <YesNoComponent :name="'equalOpportunitiesOfficer'" />
                      <YesNoComponent :name="'riskOfHarmfulPollution'" />
                      <YesNoComponent :name="'unlawfulEvictionAndTakingOfLand'" />
                      <YesNoComponent :name="'useOfPrivatePublicSecurityForces'" />
                      <YesNoComponent :name="'useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights'" />
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <FormKit type="group" name="governance" label="governance">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="socialAndEmployeeMatters" class="anchor title">
                      {{ lksgSubAreaNameMappings.socialAndEmployeeMatters }}
                    </h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="socialAndEmployeeMatters">
                      <YesNoComponent :name="'responsibilitiesForFairWorkingConditions'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="environment" class="anchor title">{{ lksgSubAreaNameMappings.environment }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="environment">
                      <YesNoComponent :name="'responsibilitiesForTheEnvironment'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="osh_governance" class="anchor title">{{ lksgSubAreaNameMappings.osh }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="osh">
                      <YesNoComponent :name="'responsibilitiesForOccupationalSafety'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="riskManagement" class="anchor title">{{ lksgSubAreaNameMappings.riskManagement }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="riskManagement">
                      <YesNoComponent :name="'riskManagementSystem'" />
                    </FormKit>
                  </div>
                </div>

                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 p-3 topicLabel">
                    <h4 id="codeOfConduct" class="anchor title">{{ lksgSubAreaNameMappings.codeOfConduct }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit type="group" name="codeOfConduct">
                      <YesNoComponent :name="'codeOfConduct'" />
                      <YesNoComponent :name="'codeOfConductRiskManagementTopics'" />
                      <YesNoComponent :name="'codeOfConductTraining'" />
                    </FormKit>
                  </div>
                </div>
              </FormKit>

              <FormKit type="group" name="environmental" label="environmental">
                <div class="uploadFormSection grid">
                  <div class="col-3 p-3 topicLabel">
                    <h4 id="waste" class="anchor title">{{ lksgSubAreaNameMappings.waste }}</h4>
                    <div class="p-badge badge-green"><span>ENVIRONMENTAL</span></div>
                  </div>

                  <div class="col-9 formFields">
                    <FormKit type="group" name="waste">
                      <YesNoComponent :name="'mercuryAndMercuryWasteHandling'" />
                      <YesNoComponent :name="'mercuryAndMercuryWasteHandlingPolicy'" />
                      <YesNoComponent :name="'chemicalHandling'" />
                      <YesNoComponent :name="'environmentalManagementSystem'" />
                      <YesNoComponent :name="'environmentalManagementSystemInternationalCertification'" />
                      <YesNoComponent :name="'environmentalManagementSystemNationalCertification'" />
                      <YesNoComponent :name="'legalRestrictedWaste'" />
                      <YesNoComponent :name="'legalRestrictedWasteProcesses'" />
                      <YesNoComponent :name="'mercuryAddedProductsHandling'" />
                      <YesNoComponent :name="'mercuryAddedProductsHandlingRiskOfExposure'" />
                      <YesNoComponent :name="'mercuryAddedProductsHandlingRiskOfDisposal'" />
                      <YesNoComponent :name="'mercuryAndMercuryCompoundsProductionAndUse'" />
                      <YesNoComponent :name="'mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure'" />
                      <YesNoComponent :name="'persistentOrganicPollutantsProductionAndUse'" />
                      <YesNoComponent :name="'persistentOrganicPollutantsProductionAndUseRiskOfExposure'" />
                      <YesNoComponent :name="'persistentOrganicPollutantsProductionAndUseRiskOfDisposal'" />
                      <YesNoComponent :name="'persistentOrganicPollutantsProductionAndUseTransboundaryMovements'" />
                      <YesNoComponent :name="'persistentOrganicPollutantsProductionAndUseRiskForImportingState'" />
                      <YesNoComponent :name="'hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein'" />
                      <YesNoComponent :name="'hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein'" />
                      <YesNoComponent :name="'hazardousWasteDisposal'" />
                      <YesNoComponent :name="'hazardousWasteDisposalRiskOfImport'" />
                      <YesNoComponent :name="'hazardousAndOtherWasteImport'" />
                    </FormKit>
                  </div>
                </div>
              </FormKit>
            </FormKit>

            <!--------- SUBMIT --------->

            <div class="uploadFormSection grid">
              <div class="col-3"></div>

              <div class="col-9">
                <PrimeButton data-test="submitButton" type="submit" label="ADD DATA" />
              </div>
            </div>
          </FormKit>

          <div v-if="postLkSGDataProcessed">
            <SuccessUpload v-if="uploadSucceded" :messageId="messageCounter" />
            <FailedUpload v-else :message="message" :messageId="messageCounter" />
          </div>
        </div>

        <div id="jumpLinks" class="col-3 p-3 text-left jumpLinks">
          <h4 id="topicTitles" class="title">On this page</h4>
          <ul>
            <li><a href="#general">General</a></li>
            <li><a href="#childLabour">Child labour</a></li>
            <li><a href="#forcedLabourSlaveryAndDebtBondage">Forced labour, slavery and debt bondage</a></li>
            <li><a href="#evidenceCertificatesAndAttestations">Evidence, certificates and attestations</a></li>
            <li><a href="#grievanceMechanism">Grievance mechanism</a></li>
            <li><a href="#osh">OSH</a></li>
            <li><a href="#freedomOfAssociation">Freedom of association</a></li>
            <li><a href="#humanRights">Human rights</a></li>
            <li><a href="#socialAndEmployeeMatters">Social and employee matters</a></li>
            <li><a href="#environment">Environment</a></li>
            <li><a href="#riskManagement">Risk management</a></li>
            <li><a href="#codeOfConduct">Code of Conduct</a></li>
            <li><a href="#waste">Waste</a></li>
          </ul>
        </div>
      </div>
    </template>
  </Card>
</template>

<script lang="ts">
import { FormKit } from "@formkit/vue";
import { FormKitNode } from "@formkit/core";
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
  lksgKpiInfoMappings,
  lksgKpiNameMappings,
  lksgSubAreaNameMappings,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { AxiosError } from "axios";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: { UploadFormHeader, SuccessUpload, FailedUpload, FormKit, Card, PrimeButton, YesNoComponent, Calendar },
  directives: {
    tooltip: Tooltip,
  },

  data: () => ({
    isYourCompanyManufacturingCompany: "No",
    listOfProductionSites: [
      {
        id: 0,
        listOfGoodsOrServices: [],
        listOfGoodsOrServicesString: "",
      },
    ],
    allCountry: getAllCountryNamesWithCodes(),
    dataDate: "",
    convertedDataDate: "",
    lkSGDataModel: {},
    message: "",
    uploadSucceded: false,

    postLkSGDataProcessed: false,
    messageCounter: 0,

    lksgKpiInfoMappings,
    lksgKpiNameMappings,
    lksgSubAreaNameMappings,
  }),
  props: {
    companyID: {
      type: String,
    },
  },
  watch: {
    dataDate: function (newValue: Date) {
      if (newValue) {
        this.convertedDataDate = `${newValue.getFullYear()}-${("0" + (newValue.getMonth() + 1).toString()).slice(
          -2
        )}-${("0" + newValue.getDate().toString()).slice(-2)}`;
      } else {
        this.convertedDataDate = "";
      }
    },
  },
  methods: {
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
        this.$formkit.reset("createLkSGForm");
        this.isYourCompanyManufacturingCompany = "No";
        this.listOfProductionSites = [
          {
            id: 0,
            listOfGoodsOrServices: [],
            listOfGoodsOrServicesString: "",
          },
        ];
        this.dataDate = "";
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
     * Checks which inputs are not filled correctly
     *
     * @param node - single form field
     */
    checkCustomInputs(node: FormKitNode) {
      const invalidElements: HTMLElement[] = [];
      node.walk((child: FormKitNode) => {
        // Check if this child has errors
        if ((child.ledger.value("blocking") || child.ledger.value("errors")) && child.type !== "group") {
          // We found an input with validation errors
          if (typeof child.props.id === "string") {
            const invalidElement = document.getElementById(child.props.id);
            if (invalidElement) {
              invalidElements.push(invalidElement);
            }
          }
        }
      }, true);
      invalidElements.find((el) => el !== null)?.scrollIntoView({ behavior: "smooth", block: "center" });
    },

    /**
     * Adds a new Object to the ProductionSite array
     */
    addNewProductionSite() {
      this.listOfProductionSites.push({
        id: Math.random(),
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
