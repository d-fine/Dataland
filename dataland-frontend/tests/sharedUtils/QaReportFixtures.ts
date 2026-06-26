import { type FixtureData } from "@sharedUtils/Fixtures.ts";
import { type QaReportDataPointString } from "@clients/qaservice";
import {Rule} from "eslint";
import Fix = Rule.Fix;

export interface QaReportFixtureData<FrameworkDataType> {
  preparedFixture : FixtureData<FrameworkDataType>;
  qaReports: {[key: string]: QaReportDataPointString};
}

