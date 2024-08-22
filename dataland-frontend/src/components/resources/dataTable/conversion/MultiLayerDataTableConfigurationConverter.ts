import { type Category, type Subcategory } from '@/utils/GenericFrameworkTypes';
import {
  type BadgeColor,
  type MLDTConfig,
  type MLDTSectionConfig,
} from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { getDataModelFieldCellConfig } from '@/components/resources/dataTable/conversion/MultiLayerDataTableFieldConverter';

// The effort of making this file type-safe greatly outweighs the benefit.
/* eslint @typescript-eslint/no-explicit-any: 0 */

const autoExpandingCategoryNames = new Set([
  'general',
  'masterData',
  'governance',
  'climateTargets',
  'emissionsPlanning',
  'investmentPlanning',
  'ammonia',
  'automotive',
  'hvcPlastics',
  'commercialRealEstate',
  'residentialRealEstate',
  'steel',
  'freightTransportByRoad',
  'electricityGeneration',
  'livestockFarming',
  'cement',
  'decarbonisation',
  'defossilisation',
  'energy',
  'technologyValueCreation',
  'materials',
  'recycling',
  'buildingEfficiency',
  'energySource',
  'technology',
  'emissionsFromManureAndFertiliserAndLivestock',
  'animalWelfare',
  'animalFeed',
  'externalFeedCertification',
  'material',
]);

/**
 * Converts a Data-Model-Ts Category to a MLDTSectionConfig
 * @param category the category to convert
 * @returns the converted category
 */
function convertCategoryToMLDTSectionConfig(category: Category): MLDTSectionConfig<any> {
  const mldtCategoryChildren: MLDTConfig<any> = category.subcategories.map((subcategory) =>
    convertSubCategoryToMLDTSectionConfig(category, subcategory)
  );

  return {
    type: 'section',
    label: category.label,
    expandOnPageLoad: autoExpandingCategoryNames.has(category.name),
    children: mldtCategoryChildren,
    shouldDisplay: category.showIf,
    labelBadgeColor: category.color as BadgeColor,
  };
}

/**
 * Converts a Data-Model-Ts Subcategory to a MLDTSectionConfig
 * @param category the parent category
 * @param subcategory the subcategory to convert
 * @returns the converted subcategory
 */
function convertSubCategoryToMLDTSectionConfig(category: Category, subcategory: Subcategory): MLDTSectionConfig<any> {
  const mldtSubcategoryChildren: MLDTConfig<any> = [];

  for (const field of subcategory.fields) {
    const cellConfig = getDataModelFieldCellConfig(category.name + '.' + subcategory.name + '.' + field.name, field);
    if (cellConfig) {
      mldtSubcategoryChildren.push(cellConfig);
    }
  }

  return {
    type: 'section',
    label: subcategory.label,
    expandOnPageLoad: autoExpandingCategoryNames.has(subcategory.name),
    children: mldtSubcategoryChildren,
    shouldDisplay: () => true,
  };
}

/**
 * Converts a Data-Model-Ts DataModel to a Multi-Layer-Data-Table Display configuration
 * @param dataModel the data model to convert
 * @returns the view configuration
 */
export function convertDataModelToMLDTConfig(dataModel: Array<Category>): MLDTConfig<any> {
  return dataModel.map((category) => convertCategoryToMLDTSectionConfig(category));
}
