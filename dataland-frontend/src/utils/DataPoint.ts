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
  for (const category of Object.values(dataResponseData)) {
    if (category && typeof category === 'object') {
      for (const subCategory of Object.values(category as ObjectType)) {
        if (subCategory && typeof subCategory === 'object') {
          for (const kpi of Object.keys(subCategory)) {
            if (subCategory[kpi as keyof typeof subCategory]) {
              listOfFilledKpis.push(kpi);
            }
          }
        }
      }
    }
  }
  return listOfFilledKpis;
}
