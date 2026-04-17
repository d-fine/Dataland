export interface ProblemSolutionPair {
  problemTitle: string;
  problemText: string;
  solutionTitle: string;
  solutionText: string;
}

export interface FrameworkCard {
  title: string;
  subtitle: string;
  description: string;
}

export interface CustomerStorySummary {
  logo: string;
  logoClassName?: string;
  title: string;
  primaryTag: string;
  secondaryTags: string[];
  text: string;
  link: string;
}

export const INDEX_PAGE_CONTENT = {
  meta: { title: 'Dataland - Non-profit sustainability data' },
  hero: {
    ariaLabel: 'The open-source platform for structured sustainability data',
    titleLeading: 'Open-source platform for',
    titleAccent: 'structured sustainability data',
    strapline: 'Non-profit. Source-based. Unrestricted use.',
    stats: [
      { value: '1,000', label: 'Users' },
      { value: '3,000', label: 'Datasets' },
      { value: '300,000', label: 'Live data points' },
    ],
    buttons: { try: 'Try it free', more: 'More about us', contact: 'Get in touch' },
  },
  search: { title: 'Search sustainability data by company name or LEI' },
  whyUs: {
    title: 'Why financial institutions choose Dataland',
    intro: 'It comes down to how we solve common challenges in accessing, using, and sourcing ESG data.',
    buttons: { features: 'Discover platform features', useCases: 'Explore use cases', contact: 'Get in touch' },
  },
  trustedBy: {
    title: 'Trusted by',
    intro: 'A growing network of members, partners, and sponsors across Europe.',
  },
  customerStories: {
    title: 'How our members use Dataland',
    intro: 'A number of ESG data challenges are addressed across use cases in reporting, risk, and compliance.',
    readMore: 'Read more',
  },
  testimonials: {
    title: 'What our members say',
    intro: 'Insights from members, partners, and supporters based on their experience.',
    watch: 'Watch member testimonials',
  },
  frameworks: {
    title: 'Frameworks on Dataland',
    intro: 'The platform covers the ESG frameworks most relevant to European financial institutions.',
    cta: 'Explore how companies and investors report under these frameworks',
  },
  cta: { title: 'Ready to get started?', buttons: { contact: 'Get in touch', try: 'Try it free' } },
} as const;

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
    solutionTitle: 'AI extraction, human verification, source traceability',
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

export const FRAMEWORK_CARDS: FrameworkCard[] = [
  { title: 'EU Taxonomy', subtitle: 'Financials', description: 'The EU Taxonomy Regulation enables financial institutions to assess and report the share of environmentally sustainable economic activities within their portfolios, based on eligibility and alignment metrics.' },
  { title: 'EU Taxonomy', subtitle: 'Non-Financials', description: 'The EU Taxonomy Regulation provides a framework for non-financial companies to disclose the extent to which their activities are environmentally sustainable, based on defined technical screening criteria.' },
  { title: 'EU Taxonomy', subtitle: 'Nuclear and Gas', description: 'The EU Taxonomy includes specific criteria for nuclear and gas activities under transitional provisions, allowing companies to report their contribution to climate objectives under defined conditions.' },
  { title: 'SFDR', subtitle: '', description: 'The Sustainable Finance Disclosure Regulation requires financial market participants to disclose how sustainability risks are integrated into investment decisions and to report Principal Adverse Impact indicators at entity and product level.' },
  { title: 'PCAF', subtitle: '', description: 'The PCAF standard provides a methodology for financial institutions to measure and disclose financed emissions associated with their lending and investment portfolios.' },
  { title: 'LkSG', subtitle: '', description: 'Lieferkettensorgfaltspflichtengesetz is a German law requiring companies to identify, assess, and manage human rights and environmental risks within their supply chains.' },
];

export const CUSTOMER_STORY_SUMMARIES: CustomerStorySummary[] = [
  { logo: '/static/logos/logo_meag_2026.svg', logoClassName: 'scale-[1.18]', title: 'Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition', primaryTag: 'Asset manager', secondaryTags: ['SFDR', 'EU Taxo', 'Audit'], text: 'Filling SFDR gaps and EU Taxo template transition', link: '/product#meag' },
  { logo: '/static/logos/logo_nordlb.svg', title: 'Primary source of EU Taxonomy data with automated delivery', primaryTag: 'Bank', secondaryTags: ['EU Taxo', 'API integration'], text: 'Primary source of EU Taxonomy data with automated delivery', link: '/product#nordlb' },
  { logo: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.png', logoClassName: 'scale-[1.3]', title: 'Using Dataland as an Independent Source to Validate PAI Data for Audit', primaryTag: 'Insurance', secondaryTags: ['SFDR', 'Validation', 'Audit'], text: 'PAI lineage and source transparency for compliance', link: '/product#ovbraunschweig' },
];
