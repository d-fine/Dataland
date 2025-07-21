import { groupReportingPeriodsPerFrameworkForCompany } from '@/utils/FileDownloadUtils.ts';

describe('groupReportingPeriodsPerFramework', () => {
  it('groups reporting periods by framework without duplicates', () => {
    const testData = [
      { metaInfo: { dataType: 'sfdr', reportingPeriod: '2024' } },
      { metaInfo: { dataType: 'sfdr', reportingPeriod: '2023' } },
      { metaInfo: { dataType: 'sfdr', reportingPeriod: '2023' } },
      { metaInfo: { dataType: 'eu-taxonomy-financials', reportingPeriod: '2022' } },
    ];

    cy.then(() => {
      const result = groupReportingPeriodsPerFrameworkForCompany(testData);

      expect(result instanceof Map).to.be.true;
      expect(result.get('sfdr')).to.deep.equal(['2024', '2023']);
      expect(result.get('eu-taxonomy-financials')).to.deep.equal(['2022']);
    });
  });
});
