export interface CustomerStorySummary {
  logo: string;
  logoClassName?: string;
  title: string;
  primaryTag: string;
  secondaryTags: string[];
  text: string;
  link: string;
}

export const CUSTOMER_STORY_SUMMARIES: CustomerStorySummary[] = [
  {
    logo: '/static/logos/logo_meag_2026.svg',
    logoClassName: 'scale-[1.28]',
    title: 'Closing SFDR Data Gaps and Simplifying the EU Taxonomy Template Transition',
    primaryTag: 'Asset manager',
    secondaryTags: ['SFDR', 'EU Taxo', 'Audit'],
    text: 'Filling SFDR gaps and EU Taxo template transition',
    link: '/product#meag',
  },
  {
    logo: '/static/logos/logo_nordlb.svg',
    title: 'Primary source of EU Taxonomy data with automated delivery',
    primaryTag: 'Bank',
    secondaryTags: ['EU Taxo', 'API integration'],
    text: 'Primary source of EU Taxonomy data with automated delivery',
    link: '/product#nordlb',
  },
  {
    logo: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.jpg',
    logoClassName: 'scale-[1.3]',
    title: 'Using Dataland as an Independent Source to Validate PAI Data for Audit',
    primaryTag: 'Insurance',
    secondaryTags: ['SFDR', 'Validation', 'Audit'],
    text: 'PAI lineage and source transparency for compliance',
    link: '/product#ovbraunschweig',
  },
];
