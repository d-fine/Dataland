import { DataTypeEnum } from "@clients/backend";

/**
 * Contains global constants
 */

export const ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE = Object.values(DataTypeEnum).filter(
  (frameworkName) => [DataTypeEnum.Sme as string].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM = Object.values(DataTypeEnum).filter(
  (frameworkName) => [DataTypeEnum.Sfdr as string, DataTypeEnum.Sme as string].indexOf(frameworkName) === -1
) as Array<DataTypeEnum>;

export const UPLOAD_FILE_SIZE_DISPLAY_DECIMALS = 2;

export const UPLOAD_MAX_FILE_SIZE_IN_BYTES = 2000000;

export const TIME_DELAY_BETWEEN_UPLOAD_AND_REDIRECT_IN_MS = 2000;

export const TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS = 5000;

// This always has to be shorter than the ssoSessionIdleTimeout value in the realm settings.
export const TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS = 10 * 1000; // TODO set to 2*60*1000 before merge to main

export const KEYCLOAK_INIT_OPTIONS = {
  realm: "datalandsecurity",
  url: "/keycloak",
  clientId: "dataland-public",
};
