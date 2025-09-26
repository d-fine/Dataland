<template>
  <AuthorizationWrapper :required-role="KEYCLOAK_ROLE_UPLOADER" :company-id="companyID">
    <TheContent>
      <MarginWrapper class="mb-2">
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
</template>

<script lang="ts">
import TheContent from '@/components/generics/TheContent.vue';
import CompanyInformation from '@/components/pages/CompanyInformation.vue';
import MetaInfoPerCompanyAndFramework from '@/components/resources/chooseFrameworkForDataUpload/MetaInfoPerCompanyAndFramework.vue';
import AuthorizationWrapper from '@/components/wrapper/AuthorizationWrapper.vue';
import MarginWrapper from '@/components/wrapper/MarginWrapper.vue';
import { ApiClientProvider } from '@/services/ApiClients';
import { FRONTEND_SUPPORTED_FRAMEWORKS } from '@/utils/Constants';
import { KEYCLOAK_ROLE_UPLOADER } from '@/utils/KeycloakRoles';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { type DataMetaInformation, DataTypeEnum } from '@clients/backend';
import type Keycloak from 'keycloak-js';
import Card from 'primevue/card';
import { defineComponent, inject } from 'vue';

export default defineComponent({
  name: 'ChooseFramework',
  components: {
    MarginWrapper,
    AuthorizationWrapper,
    CompanyInformation,
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
    return {
      allFrameworksExceptEuTaxonomy: FRONTEND_SUPPORTED_FRAMEWORKS.filter(
        (frameworkName) =>
          ![DataTypeEnum.EutaxonomyFinancials as string, DataTypeEnum.EutaxonomyNonFinancials as string].includes(
            frameworkName
          )
      ),
      waitingForData: true,
      DataTypeEnum,
      humanizeString: humanizeStringOrNumber,
      mapOfDataTypeToListOfDataMetaInfo: new Map<DataTypeEnum, DataMetaInformation[]>(),
      KEYCLOAK_ROLE_UPLOADER,
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
      for (const listOfDataMetaInfoForUniqueReportingPeriod of Array.from(
        mapOfReportingPeriodToListOfDataMetaInfo.values()
      )) {
        resultArray.push(...this.sortListOfDataMetaInfoByUploadTime(listOfDataMetaInfoForUniqueReportingPeriod));
      }
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
        for (const [key, value] of this.mapOfDataTypeToListOfDataMetaInfo.entries()) {
          this.mapOfDataTypeToListOfDataMetaInfo.set(key, this.groupAndSortListOfDataMetaInfo(value));
        }
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
      if (this.waitingForData) {
        return [];
      } else {
        return this.mapOfDataTypeToListOfDataMetaInfo.get(dataType) ?? [];
      }
    },
  },
});
</script>
<style>
.d-card {
  background: var(--default-neutral-white);
  padding: var(--spacing-md);
  box-shadow: 0 0 3px 3px var(--shadow-color);
}

.top-border-section {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--input-separator);
}

.bottom-border-section-dots {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px dotted var(--input-separator);
}

.uploadFormWrapper {
  input[type='checkbox'],
  input[type='radio'] {
    display: grid;
    place-content: center;
    height: 18px;
    width: 18px;
    cursor: pointer;
    margin: 0 10px 0 0;
  }
  input[type='checkbox'] {
    background-color: var(--input-text-bg);
    border: 2px solid var(--input-checked-color);
    border-radius: 2px;
  }
  input[type='radio'],
  input[type='checkbox']::before,
  input[type='radio']::before {
    content: '';
    width: 5px;
    height: 7px;
    border-width: 0 2px 2px 0;
    transform: rotate(45deg);
    margin-top: -2px;
    display: none;
  }
  input[type='checkbox']::before {
    border-style: solid;
    border-color: var(--input-text-bg);
  }
  input[type='radio']::before,
  input[type='checkbox']:checked::before,
  input[type='radio']:checked::before {
    display: block;
  }
  label[data-checked='true'] input[type='radio']::before {
    display: block;
  }
}
</style>
