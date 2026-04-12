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

export const FRAMEWORK_DOC = {
  label: 'Framework documentation overview',
  url: URL_DOC_FRAMEWORK,
  description: 'Understand the data frameworks and structures behind Dataland — coverage, field definitions, and supported standards.',
};

export const API_DOC_LINKS: DocumentationLink[] = [
  { label: 'Backend API', url: URL_DOC_BACKEND_API },
  { label: 'Document Manager', url: URL_DOC_DOCUMENT_MANAGER },
  { label: 'Community Manager', url: URL_DOC_COMMUNITY_MANAGER },
  { label: 'Quality Assurance', url: URL_DOC_QA },
  { label: 'Users', url: URL_DOC_USERS },
  { label: 'Data Sourcing', url: URL_DOC_DATA_SOURCING },
  { label: 'Accounting', url: URL_DOC_ACCOUNTING },
  { label: 'Specifications', url: URL_DOC_SPECIFICATIONS },
];

export const DOCUMENTATION_LINKS: DocumentationLink[] = [
  { label: 'Framework documentation overview', url: URL_DOC_FRAMEWORK },
  ...API_DOC_LINKS,
];

// ---- Use Cases (spec section 4.8) ----

export interface UseCase {
  icon: string;
  tags: string[];
  title: string;
  titleLines?: [string, string];
  description: string;
}

export const USE_CASES: UseCase[] = [
  {
    icon: '/static/images/use_cases/img_use_case_primary.png',
    tags: ['Primary provider', 'Core workflows'],
    title: 'Dataland as primary ESG data source',
    titleLines: ['Dataland as primary', 'ESG data source'],
    description:
      'Dataland serves as the main source of ESG datasets, with data retrieval, reporting, and analysis processes built directly on its datasets. Reliance on traditional ESG data vendors can be reduced or eliminated.',
  },
  {
    icon: '/static/images/use_cases/img_use_case_complement.png',
    tags: ['Provider complement', 'SME coverage'],
    title: 'Dataland complementing primary data provider',
    titleLines: ['Dataland complementing', 'primary data provider'],
    description:
      'Dataland complements a primary ESG data provider by closing remaining data gaps. This includes access to ESG data for SMEs and private companies that are typically not covered by large commercial vendors, extending ESG analysis beyond listed entities and enabling broader coverage of real-economy exposures, especially in lending, private markets, and insurance portfolios.',
  },
  {
    icon: '/static/images/use_cases/img_use_case_validation.png',
    tags: ['Cross-checking', 'Auditability'],
    title: 'Independent Validation and Audit Trail',
    titleLines: ['Independent Validation', 'and Audit Trail'],
    description:
      'Dataland datasets serve as an additional reference point to cross-check consistency, plausibility, and methodological differences against a primary provider \u2014 particularly in contexts requiring high data quality and auditability. Every data point is linked to its exact location in the original source document, providing the source transparency that regulatory audits increasingly require.',
  },
  {
    icon: '/static/images/use_cases/img_use_case_portfolio_monitoring.png',
    tags: ['Portfolio coverage', 'Ongoing updates'],
    title: 'Continuous Coverage for Your Portfolio',
    titleLines: ['Continuous Coverage', 'for Your Portfolio'],
    description:
      'Retrieval of ESG datasets for defined portfolios (e.g. loan books or investment portfolios), combined with continuous identification of newly available data. Portfolio coverage remains up to date as holdings evolve and additional datasets become available.',
  },
  {
    icon: '/static/images/use_cases/img_use_case_gap_filling.png',
    tags: ['On-demand sourcing', 'Specific gaps'],
    title: 'Targeted Sourcing of Missing Datasets',
    titleLines: ['Targeted Sourcing of', 'Missing Datasets'],
    description:
      'Missing datasets for specific companies or indicators can be ordered via credits, driven by concrete internal or regulatory requirements. This enables precise data procurement without dependency on predefined data packages.',
  },
  {
    icon: '/static/images/use_cases/img_use_case_format_update.png',
    tags: ['EU Taxonomy', 'Template continuity'],
    title: 'EU Taxonomy Template Updates and Format Continuity',
    titleLines: ['EU Taxonomy Template Updates', 'and Format Continuity'],
    description:
      'Provision of EU Taxonomy datasets in both current and previous template formats, including automated format conversion. Ensures continuity in internal reporting processes when regulatory templates change.',
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
  title: string;
  text: string;
  highlights: string[];
}

export const GETTING_DATA_BLOCKS: GettingDataBlock[] = [
  {
    title: 'Dataset already available',
    text: 'Use the dataset within the platform or download it for your internal applications',
    highlights: ['Free of charge', 'Data available for immediate use'],
  },
  {
    title: 'Dataset not yet available',
    text: 'Request the dataset from your account on the platform',
    highlights: ['Delivery within a month', 'Shared costs for members requesting the same dataset'],
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
  title: 'Annual membership',
  items: [
    'EUR 5,000 per year',
    '100 credits included',
    '1 credit = 1 company, 1 framework, 1 reporting period',
    'Additional credits can be purchased for \u20AC50 per credit',
  ],
  footer: 'All datasets already on the platform are included at no additional cost. Credits are only used when new data is sourced.',
};

export const CREDITS_VISUAL = {
  title: 'How credit-sharing works',
  image: '/static/images/img_credits.png',
  caption: 'When multiple members request the same company, the credit cost is split equally between them.',
};

// ---- Detailed customer stories (spec section 4.9) ----

export interface CustomerStoryDetail {
  anchor: string;
  logo: string;
  logoClassName?: string;
  primaryTag: string;
  secondaryTags: string[];
  title: string;
  summary: string;
  challenge: string;
  solution: string;
  value: string;
  quoteText: string;
  quoteAuthor: string;
  quoteRole: string;
  successStorySlug?: string;
}

export const CUSTOMER_STORIES_DETAILED: CustomerStoryDetail[] = [
  {
    anchor: 'meag',
    logo: '/static/logos/logo_meag_2026.svg',
    logoClassName: 'scale-[1.08]',
    primaryTag: 'Asset manager',
    secondaryTags: ['SFDR', 'EU Taxo', 'Audit'],
    title: 'Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition',
    summary:
      'As an asset manager with sustainability reporting obligations, MEAG requires reliable ESG indicators across a large universe of portfolio companies. The firm uses Dataland to verify data quality and close specific data gaps that arise in the datasets delivered by its primary ESG data provider, both for internal purposes and to demonstrate reasonable effort to auditors. At the same time, MEAG expects Dataland\'s EU Taxonomy template conversion capability to simplify the upcoming transition to the revised reporting template.',
    challenge:
      'For SFDR reporting, MEAG must compile a range of sustainability indicators across a large universe of portfolio companies. While the firm\'s main ESG data provider covers most of the required data, some indicators needed for PAI calculations are not always included in the delivered datasets. In some cases, there is no data available at all for certain companies from the primary provider.',
    solution:
      'MEAG uses Dataland as a data source to close specific SFDR data gaps. When indicators required for PAI reporting are missing from the firm\'s primary dataset or there is no dataset available for that firm from the primary data provider, the reporting team retrieves the relevant ESG indicators from Dataland.',
    value:
      'Traceable sourcing and documentation strengthen audit readiness and simplify demonstrating reasonable effort. The ability to cross-check data improves overall data quality and reduces reliance on a single provider.',
    quoteText:
      'Dataland provides a reliable way to close specific SFDR data gaps in our reporting and to obtain required data quickly upon request. The upcoming EU Taxonomy template conversion is a key additional benefit, as it removes the need to build and maintain our own format conversion. The platform also helps us document reasonable effort where data cannot be sourced and gives us an additional basis for validating data delivered by our primary provider.',
    quoteAuthor: 'Dr. Arnd Pauwels',
    quoteRole: 'Head of ESG Reporting',
    successStorySlug: 'meag-sfdr-data-gaps',
  },
  {
    anchor: 'nordlb',
    logo: '/static/logos/logo_nordlb.svg',
    logoClassName: 'scale-[0.96]',
    primaryTag: 'Bank',
    secondaryTags: ['EU Taxo', 'API integration'],
    title: 'Primary source of EU Taxonomy data with automated delivery',
    summary:
      'NORD/LB uses ESG indicators across several regulatory and internal reporting processes. The bank selected Dataland for EU Taxonomy reporting because it provides high-quality, disclosure-based indicators at a competitive price while allowing the bank to retrieve only the specific datasets required for its reporting workflows. Instead of purchasing large bundled ESG data packages, NORD/LB requests and obtains EU Taxonomy data exactly for the companies it needs. The datasets are now integrated directly into the bank\'s internal systems via API, replacing an earlier manual data retrieval process.',
    challenge:
      'For EU Taxonomy, NORD/LB must compile sustainability indicators for a broad range of corporate counterparties. Traditional ESG data providers typically offer large data packages covering thousands of indicators and analytics. For banks that only require a limited number of specific metrics for regulatory reporting, this model often leads to high costs and unnecessary data procurement. When NORD/LB began using Dataland, the required datasets were initially retrieved manually from the platform and transferred into the bank\'s internal reporting systems. While this approach provided access to the necessary ESG indicators, it required manual downloads and additional handling steps during each reporting cycle. As ESG reporting requirements evolved, this manual workflow created additional operational effort for the reporting team.',
    solution:
      'NORD/LB adopted Dataland as its primary ESG data source for the Taxonomy KPIs of its counterparties that are subject to non-financial reporting, allowing the bank to obtain the specific datasets required for its regulatory reporting processes without purchasing large and expensive data bundles. To streamline the workflow further, the bank implemented a direct API integration with the Dataland platform. Through this integration, the required ESG datasets are automatically retrieved and transferred into the bank\'s internal reporting infrastructure. This ensures that the latest available data is delivered directly to the relevant reporting processes without manual intervention.',
    value:
      'Using Dataland allows NORD/LB to combine a targeted ESG data procurement model with automated data delivery. The bank receives disclosure-based ESG indicators with transparent lineage, pays only for the datasets needed for its reporting workflows, and integrates the data directly into internal systems through API access. The transition from manual downloads to automated integration has significantly reduced operational effort for the reporting team while ensuring that ESG datasets are delivered consistently and reliably to the bank\'s regulatory reporting processes.',
    quoteText:
      'The combination of high-quality disclosure-based data, a transparent pricing model, and API integration makes Dataland a very efficient ESG data source for our reporting workflows.',
    quoteAuthor: 'Sandra Piehl',
    quoteRole: 'Gesamtbankreporting',
    successStorySlug: 'nordlb-primary-esg-data',
  },
  {
    anchor: 'ovbraunschweig',
    logo: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.jpg',
    logoClassName: 'scale-[0.9]',
    primaryTag: 'Insurance',
    secondaryTags: ['SFDR', 'Validation', 'Audit'],
    title: 'Using Dataland as an Independent Source to Validate PAI Data for Audit',
    summary:
      '\u00D6ffentliche Versicherung Braunschweig (\u00D6VB) must report sustainability indicators for its investment portfolios under the Sustainable Finance Disclosure Regulation (SFDR). In particular, the calculation of Principal Adverse Impact (PAI) indicators requires reliable and traceable ESG data for portfolio companies.',
    challenge:
      'For SFDR reporting, insurers must be able to demonstrate that the ESG indicators used in their PAI calculations are reliable and properly documented. In practice, this means that the values used in regulatory reporting must be traceable back to the underlying company disclosures.',
    solution:
      '\u00D6VB therefore uses Dataland as a complementary ESG data source specifically for validation purposes. For selected portfolio companies and PAI indicators, the reporting team retrieves the corresponding datasets from Dataland.',
    value:
      'The reporting team can demonstrate to internal stakeholders and external auditors that the PAI values used in the calculations are consistent with the underlying issuer disclosures.',
    quoteText:
      'Dataland provides us with a reliable way to verify that the PAI indicators used in our reporting match the issuer\'s disclosures. This makes it much easier to demonstrate the correctness of our data during audits while giving us access to high-quality ESG data in a very cost-efficient way.',
    quoteAuthor: 'Patrick Gerling',
    quoteRole: 'Head of Capital Investment - Risk Control and Communication',
    successStorySlug: 'oeffentliche-pai-validation',
  },
];
