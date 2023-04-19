<template>
  <Card class="col-12 page-wrapper-card">
    <template #title>New Dataset - LkSG </template>
    <template #content>
      <div v-show="waitingForData" class="d-center-div text-center px-7 py-4">
        <p class="font-medium text-xl">Loading LkSG data...</p>
        <em class="pi pi-spinner pi-spin" aria-hidden="true" style="z-index: 20; color: #e67f3f" />
      </div>
      <div v-show="!waitingForData" class="grid uploadFormWrapper">
        <div id="uploadForm" class="text-left uploadForm col-9">
          <FormKit
            v-model="companyAssociatedLksgData"
            :actions="false"
            type="form"
            id="createLkSGForm"
            name="createLkSGForm"
            @submit="postLkSGData"
            @submit-invalid="checkCustomInputs"
          >
            <FormKit type="hidden" name="companyId" :model-value="companyID!" disabled="true" />
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
                    <div>
                      <!-- TODO: PROTOTYPE -->
                      <h1>TODO: NACE SELECTOR PROTOTYPE</h1>
                      <NaceSectorSelector v-model="selectedNaceCodes" />
                    </div>
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

                <div v-for="section in lksgDataModel" :key="section">
                  <div class="uploadFormSection grid" v-for="subsection in section.categories" :key="subsection">
                    <div class="col-3 p-3 topicLabel">
                      <h4 class="anchor title">{{ subsection.label }}</h4>
                      <div :class="`p-badge badge-${section.color}`">
                        <span>{{ section.label.toUpperCase() }}</span>
                      </div>
                    </div>

                    <div class="col-9 formFields">
                      <FormKit v-for="field in subsection.fields" :key="field" type="group" :name="subsection.name">
                        <component
                          v-if="isYes(field.dependency)"
                          :is="field.component"
                          :displayName="field.label"
                          :info="field.description"
                          :name="field.name"
                          :placeholder="field.placeholder"
                          :options="field.options"
                        />
                      </FormKit>
                    </div>
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
                    <FormKit
                      v-for="area in lksgSubAreas.forcedLabourSlaveryAndDebtBondage"
                      type="group"
                      name="forcedLabourSlaveryAndDebtBondage"
                      :key="area"
                    >
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit
                      v-for="area in lksgSubAreas.evidenceCertificatesAndAttestations"
                      type="group"
                      name="evidenceCertificatesAndAttestations"
                      :key="area"
                    >
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit
                      v-for="area in lksgSubAreas.grievanceMechanism"
                      type="group"
                      name="grievanceMechanism"
                      :key="area"
                    >
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
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
                    <FormKit v-for="area in lksgSubAreas.osh" type="group" name="osh" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit
                      v-for="area in lksgSubAreas.freedomOfAssociation"
                      type="group"
                      name="freedomOfAssociation"
                      :key="area"
                    >
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit v-for="area in lksgSubAreas.humanRights" type="group" name="humanRights" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit
                      v-for="area in lksgSubAreas.socialAndEmployeeMatters"
                      type="group"
                      name="socialAndEmployeeMatters"
                      :key="area"
                    >
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit v-for="area in lksgSubAreas.environment" type="group" name="environment" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <h4 id="riskManagement" class="anchor title">{{ lksgSubAreasNameMappings.riskManagement }}</h4>
                    <div class="p-badge badge-blue"><span>GOVERNANCE</span></div>
                  </div>
                  <div class="col-9 formFields">
                    <FormKit v-for="area in lksgSubAreas.riskManagement" type="group" name="riskManagement" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit v-for="area in lksgSubAreas.codeOfConduct" type="group" name="codeOfConduct" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
                    <FormKit v-for="area in lksgSubAreas.waste" type="group" name="waste" :key="area">
                      <component
                        :is="lksgFieldComponentTypes[area]"
                        :displayName="lksgKpisNameMappings[area]"
                        :info="lksgKpisInfoMappings[area]"
                        :name="area"
                        :displayed="getYesNoValue(lksgFieldDependencies[area])"
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
        <div id="jumpLinks" ref="jumpLinks" class="col-3 p-3 text-left jumpLinks">
          <h4 id="topicTitles" class="title">On this page</h4>
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
        </div>
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
import UploadFormHeader from "@/components/forms/parts/elements/UploadFormHeader.vue";
import YesNoComponent from "@/components/forms/parts/elements/YesNoComponent.vue";
import Calendar from "primevue/calendar";
import SuccessUpload from "@/components/messages/SuccessUpload.vue";
import FailedUpload from "@/components/messages/FailedUpload.vue";
import {
  lksgKpisInfoMappings,
  lksgKpisNameMappings,
  lksgSubAreasNameMappings,
  lksgSubAreas,
  lksgFieldDependencies,
  lksgFieldComponentTypes,
  test,
  test2,
  test3,
  lksgDataModel,
} from "@/components/resources/frameworkDataSearch/lksg/DataModelsTranslations";
import { getAllCountryNamesWithCodes } from "@/utils/CountryCodeConverter";
import { AxiosError } from "axios";
import { humanizeString } from "@/utils/StringHumanizer";
import { CompanyAssociatedDataLksgData, InHouseProductionOrContractProcessing } from "@clients/backend";
import { useRoute } from "vue-router";
import { getHyphenatedDate } from "@/utils/DataFormatUtils";
import { smoothScroll } from "@/utils/smoothScroll";
import { checkCustomInputs } from "@/utils/validationsUtils";
import NaceSectorSelector from "@/components/forms/parts/NaceSectorSelector.vue";
import FreeTextFormElement from "@/components/forms/parts/elements/FreeTextFormElement.vue";
import NumberFormElement from "@/components/forms/parts/elements/NumberFormElement.vue";
import DateFormElement from "@/components/forms/parts/elements/DateFormElement.vue";
import SingleSelectFormElement from "@/components/forms/parts/elements/SingleSelectFormElement.vue";
import MultiSelectFormElement from "@/components/forms/parts/elements/MultiSelectFormElement.vue";

export default defineComponent({
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },
  name: "CreateLksgDataset",
  components: {
    UploadFormHeader,
    SuccessUpload,
    FailedUpload,
    FormKit,
    Card,
    PrimeButton,
    Calendar,
    YesNoComponent,
    FreeTextFormElement,
    NumberFormElement,
    DateFormElement,
    SingleSelectFormElement,
    MultiSelectFormElement,
    NaceSectorSelector,
  },
  directives: {
    tooltip: Tooltip,
  },

  data() {
    return {
      selectedNaceCodes: [], // TODO: PROTOTYPE
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
      companyAssociatedLksgData: {} as CompanyAssociatedDataLksgData,
      route: useRoute(),
      message: "",
      uploadSucceded: false,
      postLkSGDataProcessed: false,
      messageCounter: 0,
      lksgKpisInfoMappings,
      lksgKpisNameMappings,
      lksgSubAreasNameMappings,
      lksgSubAreas,
      lksgFieldDependencies,
      lksgFieldComponentTypes,
      test,
      test2,
      test3,
      lksgDataModel,
      elementPosition: 0,
      scrollListener: (): null => null,
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
    yearOfDataDate(): string {
      return this.dataDate?.getFullYear()?.toString() || "";
    },
    convertedDataDate(): string {
      if (this.dataDate) {
        return getHyphenatedDate(this.dataDate);
      } else {
        return "";
      }
    },
  },
  props: {
    companyID: {
      type: String,
    },
  },
  mounted() {
    const jumpLinkselement = this.$refs.jumpLinks as HTMLElement;
    this.elementPosition = jumpLinkselement.getBoundingClientRect().top;
    this.scrollListener = (): null => {
      if (window.scrollY > this.elementPosition) {
        jumpLinkselement.style.position = "fixed";
        jumpLinkselement.style.top = "60px";
      } else {
        jumpLinkselement.style.position = "relative";
        jumpLinkselement.style.top = "0";
      }
      return null;
    };
    window.addEventListener("scroll", this.scrollListener);

    const dataId = this.route.query.templateDataId;
    if (dataId !== undefined && typeof dataId === "string" && dataId !== "") {
      void this.loadLKSGData(dataId);
    }
  },
  unmounted() {
    window.removeEventListener("scroll", this.scrollListener);
  },
  methods: {
    /**
     * Returns the value of a given YesNo variable
     *
     * @param variable the string representation of the YesNo variable to be read out
     * @returns either "Yes" or "No"
     */
    getYesNoValue(variable: string | undefined): string {
      if (variable == undefined || variable == "") {
        return "Yes";
      }
      return eval(variable) as string;
    },
    /**
     * Returns the value of a given YesNo variable is Yes
     *
     * @param variable the string representation of the YesNo variable to be read out
     * @returns the boolean result
     */
    isYes(variable: string | undefined): boolean {
      if (variable == undefined || variable == "") {
        return true;
      }
      return eval(variable) === "Yes";
    },
    /**
     * Loads the LkSG-Dataset identified by the provided dataId and pre-configures the form to contain the data
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
      this.companyAssociatedLksgData = lksgDataset;
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
        await lkSGDataControllerApi.postCompanyAssociatedLksgData(this.companyAssociatedLksgData);
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
<style scoped lang="scss">
.anchor {
  scroll-margin-top: 100px;
}
</style>
