import { DatasetStatus, getDatasetStatus } from "@/components/resources/datasetOverview/DatasetTableInfo";
import { DataMetaInformation } from "@clients/backend";
import { assertDefined } from "@/utils/TypeScriptUtils";

/**
 * Gets the badge's html element for the status of a dataset
 *
 * @param dataMetaInfo the meta info of the dataset
 * @returns the composed html element
 */
export function getBadgeElement(dataMetaInfo: DataMetaInformation): string {
  return assertDefined(DatasetStatusBadgeElements.get(getDatasetStatus(dataMetaInfo)));
}

/**
 * Maps a dataset status to the corresponding badge's html element
 */
export const DatasetStatusBadgeElements = new Map<DatasetStatus, string>([
  [DatasetStatus.QAApproved, composeBadge("green", "APPROVED")],
  [DatasetStatus.QAPending, composeBadge("yellow", "PENDING")],
  [DatasetStatus.Outdated, composeBadge("brown", "OUTDATED")],
]);

/**
 * Composes a badge's html element
 *
 * @param color the color of the badge
 * @param text the text inside the badge
 * @returns the composed badge element
 */
function composeBadge(color: string, text: string): string {
  return `<div class="p-badge badge-${color}">` + `<span>${text}</span>` + "</div>";
}
