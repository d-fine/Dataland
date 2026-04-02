import type { CompanyIdAndName } from '../composables/useCompanySearch';

/**
 * Fetches matching companies from the public /api/companies/names endpoint
 * (no auth required).  Passed as the fetchCompanies argument to useCompanySearch().
 */
export async function fetchCompaniesByNameOrLei(
  searchString: string,
  resultLimit: number
): Promise<CompanyIdAndName[]> {
  const params = new URLSearchParams({ searchString, resultLimit: String(resultLimit) });
  const response = await fetch(`${window.location.origin}/api/companies/names?${params}`);
  if (!response.ok) {
    console.error(`Company search failed: ${response.status} ${response.statusText}`);
    return [];
  }
  return (await response.json()) as CompanyIdAndName[];
}
