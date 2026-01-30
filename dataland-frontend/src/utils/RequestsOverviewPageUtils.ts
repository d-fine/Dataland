import { RequestPriority } from '@clients/communitymanager';
import { type FrameworkSelectableItem, type SelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { ADMIN_FILTERABLE_REQUESTS_REPORTING_PERIODS, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import { type DataSourcingEnhancedRequest, DataSourcingState, RequestState } from '@clients/datasourcingservice';

/**
 * Compares two states, either data sourcing states or request states
 * @param a state to compare
 * @param b state to compare
 * @param sortOrder is a reactive variable in the vue components that use this sorting function
 * @returns result of the comparison
 */
export function customCompareForState(
  a: DataSourcingState | RequestState,
  b: DataSourcingState | RequestState,
  sortOrder: number
): number {
  const sortOrderRequestState: { [key: string]: number } = {};
  sortOrderRequestState[RequestState.Open] = 0;
  sortOrderRequestState[DataSourcingState.Initialized] = 1;
  sortOrderRequestState[DataSourcingState.DocumentSourcing] = 2;
  sortOrderRequestState[DataSourcingState.DocumentSourcingDone] = 3;
  sortOrderRequestState[DataSourcingState.DataExtraction] = 4;
  sortOrderRequestState[DataSourcingState.DataVerification] = 5;
  sortOrderRequestState[DataSourcingState.Done] = 6;
  sortOrderRequestState[DataSourcingState.NonSourceable] = 7;
  if (sortOrderRequestState[a]! <= sortOrderRequestState[b]!) return -1 * sortOrder;
  return sortOrder;
}

/**
 * Retrieves the displayed state of a data sourcing request.
 * If the data sourcing details are available, it returns the data sourcing state;
 * otherwise, it defaults to 'Open'.
 *
 * @param {DataSourcingEnhancedRequest} request - The data sourcing request object.
 * @returns {string} The displayed state of the request.
 */
export function getDisplayedState(request: DataSourcingEnhancedRequest): string {
  if (request.state in [RequestState.Open, RequestState.Withdrawn]) {
    return request.state;
  }
  return request.dataSourcingDetails?.dataSourcingState ?? RequestState.Open;
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
 * Gets list with all available data sourcing states
 * @returns array of SelectableItem
 */
export function retrieveAvailableDataSourcingStates(): Array<SelectableItem> {
  return Object.values(DataSourcingState).map((DataSourcingState) => {
    return {
      displayName: DataSourcingState,
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
