export interface DocumentationLink {
  label: string;
  url: string;
}

export interface UseCase {
  icon: string;
  tags: string[];
  title: string;
  titleLines?: [string, string];
  description: string;
}

export interface FeatureCard {
  title: string;
  tags: string[];
  text: string;
}

export interface HowItWorksBlock {
  leftTitle: string;
  leftText: string;
  rightTitle: string;
  rightText: string;
}

export interface GettingDataBlock {
  title: string;
  text: string;
  highlights: string[];
}

export interface ProductApplicationArea {
  icon: string;
  titleLines: readonly [string, string];
}

export interface PricingFeatureItemData {
  title: string;
  text: string;
}

export interface CreditCostRow {
  members: string;
  cost: string;
}

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

export const PRODUCT_PAGE_CONTENT = {
  meta: {
    title: 'Product - Dataland',
    description: "Explore Dataland's ESG data platform features, frameworks, use cases, and pricing.",
  },
  hero: {
    titleLeading: 'Structured, source-based sustainability data',
    titleAccent: 'for regulatory reporting and investment analysis.',
    buttons: { try: 'Try it free', demo: 'Book a demo', contact: 'Get in touch' },
  },
  howItWorks: {
    title: 'How it works',
    intro: 'Access and use Dataland in the way that fits your team.',
  },
  gettingData: {
    title: 'Getting the data you need',
    intro: 'Members can use all data available on the platform or request additional datasets as needed.',
  },
  features: {
    title: 'Platform features',
    intro: 'Core platform capabilities for accessing, managing, and exporting Dataland data in daily workflows.',
  },
  useCases: {
    title: 'Use cases',
    intro: 'Dataland supports different ESG data use cases across reporting, analysis, and portfolio workflows.',
    supportText: 'These use cases support different business areas across the company.',
  },
  customerStories: {
    title: 'Customer stories',
    intro: 'Real-world experiences show how organizations use Dataland to solve ESG data challenges in practice.',
    labels: { challenge: 'Challenge', solution: 'Solution', value: 'Value' },
    readFullStory: 'Read full story',
  },
  pricing: {
    title: 'Membership and pricing',
    intro: 'A straightforward membership model with transparent pricing and shared sourcing costs.',
    annualMembership: {
      title: 'Annual membership',
      price: '5,000 EUR',
      intro: 'For this fee, everything already available on the platform is free for all Members:',
    },
    sharedCost: {
      title: 'Shared cost approach',
      intro: 'The cost of sourcing datasets is shared among members requesting the same data.',
      footnote: 'Dataset = 1 company, 1 framework, 1 reporting period.',
      highlight: 'The more members request the same company, the lower the cost per dataset.',
      membersHeader: '# of Members',
      costHeader: 'Cost (Credits)',
    },
    ctaTitle:
      'Join now to access high-quality ESG data and be part of building a non-profit European data infrastructure.',
    buttons: { contact: 'Get in touch', try: 'Try it free' },
  },
  documentation: {
    title: 'Documentation',
    intro: 'Technical documentation enables seamless integration and efficient use of the platform.',
    frameworkGuideTitle: 'Framework Guide',
    frameworkGuideLink: 'View on GitHub',
    apiReferenceTitle: 'API Reference',
    apiReferenceDescription: 'Interactive Swagger documentation for every Dataland service.',
    ctaTitle: 'Ready to get started?',
    buttons: { contact: 'Get in touch', try: 'Try it free' },
  },
} as const;

export const FRAMEWORK_DOC = {
  label: 'Framework documentation overview',
  url: 'https://github.com/d-fine/Dataland/wiki/Data-Framework-Documentation',
  description:
    'Understand the data frameworks and structures behind Dataland - coverage, field definitions, and supported standards.',
};

export const API_DOC_LINKS: DocumentationLink[] = [
  { label: 'Backend API', url: 'https://dev3.dataland.com/api/swagger-ui/index.html' },
  { label: 'Document Manager', url: '/documents/swagger-ui/index.html' },
  { label: 'Community Manager', url: '/community/swagger-ui/index.html' },
  { label: 'Quality Assurance', url: '/qa/swagger-ui/index.html' },
  { label: 'Users', url: '/users/swagger-ui/index.html' },
  { label: 'Data Sourcing', url: '/data-sourcing/swagger-ui/index.html' },
  { label: 'Accounting', url: '/accounting/swagger-ui/index.html' },
  { label: 'Specifications', url: '/specifications/swagger-ui/index.html' },
];

export const FEATURE_CARDS: FeatureCard[] = [
  {
    title: 'Download data',
    tags: ['Flexible formats', 'Internal use'],
    text: 'Download datasets as CSV or XLSX with or without metadata.',
  },
  {
    title: 'Portfolio management',
    tags: ['Portfolio setup', 'Monitoring'],
    text: 'Build your own portfolios of companies and monitor ESG data availability.',
  },
  {
    title: 'Portfolio sharing',
    tags: ['Collaboration', 'Team access'],
    text: 'Share company portfolios with colleagues and teams.',
  },
  {
    title: 'Request data',
    tags: ['On-demand sourcing', 'Missing data'],
    text: 'Order missing datasets directly through the platform.',
  },
  {
    title: 'Source transparency',
    tags: ['Traceability', 'Quality comments'],
    text: 'Inspect original source documents and quality comments.',
  },
  {
    title: 'Multi-framework export',
    tags: ['Reporting-ready formats', 'Framework coverage'],
    text: 'Export datasets in formats suitable for different reporting frameworks.',
  },
];

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

export const USE_CASES: UseCase[] = [
  {
    icon: '/static/product/use_cases/img_use_case_primary.png',
    tags: ['Primary provider', 'Core workflows'],
    title: 'Dataland as primary ESG data source',
    titleLines: ['Dataland as primary', 'ESG data source'],
    description:
      'Dataland serves as the main source of ESG datasets, with data retrieval, reporting, and analysis processes built directly on its datasets. Reliance on expensive ESG data monopolists can be reduced or eliminated.',
  },
  {
    icon: '/static/product/use_cases/img_use_case_complement.png',
    tags: ['Provider complement', 'SME coverage'],
    title: 'Dataland complementing primary data provider',
    titleLines: ['Dataland complementing', 'primary data provider'],
    description:
      'Dataland complements a primary ESG data provider by closing remaining data gaps. This includes access to ESG data for SMEs and private companies that are typically not covered by large commercial vendors, extending ESG analysis beyond listed entities and enabling broader coverage of real-economy exposures, especially in lending, private markets, and insurance portfolios.',
  },
  {
    icon: '/static/product/use_cases/img_use_case_validation.png',
    tags: ['Cross-checking', 'Auditability'],
    title: 'Independent Validation and Audit Trail',
    titleLines: ['Independent Validation', 'and Audit Trail'],
    description:
      'Dataland datasets serve as an additional reference point to cross-check consistency, plausibility, and methodological differences against a primary provider - particularly in contexts requiring high data quality and auditability. Every data point is linked to its exact location in the original source document, providing the source transparency that regulatory audits increasingly require.',
  },
  {
    icon: '/static/product/use_cases/img_use_case_portfolio_monitoring.png',
    tags: ['Portfolio coverage', 'Ongoing updates'],
    title: 'Continuous Coverage for Your Portfolio',
    titleLines: ['Continuous Coverage', 'for Your Portfolio'],
    description:
      'Retrieval of ESG datasets for defined portfolios (e.g. loan books or investment portfolios), combined with continuous identification of newly available data. Portfolio coverage remains up to date as holdings evolve and additional datasets become available.',
  },
  {
    icon: '/static/product/use_cases/img_use_case_gap_filling.png',
    tags: ['On-demand sourcing', 'Specific gaps'],
    title: 'Targeted Sourcing of Missing Datasets',
    titleLines: ['Targeted Sourcing of', 'Missing Datasets'],
    description:
      'Missing datasets for specific companies or indicators can be ordered via credits, driven by concrete internal or regulatory requirements. This enables precise data procurement without dependency on predefined data packages.',
  },
  {
    icon: '/static/product/use_cases/img_use_case_format_update.png',
    tags: ['EU Taxonomy', 'Template continuity'],
    title: 'EU Taxonomy Format Continuity',
    titleLines: ['EU Taxonomy Format', 'Continuity'],
    description:
      'Provision of EU Taxonomy datasets in both current and previous template formats, including automated format conversion. Ensures continuity in internal reporting processes when regulatory templates change.',
  },
];

export const PRODUCT_APPLICATION_AREAS: ProductApplicationArea[] = [
  { icon: '/static/images/img_icon_badge.svg', titleLines: ['Regulatory', 'reporting'] },
  { icon: '/static/images/img_icon_invest.svg', titleLines: ['Investment', 'analysis'] },
  { icon: '/static/images/img_icon_gauge.svg', titleLines: ['Risk', 'management'] },
  { icon: '/static/images/img_icon_portfolio.svg', titleLines: ['Portfolio', 'management'] },
  { icon: '/static/images/img_icon_bars_some.svg', titleLines: ['Performance', 'management'] },
  { icon: '/static/images/img_icon_contact.svg', titleLines: ['Client', 'advisory'] },
];

export const MEMBERSHIP_INCLUDED_ITEMS: PricingFeatureItemData[] = [
  {
    title: 'Free unrestricted data use',
    text: 'All datasets available on Dataland can be accessed and used for internal purposes without restrictions.',
  },
  {
    title: 'Free use of all features',
    text: 'Current features, from download to template conversion, as well as all future features we will develop, are free for all members.',
  },
  {
    title: 'On-demand sourcing',
    text: 'Missing datasets can be requested and automatically delivered through Active Portfolio Monitoring.',
  },
  { title: '100 credits', text: 'for requesting missing datasets.' },
];

export const SHARED_COST_ITEMS: PricingFeatureItemData[] = [
  {
    title: 'Additional credits',
    text: 'Members can use the credits received with their annual subscription and purchase additional credits for EUR 50 per credit.',
  },
  {
    title: 'Pay only for new data',
    text: 'Credits are only used when new data is sourced. All datasets already on the platform are included at no additional cost.',
  },
];

export const PRODUCT_CREDITS_TABLE: CreditCostRow[] = [
  { members: '1', cost: '1' },
  { members: '2', cost: '0.5' },
  { members: '3', cost: '0.4' },
  { members: '4', cost: '0.3' },
  { members: '5-9', cost: '0.2' },
  { members: '> 10', cost: '0.1' },
];

export const CUSTOMER_STORIES_DETAILED: CustomerStoryDetail[] = [
  {
    anchor: 'meag',
    logo: '/static/logos/logo_meag_2026.svg',
    logoClassName: 'scale-[1.08]',
    primaryTag: 'Asset manager',
    secondaryTags: ['SFDR', 'EU Taxo', 'Audit', 'Compliance', 'Quality'],
    title: 'Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition',
    summary:
      "As an asset manager with sustainability reporting obligations, MEAG requires reliable ESG indicators across a large universe of portfolio companies. The firm uses Dataland to verify data quality and close specific data gaps that arise in the datasets delivered by its primary ESG data provider, both for internal purposes and to demonstrate reasonable effort to auditors. At the same time, MEAG expects Dataland's EU Taxonomy template conversion capability to simplify the upcoming transition to the revised reporting template.",
    challenge:
      "For SFDR reporting, MEAG must compile a range of sustainability indicators across a large universe of portfolio companies. While the firm's main ESG data provider covers most of the required data, some indicators needed for PAI calculations are not always included in the delivered datasets. In some cases, there is no data available at all for certain companies from the primary provider.",
    solution:
      "MEAG uses Dataland as a data source to close specific SFDR data gaps. When indicators required for PAI reporting are missing from the firm's primary dataset or there is no dataset available for that firm from the primary data provider, the reporting team retrieves the relevant ESG indicators from Dataland.",
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
    secondaryTags: ['EU Taxo', 'API integration', 'Lean model', 'Efficiency'],
    title: 'Primary source of EU Taxonomy data with automated delivery',
    summary:
      "NORD/LB uses ESG indicators across several regulatory and internal reporting processes. The bank selected Dataland for EU Taxonomy reporting because it provides high-quality, disclosure-based indicators at a competitive price while allowing the bank to retrieve only the specific datasets required for its reporting workflows. Instead of purchasing large bundled ESG data packages, NORD/LB requests and obtains EU Taxonomy data exactly for the companies it needs. The datasets are now integrated directly into the bank's internal systems via API, replacing an earlier manual data retrieval process.",
    challenge:
      "For EU Taxonomy, NORD/LB must compile sustainability indicators for a broad range of corporate counterparties. Traditional ESG data providers typically offer large data packages covering thousands of indicators and analytics. For banks that only require a limited number of specific metrics for regulatory reporting, this model often leads to high costs and unnecessary data procurement. When NORD/LB began using Dataland, the required datasets were initially retrieved manually from the platform and transferred into the bank's internal reporting systems. While this approach provided access to the necessary ESG indicators, it required manual downloads and additional handling steps during each reporting cycle. As ESG reporting requirements evolved, this manual workflow created additional operational effort for the reporting team.",
    solution:
      "NORD/LB adopted Dataland as its primary ESG data source for the Taxonomy KPIs of its counterparties that are subject to non-financial reporting, allowing the bank to obtain the specific datasets required for its regulatory reporting processes without purchasing large and expensive data bundles. To streamline the workflow further, the bank implemented a direct API integration with the Dataland platform. Through this integration, the required ESG datasets are automatically retrieved and transferred into the bank's internal reporting infrastructure. This ensures that the latest available data is delivered directly to the relevant reporting processes without manual intervention.",
    value:
      "Using Dataland allows NORD/LB to combine a targeted ESG data procurement model with automated data delivery. The bank receives disclosure-based ESG indicators with transparent lineage, pays only for the datasets needed for its reporting workflows, and integrates the data directly into internal systems through API access. The transition from manual downloads to automated integration has significantly reduced operational effort for the reporting team while ensuring that ESG datasets are delivered consistently and reliably to the bank's regulatory reporting processes.",
    quoteText:
      'The combination of high-quality disclosure-based data, a transparent pricing model, and API integration makes Dataland a very efficient ESG data source for our reporting workflows.',
    quoteAuthor: 'Sandra Piehl',
    quoteRole: 'Gesamtbankreporting',
    successStorySlug: 'nordlb-primary-esg-data',
  },
  {
    anchor: 'ovbraunschweig',
    logo: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.png',
    logoClassName: 'scale-[0.9]',
    primaryTag: 'Insurance',
    secondaryTags: ['SFDR', 'Validation', 'Audit', 'Efficiency', 'Quality'],
    title: 'Using Dataland as an Independent Source to Validate PAI Data for Audit',
    summary:
      'Oeffentliche Versicherung Braunschweig (OeVB) must report sustainability indicators for its investment portfolios under the Sustainable Finance Disclosure Regulation (SFDR). In particular, the calculation of Principal Adverse Impact (PAI) indicators requires reliable and traceable ESG data for portfolio companies.',
    challenge:
      'For SFDR reporting, insurers must be able to demonstrate that the ESG indicators used in their PAI calculations are reliable and properly documented. In practice, this means that the values used in regulatory reporting must be traceable back to the underlying company disclosures.',
    solution:
      'OeVB therefore uses Dataland as a complementary ESG data source specifically for validation purposes. For selected portfolio companies and PAI indicators, the reporting team retrieves the corresponding datasets from Dataland.',
    value:
      'The reporting team can demonstrate to internal stakeholders and external auditors that the PAI values used in the calculations are consistent with the underlying issuer disclosures.',
    quoteText:
      "Dataland provides us with a reliable way to verify that the PAI indicators used in our reporting match the issuer's disclosures. This makes it much easier to demonstrate the correctness of our data during audits while giving us access to high-quality ESG data in a very cost-efficient way.",
    quoteAuthor: 'Patrick Gerling',
    quoteRole: 'Head of Capital Investment - Risk Control and Communication',
    successStorySlug: 'oeffentliche-pai-validation',
  },
];
