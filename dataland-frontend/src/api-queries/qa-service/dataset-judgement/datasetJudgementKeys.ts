export const datasetJudgementKeys = {
  all: ['qaJudgementResponse'] as const,
  detail: (datasetJudgementId: string | undefined) => ['qaJudgementResponse', 'detail', datasetJudgementId] as const,
};
