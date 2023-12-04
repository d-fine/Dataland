import { type BaseDocumentReference, type ExtendedDocumentReference, type QualityOptions } from "@clients/backend";
import { type ObjectType } from "@/utils/UpdateObjectUtils";

export interface ExtendedDataPoint<T> {
  value?: T | null;
  dataSource?: ExtendedDocumentReference | null;
  quality: QualityOptions;
  comment?: string | null;
}

export interface BaseDataPoint<T> {
  value: T;
  dataSource?: BaseDocumentReference | null;
}

export type DataPointDisplay = {
  value: string;
  quality?: string;
  dataSource?: ExtendedDocumentReference | BaseDocumentReference;
  comment?: string;
};

/**
 * Checks if a potential data point has a value that is neither undefined nor
 * @param dataPoint the potential data point
 * @param dataPoint.value the value of the potential data point
 * @returns the result of the evaluation
 */
export function hasDataPointProperValue(dataPoint: { value: unknown }): boolean {
  return dataPoint.value != undefined && dataPoint.value != "";
}

/**
 * Creates a list of the names of all Kpis that have some value
 * @param dataResponseData Data to prefill upload form
 * @returns array of Kpis names that have some value
 */
export function getFilledKpis(dataResponseData: object): string[] {
  const listOfFilledKpis = (dataResponseData: object): Array<string> => {
    return Object.values(dataResponseData).flatMap((category) =>
      Object.values(category as ObjectType).flatMap((subCategory) =>
        Object.keys(subCategory).filter((kpi) => subCategory[kpi as keyof typeof subCategory]),
      ),
    );
  };
  return listOfFilledKpis(dataResponseData);
}
