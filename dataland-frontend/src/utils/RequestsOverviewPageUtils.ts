import { RequestStatus } from '@clients/communitymanager';

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
