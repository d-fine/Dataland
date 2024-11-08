import { AccessStatus, RequestStatus } from '@clients/communitymanager';
import { type FrameworkSelectableItem, type SelectableItem, type PrioritySelectableItem } from '@/utils/FrameworkDataSearchDropDownFilterTypes';
import { FRAMEWORKS_WITH_VIEW_PAGE, PRIORITY_ENUM } from '@/utils/Constants';
import { humanizeStringOrNumber } from '@/utils/StringFormatter';
import { getFrontendFrameworkDefinition } from '@/frameworks/FrontendFrameworkRegistry';

/**
 * Compares two request status
 * @param a RequestStatus to compare
 * @param b RequestStatus to compare
 * @param sortOrder is a reactive variable in the vue components that use this sorting function
 * @returns result of the comparison
 */
export function customCompareForRequestStatus(a: RequestStatus, b: RequestStatus, sortOrder: number): number {
  const sortOrderRequestStatus: { [key: string]: number } = {};
  sortOrderRequestStatus[RequestStatus.Answered] = 1;
  sortOrderRequestStatus[RequestStatus.Open] = 2;
  sortOrderRequestStatus[RequestStatus.Resolved] = 3;
  sortOrderRequestStatus[RequestStatus.Closed] = 4;
  sortOrderRequestStatus[RequestStatus.Withdrawn] = 5;
  if (sortOrderRequestStatus[a] <= sortOrderRequestStatus[b]) return -1 * sortOrder;
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
 * Gets list with all available access status
 * @returns array of SelectableItem
 */
export function retrieveAvailableAccessStatus(): Array<SelectableItem> {
  return Object.values(AccessStatus).map((status) => {
    return {
      displayName: status,
      disabled: false,
    };
  });
}

/**
 * Gets list with all available request status
 * @returns array of SelectableItem
 */
export function retrieveAvailableRequestStatus(): Array<SelectableItem> {
  return Object.values(RequestStatus).map((status) => {
    return {
      displayName: status,
      disabled: false,
    };
  });
}

export function retrieveAvailablePriority(): Array<SelectableItem>{
  return PRIORITY_ENUM.map((dataTypeEnum) => {
    let displayName = humanizeStringOrNumber(dataTypeEnum);
    const priorityDefinition = getFrontendFrameworkDefinition(dataTypeEnum);
    if (priorityDefinition) {
      displayName = priorityDefinition.label;
    }
    return {
      priorityDataType: dataTypeEnum,
      displayName: displayName,
      disabled: false,
    };
  });
}


