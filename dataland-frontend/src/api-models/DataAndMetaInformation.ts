import { type DataMetaInformation } from '@clients/backend';

/**
 * A clone of the generic DataAndMetaInformation interface from the backend.
 */
export interface DataAndMetaInformation<T> {
  metaInfo: DataMetaInformation;
  data: T;
}
