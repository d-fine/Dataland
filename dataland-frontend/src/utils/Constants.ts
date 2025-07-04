import { DataTypeEnum } from '@clients/backend';

// - Available frameworks settings

export const ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER = Object.values(DataTypeEnum);
export const PRIVATE_FRAMEWORKS = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter(
  (framework) => framework == DataTypeEnum.Vsme
);
export const PUBLIC_FRAMEWORKS = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter(
  (framework) => framework != DataTypeEnum.Vsme
);
export const FRONTEND_SUPPORTED_FRAMEWORKS = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER;
export const FRAMEWORKS_WITH_VIEW_PAGE = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER;
const MAIN_FRAMEWORKS = ['eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas', 'sfdr'];
export const FRAMEWORKS_WITH_UPLOAD_FORM = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER;
export const MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) =>
  MAIN_FRAMEWORKS.includes(framework)
);
export const ALL_FRAMEWORKS_IN_DISPLAYED_ORDER = [...MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER];
ALL_FRAMEWORKS_IN_DISPLAYED_ORDER.push(
  ...ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) => !MAIN_FRAMEWORKS_IN_ENUM_CLASS_ORDER.includes(framework))
);
export const EU_TAXONOMY_FRAMEWORKS: string[] = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) =>
  ['eutaxonomy-financials', 'eutaxonomy-non-financials', 'nuclear-and-gas'].includes(framework)
);

export const EU_TAXONOMY_FRAMEWORKS_FINANCIALS: string[] = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) =>
  ['eutaxonomy-financials', 'nuclear-and-gas'].includes(framework)
);

export const EU_TAXONOMY_FRAMEWORKS_NON_FINANCIALS: string[] = ALL_FRAMEWORKS_IN_ENUM_CLASS_ORDER.filter((framework) =>
  ['eutaxonomy-non-financials', 'nuclear-and-gas'].includes(framework)
);

export const LATEST_PERIOD: number = 2024;

// - Keycloak and session management related settings

export const KEYCLOAK_INIT_OPTIONS = {
  realm: 'datalandsecurity',
  url: '/keycloak',
  clientId: 'dataland-public',
};

export const TIME_DISTANCE_SET_INTERVAL_SESSION_CHECK_IN_MS = 5000;

export const TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS = 2 * 60 * 1000;
// => This always has to be shorter than the ssoSessionIdleTimeout value in the realm settings.

// - Other settings

export const TIME_DELAY_BETWEEN_SUBMIT_AND_NEXT_ACTION_IN_MS = 2000;

// Same as standard for Windows machines. File name can't have one of the special characters <>:"|?/*\
// and shouldn't start or end with a whitespace or end with the special character .
export const REGEX_FOR_FILE_NAMES = /^[^<>:"|?/*\\\s][^<>:"|?/*\\]{0,252}[^<>:"|?/*\\.\s]$/;

export const MS_PER_DAY = 24 * 60 * 60 * 1000;

export const NO_DATA_PROVIDED = 'No data provided';
export const ONLY_AUXILIARY_DATA_PROVIDED = 'Only auxiliary data provided';
