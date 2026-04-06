export interface CustomerStorySummary {
  logo: string;
  tag: string;
  text: string;
  link: string;
}

export const CUSTOMER_STORY_SUMMARIES: CustomerStorySummary[] = [
  {
    logo: '/static/logos/logo_Meag_2026.svg',
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
    logo: '/static/logos/logo_Oeffentliche_Wort-Bildmarke_Blau_RGB.jpg',
    tag: 'Insurance',
    text: 'PAI lineage and source transparency for compliance',
    link: '/product#ovbraunschweig',
  },
];
