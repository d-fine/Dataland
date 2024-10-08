import { type BaseDocumentReference, type ExtendedDocumentReference, type QualityOptions } from '@clients/backend';
import { type ObjectType } from '@/utils/UpdateObjectUtils';

export interface ExtendedDataPoint<T> {
  value?: T | null;
  dataSource?: ExtendedDocumentReference | null;
  quality?: QualityOptions | null;
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
 * Creates a list of the names of all Kpis that have some value
 * @param dataResponseData Data to prefill upload form
 * @returns array of Kpis names that have some value
 */
export function getFilledKpis(dataResponseData: object): string[] {
  const listOfFilledKpis: string[] = [];
  Object.values(dataResponseData).forEach((category) => {
    if (category && typeof category === 'object') {
      Object.values(category as ObjectType).forEach((subCategory) => {
        if (subCategory && typeof subCategory === 'object') {
          Object.keys(subCategory).forEach((kpi) => {
            if (subCategory[kpi as keyof typeof subCategory]) {
              listOfFilledKpis.push(kpi);
            }
          });
        }
      });
    }
  });
  return listOfFilledKpis;
}
