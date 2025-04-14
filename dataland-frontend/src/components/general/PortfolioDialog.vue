<template>
  <!--This component uses a grid layout -->
  <div class="bg-white radius-1 p-4 portfolio-dialog-content">
    <div class="grid">
      <div class="col-12" id="portfolio-name-row">
        <FormKit
          v-model="portfolioName"
          type="text"
          label="Portfolio Name"
          name="portfolioName"
          :placeholder="portfolioName"
        />
      </div>
      <div class="col-6">
        <label class="formkit-label" for="company-identifiers">Add Company Identifier</label>
        <FormKit
          v-model="companyIdentifiersInput"
          type="textarea"
          name="company-identifiers"
          placeholder="Enter company identifier, e.g. DE-000402625-0, SWE402626."
          :disabled="isCompaniesLoading"
        />
        <div class="grid">
          <div class="col-6 m-0">
            <p class="gray-text font-italic text-xs m-0">
              Accepted identifiers: DUNS Number, LEI, ISIN & permID. Expected in comma separated format.
            </p>
          </div>
          <div class="col-6 m-0">
            <PrimeButton
              label="Add Companies"
              icon="pi pi-plus"
              :loading="isCompaniesLoading"
              @click="addCompanies"
              class="primary-button"
            />
          </div>
        </div>
      </div>
      <div class="col-6">
        <label class="formkit-label" for="frameworks">Add Frameworks</label>
        <div class="formkit-inner">
          <MultiSelect
            v-model="portfolioFrameworks"
            name="frameworks-selector"
            data-test="selectFramework"
            placeholder="Select frameworks"
            selected-items-label="Select frameworks"
            :options="selectableFrameworks"
            option-label="label"
            option-value="value"
            :max-selected-labels="0"
            :show-toggle-all="false"
            variant="outlined"
            class="w-full"
          />
        </div>
      </div>
    </div>
    <div class="grid">
      <div class="col-6">
        <label class="formkit-label" for="existing-company-identifiers">Company Identifiers in Portfolio</label>
        <ul class="list-none overflow-y-auto h-6rem" id="existing-company-identifiers">
          <li
            v-for="(company, index) in portfolioCompanies"
            :key="company.companyId"
            @click="portfolioCompanies.splice(index, 1)"
          >
            <i class="pi pi-trash" /> {{ company.companyName }}
          </li>
        </ul>
      </div>
      <div class="col-6">
        <label class="formkit-label" for="existing-frameworks">Frameworks in Portfolio</label>
        <ul class="list-none overflow-y-auto h-6rem">
          <li
            v-for="(framework, index) in portfolioFrameworks"
            :key="framework"
            @click="portfolioFrameworks.splice(index, 1)"
          >
            <i class="pi pi-trash mr-2" /> {{ humanizeStringOrNumber(framework) }}
          </li>
        </ul>
      </div>
      <div class="col-12 pb-0 h-3rem">
        <div class="grid">
          <div class="col-7 vertical-middle">
            <p v-if="!isValidPortfolioUpload" class="formkit-message">
              Please provide a portfolio name, at least one company, and at least one framework.
            </p>
            <Message v-if="portfolioErrors" severity="error" class="m-0" :life="3000">
              {{ portfolioErrors }}
            </Message>
          </div>
          <div class="col-5" style="text-align: end">
            <PrimeButton
              label="Cancel"
              icon="pi pi-times"
              @click="dialogRef?.close"
              class="primary-button mr-2"
              severity="secondary"
            />
            <PrimeButton
              label="Save Portfolio"
              icon="pi pi-save"
              :disabled="!isValidPortfolioUpload"
              :loading="isPortfolioSaving"
              @click="savePortfolio()"
              class="primary-button"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ApiClientProvider } from '@/services/ApiClients.ts';
import { FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants.ts';
import type { DropdownOption } from '@/utils/PremadeDropdownDatasets.ts';
import { humanizeStringOrNumber } from '@/utils/StringFormatter.ts';
import { assertDefined } from '@/utils/TypeScriptUtils.ts';
import type {
  BasePortfolioName,
  EnrichedPortfolio,
  EnrichedPortfolioEntry,
  PortfolioUpload,
  PortfolioUploadFrameworksEnum,
} from '@clients/userservice';
import { AxiosError } from 'axios';
import type Keycloak from 'keycloak-js';
import PrimeButton from 'primevue/button';
import type { DynamicDialogInstance } from 'primevue/dynamicdialogoptions';
import Message from 'primevue/message';
import MultiSelect from 'primevue/multiselect';
import { computed, inject, onMounted, type Ref, ref } from 'vue';

type CompanyIdAndName = {
  companyId: string;
  companyName: string;
};

const dialogRef = inject<Ref<DynamicDialogInstance>>('dialogRef');
const getKeycloakPromise = inject<() => Promise<Keycloak>>('getKeycloakPromise');

const companyIdentifiersInput = ref('');
const isCompaniesLoading = ref(false);
const isPortfolioSaving = ref(false);
const portfolioErrors = ref('');
const portfolioId = ref<string | undefined>(undefined);
const portfolioName = ref<string | undefined>(undefined);
const portfolioCompanies = ref<CompanyIdAndName[]>([]);
const portfolioFrameworks = ref<string[]>([]);

const allFrameworks: DropdownOption[] = FRAMEWORKS_WITH_VIEW_PAGE.map((framework) => {
  return {
    value: framework,
    label: humanizeStringOrNumber(framework),
  };
});
const apiClientProvider = new ApiClientProvider(assertDefined(getKeycloakPromise)());

const selectableFrameworks = computed(() =>
  allFrameworks.filter((frameworkOption) => {
    return !portfolioFrameworks.value.includes(frameworkOption.value);
  })
);

const isValidPortfolioUpload = computed(
  () => portfolioName.value && portfolioFrameworks.value?.length > 0 && portfolioCompanies.value?.length > 0
);

onMounted(() => {
  const data = dialogRef?.value.data;
  if (!data || !data.portfolio) return;
  const portfolio = data.portfolio as EnrichedPortfolio;
  portfolioId.value = portfolio.portfolioId;
  portfolioName.value = portfolio.portfolioName;
  portfolioCompanies.value = getUniqueCompanies(portfolio.entries);
  portfolioFrameworks.value = getUniqueFrameworks(portfolio.entries);
});

/**
 * Retrieve array of frameworks from EnrichedPortfolioEntries
 */
function getUniqueFrameworks(entries: EnrichedPortfolioEntry[]): string[] {
  const frameworks = entries.map((entry) => entry.framework).filter(isFramework);
  return Array.from(new Set(frameworks));
}

/**
 * Retrieve array of unique companyIdAndNames from EnrichedPortfolioEntry
 */
function getUniqueCompanies(entries: EnrichedPortfolioEntry[]): CompanyIdAndName[] {
  const uniqueCompanyIds = new Set([...entries.map((entry) => entry.companyId)]);
  return Array.from(uniqueCompanyIds).map((companyId): CompanyIdAndName => {
    return {
      companyId: companyId,
      companyName: entries.find((entry) => entry.companyId == companyId)?.companyName || 'undefined',
    };
  });
}

/**
 * Add identifiers from companyIdentifierInput to list. Invalid Identifiers remain in the input textfield.
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

    const allIdentifiers = new Set([...portfolioCompanies.value, ...validIdentifiers]);
    portfolioCompanies.value = Array.from(allIdentifiers).sort((a, b) => a.companyName.localeCompare(b.companyName));
  } catch (exception) {
    console.log(exception);
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
      frameworks: portfolioFrameworks.value as unknown as Set<PortfolioUploadFrameworksEnum>,
      companyIds: portfolioCompanies.value.map((company) => company.companyId) as unknown as Set<string>,
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
 * Type Guard to convince typescript that undefined is really filtered out
 * @param framework
 */
function isFramework(framework: string | undefined): framework is string {
  return !!framework;
}

/**
 * Processes the companyIdentifierInput, replaces whitespaces by commas, adds the individual companyIds to
 * portfolioCompanyIds, and afterward updates the input
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
  display: flex;
  flex-direction: column;
  width: 50vw;
}

.p-message :deep(.p-message-wrapper) {
  padding: 0.5rem !important;
}

.p-multiselect {
  width: 100%;
  padding: var(--fk-padding-input);
}

.formkit-outer,
.formkit-inner {
  width: 100%;
}

ul {
  padding-inline-start: 0;
}

li:has(> i.pi-trash):hover {
  cursor: pointer;
}

i.pi-trash {
  color: var(--primary-color);
}
</style>
