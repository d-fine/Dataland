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
  sortOrderRequestState[RequestState.Withdrawn] = 8;
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
  if (request.state === RequestState.Open || request.state === RequestState.Withdrawn) {
    return request.state;
  }
  return request.dataSourcingDetails?.dataSourcingState ?? RequestState.Open;
}

/**
 * Returns a user-facing label for the displayed request state.
 * This is intended for UI display (e.g. inside a tag) and is intentionally decoupled from enum values.
 *
 * @param displayedState - The state to get the label for, either a DataSourcingState or RequestState.
 * @returns The user-facing label for the given state.
 */
export function getDisplayedStateLabel(displayedState: DataSourcingState | RequestState): string {
  const stateLabelMap: Partial<Record<DataSourcingState | RequestState, string>> = {
    [RequestState.Open]: 'Open',
    [RequestState.Withdrawn]: 'Withdrawn',

    [DataSourcingState.Initialized]: 'Validated',
    [DataSourcingState.DocumentSourcing]: 'Document Sourcing',
    [DataSourcingState.DocumentSourcingDone]: 'Document Verification',
    [DataSourcingState.DataExtraction]: 'Data Extraction',
    [DataSourcingState.DataVerification]: 'Data Verification',
    [DataSourcingState.NonSourceable]: 'Non-Sourceable',
    [DataSourcingState.Done]: 'Done',
  };

  return stateLabelMap[displayedState] ?? humanizeStringOrNumber(displayedState);
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
      displayName: getDisplayedStateLabel(DataSourcingState),
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
