<template>
  <div v-bind="$attrs">
    <div v-if="isPending" class="d-center-div text-center px-7 py-4">
      <h1>Loading portfolio data...</h1>
      <DatalandProgressSpinner/>
    </div>
    <div v-else-if="isError" class="d-center-div text-center px-7 py-4">
      <h1>Error loading portfolio data</h1>
      An unexpected error occurred. Please try again later or with [Ctrl] + [F5] or contact the support team if the
      issue persists.
    </div>
    <div v-else>
      <div class="button_bar">
        <Button data-test="edit-portfolio" icon="pi pi-pencil" label="EDIT PORTFOLIO" @click="openEditModal()"/>
        <Button
            data-test="download-portfolio"
            icon="pi pi-download"
            label="DOWNLOAD PORTFOLIO"
            @click="openDownloadModal()"
        />
        <div :title="!isUserDatalandMemberOrAdmin ? 'Only Dataland members can activate monitoring' : ''">
          <Button
              :disabled="!isUserDatalandMemberOrAdmin"
              data-test="monitor-portfolio"
              icon="pi pi-bell"
              label="ACTIVE MONITORING"
              @click="openMonitoringModal()"
          />
        </div>

        <Tag data-test="is-monitored-tag" v-if="isBasePortfolioSuccess" v-bind="monitoredTagAttributes"/>
        <Button
            class="reset-button-align-right"
            data-test="reset-filter"
            label="RESET"
            variant="text"
            @click="resetFilters()"
        />
      </div>

      <DataTable
          v-model:filters="filters"
          :paginator="portfolioEntriesToDisplay.length > MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE"
          :rows="MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE"
          :sortOrder="1"
          :value="portfolioEntriesToDisplay"
          filterDisplay="menu"
          removableSort
          sortField="companyName"
          stripedRows
          tableStyle="min-width: 50rem"
      >
        <template #empty>
          Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to
          add companies or remove filter criteria.
        </template>
        <Column
            :showFilterMatchModes="false"
            :sortable="true"
            field="companyName"
            header="Company Name"
            style="width: 15%"
        >
          <template #body="portfolioEntry">
            <Button
                :label="portfolioEntry.data.companyName"
                :pt="{
                label: {
                  style: 'font-weight: normal; text-align: left;',
                },
                root: {
                  style: 'padding-left: 0;',
                },
              }"
                data-test="view-company-button"
                variant="link"
                @click="router.push(`/companies/${portfolioEntry.data.companyId}`)"
            />
          </template>
          <template #filter="{ filterModel, filterCallback }">
            <InputText
                v-model="filterModel.value"
                :data-test="'companyNameFilterValue'"
                placeholder="Filter by company name"
                type="text"
                @input="filterCallback()"
            />
          </template>
        </Column>
        <Column :showFilterMatchModes="false" :sortable="true" field="country" header="Country" style="width: 12.5%">
          <template #filter="{ filterModel, filterCallback }">
            <div data-test="countryFilterOverlay">
              <div v-for="country of countryOptions" :key="country" class="filter-checkbox">
                <Checkbox
                    v-model="filterModel.value"
                    :inputId="country"
                    :value="country"
                    data-test="countryFilterValue"
                    name="country"
                    @change="filterCallback"
                />
                <label :for="country">{{ country }}</label>
              </div>
            </div>
          </template>
        </Column>
        <Column :showFilterMatchModes="false" :sortable="true" field="sector" header="Sector" style="width: 12.5%">
          <template #filter="{ filterModel, filterCallback }">
            <div data-test="sectorFilterOverlay">
              <div v-for="sector of sectorOptions" :key="sector" class="filter-checkbox">
                <Checkbox
                    v-model="filterModel.value"
                    :inputId="sector"
                    :value="sector"
                    data-test="sectorFilterValue"
                    name="sector"
                    @change="filterCallback"
                />
                <label :for="sector">{{ sector }}</label>
              </div>
            </div>
          </template>
        </Column>
        <Column
            v-for="framework in MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER"
            :key="framework"
            :field="convertKebabCaseToCamelCase(framework) + 'AvailableReportingPeriods'"
            :header="humanizeStringOrNumber(framework)"
            :showFilterMatchModes="false"
            :sortable="true"
            :style="'width: ' + widthOfFrameworkColumn(framework) + '%'"
        >
          <template #body="portfolioEntry">
            <Button
                v-if="portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework)"
                :label="getAvailableReportingPeriods(portfolioEntry.data, framework)"
                :pt="{
                label: {
                  style: 'font-weight: normal; text-align: left;',
                },
                root: {
                  style: 'padding-left: 0;',
                },
              }"
                variant="link"
                @click="router.push(portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework))"
            />
            <span v-else>{{ getAvailableReportingPeriods(portfolioEntry.data, framework) }}</span>
          </template>
          <template #filter="{ filterModel, filterCallback }">
            <div :data-test="convertKebabCaseToCamelCase(framework) + 'AvailableReportingPeriodsFilterOverlay'">
              <div
                  v-for="availableReportingPeriods in reportingPeriodOptions.get(framework)"
                  :key="availableReportingPeriods"
                  class="filter-checkbox"
              >
                <Checkbox
                    v-model="filterModel.value"
                    :data-test="convertKebabCaseToCamelCase(framework) + 'AvailableReportingPeriodsFilterValue'"
                    :inputId="availableReportingPeriods"
                    :value="availableReportingPeriods"
                    name="availableReportingPeriods"
                    @change="filterCallback"
                />
                <label :for="availableReportingPeriods">{{ availableReportingPeriods }}</label>
              </div>
            </div>
          </template>
        </Column>
      </DataTable>
    </div>
    <SuccessDialog
        :message="successDialogMessage"
        :visible="isSuccessDialogVisible"
        @close="isSuccessDialogVisible = false"
    />
  </div>
</template>

<script lang="ts" setup>
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import {ApiClientProvider} from '@/services/ApiClients.ts';
import {
  ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER,
  MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER,
  MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE,
} from '@/utils/Constants.ts';
import {getCountryNameFromCountryCode} from '@/utils/CountryCodeConverter.ts';
import {convertKebabCaseToCamelCase, humanizeStringOrNumber} from '@/utils/StringFormatter.ts';
import {assertDefined} from '@/utils/TypeScriptUtils.ts';
import type {EnrichedPortfolioEntry} from '@clients/userservice';
import {type CompanyIdAndName, DataTypeEnum, ExportFileType} from '@clients/backend';
import {FilterMatchMode} from '@primevue/core/api';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import Checkbox from 'primevue/checkbox';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import Tag from 'primevue/tag';
import {useDialog} from 'primevue/usedialog';
import {computed, inject, onMounted, ref} from 'vue';
import PortfolioMonitoring from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import DownloadData from '@/components/general/DownloadData.vue';
import SuccessDialog from '@/components/general/SuccessDialog.vue';
import type {PublicFrameworkDataApi} from '@/utils/api/UnifiedFrameworkDataApi.ts';
import type {FrameworkData} from '@/utils/GenericFrameworkTypes.ts';
import {getFrameworkDataApiForIdentifier} from '@/frameworks/FrameworkApiUtils.ts';
import {ExportFileTypeInformation} from '@/types/ExportFileTypeInformation.ts';
import type {AxiosError, AxiosRequestConfig} from 'axios';
import {getDateStringForDataExport} from '@/utils/DataFormatUtils.ts';
import {forceFileDownload, groupAllReportingPeriodsByFrameworkForPortfolio} from '@/utils/FileDownloadUtils.ts';
import router from '@/router';
import {checkIfUserHasRole} from '@/utils/KeycloakUtils.ts';
import {KEYCLOAK_ROLE_ADMIN} from '@/utils/KeycloakRoles.ts';
import {keepPreviousData, useQuery} from '@tanstack/vue-query';

/**
 * This class prepares raw `EnrichedPortfolioEntry` data for use in UI components
 * by transforming and enriching fields, such as converting country codes to names,
 * resolving fallback values, and constructing framework-related URLs.
 */
class PortfolioEntryPrepared {
  readonly companyId: string;
  readonly companyName: string;
  readonly sector?: string;
  readonly country: string;
  readonly companyCockpitRef: string;
  readonly frameworkHyphenatedNamesToDataRef: Map<string, string | undefined>;
  readonly sfdrAvailableReportingPeriods: string | undefined;
  readonly eutaxonomyFinancialsAvailableReportingPeriods: string | undefined;
  readonly eutaxonomyNonFinancialsAvailableReportingPeriods: string | undefined;
  readonly nuclearAndGasAvailableReportingPeriods: string | undefined;

  constructor(portfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = portfolioEntry.companyId;
    this.companyName = portfolioEntry.companyName;
    this.sector = portfolioEntry.sector;
    this.country = getCountryNameFromCountryCode(portfolioEntry.countryCode) ?? 'unknown';
    this.companyCockpitRef = portfolioEntry.companyCockpitRef;
    this.frameworkHyphenatedNamesToDataRef = new Map<string, string | undefined>();

    for (const framework of MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER) {
      this.frameworkHyphenatedNamesToDataRef.set(
          framework,
          portfolioEntry.frameworkHyphenatedNamesToDataRef[framework] ||
          (portfolioEntry.availableReportingPeriods[framework]
              ? `/companies/${portfolioEntry.companyId}/frameworks/${framework}`
              : undefined)
      );
    }

    this.sfdrAvailableReportingPeriods =
        portfolioEntry.availableReportingPeriods[DataTypeEnum.Sfdr] || 'No data available';
    this.eutaxonomyFinancialsAvailableReportingPeriods =
        portfolioEntry.availableReportingPeriods[DataTypeEnum.EutaxonomyFinancials] || 'No data available';
    this.eutaxonomyNonFinancialsAvailableReportingPeriods =
        portfolioEntry.availableReportingPeriods[DataTypeEnum.EutaxonomyNonFinancials] || 'No data available';
    this.nuclearAndGasAvailableReportingPeriods =
        portfolioEntry.availableReportingPeriods[DataTypeEnum.NuclearAndGas] || 'No data available';
  }
}

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
const isDownloading = ref(false);
const downloadErrors = ref('');

const filters = ref({
  companyName: {value: null, matchMode: FilterMatchMode.CONTAINS},
  country: {value: [], matchMode: FilterMatchMode.IN},
  sector: {value: [], matchMode: FilterMatchMode.IN},
  sfdrAvailableReportingPeriods: {value: [], matchMode: FilterMatchMode.IN},
  eutaxonomyFinancialsAvailableReportingPeriods: {value: [], matchMode: FilterMatchMode.IN},
  eutaxonomyNonFinancialsAvailableReportingPeriods: {value: [], matchMode: FilterMatchMode.IN},
  nuclearAndGasAvailableReportingPeriods: {value: [], matchMode: FilterMatchMode.IN},
});

const props = defineProps<{
  portfolioId: string;
}>();

const successDialogMessage = computed(() => {
      if (isBasePortfolioFetching.value) return 'Updating portfolio monitoring...';
      else if (isBasePortfolioError.value) return 'An error occurred while updating portfolio monitoring. Please try again later.'
      else return basePortfolio.value?.isMonitored
            ? 'Portfolio monitoring updated successfully.\nData requests will be created automatically overnight.'
            : 'Portfolio monitoring updated successfully.'
    }
);
const isSuccessDialogVisible = ref(false);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const isUserDatalandMemberOrAdmin = ref(false);

const monitoredTagAttributes = computed(() => ({
  value: basePortfolio.value?.isMonitored ? 'Portfolio actively monitored' : 'Portfolio not actively monitored',
  icon: basePortfolio.value?.isMonitored ? 'pi pi-check-circle' : 'pi pi-times-circle',
  severity: basePortfolio.value?.isMonitored ? 'success' : 'danger',
}));

const {
  data: enrichedPortfolio,
  isPending,
  isError,
} = useQuery({
  queryKey: ['enrichedPortfolio', props.portfolioId],
  staleTime: 3600 * 1000,
  queryFn: () =>
      apiClientProvider.apiClients.portfolioController
          .getEnrichedPortfolio(props.portfolioId)
          .then((response) => response.data),
  placeholderData: keepPreviousData,
});

const {
  data: basePortfolio,
  isError: isBasePortfolioError,
  isFetching: isBasePortfolioFetching,
  isSuccess: isBasePortfolioSuccess,
} = useQuery({
  queryKey: ['basePortfolio', props.portfolioId],
  queryFn: () =>
      apiClientProvider.apiClients.portfolioController
          .getPortfolio(props.portfolioId)
          .then((response) => response.data),
  placeholderData: keepPreviousData,
});

onMounted(() => {
  void checkDatalandMembershipOrAdminRights();
});

const portfolioEntriesToDisplay = computed(() => {
  if (!enrichedPortfolio.value) return [];
  return enrichedPortfolio.value.entries.map((item) => new PortfolioEntryPrepared(item));
});

const reportingPeriodsPerFramework = computed(() => {
  if (!enrichedPortfolio.value) return new Map();
  return groupAllReportingPeriodsByFrameworkForPortfolio(enrichedPortfolio.value);
});

const countryOptions = computed(() => {
  const entries = portfolioEntriesToDisplay.value;
  return Array.from(new Set(entries.map((entry) => entry.country).filter((c): c is string => !!c))).sort();
});

const sectorOptions = computed(() => {
  const entries = portfolioEntriesToDisplay.value;
  return Array.from(new Set(entries.map((entry) => entry.sector).filter((s): s is string => !!s))).sort();
});

const reportingPeriodOptions = computed(() => {
  const options = new Map<string, string[]>();
  const entries = portfolioEntriesToDisplay.value;

  for (const framework of MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER) {
    options.set(
        framework,
        Array.from(
            new Set(entries.map((entry) => getAvailableReportingPeriods(entry, framework)).filter((p): p is string => !!p))
        ).sort()
    );
  }
  return options;
});

/**
 * Checks whether the logged-in User is Dataland member or Admin
 */
async function checkDatalandMembershipOrAdminRights(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();
  const keycloakUserId = keycloak.idTokenParsed?.sub;

  if (keycloakUserId === undefined) {
    isUserDatalandMemberOrAdmin.value = false;
    return;
  }

  const response = await apiClientProvider.apiClients.inheritedRolesController.getInheritedRoles(keycloakUserId);
  const inheritedRolesMap = response.data;

  isUserDatalandMemberOrAdmin.value =
      Object.values(inheritedRolesMap).flat().includes('DatalandMember') ||
      (await checkIfUserHasRole(KEYCLOAK_ROLE_ADMIN, getKeycloakPromise));
}

/**
 * Returns the width (in percent of the total screen width) of a portfolio datatable column
 * associated with a framework.
 * @param framework the hyphenated name of the framework in question
 */
function widthOfFrameworkColumn(framework: string): string {
  switch (framework) {
    case 'sfdr':
      return '10';
    case 'eutaxonomy-financials':
      return '15';
    case 'eutaxonomy-non-financials':
      return '17.5';
    case 'nuclear-and-gas':
      return '17.5';
    default:
      return '15';
  }
}

/**
 * For a given prepared portfolio entry and (hyphenated) framework name, return the associated
 * string of available reporting periods.
 * @param portfolioEntryPrepared
 * @param frameworkName
 */
function getAvailableReportingPeriods(
    portfolioEntryPrepared: PortfolioEntryPrepared,
    frameworkName: string
): string | undefined {
  switch (frameworkName) {
    case DataTypeEnum.Sfdr:
      return portfolioEntryPrepared.sfdrAvailableReportingPeriods;
    case DataTypeEnum.EutaxonomyFinancials:
      return portfolioEntryPrepared.eutaxonomyFinancialsAvailableReportingPeriods;
    case DataTypeEnum.EutaxonomyNonFinancials:
      return portfolioEntryPrepared.eutaxonomyNonFinancialsAvailableReportingPeriods;
    case DataTypeEnum.NuclearAndGas:
      return portfolioEntryPrepared.nuclearAndGasAvailableReportingPeriods;
    default:
      return undefined;
  }
}

/**
 * Resets all filters
 */
function resetFilters(): void {
  let filterName: keyof typeof filters.value;
  for (filterName in filters.value) {
    filters.value[filterName].value = null;
  }
}

/**
 * Retrieve the array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Extracts company IDs from the selected portfolio
 */
function getCompanyIds(): string[] {
  portfolioCompanies.value = getUniqueSortedCompanies(enrichedPortfolio.value?.entries ?? []);
  return portfolioCompanies.value.map((company) => company.companyId);
}

/**
 * Download the dataset from the selected reporting period as a file in the selected format
 * @param selectedYears selected reporting year
 * @param selectedFileType selected export file type
 * @param selectedFramework selected data type
 * @param keepValuesOnly selected export of values only
 * @param includeAlias selected type of field names
 */
async function handleDatasetDownload(
    selectedYears: string[],
    selectedFileType: string,
    selectedFramework: DataTypeEnum,
    keepValuesOnly: boolean,
    includeAlias: boolean
): Promise<void> {
  isDownloading.value = true;
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    // DataExport Button does not exist for private frameworks, so cast is safe
    const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
        selectedFramework,
        apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const exportFileType = Object.values(ExportFileType).find((t) => t.toString() === selectedFileType);
    if (!exportFileType) throw new Error('ExportFileType undefined.');

    const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
    const options: AxiosRequestConfig | undefined =
        fileExtension === 'xlsx' ? {responseType: 'arraybuffer'} : undefined;

    const label = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.find((f) => f === humanizeStringOrNumber(selectedFramework));
    const filename = `data-export-${label ?? humanizeStringOrNumber(selectedFramework)}-${getDateStringForDataExport(new Date())}.${fileExtension}`;

    const response = await frameworkDataApi.exportCompanyAssociatedDataByDimensions(
        selectedYears,
        getCompanyIds(),
        exportFileType,
        keepValuesOnly,
        includeAlias,
        options
    );

    const content = exportFileType === 'JSON' ? JSON.stringify(response.data) : response.data;
    forceFileDownload(content, filename);
  } catch (err) {
    console.error(err);
    downloadErrors.value = `${(err as AxiosError).message}`;
  } finally {
    isDownloading.value = false;
  }
}

/**
 * Opens the PortfolioDialog with the current portfolio's data for editing.
 * Once the dialog is closed, it reloads the portfolio data and emits an update event
 * to refresh the portfolio overview.
 */
function openEditModal(): void {
  dialog.open(PortfolioDialog, {
    props: {
      header: 'Edit Portfolio',
      modal: true,
    },
    data: {
      portfolio: enrichedPortfolio.value,
    }
  });
}

/**
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function openDownloadModal(): void {
  const fullName = 'Download ' + enrichedPortfolio.value?.portfolioName;

  dialog.open(DownloadData, {
    props: {
      modal: true,
      header: fullName,
      pt: {
        title: {
          style: {
            maxWidth: '15em',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      reportingPeriodsPerFramework: reportingPeriodsPerFramework.value,
      isDownloading: isDownloading,
      downloadErrors: downloadErrors,
    },
    emits: {
      onDownloadDataset: handleDatasetDownload,
    }
  });
}

/**
 * Opens the PortfolioMonitoring with the current portfolio's data.
 * Once the dialog is closed, it reloads the portfolio data and shows the portfolio overview again.
 */
function openMonitoringModal(): void {
  const fullName = 'Monitoring of ' + enrichedPortfolio.value?.portfolioName;
  dialog.open(PortfolioMonitoring, {
    props: {
      modal: true,
      header: fullName,
      pt: {
        title: {
          style: {
            maxWidth: '18rem',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          },
        },
      },
    },
    data: {
      portfolio: basePortfolio.value,
    },
    onClose(options) {
      if (options?.data?.monitoringSaved) {
        isSuccessDialogVisible.value = true;
      }
    },
  });
}
</script>

<style scoped>
label {
  margin-left: 0.5em;
}

.filter-checkbox {
  margin: 0.25em 0;
}

.button_bar {
  display: flex;
  margin: var(--spacing-md) 0;
  padding: var(--spacing-md);
  gap: 1rem;
  align-items: center;
  background-color: var(--p-surface-50);
}

.d-center-div {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: white;
}

.reset-button-align-right {
  margin-left: auto;
}
</style>
