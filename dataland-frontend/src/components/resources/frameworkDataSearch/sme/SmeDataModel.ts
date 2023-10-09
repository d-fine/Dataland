import { type SmeData } from "@clients/backend";
import { type Category } from "@/utils/GenericFrameworkTypes";

export const smeDataModel = [
  {
    name: "general",
    label: "General",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "basicInformation",
        label: "Basic Information",
        fields: [
          {
            name: "sector",
            label: "Sector",
            description:
              "Please select the industry sector in which your company was mainly active in the relevant fiscal year.",
            unit: "",
            component: "NaceCodeFormField",
            evidenceDesired: false,
            required: true,
            showIf: (): boolean => true,
            validation: "required|length:1",
          },
          {
            name: "addressOfHeadquarters",
            label: "Address Of Headquarters",
            description: "Please provide the full address of your company's headquarters in the relevant fiscal year.",
            unit: "",
            component: "AddressFormField",
            evidenceDesired: false,
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
          {
            name: "numberOfEmployees",
            label: "Number of Employees",
            description: "Please provide the number of workforce employed by your company in the relevant fiscal year.",
            unit: "",
            component: "NumberFormField",
            evidenceDesired: false,
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
          {
            name: "fiscalYearStart",
            label: "Fiscal Year Start",
            description: "Please provide the starting date of the company's fiscal year to which you refer.",
            unit: "",
            component: "DateFormField",
            evidenceDesired: false,
            required: true,
            showIf: (): boolean => true,
            validation: "required",
          },
        ],
      },
      {
        name: "companyFinancials",
        label: "Company Financials",
        fields: [
          {
            name: "revenueInEUR",
            label: "Revenue",
            description: "Please provide your company's revenue in the relevant year in Euro.",
            unit: "EUR",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "operatingCostInEUR",
            label: "Operating Cost",
            description: "Please provide your company's operating cost in the relevant fiscal year in Euro.",
            unit: "EUR",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "capitalAssetsInEUR",
            label: "Capital assets",
            description:
              "Please provide the value of your company's capital assets in the relevant fiscal year in Euro.",
            unit: "EUR",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "production",
    label: "Production",
    color: "orange",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "sites",
        label: "Sites",
        fields: [
          {
            name: "listOfProductionSites",
            label: "List of Production Sites",
            description:
              "Please provide the percentage of revenue generated at the provided main branch/production site or your headquarters relative to your company's total revenue in the relevant fiscal year.",
            unit: "",
            component: "ProductionSitesFormFieldSme",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "products",
        label: "Products",
        fields: [
          {
            name: "listOfProducts",
            label: "List of Products",
            description:
              "Please provide the HS-Code of the product category generating the highest revenue in your company in the relevant fiscal year. Please use the provided link to identify the product category code.",
            unit: "",
            component: "ProductsFormFieldSme",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "power",
    label: "Power",
    color: "yellow",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "investments",
        label: "Investments",
        fields: [
          {
            name: "percentageRangeForInvestmentsInEnhancingEnergyEfficiency",
            label: "Investments in enhancing energy efficiency",
            description:
              "Please provide the fraction of your company's total investments that was primarily spent to enhance energy efficiency in the last fiscal year.",
            unit: "",
            component: "RadioButtonsFormField",
            evidenceDesired: false,
            options: [
              {
                label: "< 1%",
                value: "LessThan1",
              },
              {
                label: "1-5%",
                value: "Between1And5",
              },
              {
                label: "5-10%",
                value: "Between5And10",
              },
              {
                label: "10-15%",
                value: "Between10And15",
              },
              {
                label: "15-20%",
                value: "Between15And20",
              },
              {
                label: "20-25%",
                value: "Between20And25",
              },
              {
                label: "> 25%",
                value: "GreaterThan25",
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
      {
        name: "consumption",
        label: "Consumption",
        fields: [
          {
            name: "powerConsumptionInMWh",
            label: "Power consumption",
            description: "Please provide your company's power consumption in the relevant fiscal year in MWh.",
            unit: "MWh",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "powerFromRenewableSources",
            label: "Power from renewable sources",
            description:
              "Please provide information whether your company has been primarily using power from renewable sources in the relevant fiscal year.",
            unit: "",
            component: "YesNoFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "energyConsumptionHeatingAndHotWaterInMWh",
            label: "Energy consumption heating and hot water",
            description:
              "Please provide your company's power consumption for heating and hot water generation in the relevant fiscal year in MWh.",
            unit: "MWh",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "primaryEnergySourceForHeatingAndHotWater",
            label: "Primary energy source for heating and hot water",
            description:
              "Please provide the energy source primarily used by your company for heating/hot water generation in the relevant fiscal year.",
            unit: "",
            component: "RadioButtonsFormField",
            evidenceDesired: false,
            options: [
              {
                label: "Oil",
                value: "oil",
              },
              {
                label: "Gas",
                value: "gas",
              },
              {
                label: "Electric",
                value: "electric",
              },
              {
                label: "District Heating",
                value: "district heating",
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
          {
            name: "percentageRangeForEnergyConsumptionCoveredByOwnRenewablePowerGeneration",
            label: "Energy consumption covered by own renewable power generation",
            description:
              "Please provide the portion of consumed power generated by your own renewable sources relative to your company's total power consumption in the relevant fiscal year.",
            unit: "",
            component: "RadioButtonsFormField",
            evidenceDesired: false,
            options: [
              {
                label: "< 25%",
                value: "LessThan25",
              },
              {
                label: "25-50%",
                value: "Between25And50",
              },
              {
                label: "50-75%",
                value: "Between50And75",
              },
              {
                label: "> 75%",
                value: "GreaterThan75",
              },
            ],
            required: false,
            showIf: (): boolean => true,
          },
        ],
      },
    ],
  },
  {
    name: "insurances",
    label: "Insurances",
    color: "blue",
    showIf: (): boolean => true,
    subcategories: [
      {
        name: "naturalHazards",
        label: "Natural Hazards",
        fields: [
          {
            name: "insuranceAgainstNaturalHazards",
            label: "Insurance against natural hazards",
            description:
              "Please provide information whether your company has insurance against natural hazards at its branch/production site generating most revenue, or its headquarters.",
            unit: "",
            component: "YesNoFormField",
            evidenceDesired: false,
            required: false,
            showIf: (): boolean => true,
            certificateRequiredIfYes: false,
          },
          {
            name: "amountCoveredByInsuranceAgainstNaturalHazards",
            label: "Amount covered by insurance against natural hazards",
            description:
              "Please provide the amount covered by the insurance in EUR. In case your company has different policies for different natural hazards please provide the average amount covered.",
            unit: "",
            component: "NumberFormField",
            evidenceDesired: false,
            required: false,
            showIf: (dataModel: SmeData): boolean =>
              dataModel?.insurances?.naturalHazards?.insuranceAgainstNaturalHazards === "Yes",
          },
          {
            name: "naturalHazardsCovered",
            label: "Natural Hazards covered",
            description: "Please identify all natural hazards covered by your insurance.",
            unit: "",
            component: "MultiSelectFormField",
            evidenceDesired: false,
            options: [
              {
                label: "Hail",
                value: "hail",
              },
              {
                label: "Wind",
                value: "wind",
              },
              {
                label: "Flooding",
                value: "flooding",
              },
              {
                label: "Earth Quakes",
                value: "earth quakes",
              },
              {
                label: "Avalanches",
                value: "avalanches",
              },
              {
                label: "Snow",
                value: "snow",
              },
            ],
            required: false,
            showIf: (dataModel: SmeData): boolean =>
              dataModel?.insurances?.naturalHazards?.insuranceAgainstNaturalHazards === "Yes",
            placeholder: "Select Options",
          },
        ],
      },
    ],
  },
] as Array<Category>;
