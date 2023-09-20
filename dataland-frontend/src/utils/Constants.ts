import { DataTypeEnum } from "@clients/backend";

// - Available frameworks settings
export const ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE: DataTypeEnum[] = Object.values(DataTypeEnum);

export const ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM: DataTypeEnum[] = [
  DataTypeEnum.P2p,
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.Sfdr,
  DataTypeEnum.Lksg,
  DataTypeEnum.EutaxonomyNonFinancials,
];

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

export const REGEX_FOR_FILE_NAMES = /^[A-Za-z]+$/;

export const MS_PER_DAY = 24 * 60 * 60 * 1000;
