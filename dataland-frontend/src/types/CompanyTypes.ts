import { type EnrichedPortfolioEntry } from '@clients/userservice';

export class CompanyIdAndName {
  companyId: string;
  companyName: string;
  sector?: string;

  public constructor(enrichedPortfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = enrichedPortfolioEntry.companyId;
    this.companyName = enrichedPortfolioEntry.companyName;
  }
}

/**
 * Retrieve array of unique and sorted companyIdAndNames from EnrichedPortfolioEntry
 */
export function getUniqueSortedCompanies(entries: CompanyIdAndName[]): CompanyIdAndName[] {
  const companyMap = new Map(entries.map((entry) => [entry.companyId, entry]));
  return Array.from(companyMap.values()).sort((a, b) => a.companyName.localeCompare(b.companyName));
}
