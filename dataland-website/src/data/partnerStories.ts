export interface PartnerStorySummary {
  slug: string;
  name: string;
  logo: string;
  logoClassName?: string;
  title: string;
  primaryTag: string;
  secondaryTags: string[];
  text: string;
  link: string;
  externalUrl?: string;
  darkLogoFrame?: boolean;
}

export interface PartnerStoryDetail {
  slug: string;
  name: string;
  logo: string;
  logoClassName?: string;
  heroTitle: string;
  intro: string[];
  keyUseCases: {
    title: string;
    summary: string;
    detail: string;
  }[];
  benefits: string[];
}

export const PARTNER_STORY_SUMMARIES: PartnerStorySummary[] = [
  {
    slug: 'fact-first-cloud',
    name: 'FACT First Cloud',
    logo: '/static/about/logo-fact-first-cloud.svg',
    title: 'Automating ESG reporting workflows with integrated Dataland data',
    primaryTag: 'Software partner',
    secondaryTags: ['REST API', 'Reg reporting', 'Audit trail'],
    text: 'Dataland ESG data flows directly into First Cloud and becomes the basis for regulatory calculations, reporting, and traceable downstream processes.',
    link: '/partner-stories#fact-first-cloud',
  },
  {
    slug: 'iss-sopra-steria',
    name: 'ISS (Sopra Steria)',
    logo: '/static/about/logo-sopra-steria.png',
    title: 'Partner profile',
    primaryTag: 'Technology partner',
    secondaryTags: ['Integration', 'Workflow'],
    text: 'Dataland works with ISS (Sopra Steria) to extend structured ESG data into partner-led implementation and delivery contexts.',
    link: '/partner-stories#partner-directory',
    externalUrl: 'https://iss.soprasteria.de/',
  },
  {
    slug: 'eskua-ai',
    name: 'Eskua AI',
    logo: '/static/about/logo-eskua-ai.png',
    title: 'Partner profile',
    primaryTag: 'AI partner',
    secondaryTags: ['Data access', 'Enablement'],
    text: 'Eskua AI is part of the Dataland partner network and helps broaden how structured sustainability data can be operationalized.',
    link: '/partner-stories#partner-directory',
    externalUrl: 'https://www.eskua.ai/',
  },
  {
    slug: 'keynum',
    name: 'Keynum',
    logo: '/static/about/logo-keynum.webp',
    title: 'Partner profile',
    primaryTag: 'Data partner',
    secondaryTags: ['Platform', 'Integration'],
    text: 'Keynum complements the Dataland ecosystem with partner capabilities around accessing and embedding sustainability information.',
    link: '/partner-stories#partner-directory',
    externalUrl: 'https://www.keynum.com/',
    darkLogoFrame: true,
  },
];

export const PARTNER_STORY_DETAILS: PartnerStoryDetail[] = [
  {
    slug: 'fact-first-cloud',
    name: 'FACT First Cloud',
    logo: '/static/about/logo-fact-first-cloud.svg',
    heroTitle: 'FACT First Cloud integrates Dataland ESG data into a central reporting platform',
    intro: [
      'First Cloud is a central platform for managing investments and processing financial and sustainability data.',
      'Through the partnership with FACT, Dataland ESG data is automatically integrated into First Cloud, where it becomes a consistent data foundation for downstream processes.',
      'This enables guided regulatory ESG reporting based on portfolio data and integrated Dataland data for calculations, analyses, and reporting.',
    ],
    keyUseCases: [
      {
        title: 'Regulatory reporting integration',
        summary: 'Regulatory ESG requirements are embedded directly into existing reporting processes.',
        detail:
          'First Cloud uses integrated Dataland data to determine PAI indicators under the RTS and to prepare taxonomy reports aligned with the relevant reporting templates. Automated transfer into standard reports reduces manual effort and improves result consistency.',
      },
      {
        title: 'ESG data processing and KPI calculation',
        summary: 'Regulatory ESG metrics are calculated from a centralized, consistent data inventory.',
        detail:
          'The ESG data integrated into First Cloud forms the basis for automated KPI calculation and further report preparation. A central database ensures calculations stay consistent and current.',
      },
      {
        title: 'PCAF-based emissions calculation',
        summary: 'Greenhouse gas emissions are calculated in line with the PCAF standard.',
        detail:
          'First Cloud determines attribution factors based on PCAF aggregation methods and automatically calculates emissions depending on the source data available, enabling a standardized and traceable methodology.',
      },
      {
        title: 'Automated ESG data integration',
        summary: 'ESG information is imported and refreshed automatically through Dataland’s REST API.',
        detail:
          'The data is immediately available for subsequent calculations and workflows without manual intermediate steps, which shortens processing time and lowers operational friction.',
      },
      {
        title: 'Data transparency and traceability',
        summary: 'Every ESG data point and calculation step remains auditable.',
        detail:
          'First Cloud preserves end-to-end traceability down to individual records, allowing auditors, regulators, and other stakeholders to review how results were produced.',
      },
    ],
    benefits: [
      'Automated ESG data integration through REST API without manual intermediate steps',
      'End-to-end processing from the database to the final regulatory report',
      'A centralized data foundation for ESG-related workflows',
      'Standardized, traceable calculation of metrics including PAI, taxonomy, and PCAF',
      'Full transparency and auditability down to the individual data level',
    ],
  },
];
