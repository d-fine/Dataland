export const configForEutaxonomyFinancialsMLDT = [
  {
    type: "section",
    label: "General",
    expandOnPageLoad: true,
    children: [
      {
        type: "section",
        label: "General",
        expandOnPageLoad: true,
        children: [
          {
            type: "cell",
            label: "Data Date",
            explanation: "The date until when the information collected is valid",
          },
          {
            type: "cell",
            label: "Fiscal Year Deviation",
            explanation: "Does the fiscal year deviate from the calender year?",
          },
          {
            type: "cell",
            label: "Fiscal Year End",
            explanation: "The date the fiscal year ends",
          },
          {
            type: "cell",
            label: "Scope Of Entities",
            explanation:
              "Does a list of legal entities covered by Sust./Annual/Integrated report match with a list of legal entities covered by Audited Consolidated Financial Statement",
          },
          {
            type: "cell",
            label: "EU Taxonomy Activity Level Reporting",
            explanation: "Activity Level disclosure",
          },
          {
            type: "cell",
            label: "Number Of Employees",
            explanation: "Total number of employees (including temporary workers)",
          },
          {
            type: "cell",
            label: "NFRD Mandatory",
            explanation: "The reporting obligation for companies whose number of employees is greater or equal to 500",
          },
        ],
      },
    ],
    labelBadgeColor: "orange",
  },
  {
    type: "section",
    label: "Environmental",
    expandOnPageLoad: false,
    children: [
      {
        type: "section",
        label: "Greenhouse gas emissions ",
        expandOnPageLoad: false,
        children: [
          {
            type: "cell",
            label: "Trading Portfolio",
            explanation: "For Credit Institutions, the trading portfolio as a percentage of total assets",
          },
          {
            type: "cell",
            label: "Scope 2 GHG emissions",
            explanation: "What is the amount of the company's Scope 2 emissions?",
          },
          {
            type: "cell",
            label: "Scope 3 GHG emissions",
            explanation: "What is the amount of the company's Scope 3 emissions ?",
          },
          {
            type: "cell",
            label: "Enterprise Value",
            explanation: "Company Enterprise Value",
          },
          {
            type: "cell",
            label: "Total Revenue",
            explanation: "Company Total Revenue ",
          },
          {
            type: "cell",
            label: "Fossil Fuel Sector Exposure",
            explanation: "Does the company derive any revenues from fossil fuels?",
          },
        ],
      },
    ],
  },
];
