<template>
  <div class="portfolio-dialog-content">
    <FormKit
      v-model="portfolioName"
      type="text"
      label="Portfolio Name"
      name="portfolioName"
      :placeholder="portfolioName"
    />
    <label class="formkit-label" for="company-identifiers">Add Company Identifiers</label>
    <FormKit
      v-model="companyIdentifiersInput"
      type="textarea"
      name="company-identifiers"
      placeholder="Enter company identifiers, e.g. DE-000402625-0, SWE402626."
      :disabled="isCompaniesLoading"
    />
    <PrimeButton
      label="Add Companies"
      icon="pi pi-plus"
      :loading="isCompaniesLoading"
      @click="addCompanies"
      class="primary-button"
      data-test="addCompanies"
      style="margin-left: 1em; float: right"
    />
    <p class="gray-text font-italic text-xs m-0">
      Accepted identifiers: DUNS Number, LEI, ISIN & permID. Expected in comma separated format.
    </p>
    <label class="formkit-label" for="existing-company-identifiers">Company Identifiers in Portfolio</label>
    <ul class="list-none overflow-y-auto" id="existing-company-identifiers" style="margin: 0">
      <li v-for="(company, index) in portfolioCompanies" :key="company.companyId">
        <i class="pi pi-trash" @click="portfolioCompanies.splice(index, 1)" title="Remove company from portfolio" />
        {{ company.companyName }}
      </li>
    </ul>
    <div data-test="error">
      <p v-if="!isValidPortfolioUpload" class="formkit-message">
        Please provide a portfolio name and at least one company.
      </p>
      <Message v-if="portfolioErrors" severity="error" class="m-0" :life="3000">
        {{ portfolioErrors }}
      </Message>
    </div>
    <div class="buttonbar">
      <PrimeButton
        v-if="portfolioId"
        label="Delete Portfolio"
        icon="pi pi-trash"
        @click="deletePortfolio"
        class="primary-button deleteButton"
        :data-test="'deleteButton'"
        title="Delete the selected Portfolio"
        style="width: 1em; padding: 1em"
      />
      <PrimeButton
        label="Save Portfolio"
        icon="pi pi-save"
        :disabled="!isValidPortfolioUpload"
        :loading="isPortfolioSaving"
        @click="savePortfolio()"
        class="primary-button"
        :data-test="'saveButton'"
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
import { sendBulkRequestForPortfolio } from '@/utils/RequestUtils.ts';

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
const isCompaniesLoading = ref(false);
const isPortfolioSaving = ref(false);
const portfolioErrors = ref('');
const portfolioId = ref<string | undefined>(undefined);
const portfolioName = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([])
const enrichedPortfolioCompanies = ref<EnrichedPortfolioEntry[]>([]);
const portfolioFrameworks: string[] =[
  'sfdr',
  'eutaxonomy-financials',
  'eutaxonomy-non-financials',
  'nuclear-and-gas',
];

let portfolio: EnrichedPortfolio;
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const isValidPortfolioUpload = computed(
  () => portfolioName.value && portfolioFrameworks.length > 0 && portfolioCompanies.value?.length > 0
);

onMounted(() => {
  const enrichedPortfolio = dialogRef?.value.data;
  if (!enrichedPortfolio || !enrichedPortfolio.portfolio) return;
  portfolio = enrichedPortfolio.portfolio;
  portfolioId.value = portfolio.portfolioId;
  portfolioName.value = portfolio.portfolioName;
  enrichedPortfolioCompanies.value = portfolio.entries;
  portfolioCompanies.value = getUniqueSortedCompanies(
    portfolio.entries.map((entry) => new CompanyIdAndName(entry))
  );
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

    portfolioCompanies.value = getUniqueSortedCompanies([...portfolioCompanies.value, ...validIdentifiers]);
  } catch (error) {
    portfolioErrors.value = error instanceof AxiosError ? error.message : 'An unknown error occurred.';
    console.log(error);
  } finally {
    isCompaniesLoading.value = false;
    companyIdentifiersInput.value = invalidIdentifiers.join(', ') || '';
  }
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
      companyIds: portfolioCompanies.value.map((c) => c.companyId) as unknown as Set<string>,
    };

    const response = portfolioId.value
      ? await apiClientProvider.apiClients.portfolioController.replacePortfolio(portfolioId.value, portfolioUpload)
      : await apiClientProvider.apiClients.portfolioController.createPortfolio(portfolioUpload);

    if (!portfolioId.value) {
      portfolioId.value = response.data.portfolioId;
    }

    dialogRef?.value.close({
      portfolioId: response.data.portfolioId,
      portfolioName: response.data.portfolioName,
    } as BasePortfolioName);
    await Promise.all(
      sendBulkRequestForPortfolio(portfolio, assertDefined(getKeycloakPromise))
    );
  } catch (error) {
    if (error instanceof AxiosError) {
      portfolioErrors.value =
        error.status === 409
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
      deleted: true,
      portfolioId: portfolioId.value,
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

<style scoped lang="scss">
.portfolio-dialog-content {
  width: 28em;
  border-radius: 0.25rem;
  background-color: white;
  padding: 1.5rem;
}

.deleteButton {
  min-width: fit-content;
  padding: 1em;
}

.buttonbar {
  display: flex;
  gap: 1rem;
  margin-top: 1em;
  margin-left: auto;
  justify-content: end;
}

label {
  margin-top: 1.5em;
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
