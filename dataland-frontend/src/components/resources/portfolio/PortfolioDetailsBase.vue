<template>
  <div v-bind="$attrs">
    <div v-if="isLoading" class="d-center-div text-center px-7 py-4">
      <h1>Loading portfolio data...</h1>
      <DatalandProgressSpinner />
    </div>
    <div v-else-if="isError" class="d-center-div text-center px-7 py-4">
      <h1>Error loading portfolio data</h1>
      An unexpected error occurred. Please try again later or with [Ctrl] + [F5] or contact the support team if the
      issue persists.
    </div>
    <div v-else>
      <div class="button_bar">
        <slot
          name="actions"
          :enriched-portfolio="enrichedPortfolio"
          :monitored-tag-attributes="monitoredTagAttributes"
          :reset-filters="resetFilters"
          :open-download="openDownloadModal"
          :reload="loadPortfolio"
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
        :paginator="portfolioEntriesToDisplay.length > MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE"
        :rows="MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE"
      >
        <template #empty>
          <slot name="empty">
            {{ resolvedEmptyText }}
          </slot>
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
              variant="link"
              data-test="view-company-button"
              @click="router.push(`/companies/${portfolioEntry.data.companyId}`)"
              :pt="{
                label: {
                  style: 'font-weight: normal; text-align: left;',
                },
                root: {
                  style: 'padding-left: 0;',
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
          v-for="framework in PORTFOLIO_OVERVIEW_FRAMEWORKS"
          :key="framework"
          :style="'width: ' + widthOfFrameworkColumn(framework) + '%'"
          :sortable="true"
          :field="getFrameworkFieldKey(framework) + 'AvailableReportingPeriods'"
          :header="humanizeStringOrNumber(framework)"
          :showFilterMatchModes="false"
        >
          <template #body="portfolioEntry">
            <PortfolioReportingPeriodsCell
              :periods="getReportingPeriodEntries(portfolioEntry.data, framework)"
              :clickable="hasClickableLink(portfolioEntry.data, framework)"
              @navigate="router.push(portfolioEntry.data.frameworkHyphenatedNamesToDataRef.get(framework)!)"
            />
          </template>
          <template #filter="{ filterModel, filterCallback }">
            <div :data-test="getFrameworkFieldKey(framework) + 'AvailableReportingPeriodsFilterOverlay'">
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
                  :data-test="getFrameworkFieldKey(framework) + 'AvailableReportingPeriodsFilterValue'"
                  @change="filterCallback"
                />
                <label :for="availableReportingPeriods">{{ availableReportingPeriods }}</label>
              </div>
            </div>
          </template>
        </Column>
      </DataTable>
    </div>
    <slot name="dialogs" :is-monitored="isMonitored" :reload="loadPortfolio" />
  </div>
</template>

<script setup lang="ts">
import DatalandProgressSpinner from '@/components/general/DatalandProgressSpinner.vue';
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { PORTFOLIO_OVERVIEW_FRAMEWORKS, MAX_NUMBER_OF_PORTFOLIO_ENTRIES_PER_PAGE } from '@/utils/Constants.ts';
import { getCountryNameFromCountryCode } from '@/utils/CountryCodeConverter.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
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
import { useDialog } from 'primevue/usedialog';
import { computed, inject, onMounted, ref, watch } from 'vue';
import DownloadData from '@/components/general/DownloadData.vue';
import type { PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi.ts';
import type { FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { getFrameworkDataApiForIdentifier } from '@/frameworks/FrameworkApiUtils.ts';
import { ExportFileTypeInformation } from '@/types/ExportFileTypeInformation.ts';
import type { AxiosError, AxiosRequestConfig } from 'axios';
import { forceFileDownload, groupAllReportingPeriodsByFrameworkForPortfolio } from '@/utils/FileDownloadUtils.ts';
import router from '@/router';
import { pollExportJobStatus, prepareDownloadFile } from '@/utils/ExportUtils.ts';
import PortfolioReportingPeriodsCell, {
  type ReportingPeriodEntry,
} from '@/components/resources/portfolio/PortfolioReportingPeriodsCell.vue';
import type { BasicDataDimensions } from '@clients/backend';
import { useSearchNonSourceableDimensionsGroupedByCompanyAndFrameworkQuery } from '@/api-queries/backend/non-sourceability/useSearchNonSourceabilityDimensionsGroupedByCompanyAndFrameworkQuery.ts';

/**
 * Merges the (comma-separated) string of real, available reporting periods for a framework with the real
 * non-sourceable reporting periods for that same framework (from the backend), and returns a single,
 * ascendingly sorted list where each entry is marked whether it is non-sourceable.
 * @param availableReportingPeriodsCsv the comma-separated string of real, available reporting periods
 * @param nonSourceableDimensionsForFramework the set of non-sourceable data dimensions for this company/framework
 */
function mergeReportingPeriods(
  availableReportingPeriodsCsv: string | undefined,
  nonSourceableDimensionsForFramework: Set<BasicDataDimensions> | undefined
): ReportingPeriodEntry[] {
  const realYears = availableReportingPeriodsCsv ? availableReportingPeriodsCsv.split(', ') : [];
  const nonSourceableYears = Array.from(nonSourceableDimensionsForFramework ?? []).map((dim) => dim.reportingPeriod);
  const allYears = new Set([...realYears, ...nonSourceableYears]);
  return Array.from(allYears)
    .sort()
    .map((year) => ({ year, nonSourceable: !realYears.includes(year) }));
}

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
  readonly eutaxonomyNonFinancials202673AvailableReportingPeriods: string | undefined;
  readonly nuclearAndGasAvailableReportingPeriods: string | undefined;

  constructor(portfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = portfolioEntry.companyId;
    this.companyName = portfolioEntry.companyName;
    this.sector = portfolioEntry.sector;
    this.country = getCountryNameFromCountryCode(portfolioEntry.countryCode) ?? 'unknown';
    this.companyCockpitRef = portfolioEntry.companyCockpitRef;
    this.frameworkHyphenatedNamesToDataRef = new Map<string, string | undefined>();

    for (const framework of PORTFOLIO_OVERVIEW_FRAMEWORKS) {
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
    this.eutaxonomyNonFinancials202673AvailableReportingPeriods =
      portfolioEntry.availableReportingPeriods[DataTypeEnum.EutaxonomyNonFinancials202673] || 'No data available';
    this.nuclearAndGasAvailableReportingPeriods =
      portfolioEntry.availableReportingPeriods[DataTypeEnum.NuclearAndGas] || 'No data available';
  }
}

const props = withDefaults(
  defineProps<{
    portfolioId: string;
    emptyText?: string;
  }>(),
  {
    emptyText:
      'Currently there are no companies in your portfolio or no companies match your filters. Edit the portfolio to add companies or remove filter criteria.',
  }
);

const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');
const dialog = useDialog();
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const countryOptions = ref<string[]>([]);
const sectorOptions = ref<string[]>([]);
const reportingPeriodOptions = ref<Map<string, string[]>>(new Map<string, string[]>());
const isDownloading = ref(false);
const downloadErrors = ref('');
let reportingPeriodsPerFramework: Map<string, string[]>;

const filters = ref({
  companyName: { value: null, matchMode: FilterMatchMode.CONTAINS },
  country: { value: [] as string[], matchMode: FilterMatchMode.IN },
  sector: { value: [] as string[], matchMode: FilterMatchMode.IN },
  sfdrAvailableReportingPeriods: { value: [] as string[], matchMode: FilterMatchMode.IN },
  eutaxonomyFinancialsAvailableReportingPeriods: { value: [] as string[], matchMode: FilterMatchMode.IN },
  eutaxonomyNonFinancialsAvailableReportingPeriods: { value: [] as string[], matchMode: FilterMatchMode.IN },
  eutaxonomyNonFinancials202673AvailableReportingPeriods: { value: [] as string[], matchMode: FilterMatchMode.IN },
  nuclearAndGasAvailableReportingPeriods: { value: [] as string[], matchMode: FilterMatchMode.IN },
});

const enrichedPortfolio = ref<EnrichedPortfolio>();
const portfolioEntriesToDisplay = ref<PortfolioEntryPrepared[]>([]);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const isLoading = ref(true);
const isError = ref(false);
const isMonitored = ref<boolean>(false);
const portfolioCompanyIds = computed(() => enrichedPortfolio.value?.entries.map((entry) => entry.companyId) ?? []);
const nonSourceabilityRequest = computed(() => ({ companyIds: portfolioCompanyIds.value }));
const { data: nonSourceableDimensionsGrouped } = useSearchNonSourceableDimensionsGroupedByCompanyAndFrameworkQuery(
  nonSourceabilityRequest,
  { enabled: computed(() => portfolioCompanyIds.value.length > 0) }
);
const reportingPeriodEntriesByCompanyAndFramework = computed<
  Record<string, Partial<Record<string, ReportingPeriodEntry[]>>>
>(() => {
  const result: Record<string, Partial<Record<string, ReportingPeriodEntry[]>>> = {};
  for (const entry of enrichedPortfolio.value?.entries ?? []) {
    const perFramework: Partial<Record<string, ReportingPeriodEntry[]>> = {};
    for (const framework of PORTFOLIO_OVERVIEW_FRAMEWORKS) {
      const nonSourceableForFramework = nonSourceableDimensionsGrouped.value?.[entry.companyId]?.[framework];
      perFramework[framework] = mergeReportingPeriods(
        entry.availableReportingPeriods[framework],
        nonSourceableForFramework
      );
    }
    result[entry.companyId] = perFramework;
  }
  return result;
});
const monitoredTagAttributes = computed(() => ({
  value: isMonitored.value ? 'Portfolio actively monitored' : 'Portfolio not actively monitored',
  icon: isMonitored.value ? 'pi pi-check-circle' : 'pi pi-times-circle',
  severity: isMonitored.value ? 'success' : 'danger',
}));

const resolvedEmptyText = computed(() => props.emptyText);

onMounted(() => {
  loadPortfolio();
});

watch(enrichedPortfolio, () => {
  const entries = portfolioEntriesToDisplay.value || [];

  countryOptions.value = Array.from(
    new Set(entries.map((entry) => entry.country).filter((country): country is string => typeof country === 'string'))
  ).sort();

  sectorOptions.value = Array.from(
    new Set(entries.map((entry) => entry.sector).filter((sector): sector is string => typeof sector === 'string'))
  ).sort();

  for (const framework of PORTFOLIO_OVERVIEW_FRAMEWORKS) {
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
  }
});

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
 * Builds a stable data-table field key from a framework identifier.
 * We remove all hyphens (including before digits) so PrimeVue can resolve object fields reliably.
 */
function getFrameworkFieldKey(framework: string): string {
  return framework.replaceAll(/-([a-z0-9])/gi, (_, char: string) => char.toUpperCase());
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
    case DataTypeEnum.EutaxonomyNonFinancials202673:
      return portfolioEntryPrepared.eutaxonomyNonFinancials202673AvailableReportingPeriods;
    case DataTypeEnum.NuclearAndGas:
      return portfolioEntryPrepared.nuclearAndGasAvailableReportingPeriods;
    default:
      return undefined;
  }
}

/**
 * For a given prepared portfolio entry and (hyphenated) framework name, return the merged list of
 * available and non-sourceable reporting periods, looked up from the pre-computed, cached map.
 * @param portfolioEntryPrepared
 * @param frameworkName
 */
function getReportingPeriodEntries(
  portfolioEntryPrepared: PortfolioEntryPrepared,
  frameworkName: string
): ReportingPeriodEntry[] {
  return reportingPeriodEntriesByCompanyAndFramework.value[portfolioEntryPrepared.companyId]?.[frameworkName] ?? [];
}

/**
 * Determines whether the reporting-periods cell for the given framework should be rendered as a clickable
 * link, i.e. whether at least one real (non-strikethrough) reporting period exists.
 * @param portfolioEntryPrepared
 * @param frameworkName
 */
function hasClickableLink(portfolioEntryPrepared: PortfolioEntryPrepared, frameworkName: string): boolean {
  return Boolean(portfolioEntryPrepared.frameworkHyphenatedNamesToDataRef.get(frameworkName));
}

/**
 * (Re-)loads a portfolio
 */
function loadPortfolio(): void {
  isLoading.value = true;
  isError.value = false;
  apiClientProvider.apiClients.portfolioController
    .getEnrichedPortfolio(props.portfolioId)
    .then((response) => {
      enrichedPortfolio.value = response.data;
      portfolioEntriesToDisplay.value = enrichedPortfolio.value.entries.map((item) => new PortfolioEntryPrepared(item));
      reportingPeriodsPerFramework = groupAllReportingPeriodsByFrameworkForPortfolio(enrichedPortfolio.value);
      isMonitored.value = enrichedPortfolio.value?.isMonitored ?? false;
    })
    .catch((error) => {
      console.error(error);
      isError.value = true;
    })
    .finally(() => {
      isLoading.value = false;
    });
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
 * @param latestOnly whether to export only the latest reporting period per company
 */
async function handleDatasetDownload(
  selectedYears: string[],
  selectedFileType: string,
  selectedFramework: DataTypeEnum,
  keepValuesOnly: boolean,
  includeAlias: boolean,
  latestOnly: boolean
): Promise<void> {
  isDownloading.value = true;
  downloadErrors.value = '';
  try {
    const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());
    const frameworkDataApi: PublicFrameworkDataApi<FrameworkData> | null = getFrameworkDataApiForIdentifier(
      selectedFramework,
      apiClientProvider
    ) as PublicFrameworkDataApi<FrameworkData>;

    const exportFileType = Object.values(ExportFileType).find((t) => t.toString() === selectedFileType);
    if (!exportFileType) throw new Error('ExportFileType undefined.');

    const companyIds = getCompanyIds();

    let exportJobId: string;
    if (latestOnly) {
      exportJobId = (
        await frameworkDataApi.postExportLatestJobCompanyAssociatedDataByDimensions(
          companyIds,
          exportFileType,
          keepValuesOnly,
          includeAlias
        )
      ).data.id;
    } else {
      exportJobId = (
        await frameworkDataApi.postExportJobCompanyAssociatedDataByDimensions(
          selectedYears,
          companyIds,
          exportFileType,
          keepValuesOnly,
          includeAlias
        )
      ).data.id;
    }

    await pollExportJobStatus(exportJobId, apiClientProvider.apiClients.dataExportController);

    const fileExtension = ExportFileTypeInformation[exportFileType].fileExtension;
    const options: AxiosRequestConfig | undefined =
      fileExtension === 'xlsx' ? { responseType: 'arraybuffer' } : undefined;

    const response = await apiClientProvider.apiClients.dataExportController.exportCompanyAssociatedDataById(
      exportJobId,
      options
    );

    const { filename, content } = prepareDownloadFile(exportFileType, selectedFramework, response.data);

    forceFileDownload(content, filename);
  } catch (err) {
    console.error(err);
    downloadErrors.value = `${(err as AxiosError).message}`;
  } finally {
    isDownloading.value = false;
  }
}

/**
 * Opens the PortfolioDownload with the current portfolio's data for downloading.
 */
function openDownloadModal(): void {
  downloadErrors.value = '';
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
      reportingPeriodsPerFramework,
      isDownloading,
      downloadErrors,
    },
    emits: {
      onDownloadDataset: handleDatasetDownload,
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
</style>
