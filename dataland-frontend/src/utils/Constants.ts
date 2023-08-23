import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

// ----------------------DATALAND SETTINGS----------------------
export const DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_BYTES =
  // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
  (parseInt(import.meta.env.VITE_DATA_REQUEST_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES) || 2) * 1000000;

export const DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_BYTES =
  // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
  (parseInt(import.meta.env.VITE_DOCUMENT_UPLOAD_MAX_FILE_SIZE_IN_MEGABYTES) || 100) * 1000000;

export const MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY =
  // eslint-disable-next-line @typescript-eslint/no-unsafe-argument
  parseInt(import.meta.env.VITE_MAX_NUMBER_OF_DAYS_SELECTABLE_FOR_API_KEY_VALIDITY) || 3650;

// ----------------------FRONTEND SETTINGS ONLY----------------------

// - Available frameworks settings
export const ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE = Object.values(DataTypeEnum).filter(
  (frameworkName) => ([DataTypeEnum.EutaxonomyNonFinancials] as string[]).indexOf(frameworkName) === -1,
) as Array<DataTypeEnum>;

export const ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM = Object.values(DataTypeEnum).filter(
  (frameworkName) => [DataTypeEnum.Sme as string].indexOf(frameworkName) === -1,
) as Array<DataTypeEnum>;

// - Keycloak and session management related settings

export const KEYCLOAK_INIT_OPTIONS = {
  realm: "datalandsecurity",
  url: "/keycloak",
  clientId: "dataland-public",
};

export const TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS = 5000;

export const TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS = 2 * 60 * 1000;
// => This always has to be shorter than the ssoSessionIdleTimeout value in the realm settings.

// - Other settings

export const TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS = 2000;

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const MS_PER_DAY = 24 * 60 * 60 * 1000;
