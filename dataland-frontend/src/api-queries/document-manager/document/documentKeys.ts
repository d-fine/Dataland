import { type SearchForDocumentMetaInformationDocumentCategoriesEnum } from '@clients/documentmanager';

export interface DocumentMetaInformationSearchFilter {
  companyId?: string;
  documentCategories?: SearchForDocumentMetaInformationDocumentCategoriesEnum[];
  reportingPeriod?: string;
  chunkSize?: number;
  chunkIndex?: number;
}

export const documentKeys = {
  all: ['document'] as const,
  search: (filter: DocumentMetaInformationSearchFilter) =>
    [
      ...documentKeys.all,
      'search',
      { ...filter, documentCategories: filter.documentCategories ? [...filter.documentCategories].sort() : undefined },
    ] as const,
};
