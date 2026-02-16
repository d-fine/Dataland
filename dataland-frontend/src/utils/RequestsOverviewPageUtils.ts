import { RequestPriority } from '@clients/communitymanager';
import {
  type FrameworkSelectableItem,
  type MixedStateSelectableItem,
  type SelectableItem,
} from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { ADMIN_FILTERABLE_REQUESTS_REPORTING_PERIODS, FRAMEWORKS_WITH_VIEW_PAGE } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';
import {
  type DataSourcingEnhancedRequest,
  DataSourcingState,
  DisplayedState,
  RequestState,
} from '@clients/datasourcingservice';

// @ts-ignore
const stateLabelMap: Partial<Record<DataSourcingState | RequestState | DisplayedState, string>> = {
  [RequestState.Open || DisplayedState.Open]: 'Open',
  [RequestState.Withdrawn || DisplayedState.Withdrawn]: 'Withdrawn',
  [DataSourcingState.Initialized || DisplayedState.Validated]: 'Validated',
  [DataSourcingState.DocumentSourcing || DisplayedState.DocumentSourcing]: 'Document Sourcing',
  [DataSourcingState.DocumentSourcingDone || DisplayedState.DocumentVerification]: 'Document Verification',
  [DataSourcingState.DataExtraction || DisplayedState.DataExtraction]: 'Data Extraction',
  [DataSourcingState.DataVerification || DisplayedState.DataVerification]: 'Data Verification',
  [DataSourcingState.NonSourceable || DisplayedState.NonSourceable]: 'Non-Sourceable',
  [DataSourcingState.Done || DisplayedState.Done]: 'Done',
};

const displayedMixedStates: (DataSourcingState | RequestState)[] = [
  RequestState.Open,
  DataSourcingState.Initialized,
  DataSourcingState.DocumentSourcing,
  DataSourcingState.DocumentSourcingDone,
  DataSourcingState.DataExtraction,
  DataSourcingState.DataVerification,
  DataSourcingState.Done,
  DataSourcingState.NonSourceable,
  RequestState.Withdrawn,
];

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
 * Returns the request state for Open/Withdrawn requests, Done for Processed requests without
 * data sourcing details, or the data sourcing state if available.
 *
 * Note: Requests in Processing state always have an associated DataSourcing entity with a valid
 * dataSourcingState. The fallback handles Processed requests with no DataSourcing entity.
 *
 * @param request - The data sourcing request object.
 * @returns The displayed state of the request.
 */
export function getDisplayedState(request: DataSourcingEnhancedRequest): DataSourcingState | RequestState {
  if (request.state === RequestState.Open || request.state === RequestState.Withdrawn) {
    return request.state;
  }
  if (request.state === RequestState.Processed && !request.dataSourcingDetails?.dataSourcingState) {
    return DataSourcingState.Done;
  }
  return request.dataSourcingDetails?.dataSourcingState ?? DataSourcingState.Done;
}

/**
 * Returns a user-facing label for the displayed request state.
 * This is intended for UI display (e.g. inside a tag) and is intentionally decoupled from enum values.
 *
 * @param displayedState - The state to get the label for, either a DataSourcingState or RequestState.
 * @returns The user-facing label for the given state.
 */
export function getDisplayedStateLabel(displayedState: DataSourcingState | RequestState | DisplayedState): string {
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
 * Gets list with all available mixed states for the filter dropdown.
 * @returns array of MixedStateSelectableItem with user-facing labels and enum values
 */
export function retrieveAvailableMixedStates(): Array<MixedStateSelectableItem> {
  return displayedMixedStates.map((state) => ({
    displayName: stateLabelMap[state] ?? state,
    stateValue: state,
    disabled: false,
  }));
}

/**
 * Converts selected mixed states to separate requestStates and dataSourcingStates arrays for API.
 * @param selectedItems - Array of MixedStateSelectableItem selected in the filter
 * @returns Object with requestStates and dataSourcingStates arrays for API call
 */
export function convertMixedStatesToApiFilters(selectedItems: MixedStateSelectableItem[]): {
  requestStates: RequestState[] | undefined;
  dataSourcingStates: DataSourcingState[] | undefined;
} {
  const requestStates: RequestState[] = [];
  const dataSourcingStates: DataSourcingState[] = [];

  for (const item of selectedItems) {
    const stateValue = item.stateValue;
    if (Object.values(RequestState).includes(stateValue as RequestState)) {
      requestStates.push(stateValue as RequestState);
    } else if (Object.values(DataSourcingState).includes(stateValue as DataSourcingState)) {
      dataSourcingStates.push(stateValue as DataSourcingState);
    }
  }

  return {
    requestStates: requestStates.length > 0 ? requestStates : undefined,
    dataSourcingStates: dataSourcingStates.length > 0 ? dataSourcingStates : undefined,
  };
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
