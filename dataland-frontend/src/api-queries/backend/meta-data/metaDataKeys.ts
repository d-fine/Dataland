import { type DataMetaInformationSearchFilter } from '@clients/backend';

export const metaDataKeys = {
  all: ['metaData'] as const,
  listByDataId: (dataId: string | undefined) => ['metaData', 'byDataId', dataId] as const,
  search: (filters: DataMetaInformationSearchFilter) => ['metaData', 'search', filters] as const,
};
