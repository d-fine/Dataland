import { defineStore } from "pinia";
import { DataTypeEnum } from "@clients/backend";
import { SESSION_TIMEOUT_IN_SECONDS } from "@/utils/Constants";

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

export const useTimeoutLogoutStore = defineStore("timeoutLogoutStore", {
  state: () => {
    return {
      remainingSessionTimeInSeconds: SESSION_TIMEOUT_IN_SECONDS,
      // timestamp warning
      // timestamp logout
    };
  },
  actions: {
    bumpTimerReset() {
      this.remainingSessionTimeInSeconds = SESSION_TIMEOUT_IN_SECONDS;
    },
    reduceTimerBySeconds(secondsToSubstract: number) {
      this.remainingSessionTimeInSeconds -= secondsToSubstract;
    },
  },
});
