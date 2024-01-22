import { type Category } from "@/utils/GenericFrameworkTypes";
import { SfdrData } from "@clients/backend";

export const sfdrDataModel = [    {
        name: "general",
        label: "General",
        color: "",
        showIf: ():boolean => true,
        subcategories: [    {
            name: "general",
            label: "General",
            fields: [
                 {
                name: "dataDate",
                label: "Data Date",
                description: "The date until when the information collected is valid",
    
    
                component: "DateFormField",
                required: true,
                showIf: ():boolean => true,
                validation: "required",
    
                },
            {
                name: "fiscalYearEnd",
                label: "Fiscal Year End",
                description: "The date the fiscal year ends",
    
    
                component: "DateFormField",
                required: true,
                showIf: ():boolean => true,
                validation: "required",
    
                },
            {
                name: "referencedReports",
                label: "Referenced Reports",
                description: "Please upload all relevant reports for this dataset in the PDF format.",
    
    
                component: "UploadReports",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scopeOfEntities",
                label: "Scope Of Entities",
                description: "Does a list of legal entities covered by Sust./Annual/Integrated report match with a list of legal entities covered by Audited Consolidated Financial Statement ",
    
    
                component: "YesNoNaFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        ],
        },
    {
        name: "environmental",
        label: "Environmental",
        color: "",
        showIf: ():boolean => true,
        subcategories: [    {
            name: "greenhouseGasEmissions",
            label: "Greenhouse gas emissions ",
            fields: [
                 {
                name: "scope1GhgEmissionsInTonnes",
                label: "Scope 1 GHG emissions",
                description: "Scope 1 carbon emissions, namely emissions generated from sources that are controlled by the company that issues the underlying assets",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope2GhgEmissionsInTonnes",
                label: "Scope 2 GHG emissions",
                description: "Scope 2 carbon emissions, namely emissions from the consumption of purchased electricity, steam, or other sources of energy generated upstream from the company that issues the underlying assets",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope2GhgEmissionsLocationBasedInTonnes",
                label: "Scope 2 GHG emissions (location-based)",
                description: "Scope 2 carbon emissions computed using the location-based method",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope2GhgEmissionsMarketBasedInTonnes",
                label: "Scope 2 GHG emissions (market-based)",
                description: "Scope 2 carbon emissions computed using the market-based method",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope1And2GhgEmissionsInTonnes",
                label: "Scope 1 and 2 GHG emissions",
                description: "Sum of scope 1 and 2 carbon emissions",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope1And2GhgEmissionsLocationBasedInTonnes",
                label: "Scope 1 and 2 GHG emissions (location-based)",
                description: "Sum of scope 1 and 2 carbon emissions, using the location-based method to compute scope 2 carbon emissions",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope1And2GhgEmissionsMarketBasedInTonnes",
                label: "Scope 1 and 2 GHG emissions (market-based)",
                description: "Sum of scope 1 and 2 carbon emissions, using the market-based method to compute scope 2 carbon emissions",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope3GhgEmissionsInTonnes",
                label: "Scope 3 GHG emissions",
                description: "Scope 3 carbon emissions in tonnes, i.e. all indirect upstream and downstream emissions that are not included in scope 2",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "scope1And2And3GhgEmissionsInTonnes",
                label: "Scope 1 and 2 and 3 GHG emissions",
                description: "Sum of scope 1, 2 and 3 carbon emissions",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "enterpriseValue",
                label: "Enterprise Value",
                description: "The sum, at fiscal year-end, of the market capitalisation of ordinary shares, the market capitalisation of preferred shares, and the book value of total debt and non-controlling interests, without the deduction of cash or cash equivalents. See also Regulation, Annex I top (4).",
    
    
                component: "CurrencyDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "totalRevenue",
                label: "Total Revenue",
                description: "Total revenue for the financial year. i.e., income arising in the course of an entity\'s ordinary activities, the amounts derived from the sale of products and the provision of services after deducting sales rebates and value added tax and other taxes directly linked to turnover. Overall turnover is equivalent to a firm\'s total revenues over some period of time (millions)",
    
    
                component: "CurrencyDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "carbonFootprintInTonnesPerMillionEURRevenue",
                label: "Carbon footprint",
                description: "Tonnes GHG emissions / EUR million enterprise value",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "ghgIntensityInTonnesPerMillionEURRevenue",
                label: "GHG intensity",
                description: "Tonnes of GHG emissions / EUR million revenue",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "fossilFuelSectorExposure",
                label: "Fossil Fuel Sector Exposure",
                description: "Do you derive any revenues from exploration, mining, extraction, production, processing, storage, refining or distribution, including transportation, storage and trade, of fossil fuels as defined in Article 2, point (62), of Regulation (EU) 2018/1999 of the European Parliament and of the Council? See also Regulation, Annex I, top (5).",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "energyPerformance",
            label: "Energy performance",
            fields: [
                 {
                name: "renewableEnergyProductionInGWh",
                label: "Renewable Energy Production",
                description: "Total value of renewable energy produced, meaning energy from non-fossil sources, namely wind, solar (solar thermal and solar photovoltaic) and geothermal energy, ambient energy, tide, wave and other ocean energy, hydropower, biomass, landfill gas, sewage treatment plant gas, and biogas. See also Regulation, Annex I, top (6).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "renewableEnergyConsumptionInGWh",
                label: "Renewable Energy Consumption",
                description: "Total value of renewable energy consumed, meaning energy from non-fossil sources, namely wind, solar (solar thermal and solar photovoltaic) and geothermal energy, ambient energy, tide, wave and other ocean energy, hydropower, biomass, landfill gas, sewage treatment plant gas, and biogas. See also Regulation, Annex I, top (6).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyProductionInGWh",
                label: "Non-Renewable Energy Production",
                description: "Total value of non-renewable energy produced, meaning energy from sources other than non-fossil sources. See also Regulation, Annex I, top (7).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "relativeNonRenewableEnergyProductionInPercent",
                label: "Relative Non-Renewable Energy Production",
                description: "Share of non-renewable energy production from total energy production (i.e. renewable plus non-renewable).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionInGWh",
                label: "Non-Renewable Energy Consumption",
                description: "Total value of non-renewable energy consumed, meaning energy from sources other than non-fossil sources.",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "relativeNonRenewableEnergyConsumptionInPercent",
                label: "Relative Non-Renewable Energy Consumption",
                description: "Share of non-renewal energy consumption from total energy consumption (i.e. renewable plus non-renewable).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "applicableHighImpactClimateSectors",
                label: "Applicable High Impact Climate Sectors",
                description: "Please select any sector(s) applicable activities (NACE Codes A-H, L)",
    
    
                component: "HighImpactClimateSectorsFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "totalHighImpactClimateSectorEnergyConsumptionInGWh",
                label: "Total High Impact Climate Sector Energy Consumption",
                description: "High impact climate sectors’ means the sectors listed in Sections A to H and Section L of Annex I to Regulation (EC) No 1893/2006 of the European Parliament and of the Council (Regulation (EC) No 1893/2006 of the European Parliament and of the Council of 20 December 2006 establishing the statistical classification of economic activities NACE Revision 2 and amending Council Regulation (EEC) No 3037/90 as well as certain EC Regulations on specific statistical domains (OJ L 393, 30.12.2006, p. 1)).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionFossilFuelsInGWh",
                label: "Non-Renewable Energy Consumption Fossil Fuels",
                description: "Energy consumption from fossil fuels (sum of crude oil, natural gas, nuclear energy, lignite and coal) (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionCrudeOilInGWh",
                label: "Non-Renewable Energy Consumption Crude Oil",
                description: "Energy consumption from crude oil (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionNaturalGasInGWh",
                label: "Non-Renewable Energy Consumption Natural Gas",
                description: "Energy consumption from natural gas (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionLigniteInGWh",
                label: "Non-Renewable Energy Consumption Lignite",
                description: "Energy consumption from lignite (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionCoalInGWh",
                label: "Non-Renewable Energy Consumption Coal",
                description: "Energy consumption from coal (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionNuclearEnergyInGWh",
                label: "Non-Renewable Energy Consumption Nuclear Energy",
                description: "Energy consumption from nuclear energy (Uranium) (non-renewable energy source)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRenewableEnergyConsumptionOtherInGWh",
                label: "Non-Renewable Energy Consumption Other",
                description: "Energy consumption from any other available (used) non-renewable source of energy",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "biodiversity",
            label: "Biodiversity",
            fields: [
                 {
                name: "primaryForestAndWoodedLandOfNativeSpeciesExposure",
                label: "Primary Forest And Wooded Land Of Native Species Exposure",
                description: "Do you have sites/operations located in or near to primary forest and other wooded areas where activities of those sites/operations negatively affect those areas? See also Regulation, Annex I, table 1, indicator nr. 7).",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "protectedAreasExposure",
                label: "Protected Areas Exposure",
                description: "Do you have sites/operations located in or near protected areas where activities of those sites/operations negatively affect those areas? See also Regulation, Annex I, table 1, indicator nr. 7).",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "rareOrEndangeredEcosystemsExposure",
                label: "Rare Or Endangered Ecosystems Exposure",
                description: "Do you have sites/operations in or near areas designated for the protection of species (including flora and fauna) and where the activities of those sites/operations lead to the deterioration of natural habitats and the habitats of those species and disturb the species for which the protected area has been designated? See also Regulation, Annex I, table 1, indicator nr. 7 and Annex I, definition 18(a).",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "highlyBiodiverseGrasslandExposure",
                label: "Highly Biodiverse Grassland Exposure",
                description: "Do you have sites/operations located in highly biodiverse grassland that is: (i) natural, namely, grassland that would remain grassland in the absence of human intervention and which maintains the natural species composition and ecological characteristics and processes; or (ii) non-natural, namely, grassland that would cease to be grassland in the absence of human intervention and which is species-rich and not degraded, unless evidence is provided that the harvesting of the raw material is necessary to preserve its grassland status?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "water",
            label: "Water",
            fields: [
                 {
                name: "emissionsToWaterInTonnes",
                label: "Emissions To Water",
                description: "Emissions to water (direct nitrates, direct phosphate emissions, direct pesticides) to water (tonnes)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "waterConsumptionInCubicMeters",
                label: "Water Consumption",
                description: "Amount of water consumed by the company",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "waterReusedInCubicMeters",
                label: "Water Reused",
                description: "Amount of water recycled and reused by the company. Linked to Regulation, Annex I, Table 2, metric 6.2.",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "relativeWaterUsageInCubicMetersPerMillionEURRevenue",
                label: "Relative Water Usage",
                description: "Average amount in cubic meters of fresh water used per million EUR revenue",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "waterManagementPolicy",
                label: "Water Management Policy",
                description: "Does the company have policies and procedures for water management? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "highWaterStressAreaExposure",
                label: "High Water Stress Area Exposure",
                description: "Do you have sites/operations in or near ‘areas of high water stress’, meaning regions where the percentage of total water withdrawn is high (40-80 %) or extremely high (greater than 80 %) where the activities of those sites/operations negatively affect those areas? See also Regulation, Annex I, definition 13.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "waste",
            label: "Waste",
            fields: [
                 {
                name: "hazardousAndRadioactiveWasteInTonnes",
                label: "Hazardous and Radioactive Waste",
                description: "Tonnes of hazardous waste and radioactive waste generated, which are Explosives, Oxidizing substances, Highly flammable, Flammable, Harmful, Toxic, Carcinogenic, Corrosive, Infectious, Toxic for reproduction, Mutagenic, waste which releases toxic or very toxic gases in contact with water, air or an acid, Sensitizing, Ecotoxic, waste capable by any means after disposal of yielding substance which possesses any of the characteristics listed above (tonnes)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "manufactureOfAgrochemicalPesticidesProducts",
                label: "Manufacture Of Agrochemical Pesticides Products",
                description: "Are you involved in the manufacturing of pesticides and other agrochemical products? (see activities which fall under Division 20.2 of Annex I to Regulation (EC) No 1893/2006)",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "landDegradationDesertificationSoilSealingExposure",
                label: "Land Degradation Desertification Soil Sealing Exposure",
                description: "Is the company involved in activities which cause land degradation, desertification or soil sealing? See also Regulation, Annex I, Table 2, indicator 10.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "sustainableAgriculturePolicy",
                label: "Sustainable Agriculture Policy",
                description: "Do you have sustainable land/agriculture practices or policies? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "sustainableOceansAndSeasPolicy",
                label: "Sustainable Oceans And Seas Policy",
                description: "Do you have sustainable oceans/seas practices or policies? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "nonRecycledWasteInTonnes",
                label: "Non-Recycled Waste",
                description: "Value of non-recycled waste generated. \"Non-recycled waste\" means any waste not recycled within the meaning of ‘recycling’ in Article 3(17) of Directive 2008/98/EC.",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "threatenedSpeciesExposure",
                label: "Threatened Species Exposure",
                description: "Do you have operations which affect threatened species?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "biodiversityProtectionPolicy",
                label: "Biodiversity Protection Policy",
                description: "Do you have a biodiversity protection policy covering operational sites owned, leased, managed in, or adjacent to, a protected area or an area of high biodiversity value outside protected areas? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "deforestationPolicy",
                label: "Deforestation Policy",
                description: "Do you have a policy to address deforestation? If yes, please share the policy with us. \"Deforestation\" means the human-induced conversion of forested land to non-forested land, which can be permanent, when this change is definitive, or temporary when this change is part of a cycle that includes natural or assisted regeneration, according to the Intergovernmental Science-Policy Platform on Biodiversity and Ecosystem Services (IPBES) as referred to in paragraph 100 of Decision No 1386/2013/EU of the European Parliament and of the Council.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "emissions",
            label: "Emissions",
            fields: [
                 {
                name: "emissionsOfInorganicPollutantsInTonnes",
                label: "Emissions of Inorganic Pollutants",
                description: "Inorganic pollutants such as those arising due to radiant energy and noise, heat, or light, including arsenic, cadmium, lead, mercury, chromium, aluminum, nitrates, nitrites, and fluorides or contaminants of water such as arsenic, fluoride, iron, nitrate, heavy metals, etc.",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "emissionsOfAirPollutantsInTonnes",
                label: "Emissions of Air Pollutants",
                description: "Air pollutants (Direct Sulphur dioxides (Sox/SO2) emissions, direct nitrogen oxides (NOx/NO2) emissions, direct ammonia (NH3) emissions, direct particulate matter (PM2.5) emissions, direct non-methane volatile organic compounds (NMVOC) emissions, direct total heavy metals (HM) emissions (encompassing cadmium, mercury and lead)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "emissionsOfOzoneDepletionSubstancesInTonnes",
                label: "Emissions of Ozone Depletion Substances",
                description: "Tonnes of ozone depletion substances, chemicals that destroy the earth\'s protective ozone layer. They include: chlorofluorocarbons (CFCs), halons, carbon tetrachloride (CCl4), methyl chloroform (CH3CCl3), hydrobromofluorocarbons (HBFCs), hydrochlorofluorocarbons (HCFCs), methyl bromide (CH3Br), bromochloromethane (CH2BrCl)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "carbonReductionInitiatives",
                label: "Carbon Reduction Initiatives",
                description: "Do you have any policies or procedures for carbon emission reduction aimed at aligning with the Paris Agreement?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        ],
        },
    {
        name: "social",
        label: "Social",
        color: "",
        showIf: ():boolean => true,
        subcategories: [    {
            name: "socialAndEmployeeMatters",
            label: "Social and employee matters",
            fields: [
                 {
                name: "humanRightsLegalProceedings",
                label: "Human Rights Legal Proceedings",
                description: "Have you been involved in Human Rights related legal proceedings?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "iloCoreLabourStandards",
                label: "ILO Core Labour Standards",
                description: "Do you abide by the ILO Core Labour Standards?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "environmentalPolicy",
                label: "Environmental Policy",
                description: "Do you have an environmental policy? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "corruptionLegalProceedings",
                label: "Corruption Legal Proceedings",
                description: "Have you been involved in corruption related legal proceedings?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "transparencyDisclosurePolicy",
                label: "Transparency Disclosure Policy",
                description: "Do you have a transparency policy? If yes, please share the policy with us. According to the OECD Guidelines for Multinational Enterprises, multinational companies should inform the public not only about their financial performance, but also about all of the important aspects of their business activities, such as how they are meeting social and environmental standards and what risks they foresee linked to their business activities.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "humanRightsDueDiligencePolicy",
                label: "Human Rights Due Diligence Policy",
                description: "Do you have policies in place to support/respect human rights and carry out due diligence to ensure that the business activities do not have a negative human rights impact? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "policyAgainstChildLabour",
                label: "Policy against Child Labour",
                description: "Do you have policies in place to abolish all forms of child labour? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "policyAgainstForcedLabour",
                label: "Policy against Forced Labour",
                description: "Do you have policies in place to abolish all forms of forced labour? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "policyAgainstDiscriminationInTheWorkplace",
                label: "Policy against Discrimination in the Workplace",
                description: "Do you have policies in place to eliminate discrimination in the workplace? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "iso14001Certificate",
                label: "ISO 14001 Certificate",
                description: "Is your company ISO 14001 certified (Environmental Management)? If yes, please share the certificate with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "policyAgainstBriberyAndCorruption",
                label: "Policy against Bribery and Corruption",
                description: "Do you have a policy on anti-corruption and anti-bribery consistent with the United Nations Convention against Corruption? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "fairBusinessMarketingAdvertisingPolicy",
                label: "Fair Business Marketing Advertising Policy",
                description: "Do you have policies and procedures in place to apply fair business, marketing and advertising practices and to guarantee the safety and quality of the goods and services? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "technologiesExpertiseTransferPolicy",
                label: "Technologies Expertise Transfer Policy",
                description: "Do you have policies and procedures in place to permit the transfer and rapid dissemination of technologies and expertise? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "fairCompetitionPolicy",
                label: "Fair Competition Policy",
                description: "Do you have policies and procedures in place related to fair competition and anti-competitive cartels? If yes, please share the relevant documents with us.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "violationOfTaxRulesAndRegulation",
                label: "Violation Of Tax Rules And Regulation",
                description: "Are you involved in violation of OECD Guidelines for Multinational Enterprises for Taxation: In the field of taxation, multinational enterprises should make their contribution to public finances within the framework of applicable law and regulations, in accordance with the tax rules and regulations of the host countries, and should cooperate with the tax authorities.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "unGlobalCompactPrinciplesCompliancePolicy",
                label: "UN Global Compact Principles Compliance Policy",
                description: "Do you have a policy to monitor compliance with the UNGC principles or OECD Guidelines for Multinational Enterprises? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "oecdGuidelinesForMultinationalEnterprisesGrievanceHandling",
                label: "OECD Guidelines For Multinational Enterprises Grievance Handling",
                description: "Do you have grievance / complaints handling mechanisms to address violations of the UNGC principles or OECD Guidelines for Multinational Enterprises?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "averageGrossHourlyEarningsMaleEmployees",
                label: "Average Gross Hourly Earnings Male Employees",
                description: "Average gross hourly earnings of male employees",
    
    
                component: "CurrencyDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "averageGrossHourlyEarningsFemaleEmployees",
                label: "Average Gross Hourly Earnings Female Employees",
                description: "Average gross hourly earnings of female employees",
    
    
                component: "CurrencyDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "unadjustedGenderPayGapInPercent",
                label: "Unadjusted gender pay gap",
                description: "Average unadjusted gender pay gap (female to male ratio, only considering gender)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "femaleBoardMembers",
                label: "Female Board Members",
                description: "Number of females on the board, i.e. means the administrative, management or supervisory body of a company",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "maleBoardMembers",
                label: "Male Board Members",
                description: "Number of males on the board, i.e. means the administrative, management or supervisory body of a company.",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "boardGenderDiversityInPercent",
                label: "Board gender diversity",
                description: "Average ratio of female to male board members, expressed as a percentage of all board members",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "controversialWeaponsExposure",
                label: "Controversial Weapons Exposure",
                description: "Involvement in the manufacture or selling of controversial weapons such as anti- personnel mines, cluster munitions, chemical weapons and biological weapons.",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "workplaceAccidentPreventionPolicy",
                label: "Workplace Accident Prevention Policy",
                description: "Do you have workplace accident prevention policy? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "rateOfAccidentsInPercent",
                label: "Rate Of Accidents",
                description: "(number of accidents * 200,000) / number of hours worked by all employees",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "workdaysLostInDays",
                label: "Workdays Lost",
                description: "Number of workdays lost to injuries, accidents, fatalities or illness",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "supplierCodeOfConduct",
                label: "Supplier Code Of Conduct",
                description: "Do you have a supplier code of conduct (against unsafe working conditions, precarious work, child labour and forced labour)? If yes, please share the document with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "grievanceHandlingMechanism",
                label: "Grievance Handling Mechanism",
                description: "Do you have any grievance/complaints handling mechanism related to employee matters?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "whistleblowerProtectionPolicy",
                label: "Whistleblower Protection Policy",
                description: "Do you have a policy on the protection of whistleblowers? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "reportedIncidentsOfDiscrimination",
                label: "Reported Incidents Of Discrimination",
                description: "Number of reported discrimination-related incidents",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "sanctionedIncidentsOfDiscrimination",
                label: "Sanctioned Incidents Of Discrimination",
                description: "Number of discrimination related incidents reported that lead to any kind of penalty and/or fine",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "ceoToEmployeePayGapRatio",
                label: "CEO to Employee Pay Gap Ratio",
                description: "Annual total compensation for the highest compensated individual divided by the median annual total compensation for all employees (excluding the highest-compensated individual).",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "excessiveCeoPayRatioInPercent",
                label: "Excessive CEO pay ratio",
                description: "Average ratio of the annual total compensation for the highest compensated individual to the median annual total compensation for all employees (excluding the highest-compensated individual)",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "greenSecurities",
            label: "Green securities",
            fields: [
                 {
                name: "securitiesNotCertifiedAsGreen",
                label: "Securities Not Certified As Green",
                description: "Do you have securities in investments not certified as green under a future EU legal act setting up an EU Green Bond Standard?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        {
            name: "humanRights",
            label: "Human rights",
            fields: [
                 {
                name: "humanRightsPolicy",
                label: "Human Rights Policy",
                description: "Do you have human rights policy? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "humanRightsDueDiligence",
                label: "Human Rights Due Diligence",
                description: "Do you have due diligence processes to identify, prevent, mitigate and address adverse human rights impacts?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "traffickingInHumanBeingsPolicy",
                label: "Trafficking In Human Beings Policy",
                description: "Do you have a policy against trafficking in human beings? If yes, please share the policy with us.",
    
    
                component: "YesNoBaseDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "reportedChildLabourIncidents",
                label: "Reported Child Labour Incidents",
                description: "Has their been any reported child labour incident (within own operations or supply chain)?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "reportedForcedOrCompulsoryLabourIncidents",
                label: "Reported Forced Or Compulsory Labour Incidents",
                description: "Has their been any reported forced or compulsory labour incident (within own operations or supply chain)?",
    
    
                component: "YesNoExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
            {
                name: "numberOfReportedIncidentsOfHumanRightsViolations",
                label: "Number Of Reported Incidents Of Human Rights Violations",
                description: "Number of cases of severe human rights issues and incidents connected to the company",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
     
            ],
            },
        {
            name: "antiCorruptionAndAntiBribery",
            label: "Anti-corruption and anti-bribery",
            fields: [
                 {
                name: "casesOfInsufficientActionAgainstBriberyAndCorruption",
                label: "Cases of Insufficient Action against Bribery and Corruption",
                description: "Identified insufficiencies in actions taken to address breaches in procedures and standards of anti-corruption and anti-bribery",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "reportedConvictionsOfBriberyAndCorruption",
                label: "Reported Convictions Of Bribery and Corruption",
                description: "Number of reported convictions for violations of anti-corruption and anti-bribery laws",
    
    
                component: "BigDecimalExtendedDataPointFormField",
                required: false,
                showIf: ():boolean => true,
                validation: "integer",
                },
            {
                name: "totalAmountOfReportedFinesOfBriberyAndCorruption",
                label: "Total Amount Of Reported Fines Of Bribery and Corruption",
                description: "Amount of fines for violations of anti-corruption and anti-bribery laws",
    
    
                component: "CurrencyDataPointFormField",
                required: false,
                showIf: ():boolean => true,
    
                },
     
            ],
            },
        ],
        },
    ] as Category[];

