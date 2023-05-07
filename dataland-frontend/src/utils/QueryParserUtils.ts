import { LocationQueryValue } from "vue-router";

/**
 * Query parameters can be either a string value or an array of strings if the parameter is specified multiple times.
 * This helper function converts both to a (possibly empty) string array
 * @param queryParam the query parameter of the vue router to convert
 * @returns the query parameter converted to an array of strings
 */
export function parseQueryParamArray(queryParam: LocationQueryValue | LocationQueryValue[]): string[] {
  if (typeof queryParam === "string" && queryParam !== "") {
    return [queryParam];
  } else if (Array.isArray(queryParam)) {
    return queryParam as string[];
  } else {
    return [];
  }
}
