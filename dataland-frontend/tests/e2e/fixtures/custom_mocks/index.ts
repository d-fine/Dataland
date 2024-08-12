import fs from 'fs';
import { extractMetaInfoAssociatedWithReportingPeriodByDataType } from '@e2e/fixtures/custom_mocks/CustomMetaDataFormatFixtures';
import { generateMetaInfoDataForOneCompany } from '@e2e/fixtures/custom_mocks/DataMetaInformationFaker';
import { generateMapOfFrameworkNameToAggregatedFrameworkDataSummary } from '@e2e/fixtures/custom_mocks/MapOfDataTypeToAggregatedFrameworkDataSummaryFaker';
import { generateListOfDataSearchStoredCompany } from '@e2e/fixtures/custom_mocks/DataSearchStoredCompanyFaker';
import { generateStoredDataRequests } from '@e2e/fixtures/custom_mocks/StoredDataRequestsFaker';
import { generateSfdrQaReportPreparedFixtures } from '@e2e/fixtures/custom_mocks/SfdrQaReportPreparedFixtures';

/**
 * Generates mocks that are not only dataset mocks
 */
export function exportCustomMocks(): void {
  const metaInfoDataForOneCompany = generateMetaInfoDataForOneCompany();
  fs.writeFileSync(
    '../testing/data/MetaInfoDataMocksForOneCompany.json',
    JSON.stringify(metaInfoDataForOneCompany, null, '\t')
  );
  fs.writeFileSync(
    '../testing/data/MetaInfoAssociatedWithReportingPeriodByDataTypeMock.json',
    JSON.stringify(extractMetaInfoAssociatedWithReportingPeriodByDataType(metaInfoDataForOneCompany), null, '\t')
  );

  fs.writeFileSync(
    '../testing/data/MapOfFrameworkNameToAggregatedFrameworkDataSummaryMock.json',
    JSON.stringify(generateMapOfFrameworkNameToAggregatedFrameworkDataSummary(), null, '\t')
  );

  fs.writeFileSync(
    '../testing/data/DataSearchStoredCompanyMocks.json',
    JSON.stringify(generateListOfDataSearchStoredCompany(), null, '\t')
  );
  fs.writeFileSync(
    '../testing/data/DataRequestsMock.json',
    JSON.stringify(
      generateStoredDataRequests(),
      // eslint-disable-next-line @typescript-eslint/no-unsafe-return
      (_key, value) => (value instanceof Set ? Array.from(value) : value),
      '\t'
    )
  );
  fs.writeFileSync(
    '../testing/data/SfdrQaReportPreparedFixtures.json',
    JSON.stringify(generateSfdrQaReportPreparedFixtures(), null, '\t')
  );
}
