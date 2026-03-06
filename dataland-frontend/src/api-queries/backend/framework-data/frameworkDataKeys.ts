export const frameworkDataKeys = {
  all: ['frameworkData'] as const,
  byFrameworkAndId: (framework: string | undefined, dataId: string | undefined) =>
    ['frameworkData', framework, dataId] as const,
};
