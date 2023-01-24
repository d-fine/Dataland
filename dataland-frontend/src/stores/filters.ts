import { defineStore } from "pinia";
import { DataTypeEnum } from "@clients/backend";

export const useFiltersStore = defineStore("frameworksFilters", {
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
