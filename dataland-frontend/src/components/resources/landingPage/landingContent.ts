// ---- Testimonials (spec section 3.8) ----

export interface Testimonial {
  author: string;
  affiliation: string;
  quote: string;
}

export const TESTIMONIALS: Testimonial[] = [
  {
    author: 'Stephen Henkel',
    affiliation: 'Managing Director at VÖB-Service GmbH',
    quote: 'Then came Dataland with the idea that ESG data should be a common good, and I think that\u2019s excellent',
  },
  {
    author: 'Jasmina Klein',
    affiliation: 'Manager at d-fine',
    quote:
      'Dataland will help to let the data flow and this is the key to solve one of the most pressing issues of our time',
  },
  {
    author: 'Matthias Kopp',
    affiliation: 'Director of Sustainable Finance at WWF Germany',
    quote:
      'Dataland can provide the data ecosystem we all need to support our transition to stay within 1.5 degrees or within the planetary boundaries',
  },
  {
    author: 'Christian Heller',
    affiliation: 'CEO of Value Balancing Alliance',
    quote:
      'Join Dataland, share your data, and make use of it to transform the economy into a just and sustainable one',
  },
  {
    author: 'Fabian Kloss',
    affiliation: 'Cloud Services Sales at T-Systems International',
    quote:
      'Have a look at Dataland and look at what benefits you can get out of it for your business. Join the community!',
  },
  {
    author: 'Ingo Speich',
    affiliation: 'Head of Sustainability & Corporate Governance at Deka Investment',
    quote:
      'We appeal to both investors and companies to make sustainability data available in a timely and cost-efficient manner',
  },
  {
    author: 'Christoph Benner',
    affiliation: 'CEO of Chom Capital',
    quote: 'Our partnership with Dataland is instrumental in progressively addressing and bridging data gaps',
  },
  {
    author: 'Rudolf Siebel',
    affiliation: 'Managing Director at BVI German Fund Association',
    quote:
      'Through Dataland, we hope that data availability, coverage and quality is improved, for the benefit of the users, the corporations and society overall',
  },
  {
    author: 'Dr. Annalisa Schwarz',
    affiliation: 'Managing Director at Werte-Stiftung',
    quote: 'Dataland will help solve data issues by ensuring transparent, open and fair access to sustainability data',
  },
  {
    author: 'Daniel Sailer',
    affiliation: 'Head of Sustainable Investment Office at Metzler Asset Management GmbH',
    quote: 'The Pathways to Paris PoC can be a simple way to make sure Dataland becomes the data platform you need',
  },
  {
    author: 'Dr. Egbert Schark',
    affiliation: 'Founder and Managing Director at d-fine GmbH',
    quote: 'I believe the fascinating idea is worth supporting. Join the mission! Join Dataland!',
  },
  {
    author: 'Sven Schuchert',
    affiliation: 'CEO of Envoria',
    quote: 'Dataland is the only platform we know that is open to everyone and based on a non-profit business model',
  },
];

// ---- News items (spec section 3.11) ----

export interface NewsItem {
  image: string;
  title: string;
  date: string;
  link: string;
}

export const URL_NEWS_EU_TAXO = 'https://www.linkedin.com/feed/update/urn:li:activity:7435335342439735296';
export const URL_NEWS_BVI_FOK = 'https://www.linkedin.com/feed/update/urn:li:activity:7432564767090905089';
export const URL_NEWS_DMM_Q12026 = 'https://www.linkedin.com/feed/update/urn:li:activity:7430511455638118400';
export const URL_NEWS_2025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7419695872156028928';
export const URL_NEWS_SFDR2 = 'https://www.linkedin.com/feed/update/urn:li:activity:7404533321671589890';
export const URL_NEWS_PCAF = 'https://www.linkedin.com/feed/update/urn:li:activity:7397576850782441472';
export const URL_NEWS_SUST2025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7395135444641947648';
export const URL_NEWS_DMMQ42025 = 'https://www.linkedin.com/feed/update/urn:li:activity:7392146170766151680';
export const URL_NEWS_ERIK = 'https://www.linkedin.com/feed/update/urn:li:activity:7391407400764772352';

export const NEWS_ITEMS: NewsItem[] = [
  {
    image: '/static/images/news_eu_taxo.png',
    title: 'Smooth transition to the new EU Taxonomy template',
    date: 'March 5, 2026',
    link: URL_NEWS_EU_TAXO,
  },
  {
    image: '/static/images/news_bvi_fok.png',
    title: 'Networking at BVI FOK',
    date: 'February 25, 2026',
    link: URL_NEWS_BVI_FOK,
  },
  {
    image: '/static/images/news_dmm_q12026.png',
    title: "Dataland Members' Meeting Q1 2026",
    date: 'February 20, 2026',
    link: URL_NEWS_DMM_Q12026,
  },
  {
    image: '/static/images/news_2025.png',
    title: '2025 in numbers',
    date: 'January 21, 2026',
    link: URL_NEWS_2025,
  },
  {
    image: '/static/images/news_sfdr2.png',
    title: 'How SFDR 2.0 reinforces the need for shared ESG data infrastructure',
    date: 'December 10, 2025',
    link: URL_NEWS_SFDR2,
  },
  {
    image: '/static/images/news_pcaf.png',
    title: 'PCAF on Dataland',
    date: 'November 21, 2025',
    link: URL_NEWS_PCAF,
  },
  {
    image: '/static/images/news_sust2025.png',
    title: 'Dataland @ Sustainability Kongress 2025',
    date: 'November 14, 2025',
    link: URL_NEWS_SUST2025,
  },
  {
    image: '/static/images/news_dmm_q42025.png',
    title: "Dataland Members' Meeting Q4 2025",
    date: 'November 6, 2025',
    link: URL_NEWS_DMMQ42025,
  },
  {
    image: '/static/images/news_erik.png',
    title: 'Leadership transition: thank you, Erik Breen!',
    date: 'November 4, 2025',
    link: URL_NEWS_ERIK,
  },
];

// ---- Customer story summaries (spec section 3.7) ----

export interface CustomerStorySummary {
  logo: string;
  tag: string;
  text: string;
  link: string;
}

export const CUSTOMER_STORY_SUMMARIES: CustomerStorySummary[] = [
  {
    logo: '/static/logos/logo_meag.svg',
    tag: 'Asset Manager',
    text: 'Filling SFDR gaps and EU Taxo template transition',
    link: '/product#meag',
  },
  {
    logo: '/static/logos/logo_nordlb.svg',
    tag: 'Bank',
    text: 'Primary source of ESG data and API integration',
    link: '/product#nordlb',
  },
  {
    logo: '/static/logos/logo_ovbraunschweig.svg',
    tag: 'Insurance',
    text: 'PAI lineage and source transparency for compliance',
    link: '/product#ovbraunschweig',
  },
];

// ---- Sector tiles (spec section 3.10) ----

export type SectorSize = 'XL' | 'L' | 'M' | 'S';

export interface SectorTile {
  title: string;
  icon: string;
  size: SectorSize;
}

export const SECTOR_TILES: SectorTile[] = [
  { title: 'Banks', icon: '/static/images/icon_bank.svg', size: 'XL' },
  { title: 'Insurance companies', icon: '/static/images/icon_insurance.svg', size: 'XL' },
  { title: 'Asset Managers', icon: '/static/images/icon_asset_manager.svg', size: 'XL' },
  { title: 'Pension funds', icon: '/static/images/icon_pension.svg', size: 'L' },
  { title: 'Public Financial Institutions', icon: '/static/images/icon_public_fin.svg', size: 'L' },
  { title: 'Data Providers', icon: '/static/images/icon_vendors.svg', size: 'M' },
  { title: 'Financial Data Infrastructure', icon: '/static/images/icon_fin_data.svg', size: 'S' },
  { title: 'ESG solution providers', icon: '/static/images/icon_esg_software.svg', size: 'S' },
  { title: 'Industry Associations', icon: '/static/images/icon_industry.svg', size: 'S' },
  { title: 'Sustainability Initiatives', icon: '/static/images/icon_esg_org.svg', size: 'S' },
  { title: 'Academic Institutions', icon: '/static/images/icon_academy.svg', size: 'S' },
];

// ---- Why Us problem-solution pairs (spec section 3.5) ----

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
      'Dataland provides the data its members actually need. If a required dataset is missing, members can request it. The data will be sourced from issuer disclosures and added to the platform, so gaps in coverage can be addressed when they arise',
  },
  {
    problemTitle: 'Poor data quality',
    problemText:
      'Many data sourcing approaches introduce errors, inconsistencies, outdated values, or unexplained gaps. Inaccurate or untraceable ESG data undermines reporting, analytics, and decision-making',
    solutionTitle: 'AI extraction with human verification and full source traceability',
    solutionText:
      'Dataland sources data directly from the original publisher and combines tailored AI extraction with manual verification steps. Every published data point is linked to its original source document, ensuring full traceability. This means datasets are not only structured efficiently and quality-assured by humans, but also independently verifiable at any time',
  },
  {
    problemTitle: 'Restrictive licensing terms',
    problemText:
      'Acquired datasets are often subject to restrictive usage rights, limiting how they can be applied across reporting, analysis, validation, and other internal workflows. This reduces the practical value of the data far beyond the original use case',
    solutionTitle: 'Unrestricted use',
    solutionText:
      'Dataland data can be used freely and published freely. This allows the same dataset to support multiple teams and workflows without unnecessary licensing constraints',
  },
  {
    problemTitle: 'High prices',
    problemText:
      'Many providers offer expensive data packages that are not well aligned with the actual needs of the data consumer. Institutions often end up paying for broad coverage, bundled content, or additional functionality that is irrelevant to their use case',
    solutionTitle: 'Lean pricing model',
    solutionText:
      'Dataland follows a shared procurement model in which pricing reflects the effort required to source a dataset. The costs of that sourcing effort are shared across the members who need the data rather than being borne by each institution individually',
  },
];

// ---- Framework cards (spec section 3.9) ----

export interface FrameworkCard {
  title: string;
  subtitle: string;
  description: string;
}

export const FRAMEWORK_CARDS: FrameworkCard[] = [
  {
    title: 'EU Taxonomy',
    subtitle: 'Financials',
    description:
      'The EU Taxonomy Regulation enables financial institutions to assess and report the share of environmentally sustainable economic activities within their portfolios, based on eligibility and alignment metrics',
  },
  {
    title: 'EU Taxonomy',
    subtitle: 'Non-Financials',
    description:
      'The EU Taxonomy Regulation provides a framework for non-financial companies to disclose the extent to which their activities are environmentally sustainable, based on defined technical screening criteria',
  },
  {
    title: 'EU Taxonomy',
    subtitle: 'Nuclear and Gas',
    description:
      'The EU Taxonomy includes specific criteria for nuclear and gas activities under transitional provisions, allowing companies to report their contribution to climate objectives under defined conditions',
  },
  {
    title: 'SFDR',
    subtitle: '',
    description:
      'The Sustainable Finance Disclosure Regulation requires financial market participants to disclose how sustainability risks are integrated into investment decisions and to report Principal Adverse Impact indicators at entity and product level',
  },
  {
    title: 'PCAF',
    subtitle: '',
    description:
      'The PCAF standard provides a methodology for financial institutions to measure and disclose financed emissions associated with their lending and investment portfolios',
  },
  {
    title: 'LkSG',
    subtitle: '',
    description:
      'Lieferkettensorgfaltspflichtengesetz is a German law requiring companies to identify, assess, and manage human rights and environmental risks within their supply chains',
  },
];

// ---- Trusted By logos (spec section 3.6) ----

export interface TrustedByLogo {
  name: string;
  imagePath: string;
}

export const TRUSTED_BY_LOGOS: TrustedByLogo[] = [
  { name: 'Atlas Metrics', imagePath: '/static/logos/logo_atlas_metrics.svg' },
  { name: 'Bantleon', imagePath: '/static/logos/logo_bantleon.svg' },
  { name: 'BayernInvest', imagePath: '/static/logos/logo_bayerninvest.svg' },
  { name: 'BayernLB', imagePath: '/static/logos/logo_bayernlb.png' },
  { name: 'BVI', imagePath: '/static/logos/logo_bvi.png' },
  { name: 'EuroDat', imagePath: '/static/logos/logo_eurodat.svg' },
  { name: 'Laiqon', imagePath: '/static/logos/logo_laiqon.svg' },
  { name: 'Deutsche R\u00fcck', imagePath: '/static/logos/logo_deutsche_rueck.svg' },
  { name: 'd-fine', imagePath: '/static/logos/logo_dfine.svg' },
  { name: 'Hansa-Invest', imagePath: '/static/logos/logo_hansa_invest.svg' },
  { name: 'NORD/LB', imagePath: '/static/logos/logo_nordlb.svg' },
  { name: 'PwC', imagePath: '/static/logos/logo_pwc.svg' },
  { name: 'T-Systems', imagePath: '/static/logos/logo_tsystems.svg' },
  { name: 'Werte-Stiftung', imagePath: '/static/logos/logo_wertestiftung.png' },
];
