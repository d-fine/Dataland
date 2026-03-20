export interface SuccessStory {
  slug: string;
  title: string;
  summary: string;
  companyType: string;
  framework: string;
  challenge: string;
  process: string;
  result: string;
  quote?: {
    text: string;
    attribution: string;
    role: string;
  };
  processSketchPath?: string;
}

// [PLACEHOLDER] All names, companies, quotes, and figures below are fictional.
// Replace with real customer stories before production launch.
export const SUCCESS_STORIES: SuccessStory[] = [
  {
    slug: 'sfdr-data-gaps-asset-manager',
    title: 'Closing SFDR Data Gaps for a Mid-Sized Asset Manager',
    summary:
      'A German asset manager with EUR 12 billion AuM needed PAI indicator data for over 400 portfolio companies to meet SFDR disclosure deadlines. Dataland provided structured, quality-assured data extracted from public sustainability reports, eliminating weeks of manual research.',
    companyType: 'Mid-sized asset manager',
    framework: 'SFDR',
    challenge:
      'With SFDR Level 2 requirements in full effect, the firm faced a critical data gap: PAI indicators were missing for nearly 60% of their portfolio holdings, particularly among small- and mid-cap European companies. Commercial ESG data providers covered the large caps, but the long tail of smaller holdings remained a blind spot. The compliance team estimated that sourcing this data manually from published sustainability reports would require over 800 person-hours per reporting cycle. The deadline for the annual PAI statement was approaching, and the team lacked both the capacity and the specialized knowledge to extract the required data points from diverse report formats.',
    process:
      "The firm submitted a batch request through Dataland, uploading a list of 420 portfolio companies with missing SFDR PAI data. Dataland's AI extraction engine processed publicly available sustainability reports, annual reports, and non-financial disclosures for each company, mapping extracted data points to the 18 mandatory PAI indicators. A human quality assurance review was conducted on all AI-extracted data before publication. The structured datasets were made available on the platform within three weeks, and the firm downloaded the complete dataset via Dataland's API for direct integration into their PAI statement generation workflow.",
    result:
      'The firm achieved 94% PAI indicator coverage across its portfolio, up from 40% before using Dataland. The entire process -- from data request to API export -- took 22 days, compared to an estimated 10 weeks of manual effort. The compliance team reported that the structured format reduced downstream processing time by approximately 70%.',
    quote: {
      text: 'We went from dreading PAI reporting season to having a reliable, repeatable process. Dataland gave us coverage where no commercial provider could, and the data quality was excellent.',
      attribution: 'Placeholder Name',
      role: 'Head of ESG Compliance, Placeholder Asset Management GmbH',
    },
    processSketchPath: '/static/images/process-sketch-sfdr.svg',
  },
  {
    slug: 'eu-taxonomy-alignment-regional-bank',
    title: 'EU Taxonomy Alignment Data for a Regional Bank',
    summary:
      "A German regional bank needed EU Taxonomy alignment data for its corporate loan portfolio to calculate its Green Asset Ratio. Dataland delivered structured Taxonomy KPIs extracted from borrowers' public disclosures, enabling the bank to meet EBA Pillar 3 reporting requirements on schedule.",
    companyType: 'Regional bank',
    framework: 'EU Taxonomy',
    challenge:
      'As a credit institution subject to CRR requirements, the bank needed to calculate and disclose its Green Asset Ratio (GAR) under the EU Taxonomy regulation. This required Taxonomy alignment data -- specifically revenue, CapEx, and OpEx KPIs -- for over 300 corporate borrowers in its loan book. Most of these borrowers were mid-market German companies that did not proactively share structured Taxonomy data with their lenders. The bank had attempted a manual outreach approach, sending questionnaires to borrowers, but response rates were below 15%. Without reliable alignment data, the bank faced the prospect of reporting near-zero GAR figures despite having a portfolio with significant exposure to climate-relevant economic activities.',
    process:
      "The bank provided Dataland with a list of corporate borrowers and the specific Taxonomy KPIs needed for GAR calculation. Dataland's platform identified and retrieved publicly available annual reports, non-financial statements, and Taxonomy-specific disclosures for each company. The AI extraction engine parsed these documents to identify reported Taxonomy-eligible and Taxonomy-aligned revenue, CapEx, and OpEx figures, along with the underlying economic activities and environmental objectives. Each extracted dataset was reviewed by Dataland's QA team for accuracy and completeness. The verified data was delivered via API in a format directly compatible with the bank's regulatory reporting system.",
    result:
      "The bank obtained Taxonomy alignment data for 78% of its corporate loan portfolio by value, transforming its GAR disclosure from a near-zero placeholder to a meaningful metric. The data was delivered six weeks ahead of the EBA reporting deadline, giving the bank's risk and compliance teams sufficient time for internal validation and board-level review.",
    quote: {
      text: 'Our borrower questionnaire approach was simply not working. Dataland allowed us to source Taxonomy data from public disclosures at scale -- something we could not have done internally without a dedicated team.',
      attribution: 'Placeholder Name',
      role: 'Head of Regulatory Reporting, Placeholder Landesbank',
    },
    processSketchPath: '/static/images/process-sketch-eu-taxonomy.svg',
  },
  {
    slug: 'lksg-supply-chain-due-diligence-investor',
    title: 'LkSG Supply Chain Due Diligence for an Institutional Investor',
    summary:
      'A large German institutional investor used Dataland to assess LkSG compliance indicators across its equity portfolio holdings. The structured due diligence data enabled the investor to identify supply chain risk concentrations and engage proactively with portfolio companies.',
    companyType: 'Institutional investor',
    framework: 'LkSG',
    challenge:
      "Under the German Supply Chain Due Diligence Act (LkSG), the institutional investor -- a pension fund with EUR 45 billion in assets -- needed to understand the human rights and environmental due diligence practices of companies in its equity portfolio. While the investor was not directly subject to LkSG obligations, its board had committed to voluntary alignment with LkSG standards as part of its responsible investment policy. The portfolio included over 250 German and European companies, many of which had complex global supply chains. Assessing each company's grievance mechanisms, risk analysis processes, preventive measures, and remedial actions from published reports was a task far beyond the capacity of the three-person ESG integration team.",
    process:
      "The investor submitted its portfolio holdings to Dataland and requested LkSG-relevant due diligence data for each company. Dataland's AI engine analyzed published sustainability reports, human rights policy documents, supply chain disclosures, and BAFA-related public statements. The extraction focused on the core LkSG requirements: risk analysis methodology, preventive and remedial measures, grievance mechanisms, and documentation practices. All data points were mapped to a structured LkSG framework and reviewed by Dataland's quality assurance team. The final dataset was delivered through the platform's download function, with each company's data linked to the source document for full traceability.",
    result:
      "The investor received structured LkSG due diligence assessments for 230 of its 250 portfolio companies within four weeks. The data revealed that 35% of assessed companies had incomplete or missing grievance mechanisms -- a finding that directly informed the investor's engagement priorities for the following year. The ESG integration team estimated that the Dataland-sourced data saved approximately 1,200 hours of analyst time.",
    quote: {
      text: 'LkSG compliance data was a black box for us before Dataland. Now we have a structured, source-linked dataset that our portfolio managers actually use in their engagement conversations.',
      attribution: 'Placeholder Name',
      role: 'Head of ESG Integration, Placeholder Pensionskasse',
    },
    processSketchPath: '/static/images/process-sketch-lksg.svg',
  },
];
