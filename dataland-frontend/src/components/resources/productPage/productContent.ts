// ---- URL Constants (spec section 7.2) ----

export const URL_LINKEDIN_DATALAND = 'https://www.linkedin.com/company/dataland-gmbh';
export const URL_DFINE = 'https://www.d-fine.com';
export const URL_PWC = 'https://www.pwc.com';
export const URL_PARTNER_FACT = 'https://www.fact.de/unsere-loesungen/first-cloud/';
export const URL_PARTNER_ISS = 'https://iss.soprasteria.de/';

export const CONTACT_MORITZ_MAIL = 'mailto:moritz.kiese@dataland.com';
export const CONTACT_MORITZ_LINKEDIN = 'https://www.linkedin.com/in/moritz-kiese-932b104/';
export const CONTACT_ANDREAS_MAIL = 'mailto:andreas.hoecherl@dataland.com';
export const CONTACT_ANDREAS_LINKEDIN = 'https://www.linkedin.com/in/andreas-h%C3%B6cherl-016220b4/';
export const CONTACT_SOEREN_MAIL = 'mailto:soeren.vorsmann@dataland.com';
export const CONTACT_SOEREN_LINKEDIN = 'https://www.linkedin.com/company/dataland-gmbh';
export const CONTACT_PHONE = 'tel:+491622631304';
export const CONTACT_EMAIL = 'mailto:info@dataland.com';

// ---- Documentation URLs (spec section 4.11) ----

export interface DocumentationLink {
  label: string;
  url: string;
}

export const URL_DOC_FRAMEWORK = 'https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation';
export const URL_DOC_BACKEND_API = 'https://dataland.com/api/swagger-ui/index.html';
export const URL_DOC_DOCUMENT_MANAGER = 'https://dataland.com/documents/swagger-ui/index.html';
export const URL_DOC_COMMUNITY_MANAGER = 'https://dataland.com/community/swagger-ui/index.html';
export const URL_DOC_QA = 'https://dataland.com/qa/swagger-ui/index.html';
export const URL_DOC_USERS = 'https://dataland.com/users/swagger-ui/index.html';
export const URL_DOC_DATA_SOURCING = 'https://dataland.com/data-sourcing/swagger-ui/index.html';
export const URL_DOC_ACCOUNTING = 'https://dataland.com/accounting/swagger-ui/index.html';
export const URL_DOC_SPECIFICATIONS = 'https://dataland.com/specifications/swagger-ui/index.html';

export const DOCUMENTATION_LINKS: DocumentationLink[] = [
  { label: 'Framework documentation overview', url: URL_DOC_FRAMEWORK },
  { label: 'Backend API documentation', url: URL_DOC_BACKEND_API },
  { label: 'Document manager API', url: URL_DOC_DOCUMENT_MANAGER },
  { label: 'Community manager', url: URL_DOC_COMMUNITY_MANAGER },
  { label: 'Quality assurance service', url: URL_DOC_QA },
  { label: 'Users API', url: URL_DOC_USERS },
  { label: 'Data sourcing API', url: URL_DOC_DATA_SOURCING },
  { label: 'Accounting API', url: URL_DOC_ACCOUNTING },
  { label: 'Specifications', url: URL_DOC_SPECIFICATIONS },
];

// ---- Use Cases (spec section 4.8) ----

export interface UseCase {
  title: string;
  description: string;
}

export const USE_CASES: UseCase[] = [
  {
    title: 'Complementing Existing ESG Data Providers',
    description:
      'Dataland complements a primary ESG data provider by closing remaining data gaps. Missing indicators or uncovered companies can be retrieved where the primary provider is incomplete. This includes access to ESG data for SMEs and private companies that are typically not covered by large commercial vendors, extending ESG analysis beyond listed entities and enabling broader coverage of real-economy exposures, especially in lending, private markets, and insurance portfolios',
  },
  {
    title: 'Independent Validation and Audit Trail',
    description:
      'Dataland datasets serve as an additional reference point to cross-check consistency, plausibility, and methodological differences against a primary provider \u2014 particularly in contexts requiring high data quality and auditability. Every data point is linked to its exact location in the original source document, providing the source transparency that regulatory audits increasingly require',
  },
  {
    title: 'Dataland as primary ESG data source',
    description:
      'Dataland serves as the main source of ESG datasets, with data retrieval, reporting, and analysis processes built directly on its datasets. Reliance on traditional ESG data vendors can be reduced or eliminated',
  },
  {
    title: 'Continuous Coverage for Your Portfolio',
    description:
      'Retrieval of ESG datasets for defined portfolios (e.g. loan books or investment portfolios), combined with continuous identification of newly available data. Portfolio coverage remains up to date as holdings evolve and additional datasets become available',
  },
  {
    title: 'Targeted Sourcing of Missing Datasets',
    description:
      'Missing datasets for specific companies or indicators can be ordered via credits, driven by concrete internal or regulatory requirements. This enables precise data procurement without dependency on predefined data packages',
  },
  {
    title: 'Data Access via Platform and API',
    description:
      'Access ESG datasets directly through the Dataland platform to search, retrieve, and download datasets for individual companies \u2014 suitable for ad-hoc analysis and manual workflows. For automated pipelines, integrate ESG datasets into internal IT systems (e.g. risk engines, reporting tools, data platforms) via API, supporting automated data ingestion and seamless use within existing system landscapes',
  },
  {
    title: 'EU Taxonomy Template Updates and Format Continuity',
    description:
      'Provision of EU Taxonomy datasets in both current and previous template formats, including automated format conversion. Ensures continuity in internal reporting processes when regulatory templates change',
  },
];

// ---- Feature Cards (spec section 4.7) ----

export interface FeatureCard {
  title: string;
  subtitle: string;
  text: string;
}

export const FEATURE_CARDS: FeatureCard[] = [
  {
    title: 'Download data',
    subtitle: 'Flexible formats',
    text: 'Download datasets as CSV or XLSX with or without metadata',
  },
  {
    title: 'Portfolio management',
    subtitle: 'Create and manage portfolios',
    text: 'Build your own portfolios of companies and monitor ESG data availability',
  },
  {
    title: 'Portfolio sharing',
    subtitle: 'Collaboration',
    text: 'Share company portfolios with colleagues and teams',
  },
  {
    title: 'Request data',
    subtitle: 'On-demand sourcing',
    text: 'Order missing datasets directly from the platform',
  },
  {
    title: 'Source transparency',
    subtitle: 'Traceability',
    text: 'Inspect original source documents and quality comments',
  },
  {
    title: 'Multi-framework export',
    subtitle: 'Reporting-ready formats',
    text: 'Export datasets in formats suitable for different reporting frameworks',
  },
];

// ---- How It Works blocks (spec section 4.5) ----

export interface HowItWorksBlock {
  leftTitle: string;
  leftText: string;
  rightTitle: string;
  rightText: string;
}

export const HOW_IT_WORKS_BLOCKS: HowItWorksBlock[] = [
  {
    leftTitle: 'Platform access',
    leftText: 'Access ESG datasets directly through the Dataland platform and download them in structured formats',
    rightTitle: 'Browse, search, and download',
    rightText: 'Browse companies, portfolios and datasets interactively',
  },
  {
    leftTitle: 'API integration',
    leftText: 'Integrate Dataland data into your internal systems and analytics pipelines',
    rightTitle: 'Automated workflows',
    rightText: 'Retrieve ESG datasets programmatically through stable APIs',
  },
  {
    leftTitle: 'Partner integration',
    leftText: 'Access Dataland data through software partners and ESG data platforms',
    rightTitle: 'Embedded data services',
    rightText: 'Partners integrate Dataland datasets into their own solutions',
  },
];

// ---- Getting Data blocks (spec section 4.6) ----

export interface GettingDataBlock {
  leftTitle: string;
  leftText: string;
  rightTitle: string;
  rightText: string;
}

export const GETTING_DATA_BLOCKS: GettingDataBlock[] = [
  {
    leftTitle: 'Dataset already available',
    leftText: 'Use the dataset within the platform or download it for your internal applications',
    rightTitle: 'No additional cost',
    rightText: 'Members can access existing datasets without any delay and free of charge',
  },
  {
    leftTitle: 'Dataset not yet available',
    leftText: 'Request the dataset through the platform',
    rightTitle: 'Delivered within one month',
    rightText: 'Costs are shared between members requesting the same dataset',
  },
];

// ---- Pricing data (spec section 4.10) ----

export interface ValueProposition {
  icon: string;
  title: string;
  text: string;
}

export const VALUE_PROPOSITIONS: ValueProposition[] = [
  {
    icon: '/static/images/icon_data_access.svg',
    title: 'Full data access',
    text: 'All datasets available on Dataland can be accessed and used for internal purposes without restrictions',
  },
  {
    icon: '/static/images/icon_requesting.svg',
    title: 'On-demand data sourcing',
    text: 'Missing datasets can be requested and are delivered automatically through Active Portfolio Monitoring',
  },
  {
    icon: '/static/images/icon_community.svg',
    title: 'Shared cost model',
    text: 'The cost of sourcing datasets is shared among members requesting the same data',
  },
];

export interface PricingCard {
  title: string;
  items: string[];
  footer: string;
}

export const PRICING_CARD: PricingCard = {
  title: 'Membership and credits',
  items: [
    '\u20AC5,000 per year membership',
    'Includes 100 credits',
    '1 credit corresponds to one dataset',
    'Additional 100 credits for \u20AC5,000',
  ],
  footer: 'Credits are only used when new datasets are sourced',
};

export const CREDITS_VISUAL = {
  title: 'Cost per dataset decreases as more members request the same data',
  image: '/static/images/img_credits.png',
  caption: 'Credits are split automatically between members requesting the same dataset',
};

export const PRICING_BOTTOM_NOTE =
  'Members only pay for data that is not yet available. All existing datasets are immediately accessible without additional cost.';

// ---- Detailed customer stories (spec section 4.9) ----

export interface CustomerStoryDetail {
  anchor: string;
  logo: string;
  tag: string;
  title: string;
  summary: string;
  challenge: string;
  solution: string;
  value: string;
  quoteText: string;
  quoteAuthor: string;
  quoteRole: string;
}

export const CUSTOMER_STORIES_DETAILED: CustomerStoryDetail[] = [
  {
    anchor: 'meag',
    logo: '/static/logos/logo_meag.svg',
    tag: 'Asset Manager',
    title: 'Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition',
    summary:
      'As an asset manager with extensive SFDR reporting obligations, MEAG requires reliable ESG indicators across a large universe of portfolio companies. The firm uses Dataland to close specific data gaps that arise in the datasets delivered by its primary ESG data provider. At the same time, MEAG expects Dataland\u2019s EU Taxonomy template conversion capability to simplify the upcoming transition to the revised reporting template.',
    challenge:
      'For SFDR reporting, MEAG must compile a range of sustainability indicators across a large universe of portfolio companies. While the firm\u2019s main ESG data provider covers most of the required data, some indicators needed for PAI calculations are not always included in the delivered datasets. These gaps create operational friction for the ESG reporting team. In parallel, MEAG faces another operational challenge related to the transition to the revised EU Taxonomy reporting template.',
    solution:
      'MEAG uses Dataland as a targeted data source to close specific SFDR data gaps. When indicators required for PAI reporting are missing from the firm\u2019s primary dataset, the reporting team retrieves the relevant ESG indicators from Dataland. At the same time, Dataland\u2019s EU Taxonomy template conversion functionality will ensure that datasets are available in both template structures.',
    value:
      'Using Dataland allows MEAG to resolve two operational challenges within its ESG reporting processes. First, the platform provides a practical way to fill SFDR data gaps. Second, the EU Taxonomy template conversion will simplify the transition to the new reporting format.',
    quoteText:
      'We initially joined Dataland to close specific SFDR data gaps in our reporting. The upcoming EU Taxonomy template conversion is another strong advantage.',
    quoteAuthor: 'Dr. Arnd Pauwels',
    quoteRole: 'Head of ESG Reporting',
  },
  {
    anchor: 'nordlb',
    logo: '/static/logos/logo_nordlb.svg',
    tag: 'Bank',
    title: 'Dataland as a Primary ESG Data Source with Automated Delivery',
    summary:
      'NORD/LB uses ESG indicators across several regulatory and internal reporting processes. The bank selected Dataland as its primary ESG data source because it provides high-quality, disclosure-based indicators at a competitive price while allowing the bank to retrieve only the specific datasets required for its reporting workflows.',
    challenge:
      'For regulatory frameworks such as SFDR and EU Taxonomy, NORD/LB must compile sustainability indicators for a broad range of corporate counterparties. Traditional ESG data providers typically offer large data packages. When NORD/LB began using Dataland, datasets were initially retrieved manually.',
    solution:
      'NORD/LB adopted Dataland as its primary ESG data source. The bank implemented a direct API integration for automated retrieval.',
    value:
      'The bank receives disclosure-based ESG indicators with transparent lineage, pays only for needed datasets, and integrates data directly into internal systems through API access.',
    quoteText:
      'The combination of high-quality disclosure-based data, a transparent pricing model, and API integration makes Dataland a very efficient ESG data source.',
    quoteAuthor: '[Name TBD]',
    quoteRole: '[Role TBD]',
  },
  {
    anchor: 'ovbraunschweig',
    logo: '/static/logos/logo_ovbraunschweig.svg',
    tag: 'Insurance',
    title: 'Using Dataland as an Independent Source to Validate PAI Data for Audit',
    summary:
      'ÖVB uses Dataland as a secondary ESG data source alongside its primary provider to strengthen the robustness of PAI indicator data for SFDR reporting and audit documentation.',
    challenge:
      'For SFDR reporting, insurers must demonstrate that ESG indicators used in PAI calculations are reliable and properly documented. Without clear reference to source documents, validation can be difficult during audit reviews.',
    solution:
      'ÖVB uses Dataland as a complementary data source specifically for validation. Because Dataland extracts from public disclosures with transparent source references, the team can verify values used in their reporting.',
    value:
      'The reporting team can demonstrate to internal stakeholders and external auditors that PAI values are consistent with underlying issuer disclosures.',
    quoteText:
      'Dataland provides us with a reliable way to verify that the PAI indicators used in our reporting match the issuer\u2019s disclosures.',
    quoteAuthor: '[Name TBD]',
    quoteRole: '[Role TBD]',
  },
];
