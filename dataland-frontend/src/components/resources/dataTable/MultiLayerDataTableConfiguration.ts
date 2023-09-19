import { type AvailableDisplayValues } from "@/components/resources/dataTable/MultiLayerDataTableCells";

export type MLDTConfig<FrameworkDataType> = Array<
  MLDTCellConfig<FrameworkDataType> | MLDTSectionConfig<FrameworkDataType>
>;

export interface MLDTDataset<FrameworkDataType> {
  headerLabel: string;
  dataset: FrameworkDataType;
}

export type BadgeColors = "yellow" | "green" | "red" | "blue" | "purple" | "gray" | "brown" | "orange";

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
  labelBadgeColor?: BadgeColors;
  expandOnPageLoad: boolean;
  shouldDisplay: (dataset: FrameworkDataType) => boolean;
  children: MLDTConfig<FrameworkDataType>;
}

/**
 * Checks if the specified MLDT-Element should be displayed given the provided datasets.
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
 * Checks if the specified MLDT-Cell should be displayed given the provided datasets.
 * @param cellConfig The cell to check
 * @param datasets The datasets to use during the check
 * @returns true iff the cell should be displayed
 */
function isCellRowVisible<FrameworkDataType>(
  cellConfig: MLDTCellConfig<FrameworkDataType>,
  datasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  for (const datasetEntry of datasets) {
    if (cellConfig.shouldDisplay(datasetEntry.dataset)) {
      return true;
    }
  }
  return false;
}

/**
 * Checks if the specified MLDT-Section should be displayed given the provided datasets.
 * @param sectionConfig The section to check
 * @param datasets The datasets to use during the check
 * @returns true iff the section should be displayed
 */
function isCellSectionVisible<FrameworkDataType>(
  sectionConfig: MLDTSectionConfig<FrameworkDataType>,
  datasets: Array<MLDTDataset<FrameworkDataType>>,
): boolean {
  let displaySectionPrimer = false;
  for (const datasetElement of datasets) {
    if (sectionConfig.shouldDisplay(datasetElement.dataset)) {
      displaySectionPrimer = true;
      break;
    }
  }

  if (!displaySectionPrimer) return false;

  for (const child of sectionConfig.children) {
    if (isElementVisible(child, datasets)) {
      return true;
    }
  }
  return false;
}
