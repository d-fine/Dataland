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
            v-model="Model"
            :actions="false"
            type="form"
            id="createLkSGForm"
            @submit="postLkSGData"
            #default="{ state: { valid } }"
          >
            <FormKit type="group" name="social" label="social">
              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="general" class="anchor title">{{ lksgSubAreaNameMappings._general }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                  <p>Please input all relevant basic information about the dataset</p>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="general" :label="lksgSubAreaNameMappings._general">
                    <div class="form-field">
                      <UploadFormHeader :name="lksgKpiNameMappings.dataDate" :explanation="lksgKpiInfoMappings.dataDate" />
                      <FormKit type="date" help="Enter date" />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader :name="lksgKpiNameMappings.lksgInScope" :explanation="lksgKpiInfoMappings.lksgInScope" />
                      <FormKit
                        type="radio"
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
                      />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader
                        :name="lksgKpiNameMappings.companyLegalForm"
                        :explanation="lksgKpiInfoMappings.companyLegalForm"
                      />
                      <FormKit type="text" name="companyLegalForm" />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader
                        :name="lksgKpiNameMappings.vatIdentificationNumber"
                        :explanation="lksgKpiInfoMappings.vatIdentificationNumber"
                      />
                      <FormKit type="number" name="VATidentificationNumber" step="1" />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader
                        :name="lksgKpiNameMappings.numberOfEmployees"
                        :explanation="lksgKpiInfoMappings.numberOfEmployees"
                      />
                      <FormKit
                        type="number"
                        name="numberOfEmployees"
                        placeholder="Value"
                        step="1"
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
                        placeholder="Value %"
                        step="1"
                        :inner-class="{
                          short: true,
                        }"
                      />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader :name="lksgKpiNameMappings.totalRevenue" :explanation="lksgKpiInfoMappings.totalRevenue" />
                      <div class="next-to-each-other">
                        <FormKit type="number" name="totalRevenue" placeholder="Value" step="1" />
                        <FormKit type="select" name="unit" placeholder="Unit" :options="['CHF', 'USD']" />
                      </div>
                    </div>

                    <div class="form-field">
                      <UploadFormHeader
                        :name="lksgKpiNameMappings.totalRevenueCurrency"
                        :explanation="lksgKpiInfoMappings.totalRevenueCurrency"
                      />
                      <FormKit
                        type="text"
                        name="totalRevenueCurrency"
                        placeholder="Currency"
                        :inner-class="{
                          medium: true,
                        }"
                      />
                    </div>

                    <div class="form-field">
                      <UploadFormHeader
                        :name="'Is your company a manufacturing company?'"
                        :explanation="lksgKpiInfoMappings.listOfProductionSites"
                      />
                      <FormKit
                        type="radio"
                        name="IsYourCompanyManufacturingCompany"
                        :options="['Yes', 'No']"
                        v-model="isYourCompanyManufacturingCompany"
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

                    <FormKit type="list" name="listOfProductionSites" label="listOfProductionSites">
                      <FormKit type="group" v-for="(item, index) in listOfProductionSites" :key="item.id">
                        <div
                          class="productionSiteSection"
                          :class="isYourCompanyManufacturingCompany === 'No' ? 'p-disabled' : ''"
                        >
                          <em @click="removeItemFromlistOfProductionSites(item.id)" class="material-icons close-section"
                            >close</em
                          >

                          <div class="form-field">
                            <UploadFormHeader
                              :name="lksgKpiNameMappings.productionSiteName"
                              :explanation="lksgKpiInfoMappings.productionSiteName"
                            />
                            <FormKit type="text" name="productionSiteName" />
                          </div>

                          <!-- Is in-house production or is Contract Processing  -->
                          <div class="form-field">
                            <UploadFormHeader
                              :name="lksgKpiNameMappings.inHouseProductionOrContractProcessing"
                              :explanation="lksgKpiInfoMappings.inHouseProductionOrContractProcessing"
                            />
                            <FormKit
                              type="radio"
                              name="inHouseProductionOrContractProcessing"
                              :options="['In-house Production', 'Contract Processing']"
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

                          <!-- Addresses Of Production Sites  -->
                          <div class="form-field">
                            <UploadFormHeader
                              :name="lksgKpiNameMappings.addressesOfProductionSites"
                              :explanation="lksgKpiInfoMappings.addressesOfProductionSites"
                            />

                            <FormKit type="text" name="StreetHouseNumber" placeholder="Street, House number" />
                            <div class="next-to-each-other">
                              <FormKit type="select" name="Country" placeholder="Country" :options="['CHF', 'USD']" />
                              <FormKit type="select" name="City" placeholder="City" :options="['CHF', 'USD']" />
                              <FormKit type="text" name="Postcode" placeholder="Postcode" />
                            </div>
                          </div>

                          <!-- List Of Goods Or Services  -->
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
                              type="text"
                              :ignore="true"
                              v-model="listOfProductionSites[index].listOfGoodsOrServicesString"
                              placeholder="Add comma (,) for more than one value"
                            />
                            <FormKit
                              v-model="listOfProductionSites[index].listOfGoodsOrServices"
                              type="list"
                              name="listOfGoodsOrServices"
                            />
                            <div class="">
                              <span class="form-list-item" :key="element" v-for="element in item.listOfGoodsOrServices">
                                {{ element }}
                                <em @click="removeItemFromlistOfGoodsOrServices(index, element)" class="material-icons"
                                  >close</em
                                >
                              </span>
                            </div>
                          </div>
                        </div>
                      </FormKit>
                      <PrimeButton
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
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="childLabour" class="anchor title">{{ lksgSubAreaNameMappings.childLabour }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="childLabour" :label="lksgSubAreaNameMappings.childLabour">
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
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="forcedLabourSlaveryAndDebtBondage" class="anchor title">
                    {{ lksgSubAreaNameMappings.forcedLabourSlaveryAndDebtBondage }}
                  </h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit
                    type="group"
                    name="forcedLabourSlaveryAndDebtBondage"
                    :label="lksgSubAreaNameMappings.forcedLabourSlaveryAndDebtBondage"
                  >
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
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="evidenceCertificatesAndAttestations" class="anchor title">
                    {{ lksgSubAreaNameMappings.evidenceCertificatesAndAttestations }}
                  </h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit
                    type="group"
                    name="evidenceCertificatesAndAttestations"
                    :label="lksgSubAreaNameMappings.evidenceCertificatesAndAttestations"
                  >
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
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="grievanceMechanism" class="anchor title">{{ lksgSubAreaNameMappings.grievanceMechanism }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="grievanceMechanism" :label="lksgSubAreaNameMappings.grievanceMechanism">
                    <YesNoComponent :name="'grievanceHandlingMechanism'" />
                    <YesNoComponent :name="'grievanceHandlingMechanismUsedForReporting'" />
                    <YesNoComponent :name="'legalProceedings'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="osh" class="anchor title">{{ lksgSubAreaNameMappings.osh }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="osh" :label="lksgSubAreaNameMappings.osh">
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
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="freedomOfAssociation" class="anchor title">{{ lksgSubAreaNameMappings.freedomOfAssociation }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="freedomOfAssociation" :label="lksgSubAreaNameMappings.freedomOfAssociation">
                    <YesNoComponent :name="'freedomOfAssociation'" />
                    <YesNoComponent :name="'discriminationForTradeUnionMembers'" />
                    <YesNoComponent :name="'freedomOfOperationForTradeUnion'" />
                    <YesNoComponent :name="'freedomOfAssociationTraining'" />
                    <YesNoComponent :name="'worksCouncil'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="humanRights" class="anchor title">{{ lksgSubAreaNameMappings.humanRights }}</h4>
                  <div class="p-badge badge-yellow"><span>SOCIAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="humanRights" :label="lksgSubAreaNameMappings.humanRights">
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

            <!----- GOVERNANCE ------>

            <FormKit type="group" name="governance" label="governance">
              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="socialAndEmployeeMatters" class="anchor title">
                    {{ lksgSubAreaNameMappings.socialAndEmployeeMatters }}
                  </h4>
                  <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit
                    type="group"
                    name="socialAndEmployeeMatters"
                    :label="lksgSubAreaNameMappings.socialAndEmployeeMatters"
                  >
                    <YesNoComponent :name="'responsibilitiesForFairWorkingConditions'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="environment" class="anchor title">{{ lksgSubAreaNameMappings.environment }}</h4>
                  <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                </div>
                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="environment" :label="lksgSubAreaNameMappings.environment">
                    <YesNoComponent :name="'responsibilitiesForTheEnvironment'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="osh_governance" class="anchor title">{{ lksgSubAreaNameMappings.osh }}</h4>
                  <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                </div>
                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="osh" :label="lksgSubAreaNameMappings.osh">
                    <YesNoComponent :name="'responsibilitiesForOccupationalSafety'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="riskManagement" class="anchor title">{{ lksgSubAreaNameMappings.riskManagement }}</h4>
                  <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                </div>
                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="riskManagement" :label="lksgSubAreaNameMappings.riskManagement">
                    <YesNoComponent :name="'riskManagementSystem'" />
                  </FormKit>
                </div>
              </div>

              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="codeOfConduct" class="anchor title">{{ lksgSubAreaNameMappings.codeOfConduct }}</h4>
                  <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                </div>
                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="codeOfConduct" :label="lksgSubAreaNameMappings.codeOfConduct">
                    <YesNoComponent :name="'codeOfConduct'" />
                    <YesNoComponent :name="'codeOfConductRiskManagementTopics'" />
                    <YesNoComponent :name="'codeOfConductTraining'" />
                  </FormKit>
                </div>
              </div>
            </FormKit>

            <!----- ENVIRONMENTAL ------>

            <FormKit type="group" name="environmental" label="environmental">
              <div class="uploadFormSection grid">
                <div id="topicLabel" class="col-3 topicLabel">
                  <h4 id="waste" class="anchor title">{{ lksgSubAreaNameMappings.waste }}</h4>
                  <div class="p-badge badge-green"><span>ENVIRONMENTAL</span></div>
                </div>

                <div id="formFields" class="col-9 formFields">
                  <FormKit type="group" name="waste" :label="lksgSubAreaNameMappings.socialAndEmployeeMatters">
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
                <PrimeButton type="submit" label="SUBMIT" />
              </div>
            </FormKit>
          </FormKit>
        </div>

        <div id="jumpLinks" class="col-3 text-left jumpLinks">
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
import Tooltip from "primevue/tooltip";
import PrimeButton from "primevue/button";
import UploadFormHeader from "@/components/forms/parts/UploadFormHeader.vue";
import YesNoComponent from "@/components/forms/parts/YesNoComponent.vue";
import {
  lksgKpiInfoMappings,
  lksgKpiNameMappings,
  lksgSubAreaNameMappings,
} from "@/components/resources/frameworkDataSearch/DataModelsTranslations";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: { UploadFormHeader, FailedUpload, FormKit, SuccessUpload, Card, PrimeButton, YesNoComponent },
  directives: {
    tooltip: Tooltip,
  },

  data: () => ({
    isYourCompanyManufacturingCompany: "No",
    newItemsTolistOfProductionSites: "",
    listOfProductionSites: [
      {
        id: 0,
        listOfGoodsOrServices: [],
        listOfGoodsOrServicesString: "",
      },
    ],
    postEuTaxonomyDataForFinancialsProcessed: false,
    messageCount: 0,
    Model: {},
    formInputsModel: {
      social: {
        // general: {
        //   dataDate: "2023-01-24",
        //   lksgInScope: "Yes",
        //   vatIdentificationNumber: "string",
        //   numberOfEmployees: 0,
        //   shareOfTemporaryWorkers: 0,
        //   totalRevenue: 0,
        //   totalRevenueCurrency: "string",
        //   listOfProductionSites: [
        //     {
        //       name: "string",
        //       isInHouseProductionOrIsContractProcessing: "Yes",
        //       address: "string",
        //       listOfGoodsOrServices: ["first", "second"],
        //     },
        //   ],
        // },
        // childLabour: {
        //   employeeUnder18: "Yes",
        //   employeeUnder15: "Yes",
        //   employeeUnder18Apprentices: "Yes",
        //   employmentUnderLocalMinimumAgePrevention: "Yes",
        //   employmentUnderLocalMinimumAgePreventionEmploymentContracts: "Yes",
        //   employmentUnderLocalMinimumAgePreventionJobDescription: "Yes",
        //   employmentUnderLocalMinimumAgePreventionIdentityDocuments: "Yes",
        //   employmentUnderLocalMinimumAgePreventionTraining: "Yes",
        //   employmentUnderLocalMinimumAgePreventionCheckingOfLegalMinimumAge: "Yes",
        // },
        // grievanceMechanism: {
        //   grievanceHandlingMechanism: "Yes",
        //   grievanceHandlingMechanismUsedForReporting: "Yes",
        //   legalProceedings: "Yes",
        // },
        // forcedLabourSlaveryAndDebtBondage: {
        //   forcedLabourAndSlaveryPrevention: "Yes",
        //   forcedLabourAndSlaveryPreventionEmploymentContracts: "Yes",
        //   forcedLabourAndSlaveryPreventionIdentityDocuments: "Yes",
        //   forcedLabourAndSlaveryPreventionFreeMovement: "Yes",
        //   forcedLabourAndSlaveryPreventionProvisionSocialRoomsAndToilets: "Yes",
        //   forcedLabourAndSlaveryPreventionTraining: "Yes",
        //   documentedWorkingHoursAndWages: "Yes",
        //   adequateLivingWage: "Yes",
        //   regularWagesProcessFlow: "Yes",
        //   fixedHourlyWages: "Yes",
        // },
        // osh: {
        //   oshMonitoring: "Yes",
        //   oshPolicy: "Yes",
        //   oshPolicyPersonalProtectiveEquipment: "Yes",
        //   oshPolicyMachineSafety: "Yes",
        //   oshPolicyDisasterBehaviouralResponse: "Yes",
        //   oshPolicyAccidentsBehaviouralResponse: "Yes",
        //   oshPolicyWorkplaceErgonomics: "Yes",
        //   oshPolicyHandlingChemicalsAndOtherHazardousSubstances: "Yes",
        //   oshPolicyFireProtection: "Yes",
        //   oshPolicyWorkingHours: "Yes",
        //   oshPolicyTrainingAddressed: "Yes",
        //   oshPolicyTraining: "Yes",
        //   oshManagementSystem: "Yes",
        //   oshManagementSystemInternationalCertification: "Yes",
        //   oshManagementSystemNationalCertification: "Yes",
        //   workplaceAccidentsUnder10: "Yes",
        //   oshTraining: "Yes",
        // },
        // freedomOfAssociation: {
        //   freedomOfAssociation: "Yes",
        //   discriminationForTradeUnionMembers: "Yes",
        //   freedomOfOperationForTradeUnion: "Yes",
        //   freedomOfAssociationTraining: "Yes",
        //   worksCouncil: "Yes",
        // },
        // humanRights: {
        //   diversityAndInclusionRole: "Yes",
        //   preventionOfMistreatments: "Yes",
        //   equalOpportunitiesOfficer: "Yes",
        //   riskOfHarmfulPollution: "Yes",
        //   unlawfulEvictionAndTakingOfLand: "Yes",
        //   useOfPrivatePublicSecurityForces: "Yes",
        //   useOfPrivatePublicSecurityForcesAndRiskOfViolationOfHumanRights: "Yes",
        // },
        // evidenceCertificatesAndAttestations: {
        //   iso26000: "Yes",
        //   sa8000Certification: "Yes",
        //   smetaSocialAuditConcept: "Yes",
        //   betterWorkProgramCertificate: "Yes",
        //   iso45001Certification: "Yes",
        //   iso14000Certification: "Yes",
        //   emasCertification: "Yes",
        //   iso37001Certification: "Yes",
        //   iso37301Certification: "Yes",
        //   riskManagementSystemCertification: "Yes",
        //   amforiBsciAuditReport: "Yes",
        //   initiativeClauseSocialCertification: "Yes",
        //   responsibleBusinessAssociationCertification: "Yes",
        //   fairLabourAssociationCertification: "Yes",
        //   fairWorkingConditionsPolicy: "Yes",
        //   fairAndEthicalRecruitmentPolicy: "Yes",
        //   equalOpportunitiesAndNondiscriminationPolicy: "Yes",
        //   healthAndSafetyPolicy: "Yes",
        //   complaintsAndGrievancesPolicy: "Yes",
        //   forcedLabourPolicy: "Yes",
        //   childLabourPolicy: "Yes",
        //   environmentalImpactPolicy: "Yes",
        //   supplierCodeOfConduct: "Yes",
        // },
      },
      governance: {
        // socialAndEmployeeMatters: {
        //   responsibilitiesForFairWorkingConditions: "Yes",
        // },
        // environment: {
        //   responsibilitiesForTheEnvironment: "Yes",
        // },
        // osh: {
        //   responsibilitiesForOccupationalSafety: "Yes",
        // },
        // riskManagement: {
        //   riskManagementSystem: "Yes",
        // },
        // codeOfConduct: {
        //   codeOfConduct: "Yes",
        //   codeOfConductRiskManagementTopics: "Yes",
        //   codeOfConductTraining: "Yes",
        // },
      },
      environmental: {
        waste: {
          mercuryAndMercuryWasteHandling: "Yes",
          mercuryAndMercuryWasteHandlingPolicy: "Yes",
          chemicalHandling: "Yes",
          environmentalManagementSystem: "Yes",
          environmentalManagementSystemInternationalCertification: "Yes",
          environmentalManagementSystemNationalCertification: "Yes",
          legalRestrictedWaste: "Yes",
          legalRestrictedWasteProcesses: "Yes",
          mercuryAddedProductsHandling: "Yes",
          mercuryAddedProductsHandlingRiskOfExposure: "Yes",
          mercuryAddedProductsHandlingRiskOfDisposal: "Yes",
          mercuryAndMercuryCompoundsProductionAndUse: "Yes",
          mercuryAndMercuryCompoundsProductionAndUseRiskOfExposure: "Yes",
          persistentOrganicPollutantsProductionAndUse: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskOfExposure: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskOfDisposal: "Yes",
          persistentOrganicPollutantsProductionAndUseTransboundaryMovements: "Yes",
          persistentOrganicPollutantsProductionAndUseRiskForImportingState: "Yes",
          hazardousWasteTransboundaryMovementsLocatedOECDEULiechtenstein: "Yes",
          hazardousWasteTransboundaryMovementsOutsideOECDEULiechtenstein: "Yes",
          hazardousWasteDisposal: "Yes",
          hazardousWasteDisposalRiskOfImport: "Yes",
          hazardousAndOtherWasteImport: "Yes",
        },
      },
    },
    postEuTaxonomyDataForFinancialsResponse: null,
    humanizeString: humanizeString,
    lksgKpiInfoMappings,
    lksgKpiNameMappings,
    lksgSubAreaNameMappings,
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
    postLkSGData() {
      console.log("SUBMIT", this.Model);
    },
    addNewProductionSite() {
      this.listOfProductionSites.push({
        id: Math.random(),
        listOfGoodsOrServices: [],
        listOfGoodsOrServicesString: "",
      });
    },
    addNewItemsTolistOfProductionSites(index: number) {
      const items = this.listOfProductionSites[index].listOfGoodsOrServicesString.split(";").map((item) => item.trim());
      this.listOfProductionSites[index].listOfGoodsOrServices = [
        ...this.listOfProductionSites[index].listOfGoodsOrServices,
        ...items,
      ];
      this.listOfProductionSites[index].listOfGoodsOrServicesString = "";
    },
    removeItemFromlistOfProductionSites(id: number) {
      this.listOfProductionSites = this.listOfProductionSites.filter((el) => el.id !== id);
    },
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
  scroll-margin-top: 300px;
}
.formkit-icon {
  max-width: 5em;
}
</style>
