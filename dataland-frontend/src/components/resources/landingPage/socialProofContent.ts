export interface SocialProofQuote {
  text: string;
  name: string;
  role: string;
}

export interface SuccessStorySummary {
  title: string;
  summary: string;
  slug: string;
}

export const SOCIAL_PROOF_QUOTES: SocialProofQuote[] = [
  {
    text: 'Dataland is the only platform we know that is open to everyone and based on a non-profit business model.',
    name: 'Sven Schubert',
    role: 'CEO of Envoria',
  },
  {
    text: 'We hope that data availability, coverage and quality is improved, for the benefit of the users, the corporations and society overall.',
    name: 'Rudolf Siebel',
    role: 'Managing Director at BVI German Fund Association',
  },
  {
    text: 'Dataland can provide the data ecosystem we all need to support our transition to stay within 1.5 degrees or within the planetary boundaries.',
    name: 'Matthias Kopp',
    role: 'Director of Sustainable Finance at WWF Germany',
  },
  {
    text: 'We appeal to both investors and companies to make sustainability data available in a timely and cost-efficient manner.',
    name: 'Ingo Speich',
    role: 'Head of Sustainability & Corporate Governance at Deka Investment',
  },
];

// [PLACEHOLDER] All names, companies, and figures below are fictional.
export const SUCCESS_STORY_SUMMARIES: SuccessStorySummary[] = [
  {
    title: 'Closing SFDR Data Gaps for a Mid-Sized Asset Manager',
    summary:
      'A German asset manager with EUR 12 billion AuM needed PAI indicator data for over 400 portfolio companies to meet SFDR disclosure deadlines. Dataland provided structured, quality-assured data extracted from public sustainability reports, eliminating weeks of manual research.',
    slug: 'sfdr-data-gaps-asset-manager',
  },
  {
    title: 'EU Taxonomy Alignment Data for a Regional Bank',
    summary:
      "A German regional bank needed EU Taxonomy alignment data for its corporate loan portfolio to calculate its Green Asset Ratio. Dataland delivered structured Taxonomy KPIs extracted from borrowers' public disclosures, enabling the bank to meet EBA Pillar 3 reporting requirements on schedule.",
    slug: 'eu-taxonomy-alignment-regional-bank',
  },
  {
    title: 'LkSG Supply Chain Due Diligence for an Institutional Investor',
    summary:
      'A large German institutional investor used Dataland to assess LkSG compliance indicators across its equity portfolio holdings. The structured due diligence data enabled the investor to identify supply chain risk concentrations and engage proactively with portfolio companies.',
    slug: 'lksg-supply-chain-due-diligence-investor',
  },
];
