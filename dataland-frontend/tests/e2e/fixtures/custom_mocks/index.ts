import fs from 'fs';
import {
  generateMetaInfoDataForOneCompany,
  extractMetaInfoAssociatedWithReportingPeriodByDataType,
} from '@e2e/fixtures/custom_mocks/DataMetaInformationFaker';
import { generateMapOfFrameworkNameToAggregatedFrameworkDataSummary } from '@e2e/fixtures/custom_mocks/MapOfDataTypeToAggregatedFrameworkDataSummaryFaker';
import { generateListOfDataSearchStoredCompany } from '@e2e/fixtures/custom_mocks/DataSearchStoredCompanyFaker';
import { generateStoredDataRequests } from '@e2e/fixtures/custom_mocks/StoredDataRequestsFaker';
import {
  generateSfdrLinkedQaReports,
  generateSfdrQaReportPreparedFixtures,
} from '@e2e/fixtures/custom_mocks/SfdrQaReportPreparedFixtures';
import { generateEuTaxonomyNonFinancialsQaReportPreparedFixtures } from '@e2e/fixtures/custom_mocks/EuTaxonomyNonFinancialsQaReportPreparedFixtures';
import { generateAdditionalCompanyInformationLinkedQaReports } from '@e2e/fixtures/custom_mocks/AdditionalCompanyInformationQaPreparedFixtures';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
import { type FrameworkData } from '@/utils/GenericFrameworkTypes';

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
    '../testing/data/DataAndMetaInfoMocksForOneCompany.json',
    JSON.stringify(
      metaInfoDataForOneCompany.map((metaInfo) => {
        return { metaInfo: metaInfo, data: {} } as DataAndMetaInformation<FrameworkData>;
      }),
      null,
      '\t'
    )
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

      (_key, value) => (value instanceof Set ? Array.from(value) : value),
      '\t'
    )
  );
  fs.writeFileSync(
    '../testing/data/SfdrQaReportPreparedFixtures.json',
    JSON.stringify(generateSfdrQaReportPreparedFixtures(), null, '\t')
  );
  fs.writeFileSync(
    '../testing/data/EuTaxonomyNonFinancialsQaReportPreparedFixtures.json',
    JSON.stringify(generateEuTaxonomyNonFinancialsQaReportPreparedFixtures(), null, '\t')
  );
  fs.writeFileSync(
    '../testing/data/AdditionalCompanyInformationQaReportPreparedFixtures.json',
    JSON.stringify(generateAdditionalCompanyInformationLinkedQaReports(), null, '\t')
  );
  fs.writeFileSync(
    '../testing/data/SfdrLinkedDataAndQaReportPreparedFixtures.json',
    JSON.stringify(generateSfdrLinkedQaReports(), null, '\t')
  );
}
