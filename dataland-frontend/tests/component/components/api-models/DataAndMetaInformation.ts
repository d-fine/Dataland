import { type Equals } from '@/utils/TypeScriptUtils';
import {
  type DataAndMetaInformationLksgData,
  type DataAndMetaInformationSfdrData,
  type LksgData,
  type SfdrData,
} from '@clients/backend';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';

/**
 * NOTE: These assignments are compile-time checks of the generic interfaces.
 * Tests run during (npm run checkcypresscompilation)
 * Therefore, it is expected for them to be unused!
 */

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const genericDataAndMetaInformationInterfaceWorksForLksgData: Equals<
  DataAndMetaInformation<LksgData>,
  DataAndMetaInformationLksgData
> = true;

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const genericDataAndMetaInformationInterfaceWorksForSfdrData: Equals<
  DataAndMetaInformation<SfdrData>,
  DataAndMetaInformationSfdrData
> = true;
