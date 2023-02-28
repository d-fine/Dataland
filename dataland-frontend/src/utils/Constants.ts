import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRONTEND_INCLUDED_FRAMEWORKS = Object.values(DataTypeEnum).filter(
  (frameworkName) => ["sme"].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const UPLOAD_MAX_FILE_SIZE_IN_BYTES = 2000000;

export const TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS = 2000;
