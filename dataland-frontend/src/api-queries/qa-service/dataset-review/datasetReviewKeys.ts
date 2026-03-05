export const datasetReviewKeys = {
  all: ['qaReviewResponse'] as const,
  listByDataId: (dataId: string | undefined) => ['qaReviewResponse', 'byDataId', dataId] as const,
  detail: (datasetReviewId: string | undefined) => ['qaReviewResponse', 'detail', datasetReviewId] as const,
};
