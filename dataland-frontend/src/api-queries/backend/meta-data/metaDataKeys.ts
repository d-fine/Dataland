export const metaDataKeys = {
  all: ['metaData'] as const,
  listByDataId: (dataId: string | undefined) => ['metaData', 'byDataId', dataId] as const,
};
