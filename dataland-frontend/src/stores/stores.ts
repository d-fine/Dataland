import { defineStore } from "pinia";
import { DataTypeEnum } from "@clients/backend";
import { TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS } from "@/utils/Constants";

export const useFrameworkFiltersStore = defineStore("frameworkFilters", {
  state: () => {
    return {
      selectedFiltersForFrameworks: [] as DataTypeEnum[],
    };
  },
  actions: {
    setSelectedFiltersForFrameworks(setFilters: DataTypeEnum[]) {
      this.selectedFiltersForFrameworks = setFilters;
    },
  },
});

export const useFunctionIdsStore = defineStore("functionIdsStore", {
  state: () => {
    return {
      functionIdOfSetIntervalForSessionWarning: undefined as undefined | number,
    };
  },
});

export const useSessionStateStore = defineStore("sessionStateStore", {
  state: () => {
    return {
      refreshToken: undefined as undefined | string,
      refreshTokenExpiryTimestampInMs: undefined as undefined | number,
    };
  },
  getters: {
    sessionWarningTimestampInMs: (state) => {
      if (state.refreshTokenExpiryTimestampInMs) {
        return (
          state.refreshTokenExpiryTimestampInMs - TIME_BEFORE_REFRESH_TOKEN_EXPIRY_TO_DISPLAY_SESSION_WARNING_IN_MS
        );
      }
    },
  },

  share: {
    enable: true,
  },
});
