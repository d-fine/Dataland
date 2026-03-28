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
