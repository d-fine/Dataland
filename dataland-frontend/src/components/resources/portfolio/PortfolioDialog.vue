<template>
  <div class="portfolio-dialog-content">
    <div class="container">
      <p class="header-styling">Portfolio Name</p>
      <InputText v-model="portfolioName" data-test="portfolio-name-input" :placeholder="portfolioName" fluid />
    </div>
    <div class="container">
      <p class="header-styling">Add company identifiers</p>
      <Textarea
        v-model="companyIdentifiersInput"
        data-test="company-identifiers-input"
        :disabled="isCompaniesLoading"
        placeholder="Enter company identifiers, e.g. DE-000402625-0, SWE402626."
        rows="5"
        class="no-resize"
        fluid
        @focus="showIdentifierError = false"
      />
      <div v-if="showIdentifierError">
        <Message severity="error" data-test="invalidIdentifierErrorMessage" variant="simple" size="small">
          Identifiers left in the dialog couldn't be added to the portfolio.
        </Message>
      </div>
      <div class="company-info-container">
        <p class="dataland-info-text small">
          Accepted identifiers: DUNS Number, LEI, ISIN & permID. Expected in comma separated format.
        </p>
        <div class="button-col">
          <PrimeButton
            label="ADD COMPANIES"
            icon="pi pi-plus"
            :loading="isCompaniesLoading"
            @click="addCompanies"
            data-test="portfolio-dialog-add-companies"
            :pt="{ label: { style: { whiteSpace: 'nowrap' } } }"
            fluid
          />
          <PrimeButton
            label="REQUEST SUPPORT"
            icon="pi pi-question"
            @click="openHelpDialog"
            fluid
            :pt="{ label: { style: { whiteSpace: 'nowrap' } } }"
          />
        </div>
      </div>
    </div>
    <div>
      <p class="header-styling">Company identifiers in portfolio</p>
      <ul class="list-none overflow-y-auto" id="existing-company-identifiers" style="margin: 0">
        <li v-for="(company, index) in portfolioCompanies" :key="company.companyId">
          <i class="pi pi-trash" @click="portfolioCompanies.splice(index, 1)" title="Remove company from portfolio" />
          {{ company.companyName }}
        </li>
      </ul>
    </div>
    <Message v-if="!isValidPortfolioUpload" severity="error" variant="simple" size="small">
      Please provide a portfolio name and at least one company.
    </Message>
    <Message v-if="portfolioErrors" severity="error" :life="3000" data-test="unknown-portfolio-error">
      {{ portfolioErrors }}
    </Message>
    <div class="buttonbar">
      <PrimeButton
        v-if="portfolioId"
        label="DELETE PORTFOLIO"
        icon="pi pi-trash"
        @click="deletePortfolio"
        data-test="portfolio-dialog-delete-button"
      />
      <PrimeButton
        label="SAVE PORTFOLIO"
        icon="pi pi-save"
        :disabled="!isValidPortfolioUpload"
        :loading="isPortfolioSaving"
        @click="savePortfolio()"
        data-test="portfolio-dialog-save-button"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type {
  BasePortfolioName,
  EnrichedPortfolio,
  EnrichedPortfolioEntry,
  PortfolioUpload,
} from '@clients/userservice';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import { computed, inject, onMounted, type Ref, ref } from 'vue';
import { useDialog } from 'primevue/usedialog';
import GetHelpDialog from '@/components/resources/portfolio/GetHelpDialog.vue';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';

class CompanyIdAndName {
  companyId: string;
  companyName: string;

  public constructor(portfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = portfolioEntry.companyId;
    this.companyName = portfolioEntry.companyName;
  }
}

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const companyIdentifiersInput = ref('');
const showIdentifierError = ref(false);
const isCompaniesLoading = ref(false);
const isPortfolioSaving = ref(false);
const portfolioErrors = ref('');
const portfolioId = ref<string | undefined>(undefined);
const portfolioName = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const enrichedPortfolio = ref<EnrichedPortfolio>();
const portfolioFrameworks = ref<string[]>([
  'sfdr',
  'eutaxonomy-financials',
  'eutaxonomy-non-financials',
  'nuclear-and-gas',
]);

const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isValidPortfolioUpload = computed(
  () => portfolioName.value && portfolioFrameworks.value?.length > 0 && portfolioCompanies.value?.length > 0
);
const dialog = useDialog();

onMounted(() => {
  const data = dialogRef?.value.data;
  if (!data || !data.portfolio) return;
  const portfolio = data.portfolio as EnrichedPortfolio;
  portfolioId.value = portfolio.portfolioId;
  portfolioName.value = portfolio.portfolioName;
  enrichedPortfolio.value = portfolio;
  portfolioCompanies.value = getUniqueSortedCompanies(portfolio.entries.map((entry) => new CompanyIdAndName(entry)));
});

/**
 * Retrieve array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}

/**
 * Add identifiers from companyIdentifierInput to list. Invalid Identifiers remain in the input text field.
 */
async function addCompanies(): Promise<void> {
  const newIdentifiers = processCompanyInputString();
  let invalidIdentifiers: string[] = [];

  try {
    isCompaniesLoading.value = true;
    const companyValidationResults = (
      await apiClientProvider.backendClients.companyDataController.postCompanyValidation(newIdentifiers)
    ).data;

    const validIdentifiers: CompanyIdAndName[] = companyValidationResults
      .filter((validationResult) => validationResult.companyInformation)
      .map((validEntry): CompanyIdAndName => {
        return {
          companyId: validEntry.companyInformation!.companyId,
          companyName: validEntry.companyInformation!.companyName,
        };
      });
    invalidIdentifiers = companyValidationResults
      .filter((validationResult) => !validationResult.companyInformation)
      .map((it) => it.identifier);

    if (invalidIdentifiers.length > 0) {
      showIdentifierError.value = true;
    }

    companyIdentifiersInput.value = invalidIdentifiers.join(', ') || '';
    portfolioCompanies.value = getUniqueSortedCompanies([...portfolioCompanies.value, ...validIdentifiers]);
  } catch (error) {
    portfolioErrors.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
    console.log(error);
  } finally {
    isCompaniesLoading.value = false;
  }
}

/**
 * Function to open the help dialog
 */
function openHelpDialog(): void {
  dialog.open(GetHelpDialog, {
    props: {
      header: 'Request of Support',
      modal: true,
      style: { width: '22rem' },
    },
  });
}

/**
 * Function used to create or update portfolio.
 * If portfolioId is undefined, the modal has been loaded without data passed to it, i.e. we want to add a new
 * portfolio. Otherwise, we call the PUT endpoint.
 * If creating/updating portfolio is successful, the name and id of the created/updated portfolio is passed to the
 * dialog's close function. This way, the parent component (portfolioOverview) can use this information to switch to the
 * according tab.
 */
async function savePortfolio(): Promise<void> {
  if (!isValidPortfolioUpload.value) return;

  isPortfolioSaving.value = true;
  try {
    const portfolioUpload: PortfolioUpload = {
      portfolioName: portfolioName.value!,
      // as unknown as Set<string> cast required to ensure proper json is created
      companyIds: portfolioCompanies.value.map((company) => company.companyId) as unknown as Set<string>,
      isMonitored: enrichedPortfolio.value?.isMonitored ?? false,
      startingMonitoringPeriod: enrichedPortfolio.value?.startingMonitoringPeriod,
      // as unknown as Set<string> cast required to ensure proper json is created
      monitoredFrameworks: Array.from(enrichedPortfolio.value?.monitoredFrameworks ?? []) as unknown as Set<string>,
    };
    const response = await (portfolioId.value
      ? apiClientProvider.apiClients.portfolioController.replacePortfolio(portfolioId.value, portfolioUpload)
      : apiClientProvider.apiClients.portfolioController.createPortfolio(portfolioUpload));

    dialogRef?.value.close({
      portfolioId: response.data.portfolioId,
      portfolioName: response.data.portfolioName,
    } as BasePortfolioName);
  } catch (error) {
    if (error instanceof AxiosError) {
      portfolioErrors.value =
        error.status == 409
          ? 'A portfolio with same name exists already. Please choose a different portfolio name.'
          : error.message;
    } else {
      portfolioErrors.value = 'An unknown error occurred.';
      console.log(error);
    }
  } finally {
    isPortfolioSaving.value = false;
  }
}

/**
 * Deletes the current portfolio if a valid portfolioId is present and demands a confirmation.
 * Logs a warning if portfolioId is missing.
 */
async function deletePortfolio(): Promise<void> {
  if (!portfolioId.value) {
    return;
  }

  const confirmed = confirm('Are you sure you want to delete this portfolio? This cannot be undone.');
  if (!confirmed) {
    return;
  }

  try {
    await apiClientProvider.apiClients.portfolioController.deletePortfolio(portfolioId.value);
    dialogRef?.value.close({
      isDeleted: true,
    });
  } catch (error) {
    portfolioErrors.value = error instanceof AxiosError ? error.message : 'Portfolio could not be deleted';
  }
}

/**
 * Parses the user input of company identifiers by replacing whitespace and separators
 * with commas, then splits and filters the input into a unique list of non-empty identifiers.
 */
function processCompanyInputString(): string[] {
  const uniqueIdentifiers = new Set<string>([
    ...companyIdentifiersInput.value.replace(/(\r\n|\n|\r|;| )/gm, ',').split(','),
  ]);
  uniqueIdentifiers.delete('');
  return Array.from(uniqueIdentifiers);
}
</script>

<style scoped>
.company-info-container {
  display: flex;
  gap: var(--spacing-lg);
  align-items: center;
}

.no-resize {
  resize: none;
}

.container {
  margin-bottom: var(--spacing-lg);
}

.header-styling {
  font-weight: var(--font-weight-bold);
}

.portfolio-dialog-content {
  width: 28em;
  border-radius: var(--spacing-xxs);
  background-color: white;
  padding: var(--spacing-lg);
}

.buttonbar {
  display: flex;
  gap: var(--spacing-md);
  margin-top: 1em;
  margin-left: auto;
  justify-content: end;
}

.button-col {
  margin-top: var(--spacing-lg);
  margin-left: auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  width: 100%;
}

ul {
  padding-inline-start: 0;
  height: 7.5em;
  overflow-x: hidden;
  overflow-y: auto;
}

i.pi-trash {
  color: var(--primary-color);
  margin-right: 0.25em;
  cursor: pointer;
}
</style>
