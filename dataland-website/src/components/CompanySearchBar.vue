<template>
  <div class="relative w-full max-w-[520px]">
    <svg
      class="absolute left-4 top-1/2 -translate-y-1/2 text-white/50 pointer-events-none"
      aria-hidden="true"
      width="20"
      height="20"
      viewBox="0 0 20 20"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M17.5 17.5L13.875 13.875M15.8333 9.16667C15.8333 12.8486 12.8486 15.8333 9.16667 15.8333C5.48477 15.8333 2.5 12.8486 2.5 9.16667C2.5 5.48477 5.48477 2.5 9.16667 2.5C12.8486 2.5 15.8333 5.48477 15.8333 9.16667Z"
        stroke="currentColor"
        stroke-width="1.67"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
    <input
      v-model="query"
      type="text"
      placeholder="Search by company name or LEI..."
      class="w-full py-3.5 pr-4 pl-11 border-[1.5px] border-white/20 rounded-lg text-base font-[var(--font-family)] text-[var(--color-text)] bg-white box-border placeholder:text-[var(--color-text-muted)]"
      aria-label="Search by company name or LEI"
      aria-autocomplete="list"
      :aria-expanded="suggestions.length > 0"
      aria-controls="company-search-listbox"
      autocomplete="off"
      @keydown="onKeydown"
      @blur="onBlur"
    />
    <ul
      v-if="suggestions.length > 0"
      id="company-search-listbox"
      role="listbox"
      class="absolute z-50 mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg overflow-y-auto max-h-[264px]"
    >
      <li
        v-for="(company, index) in suggestions"
        :key="company.companyId"
        role="option"
        :aria-selected="index === activeIndex"
        :class="[
          'px-4 py-2.5 cursor-pointer text-sm text-gray-800 flex items-center gap-2',
          index === activeIndex ? 'bg-gray-100' : 'hover:bg-gray-50',
        ]"
        @mousedown.prevent="selectCompany(company)"
      >
        <svg class="shrink-0 text-gray-400" width="14" height="14" viewBox="0 0 20 20" fill="none" aria-hidden="true">
          <path
            d="M17.5 17.5L13.875 13.875M15.8333 9.16667C15.8333 12.8486 12.8486 15.8333 9.16667 15.8333C5.48477 15.8333 2.5 12.8486 2.5 9.16667C2.5 5.48477 5.48477 2.5 9.16667 2.5C12.8486 2.5 15.8333 5.48477 15.8333 9.16667Z"
            stroke="currentColor"
            stroke-width="1.67"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
        <span v-html="highlight(company.companyName)" />
      </li>
      <li
        v-if="suggestions.length >= resultLimit"
        class="px-4 py-2 text-xs text-gray-500 font-medium border-t border-gray-100"
        aria-live="polite"
      >
        Only showing {{ resultLimit }} results, please refine your query.
      </li>
    </ul>
    <p v-if="showMinLengthWarning" class="mt-1.5 text-xs text-red-500" role="alert">
      Please type at least 3 characters.
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { useCompanySearch, type CompanyIdAndName } from '../composables/useCompanySearch';
import { fetchCompaniesByNameOrLei } from '../utils/companySearch';

export default defineComponent({
  name: 'CompanySearchBar',

  setup() {
    const onSelect = (company: CompanyIdAndName) => {
      window.location.href = `/companies/${company.companyId}`;
    };
    return useCompanySearch(fetchCompaniesByNameOrLei, onSelect);
  },
});
</script>

