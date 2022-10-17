import { LocationQueryValue } from "vue-router";

export function parseQueryParamArray(queryParam: LocationQueryValue | LocationQueryValue[]): string[] {
  if (typeof queryParam === "string" && queryParam !== "") {
    return [queryParam];
  } else if (Array.isArray(queryParam)) {
    return queryParam as string[];
  } else {
    return [];
  }
}
