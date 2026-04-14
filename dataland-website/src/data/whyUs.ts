export interface ProblemSolutionPair {
  problemTitle: string;
  problemText: string;
  solutionTitle: string;
  solutionText: string;
}

export const WHY_US_PAIRS: ProblemSolutionPair[] = [
  {
    problemTitle: 'Missing issuer data',
    problemText:
      'Large ESG data providers typically focus on listed companies, leaving smaller, regional, or unlisted issuers outside their standard coverage. Data consumers must then identify, source, and structure the missing data themselves.',
    solutionTitle: 'Data on demand',
    solutionText:
      'Dataland provides the data its members actually need. If a required dataset is missing, members can request it. The data will be sourced from issuer disclosures and added to the platform, so gaps in coverage can be addressed when they arise.',
  },
  {
    problemTitle: 'Poor data quality',
    problemText:
      'Many data sourcing approaches introduce errors, inconsistencies, outdated values, or unexplained gaps. Inaccurate or untraceable ESG data undermines reporting, analytics, and decision-making.',
    solutionTitle: 'AI extraction, human verification, source traceability.',
    solutionText:
      'Dataland sources data from original publishers and combines tailored AI extraction with manual verification. Every published data point is linked to its original document, ensuring structured, quality-assured, and fully traceable datasets.',
  },
  {
    problemTitle: 'Restrictive licensing terms',
    problemText:
      'Acquired datasets are often subject to restrictive usage rights, limiting how they can be applied across reporting, analysis, validation, and other internal workflows. This reduces the practical value of the data far beyond the original use case.',
    solutionTitle: 'Unrestricted use',
    solutionText:
      'Dataland data can be used freely and published freely. This allows the same dataset to support multiple teams and workflows without unnecessary licensing constraints.',
  },
  {
    problemTitle: 'High prices',
    problemText:
      'Many providers offer expensive data packages that are not well aligned with the actual needs of the data consumer. Institutions often end up paying for broad coverage, bundled content, or additional functionality that is irrelevant to their use case.',
    solutionTitle: 'Lean pricing model',
    solutionText:
      'Dataland follows a shared procurement model in which pricing reflects the effort required to source a dataset. The costs of that sourcing effort are shared across the members who need the data rather than being borne by each institution individually.',
  },
];
