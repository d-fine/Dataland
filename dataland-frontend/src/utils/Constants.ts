import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE = Object.values(DataTypeEnum).filter(
  (frameworkName) => [DataTypeEnum.Sme as string].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM = Object.values(DataTypeEnum).filter(
  (frameworkName) =>
    [
      DataTypeEnum.EutaxonomyFinancials as string,
      DataTypeEnum.EutaxonomyNonFinancials as string,
      DataTypeEnum.Sfdr as string,
      DataTypeEnum.Sme as string,
    ].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const UPLOAD_MAX_FILE_SIZE_IN_BYTES = 2000000;

export const TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS = 2000;

export const SESSION_TIMEOUT_IN_MS = 60 * 1000; // TODO increase
