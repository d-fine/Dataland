import { TreeNode } from "primevue/tree";
import { assertDefined } from "@/utils/TypeScriptUtils";

/**
 * Recursively filters the list of TreeNodes to only contain nodes whose label matches the searchTerm.
 * Nodes are included if their own label OR one of their child labels matches the searchTerm.
 * @param nodes the list of nodes to filter
 * @param searchTerm the searchTerm to filter for
 * @returns the filtered list of TreeNodes
 */
export function filterNodes(nodes: Array<TreeNode>, searchTerm: string): Array<TreeNode> {
  const lowerSearchTerm = searchTerm.toLowerCase().trim();
  return nodes.filter((it) => {
    const filterMatchesNode = assertDefined(it.label).toLowerCase().indexOf(lowerSearchTerm) > -1;
    it.children = filterNodes(it.children ?? [], searchTerm);
    return filterMatchesNode ?? it.children.length > 0;
  });
}

/**
 * An excerpt of the Nace Rev 2. definition
 * (Ref https://ec.europa.eu/eurostat/ramon/nomenclatures/index.cfm?TargetUrl=LST_NOM_DTL&StrNom=NACE_REV2&StrLanguageCode=EN)
 * Transformed into TreeNode format by the DatalandNaceConverter (https://github.com/d-fine/DatalandNaceConverter)
 */
export const naceCodeTree: Array<TreeNode> = [
  {
    key: "A",
    label: "A - AGRICULTURE, FORESTRY AND FISHING",
    children: [
      {
        key: "01",
        label: "01 - Crop and animal production, hunting and related service activities",
        children: [
          {
            key: "01.1",
            label: "01.1 - Growing of non-perennial crops",
            children: [
              {
                key: "01.11",
                label: "01.11 - Growing of cereals (except rice), leguminous crops and oil seeds",
                children: [],
              },
              {
                key: "01.12",
                label: "01.12 - Growing of rice",
                children: [],
              },
              {
                key: "01.13",
                label: "01.13 - Growing of vegetables and melons, roots and tubers",
                children: [],
              },
              {
                key: "01.14",
                label: "01.14 - Growing of sugar cane",
                children: [],
              },
              {
                key: "01.15",
                label: "01.15 - Growing of tobacco",
                children: [],
              },
              {
                key: "01.16",
                label: "01.16 - Growing of fibre crops",
                children: [],
              },
              {
                key: "01.19",
                label: "01.19 - Growing of other non-perennial crops",
                children: [],
              },
            ],
          },
          {
            key: "01.2",
            label: "01.2 - Growing of perennial crops",
            children: [
              {
                key: "01.21",
                label: "01.21 - Growing of grapes",
                children: [],
              },
              {
                key: "01.22",
                label: "01.22 - Growing of tropical and subtropical fruits",
                children: [],
              },
              {
                key: "01.23",
                label: "01.23 - Growing of citrus fruits",
                children: [],
              },
              {
                key: "01.24",
                label: "01.24 - Growing of pome fruits and stone fruits",
                children: [],
              },
              {
                key: "01.25",
                label: "01.25 - Growing of other tree and bush fruits and nuts",
                children: [],
              },
              {
                key: "01.26",
                label: "01.26 - Growing of oleaginous fruits",
                children: [],
              },
              {
                key: "01.27",
                label: "01.27 - Growing of beverage crops",
                children: [],
              },
              {
                key: "01.28",
                label: "01.28 - Growing of spices, aromatic, drug and pharmaceutical crops",
                children: [],
              },
              {
                key: "01.29",
                label: "01.29 - Growing of other perennial crops",
                children: [],
              },
            ],
          },
          {
            key: "01.3",
            label: "01.3 - Plant propagation",
            children: [
              {
                key: "01.30",
                label: "01.30 - Plant propagation",
                children: [],
              },
            ],
          },
          {
            key: "01.4",
            label: "01.4 - Animal production",
            children: [
              {
                key: "01.41",
                label: "01.41 - Raising of dairy cattle",
                children: [],
              },
              {
                key: "01.42",
                label: "01.42 - Raising of other cattle and buffaloes",
                children: [],
              },
              {
                key: "01.43",
                label: "01.43 - Raising of horses and other equines",
                children: [],
              },
              {
                key: "01.44",
                label: "01.44 - Raising of camels and camelids",
                children: [],
              },
              {
                key: "01.45",
                label: "01.45 - Raising of sheep and goats",
                children: [],
              },
              {
                key: "01.46",
                label: "01.46 - Raising of swine/pigs",
                children: [],
              },
              {
                key: "01.47",
                label: "01.47 - Raising of poultry",
                children: [],
              },
              {
                key: "01.49",
                label: "01.49 - Raising of other animals",
                children: [],
              },
            ],
          },
          {
            key: "01.5",
            label: "01.5 - Mixed farming",
            children: [
              {
                key: "01.50",
                label: "01.50 - Mixed farming",
                children: [],
              },
            ],
          },
          {
            key: "01.6",
            label: "01.6 - Support activities to agriculture and post-harvest crop activities",
            children: [
              {
                key: "01.61",
                label: "01.61 - Support activities for crop production",
                children: [],
              },
              {
                key: "01.62",
                label: "01.62 - Support activities for animal production",
                children: [],
              },
              {
                key: "01.63",
                label: "01.63 - Post-harvest crop activities",
                children: [],
              },
              {
                key: "01.64",
                label: "01.64 - Seed processing for propagation",
                children: [],
              },
            ],
          },
          {
            key: "01.7",
            label: "01.7 - Hunting, trapping and related service activities",
            children: [
              {
                key: "01.70",
                label: "01.70 - Hunting, trapping and related service activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "02",
        label: "02 - Forestry and logging",
        children: [
          {
            key: "02.1",
            label: "02.1 - Silviculture and other forestry activities",
            children: [
              {
                key: "02.10",
                label: "02.10 - Silviculture and other forestry activities",
                children: [],
              },
            ],
          },
          {
            key: "02.2",
            label: "02.2 - Logging",
            children: [
              {
                key: "02.20",
                label: "02.20 - Logging",
                children: [],
              },
            ],
          },
          {
            key: "02.3",
            label: "02.3 - Gathering of wild growing non-wood products",
            children: [
              {
                key: "02.30",
                label: "02.30 - Gathering of wild growing non-wood products",
                children: [],
              },
            ],
          },
          {
            key: "02.4",
            label: "02.4 - Support services to forestry",
            children: [
              {
                key: "02.40",
                label: "02.40 - Support services to forestry",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "03",
        label: "03 - Fishing and aquaculture",
        children: [
          {
            key: "03.1",
            label: "03.1 - Fishing",
            children: [
              {
                key: "03.11",
                label: "03.11 - Marine fishing",
                children: [],
              },
              {
                key: "03.12",
                label: "03.12 - Freshwater fishing",
                children: [],
              },
            ],
          },
          {
            key: "03.2",
            label: "03.2 - Aquaculture",
            children: [
              {
                key: "03.21",
                label: "03.21 - Marine aquaculture",
                children: [],
              },
              {
                key: "03.22",
                label: "03.22 - Freshwater aquaculture",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "B",
    label: "B - MINING AND QUARRYING",
    children: [
      {
        key: "05",
        label: "05 - Mining of coal and lignite",
        children: [
          {
            key: "05.1",
            label: "05.1 - Mining of hard coal",
            children: [
              {
                key: "05.10",
                label: "05.10 - Mining of hard coal",
                children: [],
              },
            ],
          },
          {
            key: "05.2",
            label: "05.2 - Mining of lignite",
            children: [
              {
                key: "05.20",
                label: "05.20 - Mining of lignite",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "06",
        label: "06 - Extraction of crude petroleum and natural gas",
        children: [
          {
            key: "06.1",
            label: "06.1 - Extraction of crude petroleum",
            children: [
              {
                key: "06.10",
                label: "06.10 - Extraction of crude petroleum",
                children: [],
              },
            ],
          },
          {
            key: "06.2",
            label: "06.2 - Extraction of natural gas",
            children: [
              {
                key: "06.20",
                label: "06.20 - Extraction of natural gas",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "07",
        label: "07 - Mining of metal ores",
        children: [
          {
            key: "07.1",
            label: "07.1 - Mining of iron ores",
            children: [
              {
                key: "07.10",
                label: "07.10 - Mining of iron ores",
                children: [],
              },
            ],
          },
          {
            key: "07.2",
            label: "07.2 - Mining of non-ferrous metal ores",
            children: [
              {
                key: "07.21",
                label: "07.21 - Mining of uranium and thorium ores",
                children: [],
              },
              {
                key: "07.29",
                label: "07.29 - Mining of other non-ferrous metal ores",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "08",
        label: "08 - Other mining and quarrying",
        children: [
          {
            key: "08.1",
            label: "08.1 - Quarrying of stone, sand and clay",
            children: [
              {
                key: "08.11",
                label: "08.11 - Quarrying of ornamental and building stone, limestone, gypsum, chalk and slate",
                children: [],
              },
              {
                key: "08.12",
                label: "08.12 - Operation of gravel and sand pits; mining of clays and kaolin",
                children: [],
              },
            ],
          },
          {
            key: "08.9",
            label: "08.9 - Mining and quarrying n.e.c.",
            children: [
              {
                key: "08.91",
                label: "08.91 - Mining of chemical and fertiliser minerals",
                children: [],
              },
              {
                key: "08.92",
                label: "08.92 - Extraction of peat",
                children: [],
              },
              {
                key: "08.93",
                label: "08.93 - Extraction of salt",
                children: [],
              },
              {
                key: "08.99",
                label: "08.99 - Other mining and quarrying n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "09",
        label: "09 - Mining support service activities",
        children: [
          {
            key: "09.1",
            label: "09.1 - Support activities for petroleum and natural gas extraction",
            children: [
              {
                key: "09.10",
                label: "09.10 - Support activities for petroleum and natural gas extraction",
                children: [],
              },
            ],
          },
          {
            key: "09.9",
            label: "09.9 - Support activities for other mining and quarrying",
            children: [
              {
                key: "09.90",
                label: "09.90 - Support activities for other mining and quarrying",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "C",
    label: "C - MANUFACTURING",
    children: [
      {
        key: "10",
        label: "10 - Manufacture of food products",
        children: [
          {
            key: "10.1",
            label: "10.1 - Processing and preserving of meat and production of meat products",
            children: [
              {
                key: "10.11",
                label: "10.11 - Processing and preserving of meat",
                children: [],
              },
              {
                key: "10.12",
                label: "10.12 - Processing and preserving of poultry meat",
                children: [],
              },
              {
                key: "10.13",
                label: "10.13 - Production of meat and poultry meat products",
                children: [],
              },
            ],
          },
          {
            key: "10.2",
            label: "10.2 - Processing and preserving of fish, crustaceans and molluscs",
            children: [
              {
                key: "10.20",
                label: "10.20 - Processing and preserving of fish, crustaceans and molluscs",
                children: [],
              },
            ],
          },
          {
            key: "10.3",
            label: "10.3 - Processing and preserving of fruit and vegetables",
            children: [
              {
                key: "10.31",
                label: "10.31 - Processing and preserving of potatoes",
                children: [],
              },
              {
                key: "10.32",
                label: "10.32 - Manufacture of fruit and vegetable juice",
                children: [],
              },
              {
                key: "10.39",
                label: "10.39 - Other processing and preserving of fruit and vegetables",
                children: [],
              },
            ],
          },
          {
            key: "10.4",
            label: "10.4 - Manufacture of vegetable and animal oils and fats",
            children: [
              {
                key: "10.41",
                label: "10.41 - Manufacture of oils and fats",
                children: [],
              },
              {
                key: "10.42",
                label: "10.42 - Manufacture of margarine and similar edible fats",
                children: [],
              },
            ],
          },
          {
            key: "10.5",
            label: "10.5 - Manufacture of dairy products",
            children: [
              {
                key: "10.51",
                label: "10.51 - Operation of dairies and cheese making",
                children: [],
              },
              {
                key: "10.52",
                label: "10.52 - Manufacture of ice cream",
                children: [],
              },
            ],
          },
          {
            key: "10.6",
            label: "10.6 - Manufacture of grain mill products, starches and starch products",
            children: [
              {
                key: "10.61",
                label: "10.61 - Manufacture of grain mill products",
                children: [],
              },
              {
                key: "10.62",
                label: "10.62 - Manufacture of starches and starch products",
                children: [],
              },
            ],
          },
          {
            key: "10.7",
            label: "10.7 - Manufacture of bakery and farinaceous products",
            children: [
              {
                key: "10.71",
                label: "10.71 - Manufacture of bread; manufacture of fresh pastry goods and cakes",
                children: [],
              },
              {
                key: "10.72",
                label: "10.72 - Manufacture of rusks and biscuits; manufacture of preserved pastry goods and cakes",
                children: [],
              },
              {
                key: "10.73",
                label: "10.73 - Manufacture of macaroni, noodles, couscous and similar farinaceous products",
                children: [],
              },
            ],
          },
          {
            key: "10.8",
            label: "10.8 - Manufacture of other food products",
            children: [
              {
                key: "10.81",
                label: "10.81 - Manufacture of sugar",
                children: [],
              },
              {
                key: "10.82",
                label: "10.82 - Manufacture of cocoa, chocolate and sugar confectionery",
                children: [],
              },
              {
                key: "10.83",
                label: "10.83 - Processing of tea and coffee",
                children: [],
              },
              {
                key: "10.84",
                label: "10.84 - Manufacture of condiments and seasonings",
                children: [],
              },
              {
                key: "10.85",
                label: "10.85 - Manufacture of prepared meals and dishes",
                children: [],
              },
              {
                key: "10.86",
                label: "10.86 - Manufacture of homogenised food preparations and dietetic food",
                children: [],
              },
              {
                key: "10.89",
                label: "10.89 - Manufacture of other food products n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "10.9",
            label: "10.9 - Manufacture of prepared animal feeds",
            children: [
              {
                key: "10.91",
                label: "10.91 - Manufacture of prepared feeds for farm animals",
                children: [],
              },
              {
                key: "10.92",
                label: "10.92 - Manufacture of prepared pet foods",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "11",
        label: "11 - Manufacture of beverages",
        children: [
          {
            key: "11.0",
            label: "11.0 - Manufacture of beverages",
            children: [
              {
                key: "11.01",
                label: "11.01 - Distilling, rectifying and blending of spirits",
                children: [],
              },
              {
                key: "11.02",
                label: "11.02 - Manufacture of wine from grape",
                children: [],
              },
              {
                key: "11.03",
                label: "11.03 - Manufacture of cider and other fruit wines",
                children: [],
              },
              {
                key: "11.04",
                label: "11.04 - Manufacture of other non-distilled fermented beverages",
                children: [],
              },
              {
                key: "11.05",
                label: "11.05 - Manufacture of beer",
                children: [],
              },
              {
                key: "11.06",
                label: "11.06 - Manufacture of malt",
                children: [],
              },
              {
                key: "11.07",
                label: "11.07 - Manufacture of soft drinks; production of mineral waters and other bottled waters",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "12",
        label: "12 - Manufacture of tobacco products",
        children: [
          {
            key: "12.0",
            label: "12.0 - Manufacture of tobacco products",
            children: [
              {
                key: "12.00",
                label: "12.00 - Manufacture of tobacco products",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "13",
        label: "13 - Manufacture of textiles",
        children: [
          {
            key: "13.1",
            label: "13.1 - Preparation and spinning of textile fibres",
            children: [
              {
                key: "13.10",
                label: "13.10 - Preparation and spinning of textile fibres",
                children: [],
              },
            ],
          },
          {
            key: "13.2",
            label: "13.2 - Weaving of textiles",
            children: [
              {
                key: "13.20",
                label: "13.20 - Weaving of textiles",
                children: [],
              },
            ],
          },
          {
            key: "13.3",
            label: "13.3 - Finishing of textiles",
            children: [
              {
                key: "13.30",
                label: "13.30 - Finishing of textiles",
                children: [],
              },
            ],
          },
          {
            key: "13.9",
            label: "13.9 - Manufacture of other textiles",
            children: [
              {
                key: "13.91",
                label: "13.91 - Manufacture of knitted and crocheted fabrics",
                children: [],
              },
              {
                key: "13.92",
                label: "13.92 - Manufacture of made-up textile articles, except apparel",
                children: [],
              },
              {
                key: "13.93",
                label: "13.93 - Manufacture of carpets and rugs",
                children: [],
              },
              {
                key: "13.94",
                label: "13.94 - Manufacture of cordage, rope, twine and netting",
                children: [],
              },
              {
                key: "13.95",
                label: "13.95 - Manufacture of non-wovens and articles made from non-wovens, except apparel",
                children: [],
              },
              {
                key: "13.96",
                label: "13.96 - Manufacture of other technical and industrial textiles",
                children: [],
              },
              {
                key: "13.99",
                label: "13.99 - Manufacture of other textiles n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "14",
        label: "14 - Manufacture of wearing apparel",
        children: [
          {
            key: "14.1",
            label: "14.1 - Manufacture of wearing apparel, except fur apparel",
            children: [
              {
                key: "14.11",
                label: "14.11 - Manufacture of leather clothes",
                children: [],
              },
              {
                key: "14.12",
                label: "14.12 - Manufacture of workwear",
                children: [],
              },
              {
                key: "14.13",
                label: "14.13 - Manufacture of other outerwear",
                children: [],
              },
              {
                key: "14.14",
                label: "14.14 - Manufacture of underwear",
                children: [],
              },
              {
                key: "14.19",
                label: "14.19 - Manufacture of other wearing apparel and accessories",
                children: [],
              },
            ],
          },
          {
            key: "14.2",
            label: "14.2 - Manufacture of articles of fur",
            children: [
              {
                key: "14.20",
                label: "14.20 - Manufacture of articles of fur",
                children: [],
              },
            ],
          },
          {
            key: "14.3",
            label: "14.3 - Manufacture of knitted and crocheted apparel",
            children: [
              {
                key: "14.31",
                label: "14.31 - Manufacture of knitted and crocheted hosiery",
                children: [],
              },
              {
                key: "14.39",
                label: "14.39 - Manufacture of other knitted and crocheted apparel",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "15",
        label: "15 - Manufacture of leather and related products",
        children: [
          {
            key: "15.1",
            label:
              "15.1 - Tanning and dressing of leather; manufacture of luggage, handbags, saddlery and harness; dressing and dyeing of fur",
            children: [
              {
                key: "15.11",
                label: "15.11 - Tanning and dressing of leather; dressing and dyeing of fur",
                children: [],
              },
              {
                key: "15.12",
                label: "15.12 - Manufacture of luggage, handbags and the like, saddlery and harness",
                children: [],
              },
            ],
          },
          {
            key: "15.2",
            label: "15.2 - Manufacture of footwear",
            children: [
              {
                key: "15.20",
                label: "15.20 - Manufacture of footwear",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "16",
        label:
          "16 - Manufacture of wood and of products of wood and cork, except furniture; manufacture of articles of straw and plaiting materials",
        children: [
          {
            key: "16.1",
            label: "16.1 - Sawmilling and planing of wood",
            children: [
              {
                key: "16.10",
                label: "16.10 - Sawmilling and planing of wood",
                children: [],
              },
            ],
          },
          {
            key: "16.2",
            label: "16.2 - Manufacture of products of wood, cork, straw and plaiting materials",
            children: [
              {
                key: "16.21",
                label: "16.21 - Manufacture of veneer sheets and wood-based panels",
                children: [],
              },
              {
                key: "16.22",
                label: "16.22 - Manufacture of assembled parquet floors",
                children: [],
              },
              {
                key: "16.23",
                label: "16.23 - Manufacture of other builders' carpentry and joinery",
                children: [],
              },
              {
                key: "16.24",
                label: "16.24 - Manufacture of wooden containers",
                children: [],
              },
              {
                key: "16.29",
                label:
                  "16.29 - Manufacture of other products of wood; manufacture of articles of cork, straw and plaiting materials",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "17",
        label: "17 - Manufacture of paper and paper products",
        children: [
          {
            key: "17.1",
            label: "17.1 - Manufacture of pulp, paper and paperboard",
            children: [
              {
                key: "17.11",
                label: "17.11 - Manufacture of pulp",
                children: [],
              },
              {
                key: "17.12",
                label: "17.12 - Manufacture of paper and paperboard",
                children: [],
              },
            ],
          },
          {
            key: "17.2",
            label: "17.2 - Manufacture of articles of paper and paperboard ",
            children: [
              {
                key: "17.21",
                label:
                  "17.21 - Manufacture of corrugated paper and paperboard and of containers of paper and paperboard",
                children: [],
              },
              {
                key: "17.22",
                label: "17.22 - Manufacture of household and sanitary goods and of toilet requisites",
                children: [],
              },
              {
                key: "17.23",
                label: "17.23 - Manufacture of paper stationery",
                children: [],
              },
              {
                key: "17.24",
                label: "17.24 - Manufacture of wallpaper",
                children: [],
              },
              {
                key: "17.29",
                label: "17.29 - Manufacture of other articles of paper and paperboard",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "18",
        label: "18 - Printing and reproduction of recorded media",
        children: [
          {
            key: "18.1",
            label: "18.1 - Printing and service activities related to printing",
            children: [
              {
                key: "18.11",
                label: "18.11 - Printing of newspapers",
                children: [],
              },
              {
                key: "18.12",
                label: "18.12 - Other printing",
                children: [],
              },
              {
                key: "18.13",
                label: "18.13 - Pre-press and pre-media services",
                children: [],
              },
              {
                key: "18.14",
                label: "18.14 - Binding and related services",
                children: [],
              },
            ],
          },
          {
            key: "18.2",
            label: "18.2 - Reproduction of recorded media",
            children: [
              {
                key: "18.20",
                label: "18.20 - Reproduction of recorded media",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "19",
        label: "19 - Manufacture of coke and refined petroleum products",
        children: [
          {
            key: "19.1",
            label: "19.1 - Manufacture of coke oven products",
            children: [
              {
                key: "19.10",
                label: "19.10 - Manufacture of coke oven products",
                children: [],
              },
            ],
          },
          {
            key: "19.2",
            label: "19.2 - Manufacture of refined petroleum products",
            children: [
              {
                key: "19.20",
                label: "19.20 - Manufacture of refined petroleum products",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "20",
        label: "20 - Manufacture of chemicals and chemical products",
        children: [
          {
            key: "20.1",
            label:
              "20.1 - Manufacture of basic chemicals, fertilisers and nitrogen compounds, plastics and synthetic rubber in primary forms",
            children: [
              {
                key: "20.11",
                label: "20.11 - Manufacture of industrial gases",
                children: [],
              },
              {
                key: "20.12",
                label: "20.12 - Manufacture of dyes and pigments",
                children: [],
              },
              {
                key: "20.13",
                label: "20.13 - Manufacture of other inorganic basic chemicals",
                children: [],
              },
              {
                key: "20.14",
                label: "20.14 - Manufacture of other organic basic chemicals",
                children: [],
              },
              {
                key: "20.15",
                label: "20.15 - Manufacture of fertilisers and nitrogen compounds",
                children: [],
              },
              {
                key: "20.16",
                label: "20.16 - Manufacture of plastics in primary forms",
                children: [],
              },
              {
                key: "20.17",
                label: "20.17 - Manufacture of synthetic rubber in primary forms",
                children: [],
              },
            ],
          },
          {
            key: "20.2",
            label: "20.2 - Manufacture of pesticides and other agrochemical products",
            children: [
              {
                key: "20.20",
                label: "20.20 - Manufacture of pesticides and other agrochemical products",
                children: [],
              },
            ],
          },
          {
            key: "20.3",
            label: "20.3 - Manufacture of paints, varnishes and similar coatings, printing ink and mastics",
            children: [
              {
                key: "20.30",
                label: "20.30 - Manufacture of paints, varnishes and similar coatings, printing ink and mastics",
                children: [],
              },
            ],
          },
          {
            key: "20.4",
            label:
              "20.4 - Manufacture of soap and detergents, cleaning and polishing preparations, perfumes and toilet preparations",
            children: [
              {
                key: "20.41",
                label: "20.41 - Manufacture of soap and detergents, cleaning and polishing preparations",
                children: [],
              },
              {
                key: "20.42",
                label: "20.42 - Manufacture of perfumes and toilet preparations",
                children: [],
              },
            ],
          },
          {
            key: "20.5",
            label: "20.5 - Manufacture of other chemical products",
            children: [
              {
                key: "20.51",
                label: "20.51 - Manufacture of explosives",
                children: [],
              },
              {
                key: "20.52",
                label: "20.52 - Manufacture of glues",
                children: [],
              },
              {
                key: "20.53",
                label: "20.53 - Manufacture of essential oils",
                children: [],
              },
              {
                key: "20.59",
                label: "20.59 - Manufacture of other chemical products n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "20.6",
            label: "20.6 - Manufacture of man-made fibres",
            children: [
              {
                key: "20.60",
                label: "20.60 - Manufacture of man-made fibres",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "21",
        label: "21 - Manufacture of basic pharmaceutical products and pharmaceutical preparations",
        children: [
          {
            key: "21.1",
            label: "21.1 - Manufacture of basic pharmaceutical products",
            children: [
              {
                key: "21.10",
                label: "21.10 - Manufacture of basic pharmaceutical products",
                children: [],
              },
            ],
          },
          {
            key: "21.2",
            label: "21.2 - Manufacture of pharmaceutical preparations",
            children: [
              {
                key: "21.20",
                label: "21.20 - Manufacture of pharmaceutical preparations",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "22",
        label: "22 - Manufacture of rubber and plastic products",
        children: [
          {
            key: "22.1",
            label: "22.1 - Manufacture of rubber products",
            children: [
              {
                key: "22.11",
                label: "22.11 - Manufacture of rubber tyres and tubes; retreading and rebuilding of rubber tyres",
                children: [],
              },
              {
                key: "22.19",
                label: "22.19 - Manufacture of other rubber products",
                children: [],
              },
            ],
          },
          {
            key: "22.2",
            label: "22.2 - Manufacture of plastic products",
            children: [
              {
                key: "22.21",
                label: "22.21 - Manufacture of plastic plates, sheets, tubes and profiles",
                children: [],
              },
              {
                key: "22.22",
                label: "22.22 - Manufacture of plastic packing goods",
                children: [],
              },
              {
                key: "22.23",
                label: "22.23 - Manufacture of builders’ ware of plastic",
                children: [],
              },
              {
                key: "22.29",
                label: "22.29 - Manufacture of other plastic products",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "23",
        label: "23 - Manufacture of other non-metallic mineral products",
        children: [
          {
            key: "23.1",
            label: "23.1 - Manufacture of glass and glass products",
            children: [
              {
                key: "23.11",
                label: "23.11 - Manufacture of flat glass",
                children: [],
              },
              {
                key: "23.12",
                label: "23.12 - Shaping and processing of flat glass",
                children: [],
              },
              {
                key: "23.13",
                label: "23.13 - Manufacture of hollow glass",
                children: [],
              },
              {
                key: "23.14",
                label: "23.14 - Manufacture of glass fibres",
                children: [],
              },
              {
                key: "23.19",
                label: "23.19 - Manufacture and processing of other glass, including technical glassware",
                children: [],
              },
            ],
          },
          {
            key: "23.2",
            label: "23.2 - Manufacture of refractory products",
            children: [
              {
                key: "23.20",
                label: "23.20 - Manufacture of refractory products",
                children: [],
              },
            ],
          },
          {
            key: "23.3",
            label: "23.3 - Manufacture of clay building materials",
            children: [
              {
                key: "23.31",
                label: "23.31 - Manufacture of ceramic tiles and flags",
                children: [],
              },
              {
                key: "23.32",
                label: "23.32 - Manufacture of bricks, tiles and construction products, in baked clay",
                children: [],
              },
            ],
          },
          {
            key: "23.4",
            label: "23.4 - Manufacture of other porcelain and ceramic products",
            children: [
              {
                key: "23.41",
                label: "23.41 - Manufacture of ceramic household and ornamental articles",
                children: [],
              },
              {
                key: "23.42",
                label: "23.42 - Manufacture of ceramic sanitary fixtures",
                children: [],
              },
              {
                key: "23.43",
                label: "23.43 - Manufacture of ceramic insulators and insulating fittings",
                children: [],
              },
              {
                key: "23.44",
                label: "23.44 - Manufacture of other technical ceramic products",
                children: [],
              },
              {
                key: "23.49",
                label: "23.49 - Manufacture of other ceramic products",
                children: [],
              },
            ],
          },
          {
            key: "23.5",
            label: "23.5 - Manufacture of cement, lime and plaster",
            children: [
              {
                key: "23.51",
                label: "23.51 - Manufacture of cement",
                children: [],
              },
              {
                key: "23.52",
                label: "23.52 - Manufacture of lime and plaster",
                children: [],
              },
            ],
          },
          {
            key: "23.6",
            label: "23.6 - Manufacture of articles of concrete, cement and plaster",
            children: [
              {
                key: "23.61",
                label: "23.61 - Manufacture of concrete products for construction purposes",
                children: [],
              },
              {
                key: "23.62",
                label: "23.62 - Manufacture of plaster products for construction purposes",
                children: [],
              },
              {
                key: "23.63",
                label: "23.63 - Manufacture of ready-mixed concrete",
                children: [],
              },
              {
                key: "23.64",
                label: "23.64 - Manufacture of mortars",
                children: [],
              },
              {
                key: "23.65",
                label: "23.65 - Manufacture of fibre cement",
                children: [],
              },
              {
                key: "23.69",
                label: "23.69 - Manufacture of other articles of concrete, plaster and cement",
                children: [],
              },
            ],
          },
          {
            key: "23.7",
            label: "23.7 - Cutting, shaping and finishing of stone",
            children: [
              {
                key: "23.70",
                label: "23.70 - Cutting, shaping and finishing of stone",
                children: [],
              },
            ],
          },
          {
            key: "23.9",
            label: "23.9 - Manufacture of abrasive products and non-metallic mineral products n.e.c.",
            children: [
              {
                key: "23.91",
                label: "23.91 - Production of abrasive products",
                children: [],
              },
              {
                key: "23.99",
                label: "23.99 - Manufacture of other non-metallic mineral products n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "24",
        label: "24 - Manufacture of basic metals",
        children: [
          {
            key: "24.1",
            label: "24.1 - Manufacture of basic iron and steel and of ferro-alloys",
            children: [
              {
                key: "24.10",
                label: "24.10 - Manufacture of basic iron and steel and of ferro-alloys ",
                children: [],
              },
            ],
          },
          {
            key: "24.2",
            label: "24.2 - Manufacture of tubes, pipes, hollow profiles and related fittings, of steel",
            children: [
              {
                key: "24.20",
                label: "24.20 - Manufacture of tubes, pipes, hollow profiles and related fittings, of steel",
                children: [],
              },
            ],
          },
          {
            key: "24.3",
            label: "24.3 - Manufacture of other products of first processing of steel",
            children: [
              {
                key: "24.31",
                label: "24.31 - Cold drawing of bars",
                children: [],
              },
              {
                key: "24.32",
                label: "24.32 - Cold rolling of narrow strip",
                children: [],
              },
              {
                key: "24.33",
                label: "24.33 - Cold forming or folding",
                children: [],
              },
              {
                key: "24.34",
                label: "24.34 - Cold drawing of wire",
                children: [],
              },
            ],
          },
          {
            key: "24.4",
            label: "24.4 - Manufacture of basic precious and other non-ferrous metals",
            children: [
              {
                key: "24.41",
                label: "24.41 - Precious metals production",
                children: [],
              },
              {
                key: "24.42",
                label: "24.42 - Aluminium production",
                children: [],
              },
              {
                key: "24.43",
                label: "24.43 - Lead, zinc and tin production",
                children: [],
              },
              {
                key: "24.44",
                label: "24.44 - Copper production",
                children: [],
              },
              {
                key: "24.45",
                label: "24.45 - Other non-ferrous metal production",
                children: [],
              },
              {
                key: "24.46",
                label: "24.46 - Processing of nuclear fuel ",
                children: [],
              },
            ],
          },
          {
            key: "24.5",
            label: "24.5 - Casting of metals",
            children: [
              {
                key: "24.51",
                label: "24.51 - Casting of iron",
                children: [],
              },
              {
                key: "24.52",
                label: "24.52 - Casting of steel",
                children: [],
              },
              {
                key: "24.53",
                label: "24.53 - Casting of light metals",
                children: [],
              },
              {
                key: "24.54",
                label: "24.54 - Casting of other non-ferrous metals",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "25",
        label: "25 - Manufacture of fabricated metal products, except machinery and equipment",
        children: [
          {
            key: "25.1",
            label: "25.1 - Manufacture of structural metal products",
            children: [
              {
                key: "25.11",
                label: "25.11 - Manufacture of metal structures and parts of structures",
                children: [],
              },
              {
                key: "25.12",
                label: "25.12 - Manufacture of doors and windows of metal",
                children: [],
              },
            ],
          },
          {
            key: "25.2",
            label: "25.2 - Manufacture of tanks, reservoirs and containers of metal",
            children: [
              {
                key: "25.21",
                label: "25.21 - Manufacture of central heating radiators and boilers",
                children: [],
              },
              {
                key: "25.29",
                label: "25.29 - Manufacture of other tanks, reservoirs and containers of metal",
                children: [],
              },
            ],
          },
          {
            key: "25.3",
            label: "25.3 - Manufacture of steam generators, except central heating hot water boilers",
            children: [
              {
                key: "25.30",
                label: "25.30 - Manufacture of steam generators, except central heating hot water boilers",
                children: [],
              },
            ],
          },
          {
            key: "25.4",
            label: "25.4 - Manufacture of weapons and ammunition",
            children: [
              {
                key: "25.40",
                label: "25.40 - Manufacture of weapons and ammunition",
                children: [],
              },
            ],
          },
          {
            key: "25.5",
            label: "25.5 - Forging, pressing, stamping and roll-forming of metal; powder metallurgy",
            children: [
              {
                key: "25.50",
                label: "25.50 - Forging, pressing, stamping and roll-forming of metal; powder metallurgy",
                children: [],
              },
            ],
          },
          {
            key: "25.6",
            label: "25.6 - Treatment and coating of metals; machining",
            children: [
              {
                key: "25.61",
                label: "25.61 - Treatment and coating of metals",
                children: [],
              },
              {
                key: "25.62",
                label: "25.62 - Machining",
                children: [],
              },
            ],
          },
          {
            key: "25.7",
            label: "25.7 - Manufacture of cutlery, tools and general hardware",
            children: [
              {
                key: "25.71",
                label: "25.71 - Manufacture of cutlery",
                children: [],
              },
              {
                key: "25.72",
                label: "25.72 - Manufacture of locks and hinges",
                children: [],
              },
              {
                key: "25.73",
                label: "25.73 - Manufacture of tools",
                children: [],
              },
            ],
          },
          {
            key: "25.9",
            label: "25.9 - Manufacture of other fabricated metal products",
            children: [
              {
                key: "25.91",
                label: "25.91 - Manufacture of steel drums and similar containers",
                children: [],
              },
              {
                key: "25.92",
                label: "25.92 - Manufacture of light metal packaging ",
                children: [],
              },
              {
                key: "25.93",
                label: "25.93 - Manufacture of wire products, chain and springs",
                children: [],
              },
              {
                key: "25.94",
                label: "25.94 - Manufacture of fasteners and screw machine products",
                children: [],
              },
              {
                key: "25.99",
                label: "25.99 - Manufacture of other fabricated metal products n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "26",
        label: "26 - Manufacture of computer, electronic and optical products",
        children: [
          {
            key: "26.1",
            label: "26.1 - Manufacture of electronic components and boards",
            children: [
              {
                key: "26.11",
                label: "26.11 - Manufacture of electronic components",
                children: [],
              },
              {
                key: "26.12",
                label: "26.12 - Manufacture of loaded electronic boards",
                children: [],
              },
            ],
          },
          {
            key: "26.2",
            label: "26.2 - Manufacture of computers and peripheral equipment",
            children: [
              {
                key: "26.20",
                label: "26.20 - Manufacture of computers and peripheral equipment",
                children: [],
              },
            ],
          },
          {
            key: "26.3",
            label: "26.3 - Manufacture of communication equipment",
            children: [
              {
                key: "26.30",
                label: "26.30 - Manufacture of communication equipment",
                children: [],
              },
            ],
          },
          {
            key: "26.4",
            label: "26.4 - Manufacture of consumer electronics",
            children: [
              {
                key: "26.40",
                label: "26.40 - Manufacture of consumer electronics",
                children: [],
              },
            ],
          },
          {
            key: "26.5",
            label:
              "26.5 - Manufacture of instruments and appliances for measuring, testing and navigation; watches and clocks",
            children: [
              {
                key: "26.51",
                label: "26.51 - Manufacture of instruments and appliances for measuring, testing and navigation",
                children: [],
              },
              {
                key: "26.52",
                label: "26.52 - Manufacture of watches and clocks",
                children: [],
              },
            ],
          },
          {
            key: "26.6",
            label: "26.6 - Manufacture of irradiation, electromedical and electrotherapeutic equipment",
            children: [
              {
                key: "26.60",
                label: "26.60 - Manufacture of irradiation, electromedical and electrotherapeutic equipment",
                children: [],
              },
            ],
          },
          {
            key: "26.7",
            label: "26.7 - Manufacture of optical instruments and photographic equipment",
            children: [
              {
                key: "26.70",
                label: "26.70 - Manufacture of optical instruments and photographic equipment",
                children: [],
              },
            ],
          },
          {
            key: "26.8",
            label: "26.8 - Manufacture of magnetic and optical media",
            children: [
              {
                key: "26.80",
                label: "26.80 - Manufacture of magnetic and optical media",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "27",
        label: "27 - Manufacture of electrical equipment",
        children: [
          {
            key: "27.1",
            label:
              "27.1 - Manufacture of electric motors, generators, transformers and electricity distribution and control apparatus",
            children: [
              {
                key: "27.11",
                label: "27.11 - Manufacture of electric motors, generators and transformers",
                children: [],
              },
              {
                key: "27.12",
                label: "27.12 - Manufacture of electricity distribution and control apparatus",
                children: [],
              },
            ],
          },
          {
            key: "27.2",
            label: "27.2 - Manufacture of batteries and accumulators",
            children: [
              {
                key: "27.20",
                label: "27.20 - Manufacture of batteries and accumulators",
                children: [],
              },
            ],
          },
          {
            key: "27.3",
            label: "27.3 - Manufacture of wiring and wiring devices",
            children: [
              {
                key: "27.31",
                label: "27.31 - Manufacture of fibre optic cables",
                children: [],
              },
              {
                key: "27.32",
                label: "27.32 - Manufacture of other electronic and electric wires and cables",
                children: [],
              },
              {
                key: "27.33",
                label: "27.33 - Manufacture of wiring devices",
                children: [],
              },
            ],
          },
          {
            key: "27.4",
            label: "27.4 - Manufacture of electric lighting equipment",
            children: [
              {
                key: "27.40",
                label: "27.40 - Manufacture of electric lighting equipment",
                children: [],
              },
            ],
          },
          {
            key: "27.5",
            label: "27.5 - Manufacture of domestic appliances",
            children: [
              {
                key: "27.51",
                label: "27.51 - Manufacture of electric domestic appliances",
                children: [],
              },
              {
                key: "27.52",
                label: "27.52 - Manufacture of non-electric domestic appliances",
                children: [],
              },
            ],
          },
          {
            key: "27.9",
            label: "27.9 - Manufacture of other electrical equipment",
            children: [
              {
                key: "27.90",
                label: "27.90 - Manufacture of other electrical equipment",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "28",
        label: "28 - Manufacture of machinery and equipment n.e.c.",
        children: [
          {
            key: "28.1",
            label: "28.1 - Manufacture of general-purpose machinery",
            children: [
              {
                key: "28.11",
                label: "28.11 - Manufacture of engines and turbines, except aircraft, vehicle and cycle engines",
                children: [],
              },
              {
                key: "28.12",
                label: "28.12 - Manufacture of fluid power equipment",
                children: [],
              },
              {
                key: "28.13",
                label: "28.13 - Manufacture of other pumps and compressors",
                children: [],
              },
              {
                key: "28.14",
                label: "28.14 - Manufacture of other taps and valves",
                children: [],
              },
              {
                key: "28.15",
                label: "28.15 - Manufacture of bearings, gears, gearing and driving elements",
                children: [],
              },
            ],
          },
          {
            key: "28.2",
            label: "28.2 - Manufacture of other general-purpose machinery",
            children: [
              {
                key: "28.21",
                label: "28.21 - Manufacture of ovens, furnaces and furnace burners",
                children: [],
              },
              {
                key: "28.22",
                label: "28.22 - Manufacture of lifting and handling equipment",
                children: [],
              },
              {
                key: "28.23",
                label:
                  "28.23 - Manufacture of office machinery and equipment (except computers and peripheral equipment)",
                children: [],
              },
              {
                key: "28.24",
                label: "28.24 - Manufacture of power-driven hand tools",
                children: [],
              },
              {
                key: "28.25",
                label: "28.25 - Manufacture of non-domestic cooling and ventilation equipment",
                children: [],
              },
              {
                key: "28.29",
                label: "28.29 - Manufacture of other general-purpose machinery n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "28.3",
            label: "28.3 - Manufacture of agricultural and forestry machinery",
            children: [
              {
                key: "28.30",
                label: "28.30 - Manufacture of agricultural and forestry machinery",
                children: [],
              },
            ],
          },
          {
            key: "28.4",
            label: "28.4 - Manufacture of metal forming machinery and machine tools",
            children: [
              {
                key: "28.41",
                label: "28.41 - Manufacture of metal forming machinery",
                children: [],
              },
              {
                key: "28.49",
                label: "28.49 - Manufacture of other machine tools",
                children: [],
              },
            ],
          },
          {
            key: "28.9",
            label: "28.9 - Manufacture of other special-purpose machinery",
            children: [
              {
                key: "28.91",
                label: "28.91 - Manufacture of machinery for metallurgy",
                children: [],
              },
              {
                key: "28.92",
                label: "28.92 - Manufacture of machinery for mining, quarrying and construction",
                children: [],
              },
              {
                key: "28.93",
                label: "28.93 - Manufacture of machinery for food, beverage and tobacco processing",
                children: [],
              },
              {
                key: "28.94",
                label: "28.94 - Manufacture of machinery for textile, apparel and leather production",
                children: [],
              },
              {
                key: "28.95",
                label: "28.95 - Manufacture of machinery for paper and paperboard production",
                children: [],
              },
              {
                key: "28.96",
                label: "28.96 - Manufacture of plastics and rubber machinery",
                children: [],
              },
              {
                key: "28.99",
                label: "28.99 - Manufacture of other special-purpose machinery n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "29",
        label: "29 - Manufacture of motor vehicles, trailers and semi-trailers",
        children: [
          {
            key: "29.1",
            label: "29.1 - Manufacture of motor vehicles",
            children: [
              {
                key: "29.10",
                label: "29.10 - Manufacture of motor vehicles",
                children: [],
              },
            ],
          },
          {
            key: "29.2",
            label:
              "29.2 - Manufacture of bodies (coachwork) for motor vehicles; manufacture of trailers and semi-trailers",
            children: [
              {
                key: "29.20",
                label:
                  "29.20 - Manufacture of bodies (coachwork) for motor vehicles; manufacture of trailers and semi-trailers",
                children: [],
              },
            ],
          },
          {
            key: "29.3",
            label: "29.3 - Manufacture of parts and accessories for motor vehicles",
            children: [
              {
                key: "29.31",
                label: "29.31 - Manufacture of electrical and electronic equipment for motor vehicles",
                children: [],
              },
              {
                key: "29.32",
                label: "29.32 - Manufacture of other parts and accessories for motor vehicles",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "30",
        label: "30 - Manufacture of other transport equipment",
        children: [
          {
            key: "30.1",
            label: "30.1 - Building of ships and boats",
            children: [
              {
                key: "30.11",
                label: "30.11 - Building of ships and floating structures",
                children: [],
              },
              {
                key: "30.12",
                label: "30.12 - Building of pleasure and sporting boats",
                children: [],
              },
            ],
          },
          {
            key: "30.2",
            label: "30.2 - Manufacture of railway locomotives and rolling stock",
            children: [
              {
                key: "30.20",
                label: "30.20 - Manufacture of railway locomotives and rolling stock",
                children: [],
              },
            ],
          },
          {
            key: "30.3",
            label: "30.3 - Manufacture of air and spacecraft and related machinery",
            children: [
              {
                key: "30.30",
                label: "30.30 - Manufacture of air and spacecraft and related machinery",
                children: [],
              },
            ],
          },
          {
            key: "30.4",
            label: "30.4 - Manufacture of military fighting vehicles",
            children: [
              {
                key: "30.40",
                label: "30.40 - Manufacture of military fighting vehicles",
                children: [],
              },
            ],
          },
          {
            key: "30.9",
            label: "30.9 - Manufacture of transport equipment n.e.c.",
            children: [
              {
                key: "30.91",
                label: "30.91 - Manufacture of motorcycles",
                children: [],
              },
              {
                key: "30.92",
                label: "30.92 - Manufacture of bicycles and invalid carriages",
                children: [],
              },
              {
                key: "30.99",
                label: "30.99 - Manufacture of other transport equipment n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "31",
        label: "31 - Manufacture of furniture",
        children: [
          {
            key: "31.0",
            label: "31.0 - Manufacture of furniture",
            children: [
              {
                key: "31.01",
                label: "31.01 - Manufacture of office and shop furniture",
                children: [],
              },
              {
                key: "31.02",
                label: "31.02 - Manufacture of kitchen furniture",
                children: [],
              },
              {
                key: "31.03",
                label: "31.03 - Manufacture of mattresses",
                children: [],
              },
              {
                key: "31.09",
                label: "31.09 - Manufacture of other furniture",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "32",
        label: "32 - Other manufacturing",
        children: [
          {
            key: "32.1",
            label: "32.1 - Manufacture of jewellery, bijouterie and related articles",
            children: [
              {
                key: "32.11",
                label: "32.11 - Striking of coins",
                children: [],
              },
              {
                key: "32.12",
                label: "32.12 - Manufacture of jewellery and related articles",
                children: [],
              },
              {
                key: "32.13",
                label: "32.13 - Manufacture of imitation jewellery and related articles",
                children: [],
              },
            ],
          },
          {
            key: "32.2",
            label: "32.2 - Manufacture of musical instruments",
            children: [
              {
                key: "32.20",
                label: "32.20 - Manufacture of musical instruments",
                children: [],
              },
            ],
          },
          {
            key: "32.3",
            label: "32.3 - Manufacture of sports goods",
            children: [
              {
                key: "32.30",
                label: "32.30 - Manufacture of sports goods",
                children: [],
              },
            ],
          },
          {
            key: "32.4",
            label: "32.4 - Manufacture of games and toys",
            children: [
              {
                key: "32.40",
                label: "32.40 - Manufacture of games and toys",
                children: [],
              },
            ],
          },
          {
            key: "32.5",
            label: "32.5 - Manufacture of medical and dental instruments and supplies",
            children: [
              {
                key: "32.50",
                label: "32.50 - Manufacture of medical and dental instruments and supplies",
                children: [],
              },
            ],
          },
          {
            key: "32.9",
            label: "32.9 - Manufacturing n.e.c.",
            children: [
              {
                key: "32.91",
                label: "32.91 - Manufacture of brooms and brushes",
                children: [],
              },
              {
                key: "32.99",
                label: "32.99 - Other manufacturing n.e.c. ",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "33",
        label: "33 - Repair and installation of machinery and equipment",
        children: [
          {
            key: "33.1",
            label: "33.1 - Repair of fabricated metal products, machinery and equipment",
            children: [
              {
                key: "33.11",
                label: "33.11 - Repair of fabricated metal products",
                children: [],
              },
              {
                key: "33.12",
                label: "33.12 - Repair of machinery",
                children: [],
              },
              {
                key: "33.13",
                label: "33.13 - Repair of electronic and optical equipment",
                children: [],
              },
              {
                key: "33.14",
                label: "33.14 - Repair of electrical equipment",
                children: [],
              },
              {
                key: "33.15",
                label: "33.15 - Repair and maintenance of ships and boats",
                children: [],
              },
              {
                key: "33.16",
                label: "33.16 - Repair and maintenance of aircraft and spacecraft",
                children: [],
              },
              {
                key: "33.17",
                label: "33.17 - Repair and maintenance of other transport equipment",
                children: [],
              },
              {
                key: "33.19",
                label: "33.19 - Repair of other equipment",
                children: [],
              },
            ],
          },
          {
            key: "33.2",
            label: "33.2 - Installation of industrial machinery and equipment",
            children: [
              {
                key: "33.20",
                label: "33.20 - Installation of industrial machinery and equipment",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "D",
    label: "D - ELECTRICITY, GAS, STEAM AND AIR CONDITIONING SUPPLY",
    children: [
      {
        key: "35",
        label: "35 - Electricity, gas, steam and air conditioning supply",
        children: [
          {
            key: "35.1",
            label: "35.1 - Electric power generation, transmission and distribution",
            children: [
              {
                key: "35.11",
                label: "35.11 - Production of electricity",
                children: [],
              },
              {
                key: "35.12",
                label: "35.12 - Transmission of electricity",
                children: [],
              },
              {
                key: "35.13",
                label: "35.13 - Distribution of electricity",
                children: [],
              },
              {
                key: "35.14",
                label: "35.14 - Trade of electricity",
                children: [],
              },
            ],
          },
          {
            key: "35.2",
            label: "35.2 - Manufacture of gas; distribution of gaseous fuels through mains",
            children: [
              {
                key: "35.21",
                label: "35.21 - Manufacture of gas",
                children: [],
              },
              {
                key: "35.22",
                label: "35.22 - Distribution of gaseous fuels through mains",
                children: [],
              },
              {
                key: "35.23",
                label: "35.23 - Trade of gas through mains",
                children: [],
              },
            ],
          },
          {
            key: "35.3",
            label: "35.3 - Steam and air conditioning supply",
            children: [
              {
                key: "35.30",
                label: "35.30 - Steam and air conditioning supply",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "E",
    label: "E - WATER SUPPLY; SEWERAGE, WASTE MANAGEMENT AND REMEDIATION ACTIVITIES",
    children: [
      {
        key: "36",
        label: "36 - Water collection, treatment and supply",
        children: [
          {
            key: "36.0",
            label: "36.0 - Water collection, treatment and supply",
            children: [
              {
                key: "36.00",
                label: "36.00 - Water collection, treatment and supply",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "37",
        label: "37 - Sewerage",
        children: [
          {
            key: "37.0",
            label: "37.0 - Sewerage",
            children: [
              {
                key: "37.00",
                label: "37.00 - Sewerage",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "38",
        label: "38 - Waste collection, treatment and disposal activities; materials recovery",
        children: [
          {
            key: "38.1",
            label: "38.1 - Waste collection",
            children: [
              {
                key: "38.11",
                label: "38.11 - Collection of non-hazardous waste",
                children: [],
              },
              {
                key: "38.12",
                label: "38.12 - Collection of hazardous waste",
                children: [],
              },
            ],
          },
          {
            key: "38.2",
            label: "38.2 - Waste treatment and disposal",
            children: [
              {
                key: "38.21",
                label: "38.21 - Treatment and disposal of non-hazardous waste",
                children: [],
              },
              {
                key: "38.22",
                label: "38.22 - Treatment and disposal of hazardous waste",
                children: [],
              },
            ],
          },
          {
            key: "38.3",
            label: "38.3 - Materials recovery",
            children: [
              {
                key: "38.31",
                label: "38.31 - Dismantling of wrecks",
                children: [],
              },
              {
                key: "38.32",
                label: "38.32 - Recovery of sorted materials",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "39",
        label: "39 - Remediation activities and other waste management services",
        children: [
          {
            key: "39.0",
            label: "39.0 - Remediation activities and other waste management services",
            children: [
              {
                key: "39.00",
                label: "39.00 - Remediation activities and other waste management services",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "F",
    label: "F - CONSTRUCTION",
    children: [
      {
        key: "41",
        label: "41 - Construction of buildings",
        children: [
          {
            key: "41.1",
            label: "41.1 - Development of building projects",
            children: [
              {
                key: "41.10",
                label: "41.10 - Development of building projects",
                children: [],
              },
            ],
          },
          {
            key: "41.2",
            label: "41.2 - Construction of residential and non-residential buildings",
            children: [
              {
                key: "41.20",
                label: "41.20 - Construction of residential and non-residential buildings",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "42",
        label: "42 - Civil engineering",
        children: [
          {
            key: "42.1",
            label: "42.1 - Construction of roads and railways",
            children: [
              {
                key: "42.11",
                label: "42.11 - Construction of roads and motorways",
                children: [],
              },
              {
                key: "42.12",
                label: "42.12 - Construction of railways and underground railways",
                children: [],
              },
              {
                key: "42.13",
                label: "42.13 - Construction of bridges and tunnels",
                children: [],
              },
            ],
          },
          {
            key: "42.2",
            label: "42.2 - Construction of utility projects",
            children: [
              {
                key: "42.21",
                label: "42.21 - Construction of utility projects for fluids",
                children: [],
              },
              {
                key: "42.22",
                label: "42.22 - Construction of utility projects for electricity and telecommunications",
                children: [],
              },
            ],
          },
          {
            key: "42.9",
            label: "42.9 - Construction of other civil engineering projects",
            children: [
              {
                key: "42.91",
                label: "42.91 - Construction of water projects",
                children: [],
              },
              {
                key: "42.99",
                label: "42.99 - Construction of other civil engineering projects n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "43",
        label: "43 - Specialised construction activities",
        children: [
          {
            key: "43.1",
            label: "43.1 - Demolition and site preparation",
            children: [
              {
                key: "43.11",
                label: "43.11 - Demolition",
                children: [],
              },
              {
                key: "43.12",
                label: "43.12 - Site preparation",
                children: [],
              },
              {
                key: "43.13",
                label: "43.13 - Test drilling and boring",
                children: [],
              },
            ],
          },
          {
            key: "43.2",
            label: "43.2 - Electrical, plumbing and other construction installation activities",
            children: [
              {
                key: "43.21",
                label: "43.21 - Electrical installation",
                children: [],
              },
              {
                key: "43.22",
                label: "43.22 - Plumbing, heat and air-conditioning installation",
                children: [],
              },
              {
                key: "43.29",
                label: "43.29 - Other construction installation",
                children: [],
              },
            ],
          },
          {
            key: "43.3",
            label: "43.3 - Building completion and finishing",
            children: [
              {
                key: "43.31",
                label: "43.31 - Plastering",
                children: [],
              },
              {
                key: "43.32",
                label: "43.32 - Joinery installation",
                children: [],
              },
              {
                key: "43.33",
                label: "43.33 - Floor and wall covering",
                children: [],
              },
              {
                key: "43.34",
                label: "43.34 - Painting and glazing",
                children: [],
              },
              {
                key: "43.39",
                label: "43.39 - Other building completion and finishing",
                children: [],
              },
            ],
          },
          {
            key: "43.9",
            label: "43.9 - Other specialised construction activities",
            children: [
              {
                key: "43.91",
                label: "43.91 - Roofing activities",
                children: [],
              },
              {
                key: "43.99",
                label: "43.99 - Other specialised construction activities n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "G",
    label: "G - WHOLESALE AND RETAIL TRADE; REPAIR OF MOTOR VEHICLES AND MOTORCYCLES",
    children: [
      {
        key: "45",
        label: "45 - Wholesale and retail trade and repair of motor vehicles and motorcycles",
        children: [
          {
            key: "45.1",
            label: "45.1 - Sale of motor vehicles",
            children: [
              {
                key: "45.11",
                label: "45.11 - Sale of cars and light motor vehicles",
                children: [],
              },
              {
                key: "45.19",
                label: "45.19 - Sale of other motor vehicles",
                children: [],
              },
            ],
          },
          {
            key: "45.2",
            label: "45.2 - Maintenance and repair of motor vehicles",
            children: [
              {
                key: "45.20",
                label: "45.20 - Maintenance and repair of motor vehicles",
                children: [],
              },
            ],
          },
          {
            key: "45.3",
            label: "45.3 - Sale of motor vehicle parts and accessories",
            children: [
              {
                key: "45.31",
                label: "45.31 - Wholesale trade of motor vehicle parts and accessories",
                children: [],
              },
              {
                key: "45.32",
                label: "45.32 - Retail trade of motor vehicle parts and accessories",
                children: [],
              },
            ],
          },
          {
            key: "45.4",
            label: "45.4 - Sale, maintenance and repair of motorcycles and related parts and accessories",
            children: [
              {
                key: "45.40",
                label: "45.40 - Sale, maintenance and repair of motorcycles and related parts and accessories",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "46",
        label: "46 - Wholesale trade, except of motor vehicles and motorcycles",
        children: [
          {
            key: "46.1",
            label: "46.1 - Wholesale on a fee or contract basis",
            children: [
              {
                key: "46.11",
                label:
                  "46.11 - Agents involved in the sale of agricultural raw materials, live animals, textile raw materials and semi-finished goods",
                children: [],
              },
              {
                key: "46.12",
                label: "46.12 - Agents involved in the sale of fuels, ores, metals and industrial chemicals",
                children: [],
              },
              {
                key: "46.13",
                label: "46.13 - Agents involved in the sale of timber and building materials",
                children: [],
              },
              {
                key: "46.14",
                label: "46.14 - Agents involved in the sale of machinery, industrial equipment, ships and aircraft",
                children: [],
              },
              {
                key: "46.15",
                label: "46.15 - Agents involved in the sale of furniture, household goods, hardware and ironmongery",
                children: [],
              },
              {
                key: "46.16",
                label: "46.16 - Agents involved in the sale of textiles, clothing, fur, footwear and leather goods",
                children: [],
              },
              {
                key: "46.17",
                label: "46.17 - Agents involved in the sale of food, beverages and tobacco",
                children: [],
              },
              {
                key: "46.18",
                label: "46.18 - Agents specialised in the sale of other particular products",
                children: [],
              },
              {
                key: "46.19",
                label: "46.19 - Agents involved in the sale of a variety of goods",
                children: [],
              },
            ],
          },
          {
            key: "46.2",
            label: "46.2 - Wholesale of agricultural raw materials and live animals",
            children: [
              {
                key: "46.21",
                label: "46.21 - Wholesale of grain, unmanufactured tobacco, seeds and animal feeds",
                children: [],
              },
              {
                key: "46.22",
                label: "46.22 - Wholesale of flowers and plants",
                children: [],
              },
              {
                key: "46.23",
                label: "46.23 - Wholesale of live animals",
                children: [],
              },
              {
                key: "46.24",
                label: "46.24 - Wholesale of hides, skins and leather",
                children: [],
              },
            ],
          },
          {
            key: "46.3",
            label: "46.3 - Wholesale of food, beverages and tobacco",
            children: [
              {
                key: "46.31",
                label: "46.31 - Wholesale of fruit and vegetables",
                children: [],
              },
              {
                key: "46.32",
                label: "46.32 - Wholesale of meat and meat products",
                children: [],
              },
              {
                key: "46.33",
                label: "46.33 - Wholesale of dairy products, eggs and edible oils and fats",
                children: [],
              },
              {
                key: "46.34",
                label: "46.34 - Wholesale of beverages",
                children: [],
              },
              {
                key: "46.35",
                label: "46.35 - Wholesale of tobacco products",
                children: [],
              },
              {
                key: "46.36",
                label: "46.36 - Wholesale of sugar and chocolate and sugar confectionery",
                children: [],
              },
              {
                key: "46.37",
                label: "46.37 - Wholesale of coffee, tea, cocoa and spices",
                children: [],
              },
              {
                key: "46.38",
                label: "46.38 - Wholesale of other food, including fish, crustaceans and molluscs",
                children: [],
              },
              {
                key: "46.39",
                label: "46.39 - Non-specialised wholesale of food, beverages and tobacco",
                children: [],
              },
            ],
          },
          {
            key: "46.4",
            label: "46.4 - Wholesale of household goods",
            children: [
              {
                key: "46.41",
                label: "46.41 - Wholesale of textiles",
                children: [],
              },
              {
                key: "46.42",
                label: "46.42 - Wholesale of clothing and footwear",
                children: [],
              },
              {
                key: "46.43",
                label: "46.43 - Wholesale of electrical household appliances",
                children: [],
              },
              {
                key: "46.44",
                label: "46.44 - Wholesale of china and glassware and cleaning materials",
                children: [],
              },
              {
                key: "46.45",
                label: "46.45 - Wholesale of perfume and cosmetics",
                children: [],
              },
              {
                key: "46.46",
                label: "46.46 - Wholesale of pharmaceutical goods",
                children: [],
              },
              {
                key: "46.47",
                label: "46.47 - Wholesale of furniture, carpets and lighting equipment",
                children: [],
              },
              {
                key: "46.48",
                label: "46.48 - Wholesale of watches and jewellery",
                children: [],
              },
              {
                key: "46.49",
                label: "46.49 - Wholesale of other household goods",
                children: [],
              },
            ],
          },
          {
            key: "46.5",
            label: "46.5 - Wholesale of information and communication equipment",
            children: [
              {
                key: "46.51",
                label: "46.51 - Wholesale of computers, computer peripheral equipment and software",
                children: [],
              },
              {
                key: "46.52",
                label: "46.52 - Wholesale of electronic and telecommunications equipment and parts",
                children: [],
              },
            ],
          },
          {
            key: "46.6",
            label: "46.6 - Wholesale of other machinery, equipment and supplies",
            children: [
              {
                key: "46.61",
                label: "46.61 - Wholesale of agricultural machinery, equipment and supplies",
                children: [],
              },
              {
                key: "46.62",
                label: "46.62 - Wholesale of machine tools",
                children: [],
              },
              {
                key: "46.63",
                label: "46.63 - Wholesale of mining, construction and civil engineering machinery",
                children: [],
              },
              {
                key: "46.64",
                label: "46.64 - Wholesale of machinery for the textile industry and of sewing and knitting machines",
                children: [],
              },
              {
                key: "46.65",
                label: "46.65 - Wholesale of office furniture",
                children: [],
              },
              {
                key: "46.66",
                label: "46.66 - Wholesale of other office machinery and equipment",
                children: [],
              },
              {
                key: "46.69",
                label: "46.69 - Wholesale of other machinery and equipment",
                children: [],
              },
            ],
          },
          {
            key: "46.7",
            label: "46.7 - Other specialised wholesale",
            children: [
              {
                key: "46.71",
                label: "46.71 - Wholesale of solid, liquid and gaseous fuels and related products",
                children: [],
              },
              {
                key: "46.72",
                label: "46.72 - Wholesale of metals and metal ores",
                children: [],
              },
              {
                key: "46.73",
                label: "46.73 - Wholesale of wood, construction materials and sanitary equipment",
                children: [],
              },
              {
                key: "46.74",
                label: "46.74 - Wholesale of hardware, plumbing and heating equipment and supplies",
                children: [],
              },
              {
                key: "46.75",
                label: "46.75 - Wholesale of chemical products",
                children: [],
              },
              {
                key: "46.76",
                label: "46.76 - Wholesale of other intermediate products",
                children: [],
              },
              {
                key: "46.77",
                label: "46.77 - Wholesale of waste and scrap",
                children: [],
              },
            ],
          },
          {
            key: "46.9",
            label: "46.9 - Non-specialised wholesale trade",
            children: [
              {
                key: "46.90",
                label: "46.90 - Non-specialised wholesale trade",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "47",
        label: "47 - Retail trade, except of motor vehicles and motorcycles",
        children: [
          {
            key: "47.1",
            label: "47.1 - Retail sale in non-specialised stores",
            children: [
              {
                key: "47.11",
                label: "47.11 - Retail sale in non-specialised stores with food, beverages or tobacco predominating",
                children: [],
              },
              {
                key: "47.19",
                label: "47.19 - Other retail sale in non-specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.2",
            label: "47.2 - Retail sale of food, beverages and tobacco in specialised stores",
            children: [
              {
                key: "47.21",
                label: "47.21 - Retail sale of fruit and vegetables in specialised stores",
                children: [],
              },
              {
                key: "47.22",
                label: "47.22 - Retail sale of meat and meat products in specialised stores",
                children: [],
              },
              {
                key: "47.23",
                label: "47.23 - Retail sale of fish, crustaceans and molluscs in specialised stores",
                children: [],
              },
              {
                key: "47.24",
                label:
                  "47.24 - Retail sale of bread, cakes, flour confectionery and sugar confectionery in specialised stores",
                children: [],
              },
              {
                key: "47.25",
                label: "47.25 - Retail sale of beverages in specialised stores",
                children: [],
              },
              {
                key: "47.26",
                label: "47.26 - Retail sale of tobacco products in specialised stores",
                children: [],
              },
              {
                key: "47.29",
                label: "47.29 - Other retail sale of food in specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.3",
            label: "47.3 - Retail sale of automotive fuel in specialised stores",
            children: [
              {
                key: "47.30",
                label: "47.30 - Retail sale of automotive fuel in specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.4",
            label: "47.4 - Retail sale of information and communication equipment in specialised stores",
            children: [
              {
                key: "47.41",
                label: "47.41 - Retail sale of computers, peripheral units and software in specialised stores",
                children: [],
              },
              {
                key: "47.42",
                label: "47.42 - Retail sale of telecommunications equipment in specialised stores",
                children: [],
              },
              {
                key: "47.43",
                label: "47.43 - Retail sale of audio and video equipment in specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.5",
            label: "47.5 - Retail sale of other household equipment in specialised stores",
            children: [
              {
                key: "47.51",
                label: "47.51 - Retail sale of textiles in specialised stores",
                children: [],
              },
              {
                key: "47.52",
                label: "47.52 - Retail sale of hardware, paints and glass in specialised stores",
                children: [],
              },
              {
                key: "47.53",
                label: "47.53 - Retail sale of carpets, rugs, wall and floor coverings in specialised stores",
                children: [],
              },
              {
                key: "47.54",
                label: "47.54 - Retail sale of electrical household appliances in specialised stores",
                children: [],
              },
              {
                key: "47.59",
                label:
                  "47.59 - Retail sale of furniture, lighting equipment and other household articles in specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.6",
            label: "47.6 - Retail sale of cultural and recreation goods in specialised stores",
            children: [
              {
                key: "47.61",
                label: "47.61 - Retail sale of books in specialised stores",
                children: [],
              },
              {
                key: "47.62",
                label: "47.62 - Retail sale of newspapers and stationery in specialised stores",
                children: [],
              },
              {
                key: "47.63",
                label: "47.63 - Retail sale of music and video recordings in specialised stores",
                children: [],
              },
              {
                key: "47.64",
                label: "47.64 - Retail sale of sporting equipment in specialised stores",
                children: [],
              },
              {
                key: "47.65",
                label: "47.65 - Retail sale of games and toys in specialised stores",
                children: [],
              },
            ],
          },
          {
            key: "47.7",
            label: "47.7 - Retail sale of other goods in specialised stores",
            children: [
              {
                key: "47.71",
                label: "47.71 - Retail sale of clothing in specialised stores",
                children: [],
              },
              {
                key: "47.72",
                label: "47.72 - Retail sale of footwear and leather goods in specialised stores",
                children: [],
              },
              {
                key: "47.73",
                label: "47.73 - Dispensing chemist in specialised stores",
                children: [],
              },
              {
                key: "47.74",
                label: "47.74 - Retail sale of medical and orthopaedic goods in specialised stores",
                children: [],
              },
              {
                key: "47.75",
                label: "47.75 - Retail sale of cosmetic and toilet articles in specialised stores",
                children: [],
              },
              {
                key: "47.76",
                label:
                  "47.76 - Retail sale of flowers, plants, seeds, fertilisers, pet animals and pet food in specialised stores",
                children: [],
              },
              {
                key: "47.77",
                label: "47.77 - Retail sale of watches and jewellery in specialised stores",
                children: [],
              },
              {
                key: "47.78",
                label: "47.78 - Other retail sale of new goods in specialised stores",
                children: [],
              },
              {
                key: "47.79",
                label: "47.79 - Retail sale of second-hand goods in stores",
                children: [],
              },
            ],
          },
          {
            key: "47.8",
            label: "47.8 - Retail sale via stalls and markets",
            children: [
              {
                key: "47.81",
                label: "47.81 - Retail sale via stalls and markets of food, beverages and tobacco products",
                children: [],
              },
              {
                key: "47.82",
                label: "47.82 - Retail sale via stalls and markets of textiles, clothing and footwear",
                children: [],
              },
              {
                key: "47.89",
                label: "47.89 - Retail sale via stalls and markets of other goods",
                children: [],
              },
            ],
          },
          {
            key: "47.9",
            label: "47.9 - Retail trade not in stores, stalls or markets",
            children: [
              {
                key: "47.91",
                label: "47.91 - Retail sale via mail order houses or via Internet",
                children: [],
              },
              {
                key: "47.99",
                label: "47.99 - Other retail sale not in stores, stalls or markets",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "H",
    label: "H - TRANSPORTATION AND STORAGE",
    children: [
      {
        key: "49",
        label: "49 - Land transport and transport via pipelines",
        children: [
          {
            key: "49.1",
            label: "49.1 - Passenger rail transport, interurban",
            children: [
              {
                key: "49.10",
                label: "49.10 - Passenger rail transport, interurban",
                children: [],
              },
            ],
          },
          {
            key: "49.2",
            label: "49.2 - Freight rail transport",
            children: [
              {
                key: "49.20",
                label: "49.20 - Freight rail transport",
                children: [],
              },
            ],
          },
          {
            key: "49.3",
            label: "49.3 - Other passenger land transport ",
            children: [
              {
                key: "49.31",
                label: "49.31 - Urban and suburban passenger land transport",
                children: [],
              },
              {
                key: "49.32",
                label: "49.32 - Taxi operation",
                children: [],
              },
              {
                key: "49.39",
                label: "49.39 - Other passenger land transport n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "49.4",
            label: "49.4 - Freight transport by road and removal services",
            children: [
              {
                key: "49.41",
                label: "49.41 - Freight transport by road",
                children: [],
              },
              {
                key: "49.42",
                label: "49.42 - Removal services",
                children: [],
              },
            ],
          },
          {
            key: "49.5",
            label: "49.5 - Transport via pipeline",
            children: [
              {
                key: "49.50",
                label: "49.50 - Transport via pipeline",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "50",
        label: "50 - Water transport",
        children: [
          {
            key: "50.1",
            label: "50.1 - Sea and coastal passenger water transport",
            children: [
              {
                key: "50.10",
                label: "50.10 - Sea and coastal passenger water transport",
                children: [],
              },
            ],
          },
          {
            key: "50.2",
            label: "50.2 - Sea and coastal freight water transport",
            children: [
              {
                key: "50.20",
                label: "50.20 - Sea and coastal freight water transport",
                children: [],
              },
            ],
          },
          {
            key: "50.3",
            label: "50.3 - Inland passenger water transport",
            children: [
              {
                key: "50.30",
                label: "50.30 - Inland passenger water transport",
                children: [],
              },
            ],
          },
          {
            key: "50.4",
            label: "50.4 - Inland freight water transport",
            children: [
              {
                key: "50.40",
                label: "50.40 - Inland freight water transport",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "51",
        label: "51 - Air transport",
        children: [
          {
            key: "51.1",
            label: "51.1 - Passenger air transport",
            children: [
              {
                key: "51.10",
                label: "51.10 - Passenger air transport",
                children: [],
              },
            ],
          },
          {
            key: "51.2",
            label: "51.2 - Freight air transport and space transport",
            children: [
              {
                key: "51.21",
                label: "51.21 - Freight air transport",
                children: [],
              },
              {
                key: "51.22",
                label: "51.22 - Space transport",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "52",
        label: "52 - Warehousing and support activities for transportation",
        children: [
          {
            key: "52.1",
            label: "52.1 - Warehousing and storage",
            children: [
              {
                key: "52.10",
                label: "52.10 - Warehousing and storage",
                children: [],
              },
            ],
          },
          {
            key: "52.2",
            label: "52.2 - Support activities for transportation",
            children: [
              {
                key: "52.21",
                label: "52.21 - Service activities incidental to land transportation",
                children: [],
              },
              {
                key: "52.22",
                label: "52.22 - Service activities incidental to water transportation",
                children: [],
              },
              {
                key: "52.23",
                label: "52.23 - Service activities incidental to air transportation",
                children: [],
              },
              {
                key: "52.24",
                label: "52.24 - Cargo handling",
                children: [],
              },
              {
                key: "52.29",
                label: "52.29 - Other transportation support activities ",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "53",
        label: "53 - Postal and courier activities",
        children: [
          {
            key: "53.1",
            label: "53.1 - Postal activities under universal service obligation",
            children: [
              {
                key: "53.10",
                label: "53.10 - Postal activities under universal service obligation",
                children: [],
              },
            ],
          },
          {
            key: "53.2",
            label: "53.2 - Other postal and courier activities",
            children: [
              {
                key: "53.20",
                label: "53.20 - Other postal and courier activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "I",
    label: "I - ACCOMMODATION AND FOOD SERVICE ACTIVITIES",
    children: [
      {
        key: "55",
        label: "55 - Accommodation",
        children: [
          {
            key: "55.1",
            label: "55.1 - Hotels and similar accommodation",
            children: [
              {
                key: "55.10",
                label: "55.10 - Hotels and similar accommodation",
                children: [],
              },
            ],
          },
          {
            key: "55.2",
            label: "55.2 - Holiday and other short-stay accommodation",
            children: [
              {
                key: "55.20",
                label: "55.20 - Holiday and other short-stay accommodation",
                children: [],
              },
            ],
          },
          {
            key: "55.3",
            label: "55.3 - Camping grounds, recreational vehicle parks and trailer parks",
            children: [
              {
                key: "55.30",
                label: "55.30 - Camping grounds, recreational vehicle parks and trailer parks",
                children: [],
              },
            ],
          },
          {
            key: "55.9",
            label: "55.9 - Other accommodation",
            children: [
              {
                key: "55.90",
                label: "55.90 - Other accommodation",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "56",
        label: "56 - Food and beverage service activities",
        children: [
          {
            key: "56.1",
            label: "56.1 - Restaurants and mobile food service activities",
            children: [
              {
                key: "56.10",
                label: "56.10 - Restaurants and mobile food service activities",
                children: [],
              },
            ],
          },
          {
            key: "56.2",
            label: "56.2 - Event catering and other food service activities",
            children: [
              {
                key: "56.21",
                label: "56.21 - Event catering activities",
                children: [],
              },
              {
                key: "56.29",
                label: "56.29 - Other food service activities",
                children: [],
              },
            ],
          },
          {
            key: "56.3",
            label: "56.3 - Beverage serving activities",
            children: [
              {
                key: "56.30",
                label: "56.30 - Beverage serving activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "J",
    label: "J - INFORMATION AND COMMUNICATION",
    children: [
      {
        key: "58",
        label: "58 - Publishing activities",
        children: [
          {
            key: "58.1",
            label: "58.1 - Publishing of books, periodicals and other publishing activities",
            children: [
              {
                key: "58.11",
                label: "58.11 - Book publishing",
                children: [],
              },
              {
                key: "58.12",
                label: "58.12 - Publishing of directories and mailing lists",
                children: [],
              },
              {
                key: "58.13",
                label: "58.13 - Publishing of newspapers",
                children: [],
              },
              {
                key: "58.14",
                label: "58.14 - Publishing of journals and periodicals",
                children: [],
              },
              {
                key: "58.19",
                label: "58.19 - Other publishing activities",
                children: [],
              },
            ],
          },
          {
            key: "58.2",
            label: "58.2 - Software publishing",
            children: [
              {
                key: "58.21",
                label: "58.21 - Publishing of computer games",
                children: [],
              },
              {
                key: "58.29",
                label: "58.29 - Other software publishing",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "59",
        label:
          "59 - Motion picture, video and television programme production, sound recording and music publishing activities",
        children: [
          {
            key: "59.1",
            label: "59.1 - Motion picture, video and television programme activities",
            children: [
              {
                key: "59.11",
                label: "59.11 - Motion picture, video and television programme production activities",
                children: [],
              },
              {
                key: "59.12",
                label: "59.12 - Motion picture, video and television programme post-production activities",
                children: [],
              },
              {
                key: "59.13",
                label: "59.13 - Motion picture, video and television programme distribution activities",
                children: [],
              },
              {
                key: "59.14",
                label: "59.14 - Motion picture projection activities",
                children: [],
              },
            ],
          },
          {
            key: "59.2",
            label: "59.2 - Sound recording and music publishing activities",
            children: [
              {
                key: "59.20",
                label: "59.20 - Sound recording and music publishing activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "60",
        label: "60 - Programming and broadcasting activities",
        children: [
          {
            key: "60.1",
            label: "60.1 - Radio broadcasting",
            children: [
              {
                key: "60.10",
                label: "60.10 - Radio broadcasting",
                children: [],
              },
            ],
          },
          {
            key: "60.2",
            label: "60.2 - Television programming and broadcasting activities",
            children: [
              {
                key: "60.20",
                label: "60.20 - Television programming and broadcasting activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "61",
        label: "61 - Telecommunications",
        children: [
          {
            key: "61.1",
            label: "61.1 - Wired telecommunications activities",
            children: [
              {
                key: "61.10",
                label: "61.10 - Wired telecommunications activities",
                children: [],
              },
            ],
          },
          {
            key: "61.2",
            label: "61.2 - Wireless telecommunications activities",
            children: [
              {
                key: "61.20",
                label: "61.20 - Wireless telecommunications activities",
                children: [],
              },
            ],
          },
          {
            key: "61.3",
            label: "61.3 - Satellite telecommunications activities",
            children: [
              {
                key: "61.30",
                label: "61.30 - Satellite telecommunications activities",
                children: [],
              },
            ],
          },
          {
            key: "61.9",
            label: "61.9 - Other telecommunications activities",
            children: [
              {
                key: "61.90",
                label: "61.90 - Other telecommunications activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "62",
        label: "62 - Computer programming, consultancy and related activities",
        children: [
          {
            key: "62.0",
            label: "62.0 - Computer programming, consultancy and related activities",
            children: [
              {
                key: "62.01",
                label: "62.01 - Computer programming activities",
                children: [],
              },
              {
                key: "62.02",
                label: "62.02 - Computer consultancy activities",
                children: [],
              },
              {
                key: "62.03",
                label: "62.03 - Computer facilities management activities",
                children: [],
              },
              {
                key: "62.09",
                label: "62.09 - Other information technology and computer service activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "63",
        label: "63 - Information service activities",
        children: [
          {
            key: "63.1",
            label: "63.1 - Data processing, hosting and related activities; web portals",
            children: [
              {
                key: "63.11",
                label: "63.11 - Data processing, hosting and related activities",
                children: [],
              },
              {
                key: "63.12",
                label: "63.12 - Web portals",
                children: [],
              },
            ],
          },
          {
            key: "63.9",
            label: "63.9 - Other information service activities",
            children: [
              {
                key: "63.91",
                label: "63.91 - News agency activities",
                children: [],
              },
              {
                key: "63.99",
                label: "63.99 - Other information service activities n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "K",
    label: "K - FINANCIAL AND INSURANCE ACTIVITIES",
    children: [
      {
        key: "64",
        label: "64 - Financial service activities, except insurance and pension funding",
        children: [
          {
            key: "64.1",
            label: "64.1 - Monetary intermediation",
            children: [
              {
                key: "64.11",
                label: "64.11 - Central banking",
                children: [],
              },
              {
                key: "64.19",
                label: "64.19 - Other monetary intermediation",
                children: [],
              },
            ],
          },
          {
            key: "64.2",
            label: "64.2 - Activities of holding companies",
            children: [
              {
                key: "64.20",
                label: "64.20 - Activities of holding companies",
                children: [],
              },
            ],
          },
          {
            key: "64.3",
            label: "64.3 - Trusts, funds and similar financial entities",
            children: [
              {
                key: "64.30",
                label: "64.30 - Trusts, funds and similar financial entities",
                children: [],
              },
            ],
          },
          {
            key: "64.9",
            label: "64.9 - Other financial service activities, except insurance and pension funding",
            children: [
              {
                key: "64.91",
                label: "64.91 - Financial leasing",
                children: [],
              },
              {
                key: "64.92",
                label: "64.92 - Other credit granting",
                children: [],
              },
              {
                key: "64.99",
                label: "64.99 - Other financial service activities, except insurance and pension funding n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "65",
        label: "65 - Insurance, reinsurance and pension funding, except compulsory social security",
        children: [
          {
            key: "65.1",
            label: "65.1 - Insurance",
            children: [
              {
                key: "65.11",
                label: "65.11 - Life insurance",
                children: [],
              },
              {
                key: "65.12",
                label: "65.12 - Non-life insurance",
                children: [],
              },
            ],
          },
          {
            key: "65.2",
            label: "65.2 - Reinsurance",
            children: [
              {
                key: "65.20",
                label: "65.20 - Reinsurance",
                children: [],
              },
            ],
          },
          {
            key: "65.3",
            label: "65.3 - Pension funding",
            children: [
              {
                key: "65.30",
                label: "65.30 - Pension funding",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "66",
        label: "66 - Activities auxiliary to financial services and insurance activities",
        children: [
          {
            key: "66.1",
            label: "66.1 - Activities auxiliary to financial services, except insurance and pension funding",
            children: [
              {
                key: "66.11",
                label: "66.11 - Administration of financial markets",
                children: [],
              },
              {
                key: "66.12",
                label: "66.12 - Security and commodity contracts brokerage",
                children: [],
              },
              {
                key: "66.19",
                label: "66.19 - Other activities auxiliary to financial services, except insurance and pension funding",
                children: [],
              },
            ],
          },
          {
            key: "66.2",
            label: "66.2 - Activities auxiliary to insurance and pension funding",
            children: [
              {
                key: "66.21",
                label: "66.21 - Risk and damage evaluation",
                children: [],
              },
              {
                key: "66.22",
                label: "66.22 - Activities of insurance agents and brokers",
                children: [],
              },
              {
                key: "66.29",
                label: "66.29 - Other activities auxiliary to insurance and pension funding",
                children: [],
              },
            ],
          },
          {
            key: "66.3",
            label: "66.3 - Fund management activities",
            children: [
              {
                key: "66.30",
                label: "66.30 - Fund management activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "L",
    label: "L - REAL ESTATE ACTIVITIES",
    children: [
      {
        key: "68",
        label: "68 - Real estate activities",
        children: [
          {
            key: "68.1",
            label: "68.1 - Buying and selling of own real estate",
            children: [
              {
                key: "68.10",
                label: "68.10 - Buying and selling of own real estate",
                children: [],
              },
            ],
          },
          {
            key: "68.2",
            label: "68.2 - Rental and operating of own or leased real estate",
            children: [
              {
                key: "68.20",
                label: "68.20 - Rental and operating of own or leased real estate",
                children: [],
              },
            ],
          },
          {
            key: "68.3",
            label: "68.3 - Real estate activities on a fee or contract basis",
            children: [
              {
                key: "68.31",
                label: "68.31 - Real estate agencies",
                children: [],
              },
              {
                key: "68.32",
                label: "68.32 - Management of real estate on a fee or contract basis",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "M",
    label: "M - PROFESSIONAL, SCIENTIFIC AND TECHNICAL ACTIVITIES",
    children: [
      {
        key: "69",
        label: "69 - Legal and accounting activities",
        children: [
          {
            key: "69.1",
            label: "69.1 - Legal activities",
            children: [
              {
                key: "69.10",
                label: "69.10 - Legal activities",
                children: [],
              },
            ],
          },
          {
            key: "69.2",
            label: "69.2 - Accounting, bookkeeping and auditing activities; tax consultancy",
            children: [
              {
                key: "69.20",
                label: "69.20 - Accounting, bookkeeping and auditing activities; tax consultancy",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "70",
        label: "70 - Activities of head offices; management consultancy activities",
        children: [
          {
            key: "70.1",
            label: "70.1 - Activities of head offices",
            children: [
              {
                key: "70.10",
                label: "70.10 - Activities of head offices",
                children: [],
              },
            ],
          },
          {
            key: "70.2",
            label: "70.2 - Management consultancy activities",
            children: [
              {
                key: "70.21",
                label: "70.21 - Public relations and communication activities",
                children: [],
              },
              {
                key: "70.22",
                label: "70.22 - Business and other management consultancy activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "71",
        label: "71 - Architectural and engineering activities; technical testing and analysis",
        children: [
          {
            key: "71.1",
            label: "71.1 - Architectural and engineering activities and related technical consultancy",
            children: [
              {
                key: "71.11",
                label: "71.11 - Architectural activities ",
                children: [],
              },
              {
                key: "71.12",
                label: "71.12 - Engineering activities and related technical consultancy",
                children: [],
              },
            ],
          },
          {
            key: "71.2",
            label: "71.2 - Technical testing and analysis",
            children: [
              {
                key: "71.20",
                label: "71.20 - Technical testing and analysis",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "72",
        label: "72 - Scientific research and development ",
        children: [
          {
            key: "72.1",
            label: "72.1 - Research and experimental development on natural sciences and engineering",
            children: [
              {
                key: "72.11",
                label: "72.11 - Research and experimental development on biotechnology",
                children: [],
              },
              {
                key: "72.19",
                label: "72.19 - Other research and experimental development on natural sciences and engineering",
                children: [],
              },
            ],
          },
          {
            key: "72.2",
            label: "72.2 - Research and experimental development on social sciences and humanities",
            children: [
              {
                key: "72.20",
                label: "72.20 - Research and experimental development on social sciences and humanities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "73",
        label: "73 - Advertising and market research",
        children: [
          {
            key: "73.1",
            label: "73.1 - Advertising",
            children: [
              {
                key: "73.11",
                label: "73.11 - Advertising agencies",
                children: [],
              },
              {
                key: "73.12",
                label: "73.12 - Media representation",
                children: [],
              },
            ],
          },
          {
            key: "73.2",
            label: "73.2 - Market research and public opinion polling",
            children: [
              {
                key: "73.20",
                label: "73.20 - Market research and public opinion polling",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "74",
        label: "74 - Other professional, scientific and technical activities",
        children: [
          {
            key: "74.1",
            label: "74.1 - Specialised design activities",
            children: [
              {
                key: "74.10",
                label: "74.10 - Specialised design activities",
                children: [],
              },
            ],
          },
          {
            key: "74.2",
            label: "74.2 - Photographic activities",
            children: [
              {
                key: "74.20",
                label: "74.20 - Photographic activities",
                children: [],
              },
            ],
          },
          {
            key: "74.3",
            label: "74.3 - Translation and interpretation activities",
            children: [
              {
                key: "74.30",
                label: "74.30 - Translation and interpretation activities",
                children: [],
              },
            ],
          },
          {
            key: "74.9",
            label: "74.9 - Other professional, scientific and technical activities n.e.c.",
            children: [
              {
                key: "74.90",
                label: "74.90 - Other professional, scientific and technical activities n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "75",
        label: "75 - Veterinary activities",
        children: [
          {
            key: "75.0",
            label: "75.0 - Veterinary activities",
            children: [
              {
                key: "75.00",
                label: "75.00 - Veterinary activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "N",
    label: "N - ADMINISTRATIVE AND SUPPORT SERVICE ACTIVITIES",
    children: [
      {
        key: "77",
        label: "77 - Rental and leasing activities",
        children: [
          {
            key: "77.1",
            label: "77.1 - Rental and leasing of motor vehicles",
            children: [
              {
                key: "77.11",
                label: "77.11 - Rental and leasing of cars and light motor vehicles",
                children: [],
              },
              {
                key: "77.12",
                label: "77.12 - Rental and leasing of trucks",
                children: [],
              },
            ],
          },
          {
            key: "77.2",
            label: "77.2 - Rental and leasing of personal and household goods",
            children: [
              {
                key: "77.21",
                label: "77.21 - Rental and leasing of recreational and sports goods",
                children: [],
              },
              {
                key: "77.22",
                label: "77.22 - Rental of video tapes and disks",
                children: [],
              },
              {
                key: "77.29",
                label: "77.29 - Rental and leasing of other personal and household goods",
                children: [],
              },
            ],
          },
          {
            key: "77.3",
            label: "77.3 - Rental and leasing of other machinery, equipment and tangible goods",
            children: [
              {
                key: "77.31",
                label: "77.31 - Rental and leasing of agricultural machinery and equipment",
                children: [],
              },
              {
                key: "77.32",
                label: "77.32 - Rental and leasing of construction and civil engineering machinery and equipment",
                children: [],
              },
              {
                key: "77.33",
                label: "77.33 - Rental and leasing of office machinery and equipment (including computers)",
                children: [],
              },
              {
                key: "77.34",
                label: "77.34 - Rental and leasing of water transport equipment",
                children: [],
              },
              {
                key: "77.35",
                label: "77.35 - Rental and leasing of air transport equipment",
                children: [],
              },
              {
                key: "77.39",
                label: "77.39 - Rental and leasing of other machinery, equipment and tangible goods n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "77.4",
            label: "77.4 - Leasing of intellectual property and similar products, except copyrighted works",
            children: [
              {
                key: "77.40",
                label: "77.40 - Leasing of intellectual property and similar products, except copyrighted works",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "78",
        label: "78 - Employment activities",
        children: [
          {
            key: "78.1",
            label: "78.1 - Activities of employment placement agencies",
            children: [
              {
                key: "78.10",
                label: "78.10 - Activities of employment placement agencies",
                children: [],
              },
            ],
          },
          {
            key: "78.2",
            label: "78.2 - Temporary employment agency activities",
            children: [
              {
                key: "78.20",
                label: "78.20 - Temporary employment agency activities",
                children: [],
              },
            ],
          },
          {
            key: "78.3",
            label: "78.3 - Other human resources provision",
            children: [
              {
                key: "78.30",
                label: "78.30 - Other human resources provision",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "79",
        label: "79 - Travel agency, tour operator and other reservation service and related activities",
        children: [
          {
            key: "79.1",
            label: "79.1 - Travel agency and tour operator activities",
            children: [
              {
                key: "79.11",
                label: "79.11 - Travel agency activities",
                children: [],
              },
              {
                key: "79.12",
                label: "79.12 - Tour operator activities",
                children: [],
              },
            ],
          },
          {
            key: "79.9",
            label: "79.9 - Other reservation service and related activities",
            children: [
              {
                key: "79.90",
                label: "79.90 - Other reservation service and related activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "80",
        label: "80 - Security and investigation activities",
        children: [
          {
            key: "80.1",
            label: "80.1 - Private security activities",
            children: [
              {
                key: "80.10",
                label: "80.10 - Private security activities",
                children: [],
              },
            ],
          },
          {
            key: "80.2",
            label: "80.2 - Security systems service activities",
            children: [
              {
                key: "80.20",
                label: "80.20 - Security systems service activities",
                children: [],
              },
            ],
          },
          {
            key: "80.3",
            label: "80.3 - Investigation activities",
            children: [
              {
                key: "80.30",
                label: "80.30 - Investigation activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "81",
        label: "81 - Services to buildings and landscape activities",
        children: [
          {
            key: "81.1",
            label: "81.1 - Combined facilities support activities",
            children: [
              {
                key: "81.10",
                label: "81.10 - Combined facilities support activities",
                children: [],
              },
            ],
          },
          {
            key: "81.2",
            label: "81.2 - Cleaning activities",
            children: [
              {
                key: "81.21",
                label: "81.21 - General cleaning of buildings",
                children: [],
              },
              {
                key: "81.22",
                label: "81.22 - Other building and industrial cleaning activities",
                children: [],
              },
              {
                key: "81.29",
                label: "81.29 - Other cleaning activities",
                children: [],
              },
            ],
          },
          {
            key: "81.3",
            label: "81.3 - Landscape service activities",
            children: [
              {
                key: "81.30",
                label: "81.30 - Landscape service activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "82",
        label: "82 - Office administrative, office support and other business support activities",
        children: [
          {
            key: "82.1",
            label: "82.1 - Office administrative and support activities",
            children: [
              {
                key: "82.11",
                label: "82.11 - Combined office administrative service activities",
                children: [],
              },
              {
                key: "82.19",
                label: "82.19 - Photocopying, document preparation and other specialised office support activities",
                children: [],
              },
            ],
          },
          {
            key: "82.2",
            label: "82.2 - Activities of call centres",
            children: [
              {
                key: "82.20",
                label: "82.20 - Activities of call centres",
                children: [],
              },
            ],
          },
          {
            key: "82.3",
            label: "82.3 - Organisation of conventions and trade shows",
            children: [
              {
                key: "82.30",
                label: "82.30 - Organisation of conventions and trade shows",
                children: [],
              },
            ],
          },
          {
            key: "82.9",
            label: "82.9 - Business support service activities n.e.c.",
            children: [
              {
                key: "82.91",
                label: "82.91 - Activities of collection agencies and credit bureaus",
                children: [],
              },
              {
                key: "82.92",
                label: "82.92 - Packaging activities",
                children: [],
              },
              {
                key: "82.99",
                label: "82.99 - Other business support service activities n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "O",
    label: "O - PUBLIC ADMINISTRATION AND DEFENCE; COMPULSORY SOCIAL SECURITY",
    children: [
      {
        key: "84",
        label: "84 - Public administration and defence; compulsory social security",
        children: [
          {
            key: "84.1",
            label: "84.1 - Administration of the State and the economic and social policy of the community",
            children: [
              {
                key: "84.11",
                label: "84.11 - General public administration activities",
                children: [],
              },
              {
                key: "84.12",
                label:
                  "84.12 - Regulation of the activities of providing health care, education, cultural services and other social services, excluding social security",
                children: [],
              },
              {
                key: "84.13",
                label: "84.13 - Regulation of and contribution to more efficient operation of businesses",
                children: [],
              },
            ],
          },
          {
            key: "84.2",
            label: "84.2 - Provision of services to the community as a whole",
            children: [
              {
                key: "84.21",
                label: "84.21 - Foreign affairs",
                children: [],
              },
              {
                key: "84.22",
                label: "84.22 - Defence activities",
                children: [],
              },
              {
                key: "84.23",
                label: "84.23 - Justice and judicial activities",
                children: [],
              },
              {
                key: "84.24",
                label: "84.24 - Public order and safety activities",
                children: [],
              },
              {
                key: "84.25",
                label: "84.25 - Fire service activities",
                children: [],
              },
            ],
          },
          {
            key: "84.3",
            label: "84.3 - Compulsory social security activities",
            children: [
              {
                key: "84.30",
                label: "84.30 - Compulsory social security activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "P",
    label: "P - EDUCATION",
    children: [
      {
        key: "85",
        label: "85 - Education",
        children: [
          {
            key: "85.1",
            label: "85.1 - Pre-primary education",
            children: [
              {
                key: "85.10",
                label: "85.10 - Pre-primary education ",
                children: [],
              },
            ],
          },
          {
            key: "85.2",
            label: "85.2 - Primary education",
            children: [
              {
                key: "85.20",
                label: "85.20 - Primary education ",
                children: [],
              },
            ],
          },
          {
            key: "85.3",
            label: "85.3 - Secondary education",
            children: [
              {
                key: "85.31",
                label: "85.31 - General secondary education ",
                children: [],
              },
              {
                key: "85.32",
                label: "85.32 - Technical and vocational secondary education ",
                children: [],
              },
            ],
          },
          {
            key: "85.4",
            label: "85.4 - Higher education",
            children: [
              {
                key: "85.41",
                label: "85.41 - Post-secondary non-tertiary education",
                children: [],
              },
              {
                key: "85.42",
                label: "85.42 - Tertiary education",
                children: [],
              },
            ],
          },
          {
            key: "85.5",
            label: "85.5 - Other education",
            children: [
              {
                key: "85.51",
                label: "85.51 - Sports and recreation education",
                children: [],
              },
              {
                key: "85.52",
                label: "85.52 - Cultural education",
                children: [],
              },
              {
                key: "85.53",
                label: "85.53 - Driving school activities",
                children: [],
              },
              {
                key: "85.59",
                label: "85.59 - Other education n.e.c.",
                children: [],
              },
            ],
          },
          {
            key: "85.6",
            label: "85.6 - Educational support activities",
            children: [
              {
                key: "85.60",
                label: "85.60 - Educational support activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "Q",
    label: "Q - HUMAN HEALTH AND SOCIAL WORK ACTIVITIES",
    children: [
      {
        key: "86",
        label: "86 - Human health activities",
        children: [
          {
            key: "86.1",
            label: "86.1 - Hospital activities",
            children: [
              {
                key: "86.10",
                label: "86.10 - Hospital activities",
                children: [],
              },
            ],
          },
          {
            key: "86.2",
            label: "86.2 - Medical and dental practice activities",
            children: [
              {
                key: "86.21",
                label: "86.21 - General medical practice activities",
                children: [],
              },
              {
                key: "86.22",
                label: "86.22 - Specialist medical practice activities",
                children: [],
              },
              {
                key: "86.23",
                label: "86.23 - Dental practice activities",
                children: [],
              },
            ],
          },
          {
            key: "86.9",
            label: "86.9 - Other human health activities",
            children: [
              {
                key: "86.90",
                label: "86.90 - Other human health activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "87",
        label: "87 - Residential care activities",
        children: [
          {
            key: "87.1",
            label: "87.1 - Residential nursing care activities",
            children: [
              {
                key: "87.10",
                label: "87.10 - Residential nursing care activities",
                children: [],
              },
            ],
          },
          {
            key: "87.2",
            label: "87.2 - Residential care activities for mental retardation, mental health and substance abuse",
            children: [
              {
                key: "87.20",
                label: "87.20 - Residential care activities for mental retardation, mental health and substance abuse",
                children: [],
              },
            ],
          },
          {
            key: "87.3",
            label: "87.3 - Residential care activities for the elderly and disabled",
            children: [
              {
                key: "87.30",
                label: "87.30 - Residential care activities for the elderly and disabled",
                children: [],
              },
            ],
          },
          {
            key: "87.9",
            label: "87.9 - Other residential care activities",
            children: [
              {
                key: "87.90",
                label: "87.90 - Other residential care activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "88",
        label: "88 - Social work activities without accommodation",
        children: [
          {
            key: "88.1",
            label: "88.1 - Social work activities without accommodation for the elderly and disabled",
            children: [
              {
                key: "88.10",
                label: "88.10 - Social work activities without accommodation for the elderly and disabled",
                children: [],
              },
            ],
          },
          {
            key: "88.9",
            label: "88.9 - Other social work activities without accommodation",
            children: [
              {
                key: "88.91",
                label: "88.91 - Child day-care activities",
                children: [],
              },
              {
                key: "88.99",
                label: "88.99 - Other social work activities without accommodation n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "R",
    label: "R - ARTS, ENTERTAINMENT AND RECREATION",
    children: [
      {
        key: "90",
        label: "90 - Creative, arts and entertainment activities",
        children: [
          {
            key: "90.0",
            label: "90.0 - Creative, arts and entertainment activities",
            children: [
              {
                key: "90.01",
                label: "90.01 - Performing arts",
                children: [],
              },
              {
                key: "90.02",
                label: "90.02 - Support activities to performing arts",
                children: [],
              },
              {
                key: "90.03",
                label: "90.03 - Artistic creation",
                children: [],
              },
              {
                key: "90.04",
                label: "90.04 - Operation of arts facilities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "91",
        label: "91 - Libraries, archives, museums and other cultural activities",
        children: [
          {
            key: "91.0",
            label: "91.0 - Libraries, archives, museums and other cultural activities",
            children: [
              {
                key: "91.01",
                label: "91.01 - Library and archives activities",
                children: [],
              },
              {
                key: "91.02",
                label: "91.02 - Museums activities",
                children: [],
              },
              {
                key: "91.03",
                label: "91.03 - Operation of historical sites and buildings and similar visitor attractions",
                children: [],
              },
              {
                key: "91.04",
                label: "91.04 - Botanical and zoological gardens and nature reserves activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "92",
        label: "92 - Gambling and betting activities",
        children: [
          {
            key: "92.0",
            label: "92.0 - Gambling and betting activities",
            children: [
              {
                key: "92.00",
                label: "92.00 - Gambling and betting activities",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "93",
        label: "93 - Sports activities and amusement and recreation activities",
        children: [
          {
            key: "93.1",
            label: "93.1 - Sports activities",
            children: [
              {
                key: "93.11",
                label: "93.11 - Operation of sports facilities",
                children: [],
              },
              {
                key: "93.12",
                label: "93.12 - Activities of sports clubs",
                children: [],
              },
              {
                key: "93.13",
                label: "93.13 - Fitness facilities",
                children: [],
              },
              {
                key: "93.19",
                label: "93.19 - Other sports activities",
                children: [],
              },
            ],
          },
          {
            key: "93.2",
            label: "93.2 - Amusement and recreation activities",
            children: [
              {
                key: "93.21",
                label: "93.21 - Activities of amusement parks and theme parks",
                children: [],
              },
              {
                key: "93.29",
                label: "93.29 - Other amusement and recreation activities",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "S",
    label: "S - OTHER SERVICE ACTIVITIES",
    children: [
      {
        key: "94",
        label: "94 - Activities of membership organisations",
        children: [
          {
            key: "94.1",
            label: "94.1 - Activities of business, employers and professional membership organisations",
            children: [
              {
                key: "94.11",
                label: "94.11 - Activities of business and employers membership organisations",
                children: [],
              },
              {
                key: "94.12",
                label: "94.12 - Activities of professional membership organisations",
                children: [],
              },
            ],
          },
          {
            key: "94.2",
            label: "94.2 - Activities of trade unions",
            children: [
              {
                key: "94.20",
                label: "94.20 - Activities of trade unions",
                children: [],
              },
            ],
          },
          {
            key: "94.9",
            label: "94.9 - Activities of other membership organisations",
            children: [
              {
                key: "94.91",
                label: "94.91 - Activities of religious organisations",
                children: [],
              },
              {
                key: "94.92",
                label: "94.92 - Activities of political organisations",
                children: [],
              },
              {
                key: "94.99",
                label: "94.99 - Activities of other membership organisations n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "95",
        label: "95 - Repair of computers and personal and household goods",
        children: [
          {
            key: "95.1",
            label: "95.1 - Repair of computers and communication equipment",
            children: [
              {
                key: "95.11",
                label: "95.11 - Repair of computers and peripheral equipment",
                children: [],
              },
              {
                key: "95.12",
                label: "95.12 - Repair of communication equipment",
                children: [],
              },
            ],
          },
          {
            key: "95.2",
            label: "95.2 - Repair of personal and household goods",
            children: [
              {
                key: "95.21",
                label: "95.21 - Repair of consumer electronics",
                children: [],
              },
              {
                key: "95.22",
                label: "95.22 - Repair of household appliances and home and garden equipment",
                children: [],
              },
              {
                key: "95.23",
                label: "95.23 - Repair of footwear and leather goods",
                children: [],
              },
              {
                key: "95.24",
                label: "95.24 - Repair of furniture and home furnishings",
                children: [],
              },
              {
                key: "95.25",
                label: "95.25 - Repair of watches, clocks and jewellery",
                children: [],
              },
              {
                key: "95.29",
                label: "95.29 - Repair of other personal and household goods",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "96",
        label: "96 - Other personal service activities",
        children: [
          {
            key: "96.0",
            label: "96.0 - Other personal service activities",
            children: [
              {
                key: "96.01",
                label: "96.01 - Washing and (dry-)cleaning of textile and fur products",
                children: [],
              },
              {
                key: "96.02",
                label: "96.02 - Hairdressing and other beauty treatment",
                children: [],
              },
              {
                key: "96.03",
                label: "96.03 - Funeral and related activities",
                children: [],
              },
              {
                key: "96.04",
                label: "96.04 - Physical well-being activities",
                children: [],
              },
              {
                key: "96.09",
                label: "96.09 - Other personal service activities n.e.c.",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "T",
    label:
      "T - ACTIVITIES OF HOUSEHOLDS AS EMPLOYERS; UNDIFFERENTIATED GOODS- AND SERVICES-PRODUCING ACTIVITIES OF HOUSEHOLDS FOR OWN USE",
    children: [
      {
        key: "97",
        label: "97 - Activities of households as employers of domestic personnel",
        children: [
          {
            key: "97.0",
            label: "97.0 - Activities of households as employers of domestic personnel",
            children: [
              {
                key: "97.00",
                label: "97.00 - Activities of households as employers of domestic personnel",
                children: [],
              },
            ],
          },
        ],
      },
      {
        key: "98",
        label: "98 - Undifferentiated goods- and services-producing activities of private households for own use",
        children: [
          {
            key: "98.1",
            label: "98.1 - Undifferentiated goods-producing activities of private households for own use",
            children: [
              {
                key: "98.10",
                label: "98.10 - Undifferentiated goods-producing activities of private households for own use",
                children: [],
              },
            ],
          },
          {
            key: "98.2",
            label: "98.2 - Undifferentiated service-producing activities of private households for own use",
            children: [
              {
                key: "98.20",
                label: "98.20 - Undifferentiated service-producing activities of private households for own use",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "U",
    label: "U - ACTIVITIES OF EXTRATERRITORIAL ORGANISATIONS AND BODIES",
    children: [
      {
        key: "99",
        label: "99 - Activities of extraterritorial organisations and bodies",
        children: [
          {
            key: "99.0",
            label: "99.0 - Activities of extraterritorial organisations and bodies",
            children: [
              {
                key: "99.00",
                label: "99.00 - Activities of extraterritorial organisations and bodies",
                children: [],
              },
            ],
          },
        ],
      },
    ],
  },
];

export const naceCodeMap: Map<string, TreeNode> = new Map();

/**
 * Transforms the nace code tree to a nace code map
 * @param input an array of nace code tree nodes
 */
function populateNaceCodeMap(input: Array<TreeNode>): void {
  for (const element of input) {
    naceCodeMap.set(assertDefined(element.key), element);
    populateNaceCodeMap(element.children ?? []);
  }
}

populateNaceCodeMap(naceCodeTree);
