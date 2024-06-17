import { type Equals } from '@/utils/TypeScriptUtils';
import {
  type CompanyAssociatedDataLksgData,
  type CompanyAssociatedDataSfdrData,
  type LksgData,
  type SfdrData,
} from '@clients/backend';
import { type CompanyAssociatedData } from '@/api-models/CompanyAssociatedData';

/**
 * NOTE: These assignments are compile-time checks of the generic interfaces.
 * Tests run during (npm run checkcypresscompilation)
 * Therefore, it is expected for them to be unused!
 */

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const genericCompanyAssociatedDataInterfaceWorksForLksgData: Equals<
  CompanyAssociatedData<LksgData>,
  CompanyAssociatedDataLksgData
> = true;

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const genericCompanyAssociatedDataInterfaceWorksForSfdrData: Equals<
  CompanyAssociatedData<SfdrData>,
  CompanyAssociatedDataSfdrData
> = true;
