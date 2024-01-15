import { DataTypeEnum } from "@clients/backend";

// - Available frameworks settings

export const ARRAY_OF_SUPPORTED_FRAMEWORKS = putGdvAtTheEndOfList(Object.values(DataTypeEnum));
export const ARRAY_OF_FRAMEWORKS_WITH_VIEW_PAGE = putGdvAtTheEndOfList(Object.values(DataTypeEnum));
export const ARRAY_OF_FRAMEWORKS_WITH_UPLOAD_FORM = putGdvAtTheEndOfList([
  DataTypeEnum.P2p,
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.Sfdr,
  DataTypeEnum.Lksg,
  DataTypeEnum.EutaxonomyNonFinancials,
  DataTypeEnum.Gdv,
]);

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

// Same as standard for Windows machines. File name can't have one of the special characters <>:"|?/*\
// and shouldn't start or end with a whitespace or end with the special character .
export const REGEX_FOR_FILE_NAMES = /^[^<>:"|?/*\\\s][^<>:"|?/*\\]{0,252}[^<>:"|?/*\\.\s]$/;

export const MS_PER_DAY = 24 * 60 * 60 * 1000;

export const NO_DATA_PROVIDED = "No data provided";

/**
 * Changes the sorting of a list of data type enums by putting the gdv/vöb framework at the very end.
 * @param frameworksToInclude a unsorted list of data type enums
 * @returns the list of data type enums sorted in a way, that gdv/vöb framework is the last element
 */
export function putGdvAtTheEndOfList(frameworksToInclude: DataTypeEnum[]): DataTypeEnum[] {
  const customSort = (a: DataTypeEnum, b: DataTypeEnum): number => {
    if (a === DataTypeEnum.Gdv && b !== DataTypeEnum.Gdv) {
      return 1;
    } else if (a !== DataTypeEnum.Gdv && b === DataTypeEnum.Gdv) {
      return -1;
    } else {
      return 0;
    }
  };
  return frameworksToInclude.includes(DataTypeEnum.Gdv)
    ? [...frameworksToInclude].sort(customSort)
    : frameworksToInclude;
}
