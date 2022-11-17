import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS = Object.values(DataTypeEnum).filter(
  (frameworkName) => ["lksg", "sfdr", "sme"].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;
