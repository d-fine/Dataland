<template>
  <AuthenticationWrapper>
    <TheHeader />
    <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER" :company-id="companyID">
      <TheContent>
        <MarginWrapper class="mb-2">
          <BackButton id="backButton" label="BACK" />
          <CompanyInformation :companyId="companyID" />
        </MarginWrapper>
        <Card class="col-12 text-left page-wrapper-card">
          <template #title> New Dataset - Framework </template>
          <template #content>
            <div class="uploadFormWrapper grid">
              <div id="euTaxonomyContainer" class="col-9 flex">
                <div id="euTaxonomyLabel" class="col-3 p-3">
                  <h3>EU Taxonomy</h3>
                  <p>{{ buildSubtitle('EU Taxonomy') }}</p>
                </div>
                <div class="col-9 d-card">
                  <div id="eutaxonomyDataSetsContainer">
                    <h4 class="bottom-border-section-dots">Eu Taxonomy Data Sets:</h4>

                    <MetaInfoPerCompanyAndFramework
                      :data-type="DataTypeEnum.EutaxonomyNonFinancials"
                      :companyId="companyID"
                      :isWaitingForData="waitingForData"
                      :listOfFrameworkData="getFrameworkMetaInfos(DataTypeEnum.EutaxonomyNonFinancials)"
                      class="bottom-border-section-dots"
                    />

                    <MetaInfoPerCompanyAndFramework
                      :data-type="DataTypeEnum.EutaxonomyFinancials"
                      :companyId="companyID"
                      :isWaitingForData="waitingForData"
                      :listOfFrameworkData="getFrameworkMetaInfos(DataTypeEnum.EutaxonomyFinancials)"
                    />
                  </div>
                </div>
              </div>

              <div
                v-for="dataType in allFrameworksExceptEuTaxonomy"
                :key="dataType"
                class="col-9 flex top-border-section"
                :id="dataType + 'Container'"
              >
                <div :id="dataType + 'Label'" class="col-3 p-3">
                  <h3>{{ humanizeString(dataType) }}</h3>
                  <p>{{ buildSubtitle(humanizeString(dataType)) }}</p>
                  <p v-if="dataType === DataTypeEnum.P2p">
                    Framework based on:
                    <a
                      href="https://pathwaystoparis.com/en/tool-box/transformation-perfomance/"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      {{ humanizeString(dataType) }}</a
                    >
                  </p>
                </div>
                <div class="col-9 d-card">
                  <MetaInfoPerCompanyAndFramework
                    :data-type="dataType"
                    :companyId="companyID"
                    :isWaitingForData="waitingForData"
                    :listOfFrameworkData="getFrameworkMetaInfos(dataType)"
                  />
                </div>
              </div>
            </div>
          </template>
        </Card>
      </TheContent>
    </AuthorizationWrapper>
    <TheFooter :is-light-version="true" :sections="footerContent" />
  </AuthenticationWrapper>
</template>

<script lang="ts">
import { ApiClientProvider } from '@/services/ApiClients';
import { defineComponent, inject } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import TheContent from '@/components/generics/TheContent.vue';
import AuthenticationWrapper from '@/components/wrapper/AuthenticationWrapper.vue';
import TheHeader from '@/components/generics/TheHeader.vue';
import BackButton from '@/components/general/BackButton.vue';
import Card from 'primevue/card';
import CompanyInformation from '@/components/pages/CompanyInformation.vue';
import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';
import MetaInfoPerCompanyAndFramework from '@/components/resources/chooseFrameworkForDataUpload/MetaInfoPerCompanyAndFramework.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import TheFooter from '@/components/generics/TheNewFooter.vue';
import contentData from '@/assets/content.json';
import type { Content, Page } from '@/types/ContentTypes';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { FRONTEND_SUPPORTED_FRAMEWORKS } from '@/utils/Constants';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';

export default defineComponent({
  name: 'ChooseFramework',
  components: {
    MarginWrapper,
    TheFooter,
    AuthorizationWrapper,
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
      getKeycloakPromise: inject<() => Promise<Keycloak>>('getKeycloakPromise'),
    };
  },

  created() {
    void this.getMetaInfoAboutAllDataSetsForCurrentCompany();
  },

  data() {
    const content: Content = contentData;
    const footerPage: Page | undefined = content.pages.find((page) => page.url === '/');
    const footerContent = footerPage?.sections;
    return {
      allFrameworksExceptEuTaxonomy: FRONTEND_SUPPORTED_FRAMEWORKS.filter(
        (frameworkName) =>
          [DataTypeEnum.EutaxonomyFinancials as string, DataTypeEnum.EutaxonomyNonFinancials as string].indexOf(
            frameworkName
          ) === -1
      ),
      waitingForData: true,
      DataTypeEnum,
      humanizeString: humanizeStringOrNumber,
      mapOfDataTypeToListOfDataMetaInfo: new Map<DataTypeEnum, DataMetaInformation[]>(),
      KEYCLOAK_ROLE_UPLOADER,
      footerContent,
    };
  },
  props: {
    companyID: {
      type: String,
      required: true,
    },
  },

  methods: {
    /**
     * Function building a unified for subtitle for a framework type
     * @param dataTypeTitle the type of the framework (humanized name)
     * @returns a unified subtitle
     */
    buildSubtitle(dataTypeTitle: string): string {
      return `Overview of all existing ${dataTypeTitle} datasets for this company.`;
    },
    /**
     *  Sorts a list of data meta information alphabetically by their reporting period
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

    /**
     *  Sorts a list of data meta information descending by their uploading time
     * @param listOfDataMetaInfo the list of data meta information to be sorted
     * @returns the sorted list of data meta information
     */
    sortListOfDataMetaInfoByUploadTime(listOfDataMetaInfo: Array<DataMetaInformation>): Array<DataMetaInformation> {
      return listOfDataMetaInfo.sort(
        (dataMetaInfoA, dataMetaInfoB) => dataMetaInfoB.uploadTime - dataMetaInfoA.uploadTime
      );
    },

    /**
     *  This function assigns the elements of an array of data meta info to buckets/groups based on their reporting periods.
     *  It does so by using a map. It takes the list of data meta info and puts its elements into sub-arrays, which
     *  are the values of that map. The respective reporting period is the key of those sub-arrays.
     * @param listOfDataMetaInfo the list of data meta information to be grouped
     * @returns a map with the distinct reporting periods as keys and arrays of data meta info for that period as values
     */
    groupListOfDataMetaInfoAsMapOfReportingPeriodToListOfDataMetaInfo(
      listOfDataMetaInfo: DataMetaInformation[]
    ): Map<string, DataMetaInformation[]> {
      return listOfDataMetaInfo.reduce((groups, dataMetaInfo) => {
        if (groups.get(dataMetaInfo.reportingPeriod)) {
          groups.get(dataMetaInfo.reportingPeriod)?.push(dataMetaInfo);
        } else {
          groups.set(dataMetaInfo.reportingPeriod, [dataMetaInfo]);
        }
        return groups;
      }, new Map<string, DataMetaInformation[]>());
    },

    /**
     *  Groups a list of data meta information by their reporting periods, then executes a sorting function on
     *  each group, and then unites and returns all those groups.
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
        const backendClients = new ApiClientProvider(assertDefined(this.getKeycloakPromise)()).backendClients;
        const metaDataControllerApi = backendClients.metaDataController;
        const response = await metaDataControllerApi.getListOfDataMetaInfo(this.companyID, undefined, false);
        const listOfAllDataMetaInfo = response.data;
        this.mapOfDataTypeToListOfDataMetaInfo = listOfAllDataMetaInfo.reduce((groups, dataMetaInfo) => {
          if (groups.get(dataMetaInfo.dataType)) {
            groups.get(dataMetaInfo.dataType)?.push(dataMetaInfo);
          } else {
            groups.set(dataMetaInfo.dataType, [dataMetaInfo]);
          }
          return groups;
        }, new Map<DataTypeEnum, Array<DataMetaInformation>>());
        this.mapOfDataTypeToListOfDataMetaInfo.forEach((value, key) => {
          this.mapOfDataTypeToListOfDataMetaInfo.set(key, this.groupAndSortListOfDataMetaInfo(value));
        });
        this.waitingForData = false;
      } catch (error) {
        console.error(error);
      }
    },

    /**
     * Returns a list of the meta information available for a framework
     * @param dataType the data type of the data associated to the meta infos returned
     * @returns the meta infos of data with the specified data type
     */
    getFrameworkMetaInfos(dataType: DataTypeEnum): Array<DataMetaInformation> {
      if (!this.waitingForData) {
        return this.mapOfDataTypeToListOfDataMetaInfo.get(dataType) ?? [];
      } else {
        return [];
      }
    },
  },
});
</script>
