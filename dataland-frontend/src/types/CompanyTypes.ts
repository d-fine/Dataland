import { type EnrichedPortfolioEntry } from '@clients/userservice';

export class CompanyIdAndNameAndSector {
  companyId: string;
  companyName: string;
  sector?: string;

  public constructor(enrichedPortfolioEntry: EnrichedPortfolioEntry) {
    this.companyId = enrichedPortfolioEntry.companyId;
    this.companyName = enrichedPortfolioEntry.companyName;
    this.sector = enrichedPortfolioEntry.sector;
  }
}
