<template>
  <div v-if="isLoading" class="d-center-div text-center px-7 py-4">
    <h1>Loading portfolio data...</h1>
    <DatalandProgressSpinner />
  </div>
  <div v-else-if="isError" class="d-center-div text-center px-7 py-4">
    <h1>Error loading portfolio data</h1>
    An unexpected error occurred. Please try again later or with [Ctrl] + [F5] or contact the support team if the issue
    persists.
  </div>
  <div v-else>
    <div class="button_bar">
      <Button @click="openEditModal()" data-test="edit-portfolio" label="EDIT PORTFOLIO" icon="pi pi-pencil" />
      <Button
        @click="openDownloadModal()"
        data-test="download-portfolio"
        label="DOWNLOAD PORTFOLIO"
        icon="pi pi-download"
      />
      <div :title="!isPremiumUser ? 'Only premium users can activate monitoring' : ''">
        <Button
          @click="openMonitoringModal()"
          data-test="monitor-portfolio"
          :disabled="!isPremiumUser"
          icon="pi pi-bell"
          label="ACTIVE MONITORING"
        />
      </div>

      <Tag v-bind="monitoredTagAttributes" data-test="is-monitored-tag" />
      <Button
        class="reset-button-align-right"
        data-test="reset-filter"
        @click="resetFilters()"
        variant="text"
        label="RESET"
      />
    </div>

    <DataTable
      stripedRows
      removableSort
      v-model:filters="filters"
      filterDisplay="menu"
      :value="portfolioEntriesToDisplay"
      tableStyle="min-width: 50rem"
      sortField="companyName"
      :sortOrder="1"
    >
      <template #empty>
        Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to add
        companies or remove filter criteria.
      </template>
      <Column
        :sortable="true"
        field="companyName"
        header="Company Name"
        :showFilterMatchModes="false"
        style="width: 15%"
      >
        <template #body="portfolioEntry">
          <Button
            :label="portfolioEntry.data.companyName"
            iconPos="right"
            icon="pi pi-angle-right"
            variant="link"
            data-test="view-company-button"
            @click="router.push(`/companies/${portfolioEntry.data.companyId}`)"
            :pt="{
              label: {
                style: 'font-weight: normal;',
              },
              content: {
                style: 'margin-left: auto; margin-right: auto; align-items: left;',
              },
            }"
          />
        </template>
        <template #filter="{ filterModel, filterCallback }">
          <InputText
            v-model="filterModel.value"
            type="text"
            @input="filterCallback()"
            placeholder="Filter by company name"
            :data-test="'companyNameFilterValue'"
          />
        </template>
      </Column>
      <Column :sortable="true" field="country" header="Country" :showFilterMatchModes="false" style="width: 12.5%">
        <template #filter="{ filterModel, filterCallback }">
          <div data-test="countryFilterOverlay">
            <div v-for="country of countryOptions" :key="country" class="filter-checkbox">
              <Checkbox
                v-model="filterModel.value"
                :inputId="country"
                name="country"
                :value="country"
                data-test="countryFilterValue"
                @change="filterCallback"
              />
              <label :for="country">{{ country }}</label>
            </div>
          </div>
        </template>
      </Column>
      <Column :sortable="true" field="sector" header="Sector" :showFilterMatchModes="false" style="width: 12.5%">
        <template #filter="{ filterModel, filterCallback }">
          <div data-test="sectorFilterOverlay">
            <div v-for="sector of sectorOptions" :key="sector" class="filter-checkbox">
              <Checkbox
                v-model="filterModel.value"
                :inputId="sector"
                name="sector"
                :value="sector"
                data-test="sectorFilterValue"
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
        :style="'width: ' + widthOfFrameworkColumn(framework) + '%'"
        :sortable="true"
        :field="convertKebabCaseToCamelCase(framework) + 'AvailableReportingPeriods'"
        :header="humanizeStringOrNumber(framework)"
        :showFilterMatchModes="false"
      >
        <template #body="portfolioEntry">
          <Button
            v-if="portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework)"
            :label="getAvailableReportingPeriods(portfolioEntry.data, framework)"
            iconPos="right"
            icon="pi pi-angle-right"
            variant="link"
            data-test="view-company-button"
            @click="router.push(portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework))"
            :pt="{
              label: {
                style: 'font-weight: normal;',
              },
              content: {
                style: 'margin-left: auto; margin-right: auto; align-items: left;',
              },
            }"
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
                :inputId="availableReportingPeriods"
                name="availableReportingPeriods"
                :value="availableReportingPeriods"
                :data-test="convertKebabCaseToCamelCase(framework) + 'AvailableReportingPeriodsFilterValue'"
                @change="filterCallback"
              />
              <label :for="availableReportingPeriods">{{ availableReportingPeriods }}</label>
            </div>
          </div>
        </template>
      </Column>
    </DataTable>
  </div>
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import PortfolioDialog from '@/components/resources/portfolio/PortfolioDialog.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER, MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER } from '@/utils/Constants.ts';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter.ts';
import { convertKebabCaseToCamelCase, humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type { EnrichedPortfolio, EnrichedPortfolioEntry } from '@clients/userservice';
import { type CompanyIdAndName, DataTypeEnum, ExportFileType } from '@clients/backend';
import { FilterMatchMode } from '@primevue/core/api';
import type Keycloak from 'keycloak-js';
import Button from 'primevue/button';
import Checkbox from 'primevue/checkbox';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import InputText from 'primevue/inputtext';
import Tag from 'primevue/tag';
import { useDialog } from 'primevue/usedialog';
import { inject, onMounted, ref, watch, computed } from 'vue';
import PortfolioMonitoring from '@/components/resources/portfolio/PortfolioMonitoring.vue';
import DownloadData from '@/components/general/DownloadData.vue';
import type { PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import type { AxiosError, AxiosRequestConfig } from 'axios';
import { getDateStringForDataExport } from '@/utils/DataFormatUtils.ts';
import { forceFileDownload, groupAllReportingPeriodsByFrameworkForPortfolio } from '@/utils/FileDownloadUtils.ts';
import router from '@/router';

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

    MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER.forEach((framework) => {
      this.frameworkHyphenatedNamesToDataRef.set(
        framework,
        portfolioEntry.frameworkHyphenatedNamesToDataRef[framework] ||
          (portfolioEntry.availableReportingPeriods[framework]
            ? `/companies/${portfolioEntry.companyId}/frameworks/${framework}`
            : undefined)
      );
    });

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
const emit = defineEmits(['update:portfolio-overview']);
const countryOptions = ref<string[]>([]);
const sectorOptions = ref<string[]>([]);
const reportingPeriodOptions = ref<Map<string, string[]>>(new Map<string, string[]>());
const isDownloading = ref(false);
const downloadErrors = ref('');
let reportingPeriodsPerFramework: Map<string, string[]>;

const filters = ref({
  companyName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  country: { value: [], matchMode: FilterMatchMode.IN },
  sector: { value: [], matchMode: FilterMatchMode.IN },
  sfdrAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  eutaxonomyFinancialsAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  eutaxonomyNonFinancialsAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
  nuclearAndGasAvailableReportingPeriods: { value: [], matchMode: FilterMatchMode.IN },
});

const props = defineProps<{
  portfolioId: string;
}>();

const enrichedPortfolio = ref<EnrichedPortfolio>();
const portfolioEntriesToDisplay = ref([] as PortfolioEntryPrepared[]);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const isLoading = ref(true);
const isError = ref(false);
const isMonitored = ref<boolean>(false);
const isPremiumUser = ref(false);

const monitoredTagAttributes = computed(() => ({
  value: isMonitored.value ? 'Portfolio actively monitored' : 'Portfolio not actively monitored',
  icon: isMonitored.value ? 'pi pi-check-circle' : 'pi pi-times-circle',
  severity: isMonitored.value ? 'success' : 'danger',
}));

onMounted(() => {
  void checkPremiumRole();
  loadPortfolio();
});

watch([enrichedPortfolio], () => {
  const entries = portfolioEntriesToDisplay.value || [];

  countryOptions.value = Array.from(
    new Set(entries.map((entry) => entry.country).filter((country): country is string => typeof country === 'string'))
  ).sort();

  sectorOptions.value = Array.from(
    new Set(entries.map((entry) => entry.sector).filter((sector): sector is string => typeof sector === 'string'))
  ).sort();

  MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER.forEach((framework) => {
    reportingPeriodOptions.value.set(
      framework,
      Array.from(
        new Set(
          entries
            .map((entry) => getAvailableReportingPeriods(entry, framework))
            .filter((period): period is string => typeof period === 'string')
        )
      ).sort()
    );
  });
});

/**
 * Checks whether the logged in User is premium user
 */
async function checkPremiumRole(): Promise<void> {
  const keycloak = await assertDefined(getKeycloakPromise)();

  isPremiumUser.value = keycloak.realmAccess?.roles.includes('ROLE_PREMIUM_USER') || false;
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
 * (Re-)loads a portfolio
 */
function loadPortfolio(): void {
  isLoading.value = true;
  apiClientProvider.apiClients.portfolioController
    .getEnrichedPortfolio(props.portfolioId)
    .then((response) => {
      enrichedPortfolio.value = response.data;

      portfolioEntriesToDisplay.value = enrichedPortfolio.value.entries.map((item) => new PortfolioEntryPrepared(item));
      reportingPeriodsPerFramework = groupAllReportingPeriodsByFrameworkForPortfolio(enrichedPortfolio.value);
      isMonitored.value = enrichedPortfolio.value?.isMonitored ?? false;
    })
    .catch((reason) => {
      console.error(reason);
      isError.value = true;
    })
    .finally(() => (isLoading.value = false));
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
      fileExtension === 'xlsx' ? { responseType: 'arraybuffer' } : undefined;

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
      isMonitoring: isMonitored.value,
    },
    onClose(options) {
      if (!options?.data?.isDeleted) {
        loadPortfolio();
      }
      emit('update:portfolio-overview');
    },
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
      reportingPeriodsPerFramework: reportingPeriodsPerFramework,
      isDownloading: isDownloading,
      downloadErrors: downloadErrors,
    },
    emits: {
      onDownloadDataset: handleDatasetDownload,
    },
    onClose() {
      loadPortfolio();
      emit('update:portfolio-overview');
    },
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
      portfolio: enrichedPortfolio.value,
    },
    onClose() {
      loadPortfolio();
      emit('update:portfolio-overview');
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
