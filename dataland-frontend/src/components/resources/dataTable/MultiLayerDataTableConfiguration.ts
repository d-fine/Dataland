import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";

export type MLDTConfig<FrameworkDataType> = Array<
  MLDTCellConfig<FrameworkDataType> | MLDTSectionConfig<FrameworkDataType>
>;

export interface MLDTDataset<FrameworkDataType> {
  headerLabel: string;
  dataset: FrameworkDataType;
}

export type BadgeColor = "yellow" | "green" | "red" | "blue" | "purple" | "gray" | "brown" | "orange";

export interface MLDTCellConfig<FrameworkDataType> {
  type: "cell";
  label: string;
  explanation?: string;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  valueGetter: (dataset: FrameworkDataType) => AvailableDisplayValues;
}

export interface MLDTSectionConfig<FrameworkDataType> {
  type: "section";
  label: string;
  labelBadgeColor?: BadgeColor;
  expandOnPageLoad: boolean;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  children: MLDTConfig<FrameworkDataType>;
}

/**
 * Check if the specified MLDT-Element should be displayed given the provided datasets.
 * @param element The element to check
 * @param datasets The datasets to use during the check
 * @returns true iff the element should be displayed
 */
export function isElementVisible<FrameworkDataType>(
  element: MLDTSectionConfig<FrameworkDataType> | MLDTCellConfig<FrameworkDataType>,
  datasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  if (element.type == "cell") {
    return isCellRowVisible(element, datasets);
  } else {
    return isCellSectionVisible(element, datasets);
  }
}

/**
 * Check if the specified MLDT-Cell should be displayed given the provided datasets.
 * @param cellConfig The cell to check
 * @param datasets The datasets to use during the check
 * @returns true iff the cell should be displayed
 */
function isCellRowVisible<FrameworkDataType>(
  cellConfig: MLDTCellConfig<FrameworkDataType>,
  datasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  return datasets.some((datasetEntry) => cellConfig.shouldDisplay(datasetEntry.dataset));
}

/**
 * Check if the specified MLDT-Section should be displayed given the provided datasets.
 * @param sectionConfig The section to check
 * @param datasets The datasets to use during the check
 * @returns true iff the section should be displayed
 */
function isCellSectionVisible<FrameworkDataType>(
  sectionConfig: MLDTSectionConfig<FrameworkDataType>,
  datasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  const shouldShowSection = datasets.some((datasetElement) => sectionConfig.shouldDisplay(datasetElement.dataset));

  if (!shouldShowSection) return false;

  const anyChildrenVisible = sectionConfig.children.some((child) => isElementVisible(child, datasets));

  return shouldShowSection && anyChildrenVisible;
}
