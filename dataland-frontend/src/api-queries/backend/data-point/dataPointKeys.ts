export const dataPointKeys = {
  all: ['dataPoint'] as const,
  ByDataPointId: (dataPointId: string | undefined) => ['dataPoint', 'byDataPointId', dataPointId] as const,
};
