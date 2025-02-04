import { DataTypeEnum } from '@clients/backend';
import type { DataPointDisplay } from '@/utils/DataPoint.ts';

/**
 * This function checks if a framework is private
 * @param framework the framework to check
 * @returns true if the framework is private, false otherwise
 */
export function isFrameworkPrivate(framework: DataTypeEnum): boolean {
  return framework == DataTypeEnum.Vsme;
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
 * @param framework the framework to check
 * @returns true if the framework is editable, false otherwise
 */
export function isFrameworkEditable(framework: DataTypeEnum): boolean {
  return framework != DataTypeEnum.Vsme;
}

export interface DataPointDataTableRefProps {
  dataPointDisplay: DataPointDisplay;
  dataId?: string;
  dataType?: string;
}
