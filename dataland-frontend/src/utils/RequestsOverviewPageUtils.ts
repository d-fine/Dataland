import { RequestPriority } from '@clients/communitymanager';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { ADMIN_FILTERABLE_REQUESTS_REPORTING_PERIODS, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import { RequestState } from '@clients/datasourcingservice';

/**
 * Compares two request states
 * @param a state to compare
 * @param b state to compare
 * @param sortOrder is a reactive variable in the vue components that use this sorting function
 * @returns result of the comparison
 */
export function customCompareForRequestState(a: RequestState, b: RequestState, sortOrder: number): number {
  const sortOrderRequestState: { [key: string]: number } = {};
  sortOrderRequestState[RequestState.Processed] = 1;
  sortOrderRequestState[RequestState.Processing] = 2;
  sortOrderRequestState[RequestState.Open] = 3;
  sortOrderRequestState[RequestState.Withdrawn] = 4;
  if (sortOrderRequestState[a]! <= sortOrderRequestState[b]!) return -1 * sortOrder;
  return sortOrder;
}

/**
 * Gets list with all available frameworks
 * @returns array of frameworkSelectableItem
 */
export function retrieveAvailableFrameworks(): Array<FrameworkSelectableItem> {
  return FRAMEWORKS_WITH_VIEW_PAGE.map((dataTypeEnum) => {
    let displayName = humanizeStringOrNumber(dataTypeEnum);
    const frameworkDefinition = getFrontendFrameworkDefinition(dataTypeEnum);
    if (frameworkDefinition) {
      displayName = frameworkDefinition.label;
    }
    return {
      frameworkDataType: dataTypeEnum,
      displayName: displayName,
      disabled: false,
    };
  });
}

/**
 * Gets list with all available request states
 * @returns array of SelectableItem
 */
export function retrieveAvailableRequestStates(): Array<SelectableItem> {
  return Object.values(RequestState).map((RequestState) => {
    return {
      displayName: RequestState,
      disabled: false,
    };
  });
}

/**
 * Gets list with all available request priorities
 * @returns array of SelectableItem
 */
export function retrieveAvailablePriorities(): Array<SelectableItem> {
  return Object.values(RequestPriority).map((priority) => {
    return {
      displayName: priority,
      disabled: false,
    };
  });
}

/**
 * Gets list with all available reporting periods
 * @returns array of SelectableItem
 */
export function retrieveAvailableReportingPeriods(): Array<SelectableItem> {
  return ADMIN_FILTERABLE_REQUESTS_REPORTING_PERIODS.map((reportingPeriod) => {
    return {
      displayName: reportingPeriod,
      disabled: false,
    };
  });
}
