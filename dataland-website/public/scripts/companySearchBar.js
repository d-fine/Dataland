// companySearchBar.js

const DEBOUNCE_MS = 300;
const MIN_LENGTH_WARNING_MS = 1000;
const RESULT_LIMIT = 100;

export function initCompanySearchBar() {
  const input = document.querySelector('#company-search-input');
  const listbox = document.querySelector('#company-search-listbox');
  const warning = document.querySelector('#company-search-warning');

  if (!input || !listbox || !warning) {
    throw new Error('CompanySearchBar: DOM elements not found');
  }

  const searchInput = input;
  const resultsListbox = listbox;
  const warningEl = warning;

  let suggestions = [];
  let activeIndex = -1;
  let debounceTimer;
  let minLengthWarningTimer;

  function escapeRegex(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }

  function highlight(name, search) {
    if (!search) return name;
    return name.replace(
      new RegExp(`(${escapeRegex(search)})`, 'gi'),
      '<mark class="bg-yellow-100 text-inherit rounded-sm">$1</mark>'
    );
  }

  function renderSuggestions() {
    if (suggestions.length === 0) {
      resultsListbox.classList.add('hidden');
      resultsListbox.innerHTML = '';
      searchInput.setAttribute('aria-expanded', 'false');
      return;
    }

    const search = searchInput.value.trim();
    const items = suggestions.map((company, index) => {
      const li = document.createElement('li');
      li.setAttribute('role', 'option');
      li.setAttribute('aria-selected', String(index === activeIndex));
      li.className = [
        'px-4 py-2.5 cursor-pointer text-sm text-gray-800 flex items-center gap-2',
        index === activeIndex ? 'bg-gray-100' : 'hover:bg-gray-50',
      ].join(' ');
      li.innerHTML = `
        <svg class="shrink-0 text-gray-400" width="14" height="14" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path d="M17.5 17.5L13.875 13.875M15.8333 9.16667C15.8333 12.8486 12.8486 15.8333 9.16667 15.8333C5.48477 15.8333 2.5 12.8486 2.5 9.16667C2.5 5.48477 5.48477 2.5 9.16667 2.5C12.8486 2.5 15.8333 5.48477 15.8333 9.16667Z"
            stroke="currentColor" stroke-width="1.67" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <span>${highlight(company.companyName, search)}</span>
      `;
      li.addEventListener('mousedown', (event) => {
        event.preventDefault();
        selectCompany(company);
      });
      return li;
    });

    if (suggestions.length >= RESULT_LIMIT) {
      const footer = document.createElement('li');
      footer.className = 'px-4 py-2 text-xs text-gray-500 font-medium border-t border-gray-100';
      footer.setAttribute('aria-live', 'polite');
      footer.textContent = `Only showing ${RESULT_LIMIT} results, please refine your query.`;
      items.push(footer);
    }

    resultsListbox.replaceChildren(...items);
    resultsListbox.classList.remove('hidden');
    searchInput.setAttribute('aria-expanded', 'true');
  }

  function selectCompany(company) {
    window.location.href = `/companies/${company.companyId}`;
  }

  async function fetchSuggestions(searchString) {
    const params = new URLSearchParams({ searchString, resultLimit: String(RESULT_LIMIT) });
    const response = await fetch(`${window.location.origin}/api/companies/names?${params.toString()}`);

    if (!response.ok) {
      console.error(`Company search failed: ${response.status} ${response.statusText}`);
      return [];
    }

    return response.json();
  }

  searchInput.addEventListener('input', () => {
    if (debounceTimer !== undefined) window.clearTimeout(debounceTimer);
    if (minLengthWarningTimer !== undefined) window.clearTimeout(minLengthWarningTimer);

    const trimmed = searchInput.value.trim();

    if (trimmed.length === 0) {
      suggestions = [];
      activeIndex = -1;
      warningEl.classList.add('hidden');
      renderSuggestions();
      return;
    }

    if (trimmed.length < 3) {
      suggestions = [];
      activeIndex = -1;
      renderSuggestions();
      minLengthWarningTimer = window.setTimeout(() => {
        warningEl.classList.remove('hidden');
      }, MIN_LENGTH_WARNING_MS);
      return;
    }

    warningEl.classList.add('hidden');
    debounceTimer = window.setTimeout(async () => {
      suggestions = await fetchSuggestions(trimmed);
      activeIndex = -1;
      renderSuggestions();
    }, DEBOUNCE_MS);
  });

  searchInput.addEventListener('keydown', (event) => {
    if (suggestions.length === 0) return;

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      activeIndex = Math.min(activeIndex + 1, suggestions.length - 1);
      renderSuggestions();
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      activeIndex = Math.max(activeIndex - 1, -1);
      renderSuggestions();
    } else if (event.key === 'Enter' && activeIndex >= 0) {
      event.preventDefault();
      selectCompany(suggestions[activeIndex]);
    } else if (event.key === 'Escape') {
      suggestions = [];
      activeIndex = -1;
      renderSuggestions();
    }
  });

  searchInput.addEventListener('blur', () => {
    window.setTimeout(() => {
      suggestions = [];
      activeIndex = -1;
      renderSuggestions();
    }, 150);
  });
}
// Auto‑initialize when loaded in the browser
if (typeof window !== 'undefined') {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', () => {
      initCompanySearchBar();
    });
  } else {
    initCompanySearchBar();
  }
}
