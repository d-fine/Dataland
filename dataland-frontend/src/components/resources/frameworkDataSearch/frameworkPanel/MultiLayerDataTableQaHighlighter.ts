import {
  type MLDTCellConfig,
  type MLDTConfig,
  type MLDTSectionConfig,
} from "@/components/resources/dataTable/MultiLayerDataTableConfiguration";
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
} from "@/components/resources/dataTable/MultiLayerDataTableCellDisplayer";
/**
 * For QA it is desirable that all fields are displayed to a reviewer even if they should normally not be visible.
 * This function edits a standard view-configuration in a way that it displays all cells but highlights cells that would
 * not be displayed to a user on the view-page.
 * @param config the input view configuration
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the section or
 * cell that this function is currently looking at
 * @returns the modified hidden-highlighting view-config
 */
export function editMultiLayerDataTableConfigForHighlightingHiddenFields<T>(
  config: MLDTConfig<T>,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>,
): MLDTConfig<T> {
  return config.map((cellOrSectionConfig) => {
    if (cellOrSectionConfig.type == "cell")
      return editCellConfigForHighlightingHiddenFields(cellOrSectionConfig, displayStatusGettersOfAllParents);
    else return editSectionConfigForHighlightingHiddenFields(cellOrSectionConfig, displayStatusGettersOfAllParents);
  });
}

/**
 * Edits a single section (and all it's children) to the show-always directive
 * @param sectionConfig the section config to convert
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the section that
 * this function is currently looking at
 * @returns the modified section config
 */
function editSectionConfigForHighlightingHiddenFields<T>(
  sectionConfig: MLDTSectionConfig<T>,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>,
): MLDTSectionConfig<T> {
  const displayStatusGetterOfThisSection = sectionConfig.shouldDisplay;
  const displayStatusGettersToPassDownToChildren = ((): Array<(dataset: T) => boolean> => {
    if (!displayStatusGettersOfAllParents) {
      return [displayStatusGetterOfThisSection];
    } else {
      displayStatusGettersOfAllParents.push(displayStatusGetterOfThisSection);
      return displayStatusGettersOfAllParents;
    }
  })();

  const sectionlHasAtLeastOneParent = !!displayStatusGettersOfAllParents && displayStatusGettersOfAllParents.length > 0;
  const areThisSectionAndAllParentSectionsDisplayedForTheDataset = (dataset: T): boolean => {
    if (!sectionConfig.shouldDisplay(dataset)) {
      return false;
    } else if (sectionlHasAtLeastOneParent) {
      for (const showDisplay of displayStatusGettersOfAllParents) {
        if (!showDisplay(dataset)) {
          return false;
        }
      }
      return true;
    }
    return true;
  };

  return {
    ...sectionConfig,
    shouldDisplay: () => true,
    children: editMultiLayerDataTableConfigForHighlightingHiddenFields(
      sectionConfig.children,
      displayStatusGettersToPassDownToChildren,
    ),
    areThisSectionAndAllParentSectionsDisplayedForTheDataset: areThisSectionAndAllParentSectionsDisplayedForTheDataset, // TODO needs to be dynamically calculated
  };
}

/**
 * Edits a single cell to the show-always directive
 * @param cellConfig the cell config to convert
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the cell that this
 * function is currently looking at
 * @returns the modified cell config
 */
function editCellConfigForHighlightingHiddenFields<T>(
  cellConfig: MLDTCellConfig<T>,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>,
): MLDTCellConfig<T> {
  const cellHasAtLeastOneParent = !!displayStatusGettersOfAllParents && displayStatusGettersOfAllParents.length > 0;
  return {
    ...cellConfig,
    shouldDisplay: () => true,
    valueGetter: (dataset: T): AvailableMLDTDisplayObjectTypes => {
      const originalDisplayValue = cellConfig.valueGetter(dataset);
      const areAllParentSectionsDisplayed = (): boolean => {
        if (!cellHasAtLeastOneParent) {
          return true;
        } else {
          for (const showDisplay of displayStatusGettersOfAllParents) {
            if (!showDisplay(dataset)) {
              return false;
            }
          }
          return true;
        }
      };
      if (areAllParentSectionsDisplayed() && cellConfig.shouldDisplay(dataset)) {
        return originalDisplayValue;
      } else {
        return {
          displayComponentName: MLDTDisplayComponentName.HighlightHiddenCellDisplayComponent,
          displayValue: {
            innerContents: originalDisplayValue,
          },
        };
      }
    },
  };
}
