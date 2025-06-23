import type { EnrichedPortfolio } from '@clients/userservice';
import { groupAllReportingPeriodsByFrameworkForPortfolio } from '@/utils/FileDownloadUtils.ts';

describe('groupAllReportingPeriodsByFramework function', () => {
  let mockPortfolio: EnrichedPortfolio;

  beforeEach(() => {
    cy.fixture('enrichedPortfolio.json').then((json) => {
      mockPortfolio = json as EnrichedPortfolio;
    });
  });

  it('correctly groups reporting periods by framework', () => {
    cy.then(() => {
      const result = groupAllReportingPeriodsByFrameworkForPortfolio(mockPortfolio);

      expect(result instanceof Map).to.be.true;
      expect(result.has('sfdr')).to.be.true;
      expect(result.get('sfdr')).to.include('2024');
      expect(result.get('sfdr')).to.not.include.members(['2019','2020', '2021', '2022','2023']);

      expect(result.has('eutaxonomy-financials')).to.be.true;
      expect(result.get('eutaxonomy-financials')).to.include('2023');
      expect(result.get('eutaxonomy-financials')).to.not.include.members(['2019','2020', '2021', '2022','2024']);
    });
  });
});
