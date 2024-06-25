import {
  type MLDTCellConfig,
  type MLDTConfig,
  type MLDTSectionConfig,
} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import {
  type AvailableMLDTDisplayObjectTypes,
  MLDTDisplayComponentName,
  type MLDTDisplayComponentTypes,
} from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import { NO_DATA_PROVIDED } from '@/utils/Constants';
/**
 * For QA it is desirable that all fields are displayed to a reviewer even if they should normally not be visible.
 * This function edits a standard view-configuration in a way that it displays all cells but highlights cells that would
 * not be displayed to a user on the view-page.
 * @param config the input view configuration
 * @param inReviewMode Specifies whether we are in reviewMode
 * @param hideEmptyFields Specifies whether empty fields should be visible at the beginning
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the section or
 * cell that this function is currently looking at
 * @returns the modified hidden-highlighting view-config
 */
export function editMultiLayerDataTableConfigForHighlightingHiddenFields<T>(
  config: MLDTConfig<T>,
  inReviewMode: boolean,
  hideEmptyFields: boolean,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>
): MLDTConfig<T> {
  return config.map((cellOrSectionConfig) => {
    if (cellOrSectionConfig.type == 'cell') {
      return editCellConfigForHighlightingHiddenFields(
        cellOrSectionConfig,
        inReviewMode,
        hideEmptyFields,
        displayStatusGettersOfAllParents
      );
    } else {
      return editSectionConfigForHighlightingHiddenFields(
        cellOrSectionConfig,
        inReviewMode,
        hideEmptyFields,
        displayStatusGettersOfAllParents
      );
    }
  });
}

/**
 * Edits a single section (and all it's children) to the show-always directive
 * @param sectionConfig the section config to convert
 * @param inReviewMode Specifies whether we are in reviewMode
 * @param hideEmptyFields Specifies whether empty fields should be visible at the beginning
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the section that
 * this function is currently looking at
 * @returns the modified section config
 */
function editSectionConfigForHighlightingHiddenFields<T>(
  sectionConfig: MLDTSectionConfig<T>,
  inReviewMode: boolean,
  hideEmptyFields: boolean,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>
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
      inReviewMode,
      hideEmptyFields,
      displayStatusGettersToPassDownToChildren
    ),
    areThisSectionAndAllParentSectionsDisplayedForTheDataset: areThisSectionAndAllParentSectionsDisplayedForTheDataset,
  };
}

/**
 * Edits a single cell to the show-always directive
 * @param cellConfig the cell config to convert
 * @param inReviewMode Specifies whether we are in reviewMode
 * @param hideEmptyFields Specifies whether empty fields should be visible at the beginning
 * @param displayStatusGettersOfAllParents a list of all the showDisplay-functions of the parents of the cell that this
 * function is currently looking at
 * @returns the modified cell config
 */
function editCellConfigForHighlightingHiddenFields<T>(
  cellConfig: MLDTCellConfig<T>,
  inReviewMode: boolean,
  hideEmptyFields: boolean,
  displayStatusGettersOfAllParents?: Array<(dataset: T) => boolean>
): MLDTCellConfig<T> {
  const cellHasAtLeastOneParent = !!displayStatusGettersOfAllParents && displayStatusGettersOfAllParents.length > 0;
  return {
    ...cellConfig,
    shouldDisplay: (dataset: T): boolean => {
      return hideEmptyFields
        ? cellConfig.shouldDisplay(dataset) && checkShouldValueBeDisplayed(cellConfig.valueGetter(dataset).displayValue)
        : inReviewMode || cellConfig.shouldDisplay(dataset);
    },
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
      if (
        areAllParentSectionsDisplayed() &&
        cellConfig.shouldDisplay(dataset) &&
        checkShouldValueBeDisplayed(cellConfig.valueGetter(dataset).displayValue)
      ) {
        return originalDisplayValue;
      } else {
        return {
          displayComponentName: MLDTDisplayComponentName.HighlightHiddenCellDisplay,
          displayValue: {
            innerContents: originalDisplayValue,
          },
        };
      }
    },
  };
}

/**
 * Checks if fields with null values should be shown or not
 * @param value This is the displayValue parsed from the field config
 * @returns boolean to set hidden to true or false
 */
function checkShouldValueBeDisplayed(value: MLDTDisplayComponentTypes[MLDTDisplayComponentName]): boolean {
  switch (typeof value) {
    case 'string':
      return !!(value && value != NO_DATA_PROVIDED);
    case 'object':
      /* eslint-disable @typescript-eslint/no-unsafe-member-access */
      if (!!value && 'modalOptions' in value) {
        return !!(
          // prettier-ignore
          (value.modalOptions?.data?.listOfRowContents?.length ||
            value.modalOptions?.data?.input ||
            value.modalOptions?.data?.values?.length)
        );
      } else if (!!value && 'innerContents' in value) {
        return value.innerContents.displayValue != NO_DATA_PROVIDED && value.innerContents.displayValue != '';
      } else {
        return !!value;
      }
    /* eslint-enable @typescript-eslint/no-unsafe-member-access */
    default:
      return !!value;
  }
}
