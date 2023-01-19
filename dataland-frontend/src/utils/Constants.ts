import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS = Object.values(DataTypeEnum).filter(
  (frameworkName) => ["sfdr", "sme"].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const UPLOAD_MAX_FILE_SIZE_IN_BYTES = 2000000;

// TODO These are the frontend constants for the productive execution of the frontend. Therefore the following constants should not be here.
// TODO We should put them to the cypress e2e test Constants.ts

export const SHORT_TIMEOUT_IN_MS = 10000;
export const MEDIUM_TIMEOUT_IN_MS = 30000;
export const LONG_TIMEOUT_IN_MS = 120000;
