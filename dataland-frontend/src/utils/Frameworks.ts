import { DataTypeEnum } from '@clients/backend';
import type { DataPointDisplay } from '@/utils/DataPoint.ts';

/**
 * This function checks if a framework is private
 * @returns true if the framework is private, false otherwise
 */
export function isFrameworkPrivate(): boolean {
  return false;
}

/**
 * This function checks if a framework is public
 * @param framework the framework to check
 * @returns true if the framework is public, false otherwise
 */
export function isFrameworkPublic(framework: DataTypeEnum): boolean {
  return !isFrameworkPrivate(framework);
}

/**
 * This function checks if a framework is editable
 * @returns true if the framework is editable, false otherwise
 */
export function isFrameworkEditable(): boolean {
  return true;
}

export interface DataPointDataTableRefProps {
  dataPointDisplay: DataPointDisplay;
  dataId?: string;
  dataType?: string;
}

export const EU_TAXONOMY_FRAMEWORK_FAMILY: DataTypeEnum[] = [
  DataTypeEnum.EutaxonomyFinancials,
  DataTypeEnum.EutaxonomyFinancials202673,
  DataTypeEnum.EutaxonomyNonFinancials,
  DataTypeEnum.EutaxonomyNonFinancials202673,
];
