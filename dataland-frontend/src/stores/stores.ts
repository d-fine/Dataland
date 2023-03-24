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

export const useFunctionIdsStore = defineStore("functionIds", {
  state: () => {
    return {
      sessionCheckSetIntervalFunctionId: undefined as undefined | number,
    };
  },
});
