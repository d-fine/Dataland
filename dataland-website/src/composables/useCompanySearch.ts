import { ref, watch } from 'vue';

const DEBOUNCE_MS = 300;
const MIN_LENGTH_WARNING_MS = 1000;

export interface CompanyIdAndName {
  companyId: string;
  companyName: string;
}

export type FetchCompaniesFn = (searchString: string, resultLimit: number) => Promise<CompanyIdAndName[]>;

/**
 * NOTE: This composable is intentionally website-only.
 * dataland-frontend's CompaniesOnlySearchBar.vue delegates debounce, keyboard
 * handling and suggestions to PrimeVue's AutoComplete component, so there is
 * no shared logic to extract.  This composable exists because the website has
 * no PrimeVue and must implement those behaviours itself.
 *
 * The only injection point that differs between the two is how companies are
 * fetched (fetchCompanies) and what happens on selection (onSelect).
 */
export function useCompanySearch(
  fetchCompanies: FetchCompaniesFn,
  onSelect: (company: CompanyIdAndName) => void,
  resultLimit = 100
) {
  const query = ref('');
  const suggestions = ref<CompanyIdAndName[]>([]);
  const activeIndex = ref(-1);
  const showMinLengthWarning = ref(false);

  let debounceTimer = 0;
  let minLengthWarningTimer = 0;

  watch(query, (value) => {
    clearTimeout(debounceTimer);
    clearTimeout(minLengthWarningTimer);

    const trimmed = value.trim();

    if (trimmed.length === 0) {
      suggestions.value = [];
      showMinLengthWarning.value = false;
      return;
    }

    if (trimmed.length < 3) {
      suggestions.value = [];
      minLengthWarningTimer = window.setTimeout(() => {
        showMinLengthWarning.value = true;
      }, MIN_LENGTH_WARNING_MS);
      return;
    }

    showMinLengthWarning.value = false;

    debounceTimer = window.setTimeout(async () => {
      suggestions.value = await fetchCompanies(trimmed, resultLimit);
      activeIndex.value = -1;
    }, DEBOUNCE_MS);
  });

  function selectCompany(company: CompanyIdAndName) {
    onSelect(company);
    suggestions.value = [];
    activeIndex.value = -1;
  }

  function onKeydown(event: KeyboardEvent) {
    if (suggestions.value.length === 0) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      activeIndex.value = Math.min(activeIndex.value + 1, suggestions.value.length - 1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      activeIndex.value = Math.max(activeIndex.value - 1, -1);
    } else if (event.key === 'Enter' && activeIndex.value >= 0) {
      event.preventDefault();
      selectCompany(suggestions.value[activeIndex.value]);
    } else if (event.key === 'Escape') {
      suggestions.value = [];
      activeIndex.value = -1;
    }
  }

  function onBlur() {
    setTimeout(() => {
      suggestions.value = [];
      activeIndex.value = -1;
    }, 150);
  }

  function highlight(name: string): string {
    const search = query.value.trim();
    if (!search) return name;
    const escaped = search.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    return name.replace(new RegExp(`(${escaped})`, 'gi'), '<mark class="bg-yellow-100 text-inherit rounded-sm">$1</mark>');
  }

  return { query, suggestions, activeIndex, showMinLengthWarning, resultLimit, selectCompany, onKeydown, onBlur, highlight };
}
