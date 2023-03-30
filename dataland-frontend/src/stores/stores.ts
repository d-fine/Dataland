import { defineStore } from "pinia";
import { DataTypeEnum } from "@clients/backend";

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
      sessionWarningTimestampInMs: undefined as undefined | number,
    };
  },

  share: {
    enable: true,
  },
});
