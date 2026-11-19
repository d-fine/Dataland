export const datasetJudgementKeys = {
  all: ['datasetJudgement'] as const,
  detail: (datasetJudgementId: string | undefined) =>
    [...datasetJudgementKeys.all, 'detail', datasetJudgementId] as const,
};
