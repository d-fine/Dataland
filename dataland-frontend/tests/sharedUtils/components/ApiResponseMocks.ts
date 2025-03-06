import { type FrameworkData } from '@/utils/GenericFrameworkTypes.ts';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation.ts';
import { type DataMetaInformation } from '@clients/backend';

/**
 * Create a DataAndMetaInformation object from FrameworkData and DataMetaInformation
 * @param metaInfo the prepared dataMetaInformation mock
 * @param dataset the dataset of type FrameworkData
 * @returns a mocked DataAndMetaInformation object
 */
export function buildDataAndMetaInformationMock<T extends FrameworkData>(
  metaInfo: DataMetaInformation,
  dataset: T
): DataAndMetaInformation<T> {
  return { metaInfo: structuredClone(metaInfo), data: structuredClone(dataset) };
}
