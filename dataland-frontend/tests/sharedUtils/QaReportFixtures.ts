import { type FixtureData } from '@sharedUtils/Fixtures.ts';
import { type QaReportDataPointString } from '@clients/qaservice';

export interface QaReportFixtureData<FrameworkDataType> {
  preparedFixture: FixtureData<FrameworkDataType>;
  qaReports: { [key: string]: QaReportDataPointString };
}
