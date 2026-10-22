import type { BasicDataDimensions } from '@clients/backend';
import type { ReportingPeriodEntry } from '@/components/resources/portfolio/PortfolioReportingPeriodsCell.vue';
import type { EnrichedPortfolio, EnrichedPortfolioEntry } from '@clients/userservice';
import { PORTFOLIO_OVERVIEW_FRAMEWORKS } from '@/utils/Constants.ts';
import type { ProxiedCompany } from '@/components/resources/portfolio/PortfolioProxiedCompaniesCell.vue';

/**
 * Merges the (comma-separated) string of available reporting periods for a framework (which, for the purposes of
 * bulk download and the period filter, may already include non-sourceable periods as "resolved") with the
 * independently-fetched non-sourceable reporting periods for that same framework, and returns a single,
 * ascendingly sorted list where each entry is marked whether it is non-sourceable.
 *
 * Note: whether a year is non-sourceable is determined solely by membership in `nonSourceableDimensionsForFramework`
 * (the dedicated non-sourceability query), not by exclusion from `availableReportingPeriodsCsv` - the latter may
 * already contain non-sourceable years merged in, and must not be used to decide the strikethrough status.
 * @param availableReportingPeriodsCsv the comma-separated string of available reporting periods
 * @param nonSourceableDimensionsForFramework the set of non-sourceable data dimensions for this company/framework
 */
export function mergeReportingPeriods(
  availableReportingPeriodsCsv: string | undefined,
  nonSourceableDimensionsForFramework: Set<BasicDataDimensions> | undefined
): ReportingPeriodEntry[] {
  const realYears = availableReportingPeriodsCsv ? availableReportingPeriodsCsv.split(', ') : [];
  const nonSourceableYears = new Set(
    Array.from(nonSourceableDimensionsForFramework ?? []).map((dim) => dim.reportingPeriod)
  );
  const allYears = new Set([...realYears, ...nonSourceableYears]);
  return Array.from(allYears)
    .sort()
    .map((year) => ({ year, nonSourceable: nonSourceableYears.has(year) }));
}

/**
 * For every portfolio entry and every portfolio overview framework, computes the merged list of
 * available and non-sourceable reporting periods.
 * @param entries the entries of the enriched portfolio
 * @param nonSourceableDimensionsGrouped non-sourceable dimensions grouped by company ID and framework
 * @returns map from company ID to framework to its reporting period entries
 */
export function calculateReportingPeriodEntriesByCompanyAndFramework(
  entries: EnrichedPortfolioEntry[],
  nonSourceableDimensionsGrouped: Record<string, Record<string, Set<BasicDataDimensions>>> | undefined
): Record<string, Partial<Record<string, ReportingPeriodEntry[]>>> {
  const result: Record<string, Partial<Record<string, ReportingPeriodEntry[]>>> = {};
  for (const entry of entries) {
    const perFramework: Partial<Record<string, ReportingPeriodEntry[]>> = {};
    for (const framework of PORTFOLIO_OVERVIEW_FRAMEWORKS) {
      perFramework[framework] = mergeReportingPeriods(
        entry.availableReportingPeriods[framework],
        nonSourceableDimensionsGrouped?.[entry.companyId]?.[framework]
      );
    }
    result[entry.companyId] = perFramework;
  }
  return result;
}

/**
 * Groups the companies proxied within a portfolio by the ID of their proxy company. Names are taken from the
 * portfolio if the proxied company is part of it (then it is linkable), otherwise from the externally fetched
 * names, falling back to the company ID. Each list is deduplicated and sorted by company name.
 * @param portfolio the enriched portfolio (may be undefined while loading)
 * @param externalProxiedCompanyNames names of proxied companies that are not part of the portfolio, by company ID
 * @returns map from proxy company ID to the companies it proxies
 */
export function groupProxiedCompaniesByProxyId(
  portfolio: EnrichedPortfolio | undefined,
  externalProxiedCompanyNames: Map<string, string>
): Map<string, ProxiedCompany[]> {
  const portfolioNames = new Map((portfolio?.entries ?? []).map((entry) => [entry.companyId, entry.companyName]));
  const result = new Map<string, ProxiedCompany[]>();
  for (const replacement of portfolio?.proxyCompanyReplacements ?? []) {
    const portfolioName = portfolioNames.get(replacement.proxiedCompanyId);
    const companyName =
      portfolioName ?? externalProxiedCompanyNames.get(replacement.proxiedCompanyId) ?? replacement.proxiedCompanyId;
    const list = result.get(replacement.proxyCompanyId) ?? [];
    if (!list.some((company) => company.companyId === replacement.proxiedCompanyId)) {
      list.push({ companyId: replacement.proxiedCompanyId, companyName, linkable: portfolioName !== undefined });
    }
    result.set(replacement.proxyCompanyId, list);
  }
  for (const list of result.values()) list.sort((a, b) => a.companyName.localeCompare(b.companyName));
  return result;
}
