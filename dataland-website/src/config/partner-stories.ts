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

export interface PartnerStoryUseCase {
  title: string;
  lines: string[];
  icon: string;
}

export interface PartnerStoryExtraSection {
  title: string;
  items: {
    title: string;
    text: string;
  }[];
}

export interface PartnerStoryDetail {
  slug: string;
  name: string;
  logo: string;
  logoClassName?: string;
  darkLogoFrame?: boolean;
  heroTitle: string;
  primaryTag: string;
  secondaryTags: string[];
  intro: string[];
  keyUseCases: PartnerStoryUseCase[];
  benefits: string[];
  extraSections?: PartnerStoryExtraSection[];
  closingPrompt?: string;
  closingCtaLabel?: string;
  externalUrl?: string;
}

export const PARTNER_STORY_SUMMARIES: PartnerStorySummary[] = [
  {
    slug: 'fact-first-cloud',
    name: 'Fact \u2013 First Cloud',
    logo: '/static/logos/logo_fact_Salbei.svg',
    logoClassName: 'scale-[1.9] lg:scale-[2.15]',
    title: 'Fact \u2013 First Cloud',
    primaryTag: 'Integration partner',
    secondaryTags: ['ESG reporting', 'REST API'],
    text: 'First Cloud is a central platform for managing investments and processing financial and sustainability data.',
    link: '/partner-stories#fact-first-cloud',
    externalUrl: 'https://www.fact.de/unsere-loesungen/first-cloud/',
  },
  {
    slug: 'iss-sopra-steria',
    name: 'ISS Sopra Steria',
    logo: '/static/logos/logo_iss-soprasteria.png',
    logoClassName: 'scale-[1.25] lg:scale-[1.4]',
    title: 'ISS Sopra Steria',
    primaryTag: 'Integration partner',
    secondaryTags: ['ESG solutions', 'Platform workflows'],
    text: 'ISS (a Sopra Steria company) provides comprehensive ESG and sustainable investment solutions.',
    link: '/partner-stories#iss-sopra-steria',
    externalUrl: 'https://iss.soprasteria.de/',
  },
];

export const PARTNER_STORY_DETAILS: PartnerStoryDetail[] = [
  {
    slug: 'fact-first-cloud',
    name: 'Fact \u2013 First Cloud',
    logo: '/static/logos/logo_fact_Salbei.svg',
    logoClassName: 'scale-[1.7] lg:scale-[1.95]',
    heroTitle: 'Fact \u2013 First Cloud',
    primaryTag: 'Integration partner',
    secondaryTags: ['ESG reporting', 'REST API'],
    intro: [
      'First Cloud is a central platform for managing investments and processing financial and sustainability data.',
      'Through the partnership with Fact, Dataland\u2019s ESG data is automatically integrated into First Cloud, where it serves as a consistent data foundation for all subsequent processes.',
      'First Cloud enables the creation of regulatory ESG reports through a guided, interactive process. This is based on the portfolio data of the investments as well as the integrated ESG data from Dataland, which together form the data foundation for calculations, analyses, and reporting.',
    ],
    keyUseCases: [
      {
        title: '1. Regulatory Reporting Integration',
        icon: 'report',
        lines: [
          'First Cloud enables the creation of regulatory ESG reports based on the integrated Dataland data.',
          'Based on the provided ESG data, the principal adverse impacts (PAI indicators) are determined in accordance with the regulatory technical standards (RTS) and transferred to the corresponding standard reports (Tables 1\u20133).',
          'In addition, First Cloud calculates the taxonomy metrics for the preparation of taxonomy reports in accordance with the relevant reporting templates (Annex X and Annex XII).',
          'The automated calculation and transfer into the reports reduces manual effort and increases the consistency of the results.',
        ],
      },
      {
        title: '2. ESG Data Processing & KPI Calculation',
        icon: 'kpi',
        lines: [
          'The ESG data integrated into First Cloud forms the basis for the calculation of regulatory metrics.',
          'Based on the data inventory, ESG metrics are automatically calculated and made available for further processing and for the creation of reports.',
          'The central database ensures that all calculations are based on consistent and up-to-date data.',
        ],
      },
      {
        title: '3. PCAF-based Emissions Calculation',
        icon: 'emissions',
        lines: [
          'First Cloud determines the attribution factor based on the aggregation methods defined in the PCAF standard.',
          'Based on this, greenhouse gas emissions (GHG emissions) are automatically calculated in accordance with PCAF rules\u2014depending on the available source data.',
          'The standardized calculation enables a consistent and traceable determination of emissions.',
        ],
      },
      {
        title: '4. Automated ESG Data Integration',
        icon: 'integration',
        lines: [
          'The ESG information on the investments is automatically imported into First Cloud via Dataland\u2019s REST API.',
          'The data is thus immediately available as the basis for all further calculations and processes and is updated without manual intermediate steps.',
        ],
      },
      {
        title: '5. Data Transparency & Traceability',
        icon: 'traceability',
        lines: [
          'First Cloud ensures end-to-end traceability of data down to the level of individual data records.',
          'This allows requests from auditors, regulatory authorities, or the public to be addressed transparently and efficiently at any time.',
        ],
      },
    ],
    benefits: [
      'Automated integration of ESG data via REST API without manual intermediate steps',
      'End-to-end processing from the database to the final regulatory report',
      'Consistent and centralized database for all ESG-related processes',
      'Standardized and traceable calculation of regulatory metrics (including PAI, taxonomy, PCAF)',
      'Full transparency and audit assurance down to the individual data level',
    ],
    externalUrl: 'https://www.fact.de/unsere-loesungen/first-cloud/',
  },
  {
    slug: 'iss-sopra-steria',
    name: 'ISS Sopra Steria',
    logo: '/static/logos/logo_iss-soprasteria.png',
    logoClassName: 'scale-[1.1] lg:scale-[1.25]',
    heroTitle: 'ISS - ESG Solutions',
    primaryTag: 'Integration partner',
    secondaryTags: ['ESG solutions', 'Platform workflows'],
    intro: [
      'ISS (a Sopra Steria company) provides comprehensive ESG and sustainable investment solutions.',
      'Our integration makes Dataland\u2019s transparency-focused, source-traceable sustainability data available within the ISS platform ecosystem.',
    ],
    keyUseCases: [
      {
        title: 'Investment Research Enhancement',
        icon: 'research',
        lines: [
          'Investment teams using ISS platforms can enhance their ESG research with Dataland\u2019s primary-source datasets, providing additional verification and granularity for company sustainability assessments.',
        ],
      },
      {
        title: 'Proxy Voting Support',
        icon: 'voting',
        lines: [
          'Institutional investors can access detailed sustainability disclosures from Dataland through ISS platforms to inform their proxy voting decisions on ESG-related shareholder proposals.',
        ],
      },
      {
        title: 'Sustainable Fund Management',
        icon: 'fund',
        lines: [
          'Fund managers running sustainable investment strategies can leverage Dataland data within ISS tools to screen investments, monitor portfolio sustainability characteristics, and generate client reporting.',
        ],
      },
      {
        title: 'Compliance Verification',
        icon: 'compliance',
        lines: [
          'Compliance teams can use Dataland\u2019s source-traceable data through ISS platforms to verify and document the underlying sources for regulatory filings, providing audit-ready transparency.',
        ],
      },
    ],
    benefits: [
      'Enhanced data transparency and source documentation',
      'Complementary coverage for smaller and regional companies',
      'Access to on-demand data sourcing capabilities',
      'Seamless integration within existing ISS workflows',
      'Combined strength of multiple data providers',
    ],
    extraSections: [
      {
        title: 'Why Partner Integration Matters',
        items: [
          {
            title: 'Reduced Complexity',
            text: 'Access multiple data sources through a single familiar interface',
          },
          {
            title: 'Faster Implementation',
            text: 'Leverage existing platform integrations instead of building from scratch',
          },
          {
            title: 'Enhanced Workflows',
            text: 'Combine Dataland data with existing tools and processes seamlessly',
          },
        ],
      },
    ],
    closingPrompt:
      'Interested in becoming an integration partner or learning more about accessing Dataland through these platforms?',
    closingCtaLabel: 'Contact Us',
    externalUrl: 'https://iss.soprasteria.de/',
  },
];
