<template>
  <AuthenticationWrapper>
    <TheHeader />
    <UploaderRoleWrapper>
      <TheContent>
        <BackButton id="backButton" label="BACK" />
        <CompanyInformation :companyID="companyID" />
        <Card class="col-12 text-left page-wrapper-card">
          <template #title> New Dataset - Framework </template>
          <template #content>
            <div class="uploadFormWrapper grid">
              <div id="euTaxonomyContainer" class="col-9 flex">
                <div id="euTaxonomyLabel" class="col-3 p-3">
                  <h3>EU Taxonomy</h3>
                  <p>Overview of all existing and missing EU Taxonomy datasets for this company.</p>
                </div>
                <div class="col-9 d-card">
                  <div id="eutaxonomyDataSetsContainer">
                    <h4 class="bottom-border-section-dots">Eu Taxonomy Data Sets:</h4>

                    <MetaInfoPerCompanyAndFramework
                      title="Non-Financials"
                      :isFrontendUploadFormExisting="false"
                      :framework-url-path="DataTypeEnum.EutaxonomyNonFinancials"
                      :companyId="companyID"
                      :isWaitingForData="waitingForData"
                      :listOfFrameworkData="listOfEuTaxonomyNonFinancialsMetaInfo"
                      class="bottom-border-section-dots"
                    />

                    <MetaInfoPerCompanyAndFramework
                      title="Financials"
                      :isFrontendUploadFormExisting="false"
                      :framework-url-path="DataTypeEnum.EutaxonomyFinancials"
                      :companyId="companyID"
                      :isWaitingForData="waitingForData"
                      :listOfFrameworkData="listOfEuTaxonomyFinancialsMetaInfo"
                    />
                  </div>
                </div>
              </div>

              <div id="sfdrContainer" class="col-9 flex top-border-section">
                <div id="sfdrLabel" class="col-3 p-3">
                  <h3>SFDR</h3>
                  <p>Overview of all existing and missing SFDR datasets for this company.</p>
                </div>
                <div class="col-9 d-card">
                  <MetaInfoPerCompanyAndFramework
                    title="SFDR"
                    :isFrontendViewPageExisting="false"
                    :isFrontendUploadFormExisting="false"
                    :framework-url-path="DataTypeEnum.Sfdr"
                    :companyId="companyID"
                    :isWaitingForData="waitingForData"
                    :listOfFrameworkData="listOfSfdrMetaInfo"
                  />
                </div>
              </div>

              <div id="lksgContainer" class="col-9 flex top-border-section">
                <div id="lksgLabel" class="col-3">
                  <h3>LkSG</h3>
                  <p>Overview of all existing and missing LkSG datasets for this company.</p>
                </div>
                <div class="col-9 d-card">
                  <MetaInfoPerCompanyAndFramework
                    title="LkSG"
                    :framework-url-path="DataTypeEnum.Lksg"
                    :companyId="companyID"
                    :isWaitingForData="waitingForData"
                    :listOfFrameworkData="listOfLksgMetaInfo"
                  />
                </div>
              </div>
            </div>
          </template>
        </Card>
      </TheContent>
    </UploaderRoleWrapper>
    <DatalandFooter />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { ApiClientProvider } from "@/services/ApiClients";
import { defineComponent, inject } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";
import TheContent from "@/components/generics/TheContent.vue";
import AuthenticationWrapper from "@/components/wrapper/AuthenticationWrapper.vue";
import TheHeader from "@/components/generics/TheHeader.vue";
import BackButton from "@/components/general/BackButton.vue";
import Card from "primevue/card";
import CompanyInformation from "@/components/pages/CompanyInformation.vue";
import { DataMetaInformation, DataTypeEnum } from "@clients/backend";
import MetaInfoPerCompanyAndFramework from "@/components/resources/chooseFrameworkForDataUpload/MetaInfoPerCompanyAndFramework.vue";
import UploaderRoleWrapper from "@/components/wrapper/UploaderRoleWrapper.vue";
import DatalandFooter from "@/components/general/DatalandFooter.vue";

export default defineComponent({
  name: "ChooseFramework",
  components: {
    DatalandFooter,
    UploaderRoleWrapper,
    CompanyInformation,
    AuthenticationWrapper,
    TheHeader,
    BackButton,
    TheContent,
    Card,
    MetaInfoPerCompanyAndFramework,
  },
  setup() {
    return {
      getKeycloakPromise: inject<() => Promise<Keycloak>>("getKeycloakPromise"),
    };
  },

  mounted() {
    void this.getMetaInfoAboutAllDataSetsForCurrentCompany();
  },

  data() {
    return {
      waitingForData: true,
      listOfEuTaxonomyNonFinancialsMetaInfo: [] as Array<DataMetaInformation>,
      listOfEuTaxonomyFinancialsMetaInfo: [] as Array<DataMetaInformation>,
      listOfSfdrMetaInfo: [] as Array<DataMetaInformation>,
      listOfLksgMetaInfo: [] as Array<DataMetaInformation>,
      DataTypeEnum,
    };
  },
  props: {
    companyID: {
      type: String,
    },
  },

  watch: {},
  methods: {
    // TODO test this in unit tests
    /**
     *  Sorts a list of data meta information alphabetically by their reporting period
     *
     * @param listOfDataMetaInfo the list of data meta information to be sorted
     * @returns the sorted list of data meta information
     */
    sortListOfDataMetaInfoAlphabeticallyByReportingPeriod(
      listOfDataMetaInfo: DataMetaInformation[]
    ): DataMetaInformation[] {
      listOfDataMetaInfo.sort((dataMetaInfoA, dataMetaInfoB) => {
        if (dataMetaInfoA.reportingPeriod > dataMetaInfoB.reportingPeriod) return -1;
        else return 0;
      });
      return listOfDataMetaInfo;
    },

    // TODO test this in unit tests
    /**
     *  Sorts a list of data meta information descending by their uploading time
     *
     * @param listOfDataMetaInfo the list of data meta information to be sorted
     * @returns the sorted list of data meta information
     */
    sortListOfDataMetaInfoByUploadTime(listOfDataMetaInfo: Array<DataMetaInformation>): Array<DataMetaInformation> {
      return listOfDataMetaInfo.sort(
        (dataMetaInfoA, dataMetaInfoB) => dataMetaInfoB.uploadTime - dataMetaInfoA.uploadTime
      );
    },

    // TODO test this in unit tests
    /**
     *  This function assigns the elements of an array of data meta info to buckets/groups based on their reporting periods.
     *  It does so by using a map. It takes the list of data meta info and puts its elements into sub-arrays, which
     *  are the values of that map. The respective reporting period is the key of those sub-arrays.
     *
     * @param listOfDataMetaInfo the list of data meta information to be grouped
     * @returns a map with the distinct reporting periods as keys and arrays of data meta info for that period as values
     */
    groupListOfDataMetaInfoAsMapOfReportingPeriodToListOfDataMetaInfo(
      listOfDataMetaInfo: DataMetaInformation[]
    ): Map<string, DataMetaInformation[]> {
      const mapOfReportingPeriodToListOfDataMetaInfo = new Map<string, DataMetaInformation[]>();
      listOfDataMetaInfo.forEach((currentDataMetaInfo) => {
        const reportingPeriodOfCurrentDataMetaInfo = currentDataMetaInfo.reportingPeriod;
        const listOfDataMetaInfoForUniqueReportingPeriod = mapOfReportingPeriodToListOfDataMetaInfo.get(
          reportingPeriodOfCurrentDataMetaInfo
        );
        if (!listOfDataMetaInfoForUniqueReportingPeriod) {
          mapOfReportingPeriodToListOfDataMetaInfo.set(reportingPeriodOfCurrentDataMetaInfo, [currentDataMetaInfo]);
        } else {
          listOfDataMetaInfoForUniqueReportingPeriod.push(currentDataMetaInfo);
        }
      });
      return mapOfReportingPeriodToListOfDataMetaInfo;
    },

    // TODO test in unit tests
    /**
     *  Groups a list of data meta information by their reporting periods, then executes a sorting function on
     *  each group, and then unites and returns all those groups.
     *
     * @param listOfDataMetaInfo the list of data meta information to be grouped and sorted
     * @returns a list of data meta info as the united sub-lists of the groups
     */
    groupAndSortListOfDataMetaInfo(listOfDataMetaInfo: Array<DataMetaInformation>): Array<DataMetaInformation> {
      const listOfDataMetaInfoSortedByReportingPeriod =
        this.sortListOfDataMetaInfoAlphabeticallyByReportingPeriod(listOfDataMetaInfo);
      const mapOfReportingPeriodToListOfDataMetaInfo =
        this.groupListOfDataMetaInfoAsMapOfReportingPeriodToListOfDataMetaInfo(
          listOfDataMetaInfoSortedByReportingPeriod
        );

      const resultArray: DataMetaInformation[] = [];
      Array.from(mapOfReportingPeriodToListOfDataMetaInfo.values()).forEach(
        (listOfDataMetaInfoForUniqueReportingPeriod) => {
          resultArray.push(...this.sortListOfDataMetaInfoByUploadTime(listOfDataMetaInfoForUniqueReportingPeriod));
        }
      );
      return resultArray;
    },

    /**
     * Gets all data meta information of the company identified by the company ID in the URL and fills the lists for
     * data meta information of the various frameworks
     */
    async getMetaInfoAboutAllDataSetsForCurrentCompany() {
      try {
        const metaDataControllerApi = await new ApiClientProvider(
          assertDefined(this.getKeycloakPromise)()
        ).getMetaDataControllerApi();
        const response = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID, undefined, true);
        const listOfAllDataMetaInfo = response.data;
        this.listOfEuTaxonomyNonFinancialsMetaInfo = this.groupAndSortListOfDataMetaInfo(
          listOfAllDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.EutaxonomyNonFinancials
          )
        );
        this.listOfEuTaxonomyFinancialsMetaInfo = this.groupAndSortListOfDataMetaInfo(
          listOfAllDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.EutaxonomyFinancials
          )
        );
        this.listOfSfdrMetaInfo = this.groupAndSortListOfDataMetaInfo(
          listOfAllDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.Sfdr
          )
        );
        this.listOfLksgMetaInfo = this.groupAndSortListOfDataMetaInfo(
          listOfAllDataMetaInfo.filter(
            (dataMetaInfo: DataMetaInformation) => dataMetaInfo.dataType === DataTypeEnum.Lksg
          )
        );
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },
  },
});
</script>
