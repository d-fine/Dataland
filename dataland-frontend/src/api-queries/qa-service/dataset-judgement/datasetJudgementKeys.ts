export const datasetJudgementKeys = {
  all: ['qaReviewResponse'] as const,
  detail: (datasetReviewId: string | undefined) => ['qaReviewResponse', 'detail', datasetReviewId] as const,
};
