import {
  calculateReportingPeriodEntriesByCompanyAndFramework,
  groupProxiedCompaniesByProxyId,
  mergeReportingPeriods,
} from '@/utils/PortfolioUtils';
import { PORTFOLIO_OVERVIEW_FRAMEWORKS } from '@/utils/Constants';
import type { BasicDataDimensions } from '@clients/backend';
import type { EnrichedPortfolio, EnrichedPortfolioEntry } from '@clients/userservice';

const dimensions = (...years: string[]): Set<BasicDataDimensions> =>
  new Set(years.map((reportingPeriod) => ({ reportingPeriod }) as BasicDataDimensions));

describe('Unit test for PortfolioUtils', () => {
  it('verifies mergeReportingPeriods', () => {
    expect(mergeReportingPeriods(undefined, undefined)).to.deep.equal([]);
    expect(mergeReportingPeriods('2022, 2020', undefined)).to.deep.equal([
      { year: '2020', nonSourceable: false },
      { year: '2022', nonSourceable: false },
    ]);
    expect(mergeReportingPeriods('2020, 2021', dimensions('2021', '2019'))).to.deep.equal([
      { year: '2019', nonSourceable: true },
      { year: '2020', nonSourceable: false },
      { year: '2021', nonSourceable: true },
    ]);
  });

  it('verifies calculateReportingPeriodEntriesByCompanyAndFramework', () => {
    const framework = PORTFOLIO_OVERVIEW_FRAMEWORKS[0]!;
    const entries = [
      { companyId: 'c1', availableReportingPeriods: { [framework]: '2020' } },
    ] as unknown as EnrichedPortfolioEntry[];
    const result = calculateReportingPeriodEntriesByCompanyAndFramework(entries, {
      c1: { [framework]: dimensions('2021') },
    });

    expect(Object.keys(result['c1']!)).to.deep.equal(PORTFOLIO_OVERVIEW_FRAMEWORKS);
    expect(result['c1']![framework]).to.deep.equal([
      { year: '2020', nonSourceable: false },
      { year: '2021', nonSourceable: true },
    ]);
    expect(calculateReportingPeriodEntriesByCompanyAndFramework([], undefined)).to.deep.equal({});
  });

  it('verifies groupProxiedCompaniesByProxyId', () => {
    expect(groupProxiedCompaniesByProxyId(undefined, new Map()).size).to.equal(0);

    const portfolio = {
      entries: [{ companyId: 'in', companyName: 'Zeta' }],
      proxyCompanyReplacements: [
        { proxyCompanyId: 'p', proxiedCompanyId: 'in' },
        { proxyCompanyId: 'p', proxiedCompanyId: 'in' },
        { proxyCompanyId: 'p', proxiedCompanyId: 'ext' },
        { proxyCompanyId: 'p', proxiedCompanyId: 'unknown' },
      ],
    } as unknown as EnrichedPortfolio;

    expect(groupProxiedCompaniesByProxyId(portfolio, new Map([['ext', 'Alpha']])).get('p')).to.deep.equal([
      { companyId: 'ext', companyName: 'Alpha', linkable: false },
      { companyId: 'unknown', companyName: 'unknown', linkable: false },
      { companyId: 'in', companyName: 'Zeta', linkable: true },
    ]);
  });
});
