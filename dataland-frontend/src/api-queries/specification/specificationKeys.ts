export const specificationKeys = {
  all: ['specification'] as const,
  frameworkList: () => ['specification', 'frameworks'] as const,
  framework: (frameworkId: string | undefined) => ['specification', 'frameworks', frameworkId] as const,
  frameworkExists: (frameworkId: string | undefined) => ['specification', 'frameworks', frameworkId, 'exists'] as const,
  dataPointType: (dataPointTypeId: string | undefined) => ['specification', 'dataPointTypes', dataPointTypeId] as const,
  dataPointBaseType: (dataPointBaseTypeId: string | undefined) =>
    ['specification', 'dataPointBaseTypes', dataPointBaseTypeId] as const,
};
