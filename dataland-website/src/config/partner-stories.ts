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
    text: 'ISS (a Sopra Steria company) is an integrated investment solutions platform including ESG.',
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
          'Based on the provided ESG data, the principal adverse impacts (PAI indicators) are determined in accordance with the regulatory technical standards (RTS) and transferred to the corresponding standard reports.',
          "In addition, First Cloud calculates the taxonomy metrics for the preparation of taxonomy reports in accordance with the relevant EU taxonomy reporting templates set out in Annex X to Commission Delegated Regulation (EU) 2021/2178 and Annex III 'Annex XII to Commission Delegated Regulation (EU) 2022/1214.",
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
    heroTitle: 'ISS \u2013 ESG Solutions',
    primaryTag: 'Integration partner',
    secondaryTags: ['ESG solutions', 'Platform workflows'],
    intro: [
      'ISS (a company within the Sopra Steria Group) provides comprehensive ESG solutions and an integrated asset management offering.',
      'Through our integration, Dataland\u2019s sustainability data \u2013 designed for transparency and source traceability \u2013 is made available within the ISS Asset Management platform ecosystem.',
    ],
    keyUseCases: [
      {
        title: 'Regulatory Reporting Integration',
        icon: 'report',
        lines: [
          'ISS ESG generates regulatory ESG reports using integrated Dataland data.',
          'It derives principal adverse impacts (PAI indicators) in line with the RTS and populates the standard reports.',
          'ISS ESG also calculates EU taxonomy metrics and prepares taxonomy reports using the templates in Annex X to Commission Delegated Regulation (EU) 2021/2178 and Annex XII to Commission Delegated Regulation (EU) 2022/1214.',
        ],
      },
      {
        title: 'Enhancing Investment Research',
        icon: 'research',
        lines: [
          'Investment teams using ISS platforms can deepen their ESG analyses by combining their respective primary ESG data sources with Dataland. These provide additional verification and a higher level of detail for companies\u2019 sustainability assessments.',
        ],
      },
      {
        title: 'Sustainable Fund Management',
        icon: 'fund',
        lines: [
          'Fund managers pursuing sustainable investment strategies can use Dataland data in ISS tools to screen investments, monitor portfolio sustainability characteristics, and create client reports.',
        ],
      },
      {
        title: 'Compliance Verification',
        icon: 'compliance',
        lines: [
          'Compliance teams can use Dataland\u2019s source-traceable data via ISS platforms to verify and document the underlying sources for regulatory disclosures \u2013 ensuring audit-ready transparency.',
        ],
      },
      {
        title: 'ESG Data Hub',
        icon: 'integration',
        lines: [
          'A modular ESG data foundation for banks and insurers \u2013 governed, automated, and audit-ready.',
          'Better with ISS ESG: One seamless flow from data sourcing to analysis to disclosure.',
        ],
      },
    ],
    benefits: [
      'Improved data transparency and source documentation',
      'Complementary coverage of smaller and regional companies',
      'Access to on-demand data collection capabilities',
      'Seamless integration into existing ISS workflows',
      'Combined strength of multiple data providers',
    ],
    externalUrl: 'https://iss.soprasteria.de/',
  },
];
