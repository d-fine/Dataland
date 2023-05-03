import { TreeNode } from "primevue/tree";
import { assertDefined } from "@/utils/TypeScriptUtils";

/**
 * Recursively filteres the list of TreeNodes to only contain nodes whose label matches the searchTerm.
 * Nodes are included if their own label OR one of their childs labels matches the searchTerm.
 * @param nodes the list of nodes to filter
 * @param searchTerm the searchTerm to filter for
 * @returns the filtered list of TreeNodes
 */
export function filterNodes(nodes: Array<TreeNode>, searchTerm: string): Array<TreeNode> {
  const lowerSearchTerm = searchTerm.toLowerCase().trim();
  return nodes.filter((it) => {
    const filterMatchesNode = assertDefined(it.label).toLowerCase().indexOf(lowerSearchTerm) > -1;
    it.children = filterNodes(it.children || [], searchTerm);
    return filterMatchesNode || it.children.length > 0;
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
    label: "A - Agriculture, hunting and forestry",
    children: [
      {
        key: "AA",
        label: "AA - Agriculture, hunting and forestry",
        children: [
          {
            key: "01",
            label: "01 - Agriculture, hunting and related service activities",
            children: [
              {
                key: "01.1",
                label: "01.1 - Growing of crops; market gardening; horticulture",
                children: [
                  { key: "01.11", label: "01.11 - Growing of cereals and other crops n.e.c.", children: [] },
                  {
                    key: "01.12",
                    label: "01.12 - Growing of vegetables, horticultural specialities and nursery products",
                    children: [],
                  },
                  { key: "01.13", label: "01.13 - Growing of fruit, nuts, beverage and spice crops", children: [] },
                ],
              },
              {
                key: "01.2",
                label: "01.2 - Farming of animals",
                children: [
                  { key: "01.21", label: "01.21 - Farming of cattle, dairy farming", children: [] },
                  {
                    key: "01.22",
                    label: "01.22 - Farming of sheep, goats, horses, asses, mules and hinnies",
                    children: [],
                  },
                  { key: "01.23", label: "01.23 - Farming of swine", children: [] },
                  { key: "01.24", label: "01.24 - Farming of poultry", children: [] },
                  { key: "01.25", label: "01.25 - Other farming of animals", children: [] },
                ],
              },
              {
                key: "01.3",
                label: "01.3 - Growing of crops combined with farming of animals (mixed farming)",
                children: [
                  {
                    key: "01.30",
                    label: "01.30 - Growing of crops combined with farming of animals (mixed farming)",
                    children: [],
                  },
                ],
              },
              {
                key: "01.4",
                label:
                  "01.4 - Agricultural and animal husbandry service activities, except veterinary activities; landscape gardening",
                children: [
                  { key: "01.41", label: "01.41 - Agricultural service activities; landscape gardening", children: [] },
                  {
                    key: "01.42",
                    label: "01.42 - Animal husbandry service activities, except veterinary activities",
                    children: [],
                  },
                ],
              },
              {
                key: "01.5",
                label: "01.5 - Hunting, trapping and game propagation, including related service activities",
                children: [
                  {
                    key: "01.50",
                    label: "01.50 - Hunting, trapping and game propagation, including related service activities",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "02",
            label: "02 - Forestry, logging and related service activities",
            children: [
              {
                key: "02.0",
                label: "02.0 - Forestry, logging and related service activities",
                children: [
                  { key: "02.01", label: "02.01 - Forestry and logging", children: [] },
                  { key: "02.02", label: "02.02 - Forestry and logging related service activities", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "B",
    label: "B - Fishing",
    children: [
      {
        key: "BA",
        label: "BA - Fishing",
        children: [
          {
            key: "05",
            label: "05 - Fishing,  fish farming and related service activities",
            children: [
              {
                key: "05.0",
                label: "05.0 - Fishing,  fish farming and related service activities",
                children: [
                  { key: "05.01", label: "05.01 - Fishing", children: [] },
                  { key: "05.02", label: "05.02 -  Fish farming", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "C",
    label: "C - Mining and quarrying",
    children: [
      {
        key: "CA",
        label: "CA - Mining and quarrying of energy producing materials",
        children: [
          {
            key: "10",
            label: "10 - Mining of coal and lignite; extraction of peat",
            children: [
              {
                key: "10.1",
                label: "10.1 - Mining and agglomeration of hard coal",
                children: [{ key: "10.10", label: "10.10 - Mining and agglomeration of hard coal", children: [] }],
              },
              {
                key: "10.2",
                label: "10.2 - Mining and agglomeration of lignite",
                children: [{ key: "10.20", label: "10.20 - Mining and agglomeration of lignite", children: [] }],
              },
              {
                key: "10.3",
                label: "10.3 - Extraction and agglomeration of peat",
                children: [{ key: "10.30", label: "10.30 - Extraction and agglomeration of peat", children: [] }],
              },
            ],
          },
          {
            key: "11",
            label:
              "11 - Extraction of crude petroleum and natural gas; service activities incidental to oil and gas extraction, excluding surveying",
            children: [
              {
                key: "11.1",
                label: "11.1 - Extraction of crude petroleum and natural gas",
                children: [
                  { key: "11.10", label: "11.10 - Extraction of crude petroleum and natural gas", children: [] },
                ],
              },
              {
                key: "11.2",
                label: "11.2 - Service activities incidental to oil and gas extraction, excluding surveying",
                children: [
                  {
                    key: "11.20",
                    label: "11.20 - Service activities incidental to oil and gas extraction, excluding surveying",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "12",
            label: "12 - Mining of uranium and thorium ores",
            children: [
              {
                key: "12.0",
                label: "12.0 - Mining of uranium and thorium ores",
                children: [{ key: "12.00", label: "12.00 - Mining of uranium and thorium ores", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "CB",
        label: "CB - Mining and quarrying, except of energy producing materials",
        children: [
          {
            key: "13",
            label: "13 - Mining of metal ores",
            children: [
              {
                key: "13.1",
                label: "13.1 - Mining of iron ores",
                children: [{ key: "13.10", label: "13.10 - Mining of iron ores", children: [] }],
              },
              {
                key: "13.2",
                label: "13.2 - Mining of non-ferrous metal ores, except uranium and thorium ores",
                children: [
                  {
                    key: "13.20",
                    label: "13.20 - Mining of non-ferrous metal ores, except uranium and thorium ores",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "14",
            label: "14 - Other mining and quarrying",
            children: [
              {
                key: "14.1",
                label: "14.1 - Quarrying of stone",
                children: [
                  { key: "14.11", label: "14.11 - Quarrying of  ornamental and building stone", children: [] },
                  { key: "14.12", label: "14.12 - Quarrying of limestone, gypsum and chalk", children: [] },
                  { key: "14.13", label: "14.13 - Quarrying of slate", children: [] },
                ],
              },
              {
                key: "14.2",
                label: "14.2 - Quarrying of sand and clay",
                children: [
                  { key: "14.21", label: "14.21 - Operation of gravel and sand pits", children: [] },
                  { key: "14.22", label: "14.22 - Mining of clays and kaolin", children: [] },
                ],
              },
              {
                key: "14.3",
                label: "14.3 - Mining of chemical and fertilizer minerals",
                children: [{ key: "14.30", label: "14.30 - Mining of chemical and fertilizer minerals", children: [] }],
              },
              {
                key: "14.4",
                label: "14.4 - Production of salt",
                children: [{ key: "14.40", label: "14.40 - Production of salt", children: [] }],
              },
              {
                key: "14.5",
                label: "14.5 - Other mining and quarrying n.e.c.",
                children: [{ key: "14.50", label: "14.50 - Other mining and quarrying n.e.c.", children: [] }],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "D",
    label: "D - Manufacturing",
    children: [
      {
        key: "DA",
        label: "DA - Manufacture of food products, beverages and tobacco",
        children: [
          {
            key: "15",
            label: "15 - Manufacture of food products and beverages",
            children: [
              {
                key: "15.1",
                label: "15.1 - Production, processing and preserving of meat and meat products",
                children: [
                  { key: "15.11", label: "15.11 - Production and preserving of meat", children: [] },
                  { key: "15.12", label: "15.12 - Production and preserving of poultrymeat", children: [] },
                  { key: "15.13", label: "15.13 - Production of meat and poultrymeat products", children: [] },
                ],
              },
              {
                key: "15.2",
                label: "15.2 - Processing and preserving of fish and fish products",
                children: [
                  { key: "15.20", label: "15.20 - Processing and preserving of fish and fish products", children: [] },
                ],
              },
              {
                key: "15.3",
                label: "15.3 - Processing and preserving of fruit and vegetables",
                children: [
                  { key: "15.31", label: "15.31 - Processing and preserving of potatoes", children: [] },
                  { key: "15.32", label: "15.32 - Manufacture of fruit and vegetable juice", children: [] },
                  {
                    key: "15.33",
                    label: "15.33 - Processing and preserving of fruit and vegetables n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "15.4",
                label: "15.4 - Manufacture of vegetable and animal oils and fats",
                children: [
                  { key: "15.41", label: "15.41 - Manufacture of crude oils and fats", children: [] },
                  { key: "15.42", label: "15.42 - Manufacture of refined oils and fats", children: [] },
                  { key: "15.43", label: "15.43 - Manufacture of margarine and similar edible fats", children: [] },
                ],
              },
              {
                key: "15.5",
                label: "15.5 - Manufacture of dairy products",
                children: [
                  { key: "15.51", label: "15.51 - Operation of dairies and cheese making", children: [] },
                  { key: "15.52", label: "15.52 - Manufacture of ice cream", children: [] },
                ],
              },
              {
                key: "15.6",
                label: "15.6 - Manufacture of grain mill products, starches and starch products",
                children: [
                  { key: "15.61", label: "15.61 - Manufacture of grain mill products", children: [] },
                  { key: "15.62", label: "15.62 - Manufacture of starches and starch products", children: [] },
                ],
              },
              {
                key: "15.7",
                label: "15.7 - Manufacture of prepared animal feeds",
                children: [
                  { key: "15.71", label: "15.71 - Manufacture of prepared feeds for farm animals", children: [] },
                  { key: "15.72", label: "15.72 - Manufacture of prepared pet foods", children: [] },
                ],
              },
              {
                key: "15.8",
                label: "15.8 - Manufacture of other food products",
                children: [
                  {
                    key: "15.81",
                    label: "15.81 - Manufacture of bread; manufacture of fresh pastry goods and cakes",
                    children: [],
                  },
                  {
                    key: "15.82",
                    label: "15.82 - Manufacture of rusks and biscuits; manufacture of preserved pastry goods and cakes",
                    children: [],
                  },
                  { key: "15.83", label: "15.83 - Manufacture of sugar", children: [] },
                  {
                    key: "15.84",
                    label: "15.84 - Manufacture of cocoa; chocolate and sugar confectionery",
                    children: [],
                  },
                  {
                    key: "15.85",
                    label: "15.85 - Manufacture of macaroni, noodles, couscous and similar farinaceous products",
                    children: [],
                  },
                  { key: "15.86", label: "15.86 - Processing of tea and coffee", children: [] },
                  { key: "15.87", label: "15.87 - Manufacture of condiments and seasonings", children: [] },
                  {
                    key: "15.88",
                    label: "15.88 - Manufacture of homogenized food preparations and dietetic food",
                    children: [],
                  },
                  { key: "15.89", label: "15.89 - Manufacture of other food products n.e.c.", children: [] },
                ],
              },
              {
                key: "15.9",
                label: "15.9 - Manufacture of beverages",
                children: [
                  { key: "15.91", label: "15.91 - Manufacture of distilled potable alcoholic beverages", children: [] },
                  { key: "15.92", label: "15.92 - Production of ethyl alcohol from fermented materials", children: [] },
                  { key: "15.93", label: "15.93 - Manufacture of wines", children: [] },
                  { key: "15.94", label: "15.94 - Manufacture of cider and other fruit wines", children: [] },
                  {
                    key: "15.95",
                    label: "15.95 - Manufacture of other non-distilled fermented beverages",
                    children: [],
                  },
                  { key: "15.96", label: "15.96 - Manufacture of beer", children: [] },
                  { key: "15.97", label: "15.97 - Manufacture of malt", children: [] },
                  { key: "15.98", label: "15.98 - Production of mineral waters and soft drinks", children: [] },
                ],
              },
            ],
          },
          {
            key: "16",
            label: "16 - Manufacture of tobacco products",
            children: [
              {
                key: "16.0",
                label: "16.0 - Manufacture of tobacco products",
                children: [{ key: "16.00", label: "16.00 - Manufacture of tobacco products", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "DB",
        label: "DB - Manufacture of textiles and textile products",
        children: [
          {
            key: "17",
            label: "17 - Manufacture of textiles",
            children: [
              {
                key: "17.1",
                label: "17.1 - Preparation and spinning of textile fibres",
                children: [
                  { key: "17.11", label: "17.11 - Preparation and spinning of cotton-type fibres", children: [] },
                  { key: "17.12", label: "17.12 - Preparation and spinning of woollen-type fibres", children: [] },
                  { key: "17.13", label: "17.13 - Preparation and spinning of worsted-type fibres", children: [] },
                  { key: "17.14", label: "17.14 - Preparation and spinning of flax-type fibres", children: [] },
                  {
                    key: "17.15",
                    label:
                      "17.15 - Throwing and preparation of silk, including from noils, and throwing and texturing of synthetic or artificial filament yarns",
                    children: [],
                  },
                  { key: "17.16", label: "17.16 - Manufacture of sewing threads", children: [] },
                  { key: "17.17", label: "17.17 - Preparation and spinning of other textile fibres", children: [] },
                ],
              },
              {
                key: "17.2",
                label: "17.2 - Textile weaving",
                children: [
                  { key: "17.21", label: "17.21 - Cotton-type weaving", children: [] },
                  { key: "17.22", label: "17.22 - Woollen-type weaving", children: [] },
                  { key: "17.23", label: "17.23 - Worsted-type weaving", children: [] },
                  { key: "17.24", label: "17.24 - Silk-type weaving", children: [] },
                  { key: "17.25", label: "17.25 - Other textile weaving", children: [] },
                ],
              },
              {
                key: "17.3",
                label: "17.3 - Finishing of textiles",
                children: [{ key: "17.30", label: "17.30 - Finishing of textiles", children: [] }],
              },
              {
                key: "17.4",
                label: "17.4 - Manufacture of made-up textile articles, except apparel",
                children: [
                  {
                    key: "17.40",
                    label: "17.40 - Manufacture of made-up textile articles, except apparel",
                    children: [],
                  },
                ],
              },
              {
                key: "17.5",
                label: "17.5 - Manufacture of other textiles",
                children: [
                  { key: "17.51", label: "17.51 - Manufacture of carpets and rugs", children: [] },
                  { key: "17.52", label: "17.52 - Manufacture of cordage, rope, twine and netting", children: [] },
                  {
                    key: "17.53",
                    label: "17.53 - Manufacture of non-wovens and articles made from non-wovens, except apparel",
                    children: [],
                  },
                  { key: "17.54", label: "17.54 - Manufacture of other textiles n.e.c.", children: [] },
                ],
              },
              {
                key: "17.6",
                label: "17.6 - Manufacture of knitted and crocheted fabrics",
                children: [
                  { key: "17.60", label: "17.60 - Manufacture of knitted and crocheted fabrics", children: [] },
                ],
              },
              {
                key: "17.7",
                label: "17.7 - Manufacture of knitted and crocheted articles",
                children: [
                  { key: "17.71", label: "17.71 - Manufacture of knitted and crocheted hosiery", children: [] },
                  {
                    key: "17.72",
                    label: "17.72 - Manufacture of knitted and crocheted pullovers, cardigans and similar articles",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "18",
            label: "18 - Manufacture of wearing apparel; dressing and dyeing of fur",
            children: [
              {
                key: "18.1",
                label: "18.1 - Manufacture of leather clothes",
                children: [{ key: "18.10", label: "18.10 - Manufacture of leather clothes", children: [] }],
              },
              {
                key: "18.2",
                label: "18.2 - Manufacture of other wearing apparel and accessories",
                children: [
                  { key: "18.21", label: "18.21 - Manufacture of workwear", children: [] },
                  { key: "18.22", label: "18.22 - Manufacture of other outerwear", children: [] },
                  { key: "18.23", label: "18.23 - Manufacture of underwear", children: [] },
                  {
                    key: "18.24",
                    label: "18.24 - Manufacture of other wearing apparel and accessories n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "18.3",
                label: "18.3 - Dressing and dyeing of fur; manufacture of articles of fur",
                children: [
                  {
                    key: "18.30",
                    label: "18.30 - Dressing and dyeing of fur; manufacture of articles of fur",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DC",
        label: "DC - Manufacture of leather and leather products",
        children: [
          {
            key: "19",
            label:
              "19 - Tanning and dressing of leather; manufacture of luggage, handbags, saddlery, harness and footwear",
            children: [
              {
                key: "19.1",
                label: "19.1 - Tanning and dressing of leather",
                children: [{ key: "19.10", label: "19.10 - Tanning and dressing of leather", children: [] }],
              },
              {
                key: "19.2",
                label: "19.2 - Manufacture of luggage, handbags and the like, saddlery and harness",
                children: [
                  {
                    key: "19.20",
                    label: "19.20 - Manufacture of luggage, handbags and the like, saddlery and harness",
                    children: [],
                  },
                ],
              },
              {
                key: "19.3",
                label: "19.3 - Manufacture of footwear",
                children: [{ key: "19.30", label: "19.30 - Manufacture of footwear", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "DD",
        label: "DD - Manufacture of wood and wood products",
        children: [
          {
            key: "20",
            label:
              "20 - Manufacture of wood and of products of wood and cork, except furniture; manufacture of articles of straw and plaiting materials",
            children: [
              {
                key: "20.1",
                label: "20.1 - Sawmilling and planing of wood; impregnation of wood",
                children: [
                  { key: "20.10", label: "20.10 - Sawmilling and planing of wood; impregnation of wood", children: [] },
                ],
              },
              {
                key: "20.2",
                label:
                  "20.2 - Manufacture of veneer sheets; manufacture of plywood, laminboard, particle board, fibre board and other panels and boards",
                children: [
                  {
                    key: "20.20",
                    label:
                      "20.20 - Manufacture of veneer sheets; manufacture of plywood, laminboard, particle board, fibre board and other panels and boards",
                    children: [],
                  },
                ],
              },
              {
                key: "20.3",
                label: "20.3 - Manufacture of builders' carpentry and joinery",
                children: [
                  { key: "20.30", label: "20.30 - Manufacture of builders' carpentry and joinery", children: [] },
                ],
              },
              {
                key: "20.4",
                label: "20.4 - Manufacture of wooden containers",
                children: [{ key: "20.40", label: "20.40 - Manufacture of wooden containers", children: [] }],
              },
              {
                key: "20.5",
                label:
                  "20.5 - Manufacture of other products of wood; manufacture of articles of cork, straw and plaiting materials",
                children: [
                  { key: "20.51", label: "20.51 - Manufacture of other products of wood", children: [] },
                  {
                    key: "20.52",
                    label: "20.52 - Manufacture of articles of cork, straw and plaiting materials",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DE",
        label: "DE - Manufacture of pulp, paper and paper products; publishing and printing",
        children: [
          {
            key: "21",
            label: "21 - Manufacture of pulp, paper and paper products",
            children: [
              {
                key: "21.1",
                label: "21.1 - Manufacture of pulp, paper and paperboard",
                children: [
                  { key: "21.11", label: "21.11 - Manufacture of pulp", children: [] },
                  { key: "21.12", label: "21.12 - Manufacture of paper and paperboard", children: [] },
                ],
              },
              {
                key: "21.2",
                label: "21.2 - Manufacture of articles of paper and paperboard",
                children: [
                  {
                    key: "21.21",
                    label:
                      "21.21 - Manufacture of corrugated paper and paperboard and of containers of paper and paperboard",
                    children: [],
                  },
                  {
                    key: "21.22",
                    label: "21.22 - Manufacture of household and sanitary goods and of toilet requisites",
                    children: [],
                  },
                  { key: "21.23", label: "21.23 - Manufacture of paper stationery", children: [] },
                  { key: "21.24", label: "21.24 - Manufacture of wallpaper", children: [] },
                  {
                    key: "21.25",
                    label: "21.25 - Manufacture of other articles of paper and paperboard n.e.c.",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "22",
            label: "22 - Publishing, printing and reproduction of recorded media",
            children: [
              {
                key: "22.1",
                label: "22.1 - Publishing",
                children: [
                  { key: "22.11", label: "22.11 - Publishing of books", children: [] },
                  { key: "22.12", label: "22.12 - Publishing of newspapers", children: [] },
                  { key: "22.13", label: "22.13 - Publishing of journals and periodicals", children: [] },
                  { key: "22.14", label: "22.14 - Publishing of sound recordings", children: [] },
                  { key: "22.15", label: "22.15 - Other publishing", children: [] },
                ],
              },
              {
                key: "22.2",
                label: "22.2 - Printing and service activities related to printing",
                children: [
                  { key: "22.21", label: "22.21 - Printing of newspapers", children: [] },
                  { key: "22.22", label: "22.22 - Printing n.e.c.", children: [] },
                  { key: "22.23", label: "22.23 - Bookbinding ", children: [] },
                  { key: "22.24", label: "22.24 - Pre-press activities", children: [] },
                  { key: "22.25", label: "22.25 - Ancillary activities related to printing", children: [] },
                ],
              },
              {
                key: "22.3",
                label: "22.3 - Reproduction of recorded media",
                children: [
                  { key: "22.31", label: "22.31 - Reproduction of sound recording", children: [] },
                  { key: "22.32", label: "22.32 - Reproduction of video recording", children: [] },
                  { key: "22.33", label: "22.33 - Reproduction of computer media", children: [] },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DF",
        label: "DF - Manufacture of coke, refined petroleum products and nuclear fuel",
        children: [
          {
            key: "23",
            label: "23 - Manufacture of coke, refined petroleum products and nuclear fuel",
            children: [
              {
                key: "23.1",
                label: "23.1 - Manufacture of coke oven products",
                children: [{ key: "23.10", label: "23.10 - Manufacture of coke oven products", children: [] }],
              },
              {
                key: "23.2",
                label: "23.2 - Manufacture of refined petroleum products",
                children: [{ key: "23.20", label: "23.20 - Manufacture of refined petroleum products", children: [] }],
              },
              {
                key: "23.3",
                label: "23.3 - Processing of nuclear fuel",
                children: [{ key: "23.30", label: "23.30 - Processing of nuclear fuel", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "DG",
        label: "DG - Manufacture of chemicals, chemical products and man-made fibres",
        children: [
          {
            key: "24",
            label: "24 - Manufacture of chemicals and chemical products",
            children: [
              {
                key: "24.1",
                label: "24.1 - Manufacture of basic chemicals",
                children: [
                  { key: "24.11", label: "24.11 - Manufacture of industrial gases", children: [] },
                  { key: "24.12", label: "24.12 - Manufacture of dyes and pigments", children: [] },
                  { key: "24.13", label: "24.13 - Manufacture of other inorganic basic chemicals", children: [] },
                  { key: "24.14", label: "24.14 - Manufacture of other organic basic chemicals", children: [] },
                  { key: "24.15", label: "24.15 - Manufacture of fertilizers and nitrogen compounds", children: [] },
                  { key: "24.16", label: "24.16 - Manufacture of plastics in primary forms", children: [] },
                  { key: "24.17", label: "24.17 - Manufacture of synthetic rubber in primary forms", children: [] },
                ],
              },
              {
                key: "24.2",
                label: "24.2 - Manufacture of pesticides and other agro-chemical products",
                children: [
                  {
                    key: "24.20",
                    label: "24.20 - Manufacture of pesticides and other agro-chemical products",
                    children: [],
                  },
                ],
              },
              {
                key: "24.3",
                label: "24.3 - Manufacture of paints, varnishes and similar coatings, printing ink and mastics",
                children: [
                  {
                    key: "24.30",
                    label: "24.30 - Manufacture of paints, varnishes and similar coatings, printing ink and mastics",
                    children: [],
                  },
                ],
              },
              {
                key: "24.4",
                label: "24.4 - Manufacture of pharmaceuticals, medicinal chemicals and botanical products",
                children: [
                  { key: "24.41", label: "24.41 - Manufacture of basic pharmaceutical products", children: [] },
                  { key: "24.42", label: "24.42 - Manufacture of pharmaceutical preparations", children: [] },
                ],
              },
              {
                key: "24.5",
                label:
                  "24.5 - Manufacture of soap and detergents, cleaning and polishing preparations, perfumes and toilet preparations",
                children: [
                  {
                    key: "24.51",
                    label: "24.51 - Manufacture of soap and detergents, cleaning and polishing preparations",
                    children: [],
                  },
                  { key: "24.52", label: "24.52 - Manufacture of perfumes and toilet preparations", children: [] },
                ],
              },
              {
                key: "24.6",
                label: "24.6 - Manufacture of other chemical products",
                children: [
                  { key: "24.61", label: "24.61 - Manufacture of explosives", children: [] },
                  { key: "24.62", label: "24.62 - Manufacture of glues and gelatines", children: [] },
                  { key: "24.63", label: "24.63 - Manufacture of essential oils", children: [] },
                  { key: "24.64", label: "24.64 - Manufacture of photographic chemical material", children: [] },
                  { key: "24.65", label: "24.65 - Manufacture of prepared unrecorded media", children: [] },
                  { key: "24.66", label: "24.66 - Manufacture of other chemical products n.e.c.", children: [] },
                ],
              },
              {
                key: "24.7",
                label: "24.7 - Manufacture of man-made fibres",
                children: [{ key: "24.70", label: "24.70 - Manufacture of man-made fibres", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "DH",
        label: "DH - Manufacture of rubber and plastic products",
        children: [
          {
            key: "25",
            label: "25 - Manufacture of rubber and plastic products",
            children: [
              {
                key: "25.1",
                label: "25.1 - Manufacture of rubber products",
                children: [
                  { key: "25.11", label: "25.11 - Manufacture of rubber tyres and tubes", children: [] },
                  { key: "25.12", label: "25.12 - Retreading and rebuilding of rubber tyres", children: [] },
                  { key: "25.13", label: "25.13 - Manufacture of other rubber products", children: [] },
                ],
              },
              {
                key: "25.2",
                label: "25.2 - Manufacture of plastic products",
                children: [
                  {
                    key: "25.21",
                    label: "25.21 - Manufacture of plastic plates, sheets, tubes and profiles",
                    children: [],
                  },
                  { key: "25.22", label: "25.22 - Manufacture of plastic packing goods", children: [] },
                  { key: "25.23", label: "25.23 - Manufacture of builders' ware of plastic", children: [] },
                  { key: "25.24", label: "25.24 - Manufacture of other plastic products", children: [] },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DI",
        label: "DI - Manufacture of other non-metallic mineral products",
        children: [
          {
            key: "26",
            label: "26 - Manufacture of other non-metallic mineral products",
            children: [
              {
                key: "26.1",
                label: "26.1 - Manufacture of glass and glass products",
                children: [
                  { key: "26.11", label: "26.11 - Manufacture of flat glass", children: [] },
                  { key: "26.12", label: "26.12 - Shaping and processing of flat glass", children: [] },
                  { key: "26.13", label: "26.13 - Manufacture of hollow glass", children: [] },
                  { key: "26.14", label: "26.14 - Manufacture of glass fibres", children: [] },
                  {
                    key: "26.15",
                    label: "26.15 - Manufacture and processing of other glass, including technical glassware",
                    children: [],
                  },
                ],
              },
              {
                key: "26.2",
                label:
                  "26.2 - Manufacture of non-refractory ceramic goods other than for construction purposes; manufacture of refractory ceramic products",
                children: [
                  {
                    key: "26.21",
                    label: "26.21 - Manufacture of ceramic household and ornamental articles",
                    children: [],
                  },
                  { key: "26.22", label: "26.22 - Manufacture of ceramic sanitary fixtures", children: [] },
                  {
                    key: "26.23",
                    label: "26.23 - Manufacture of ceramic insulators and insulating fittings",
                    children: [],
                  },
                  { key: "26.24", label: "26.24 - Manufacture of other technical ceramic products", children: [] },
                  { key: "26.25", label: "26.25 - Manufacture of other ceramic products", children: [] },
                  { key: "26.26", label: "26.26 - Manufacture of refractory ceramic products", children: [] },
                ],
              },
              {
                key: "26.3",
                label: "26.3 - Manufacture of ceramic tiles and flags",
                children: [{ key: "26.30", label: "26.30 - Manufacture of ceramic tiles and flags", children: [] }],
              },
              {
                key: "26.4",
                label: "26.4 - Manufacture of bricks, tiles and construction products, in baked clay",
                children: [
                  {
                    key: "26.40",
                    label: "26.40 - Manufacture of bricks, tiles and construction products, in baked clay",
                    children: [],
                  },
                ],
              },
              {
                key: "26.5",
                label: "26.5 - Manufacture of cement, lime and plaster",
                children: [
                  { key: "26.51", label: "26.51 - Manufacture of cement", children: [] },
                  { key: "26.52", label: "26.52 - Manufacture of lime", children: [] },
                  { key: "26.53", label: "26.53 - Manufacture of plaster", children: [] },
                ],
              },
              {
                key: "26.6",
                label: "26.6 - Manufacture of articles of concrete, plaster and cement",
                children: [
                  {
                    key: "26.61",
                    label: "26.61 - Manufacture of concrete products for construction purposes",
                    children: [],
                  },
                  {
                    key: "26.62",
                    label: "26.62 - Manufacture of plaster products for construction purposes",
                    children: [],
                  },
                  { key: "26.63", label: "26.63 - Manufacture of ready-mixed concrete", children: [] },
                  { key: "26.64", label: "26.64 - Manufacture of mortars", children: [] },
                  { key: "26.65", label: "26.65 - Manufacture of fibre cement", children: [] },
                  {
                    key: "26.66",
                    label: "26.66 - Manufacture of other articles of concrete, plaster and cement",
                    children: [],
                  },
                ],
              },
              {
                key: "26.7",
                label: "26.7 - Cutting, shaping and finishing of ornamental and building stone",
                children: [
                  {
                    key: "26.70",
                    label: "26.70 - Cutting, shaping and finishing of ornamental and building stone",
                    children: [],
                  },
                ],
              },
              {
                key: "26.8",
                label: "26.8 - Manufacture of other non-metallic mineral products",
                children: [
                  { key: "26.81", label: "26.81 - Production of abrasive products", children: [] },
                  {
                    key: "26.82",
                    label: "26.82 - Manufacture of other non-metallic mineral products n.e.c.",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DJ",
        label: "DJ - Manufacture of basic metals and fabricated metal products",
        children: [
          {
            key: "27",
            label: "27 - Manufacture of basic metals",
            children: [
              {
                key: "27.1",
                label: "27.1 - Manufacture of basic iron and steel and of ferro-alloys ",
                children: [
                  {
                    key: "27.10",
                    label: "27.10 - Manufacture of basic iron and steel and of ferro-alloys ",
                    children: [],
                  },
                ],
              },
              {
                key: "27.2",
                label: "27.2 - Manufacture of tubes",
                children: [
                  { key: "27.21", label: "27.21 - Manufacture of cast iron tubes", children: [] },
                  { key: "27.22", label: "27.22 - Manufacture of steel tubes", children: [] },
                ],
              },
              {
                key: "27.3",
                label: "27.3 - Other first processing of iron and steel ",
                children: [
                  { key: "27.31", label: "27.31 - Cold drawing", children: [] },
                  { key: "27.32", label: "27.32 - Cold rolling of narrow strip", children: [] },
                  { key: "27.33", label: "27.33 - Cold forming or folding", children: [] },
                  { key: "27.34", label: "27.34 - Wire drawing", children: [] },
                ],
              },
              {
                key: "27.4",
                label: "27.4 - Manufacture of basic precious and non-ferrous metals",
                children: [
                  { key: "27.41", label: "27.41 - Precious metals production", children: [] },
                  { key: "27.42", label: "27.42 - Aluminium production", children: [] },
                  { key: "27.43", label: "27.43 - Lead, zinc and tin production", children: [] },
                  { key: "27.44", label: "27.44 - Copper production", children: [] },
                  { key: "27.45", label: "27.45 - Other non-ferrous metal production", children: [] },
                ],
              },
              {
                key: "27.5",
                label: "27.5 - Casting of metals",
                children: [
                  { key: "27.51", label: "27.51 - Casting of iron", children: [] },
                  { key: "27.52", label: "27.52 - Casting of steel", children: [] },
                  { key: "27.53", label: "27.53 - Casting of light metals", children: [] },
                  { key: "27.54", label: "27.54 - Casting of other non-ferrous metals", children: [] },
                ],
              },
            ],
          },
          {
            key: "28",
            label: "28 - Manufacture of fabricated metal products, except machinery and equipment",
            children: [
              {
                key: "28.1",
                label: "28.1 - Manufacture of structural metal products",
                children: [
                  {
                    key: "28.11",
                    label: "28.11 - Manufacture of metal structures and parts of structures",
                    children: [],
                  },
                  {
                    key: "28.12",
                    label: "28.12 - Manufacture of builders' carpentry and joinery of metal",
                    children: [],
                  },
                ],
              },
              {
                key: "28.2",
                label:
                  "28.2 - Manufacture of tanks, reservoirs and containers of metal; manufacture of central heating radiators and boilers",
                children: [
                  {
                    key: "28.21",
                    label: "28.21 - Manufacture of tanks, reservoirs and containers of metal",
                    children: [],
                  },
                  { key: "28.22", label: "28.22 - Manufacture of central heating radiators and boilers", children: [] },
                ],
              },
              {
                key: "28.3",
                label: "28.3 - Manufacture of steam generators, except central heating hot water boilers",
                children: [
                  {
                    key: "28.30",
                    label: "28.30 - Manufacture of steam generators, except central heating hot water boilers",
                    children: [],
                  },
                ],
              },
              {
                key: "28.4",
                label: "28.4 - Forging, pressing, stamping and roll forming of metal; powder metallurgy",
                children: [
                  {
                    key: "28.40",
                    label: "28.40 - Forging, pressing, stamping and roll forming of metal; powder metallurgy",
                    children: [],
                  },
                ],
              },
              {
                key: "28.5",
                label: "28.5 - Treatment and coating of metals; general mechanical engineering",
                children: [
                  { key: "28.51", label: "28.51 - Treatment and coating of metals", children: [] },
                  { key: "28.52", label: "28.52 - General mechanical engineering", children: [] },
                ],
              },
              {
                key: "28.6",
                label: "28.6 - Manufacture of cutlery, tools and general hardware",
                children: [
                  { key: "28.61", label: "28.61 - Manufacture of cutlery", children: [] },
                  { key: "28.62", label: "28.62 - Manufacture of tools", children: [] },
                  { key: "28.63", label: "28.63 - Manufacture of locks and hinges", children: [] },
                ],
              },
              {
                key: "28.7",
                label: "28.7 - Manufacture of other fabricated metal products",
                children: [
                  { key: "28.71", label: "28.71 - Manufacture of steel drums and similar containers", children: [] },
                  { key: "28.72", label: "28.72 - Manufacture of light metal packaging", children: [] },
                  { key: "28.73", label: "28.73 - Manufacture of wire products", children: [] },
                  {
                    key: "28.74",
                    label: "28.74 - Manufacture of fasteners, screw machine products, chain and springs",
                    children: [],
                  },
                  {
                    key: "28.75",
                    label: "28.75 - Manufacture of other fabricated metal products n.e.c.",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DK",
        label: "DK - Manufacture of machinery and equipment n.e.c.",
        children: [
          {
            key: "29",
            label: "29 - Manufacture of machinery and equipment n.e.c.",
            children: [
              {
                key: "29.1",
                label:
                  "29.1 - Manufacture of machinery for the production and use of mechanical power, except aircraft, vehicle and cycle engines",
                children: [
                  {
                    key: "29.11",
                    label: "29.11 - Manufacture of engines and turbines, except aircraft, vehicle and cycle engines",
                    children: [],
                  },
                  { key: "29.12", label: "29.12 - Manufacture of pumps and compressors", children: [] },
                  { key: "29.13", label: "29.13 - Manufacture of taps and valves", children: [] },
                  {
                    key: "29.14",
                    label: "29.14 - Manufacture of bearings, gears, gearing and driving elements",
                    children: [],
                  },
                ],
              },
              {
                key: "29.2",
                label: "29.2 - Manufacture of other general purpose machinery",
                children: [
                  { key: "29.21", label: "29.21 - Manufacture of furnaces and furnace burners", children: [] },
                  { key: "29.22", label: "29.22 - Manufacture of lifting and handling equipment", children: [] },
                  {
                    key: "29.23",
                    label: "29.23 - Manufacture of non-domestic cooling and ventilation equipment",
                    children: [],
                  },
                  {
                    key: "29.24",
                    label: "29.24 - Manufacture of other general purpose machinery n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "29.3",
                label: "29.3 - Manufacture of agricultural and forestry machinery",
                children: [
                  { key: "29.31", label: "29.31 - Manufacture of agricultural tractors", children: [] },
                  {
                    key: "29.32",
                    label: "29.32 - Manufacture of other agricultural and forestry machinery",
                    children: [],
                  },
                ],
              },
              {
                key: "29.4",
                label: "29.4 - Manufacture of machine tools",
                children: [
                  { key: "29.41", label: "29.41 - Manufacture of portable hand held power tools", children: [] },
                  { key: "29.42", label: "29.42 - Manufacture of other metalworking machine tools", children: [] },
                  { key: "29.43", label: "29.43 - Manufacture of other machine tools n.e.c.", children: [] },
                ],
              },
              {
                key: "29.5",
                label: "29.5 - Manufacture of other special purpose machinery",
                children: [
                  { key: "29.51", label: "29.51 - Manufacture of machinery for metallurgy", children: [] },
                  {
                    key: "29.52",
                    label: "29.52 - Manufacture of machinery for mining, quarrying and construction",
                    children: [],
                  },
                  {
                    key: "29.53",
                    label: "29.53 - Manufacture of machinery for food, beverage and tobacco processing",
                    children: [],
                  },
                  {
                    key: "29.54",
                    label: "29.54 - Manufacture of machinery for textile, apparel and leather production",
                    children: [],
                  },
                  {
                    key: "29.55",
                    label: "29.55 - Manufacture of machinery for paper and paperboard production",
                    children: [],
                  },
                  {
                    key: "29.56",
                    label: "29.56 - Manufacture of other special purpose machinery n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "29.6",
                label: "29.6 - Manufacture of weapons and ammunition",
                children: [{ key: "29.60", label: "29.60 - Manufacture of weapons and ammunition", children: [] }],
              },
              {
                key: "29.7",
                label: "29.7 - Manufacture of domestic appliances n.e.c.",
                children: [
                  { key: "29.71", label: "29.71 - Manufacture of electric domestic appliances", children: [] },
                  { key: "29.72", label: "29.72 - Manufacture of non-electric domestic appliances", children: [] },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DL",
        label: "DL - Manufacture of electrical and optical equipment",
        children: [
          {
            key: "30",
            label: "30 - Manufacture of office machinery and computers",
            children: [
              {
                key: "30.0",
                label: "30.0 - Manufacture of office machinery and computers",
                children: [
                  { key: "30.01", label: "30.01 - Manufacture of office machinery", children: [] },
                  {
                    key: "30.02",
                    label: "30.02 - Manufacture of computers and other information processing equipment",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "31",
            label: "31 - Manufacture of electrical machinery and apparatus n.e.c.",
            children: [
              {
                key: "31.1",
                label: "31.1 - Manufacture of electric motors, generators and transformers",
                children: [
                  {
                    key: "31.10",
                    label: "31.10 - Manufacture of electric motors, generators and transformers",
                    children: [],
                  },
                ],
              },
              {
                key: "31.2",
                label: "31.2 - Manufacture of electricity distribution and control apparatus",
                children: [
                  {
                    key: "31.20",
                    label: "31.20 - Manufacture of electricity distribution and control apparatus",
                    children: [],
                  },
                ],
              },
              {
                key: "31.3",
                label: "31.3 - Manufacture of insulated wire and cable",
                children: [{ key: "31.30", label: "31.30 - Manufacture of insulated wire and cable", children: [] }],
              },
              {
                key: "31.4",
                label: "31.4 - Manufacture of accumulators, primary cells and primary batteries",
                children: [
                  {
                    key: "31.40",
                    label: "31.40 - Manufacture of accumulators, primary cells and primary batteries",
                    children: [],
                  },
                ],
              },
              {
                key: "31.5",
                label: "31.5 - Manufacture of lighting equipment and electric lamps",
                children: [
                  { key: "31.50", label: "31.50 - Manufacture of lighting equipment and electric lamps", children: [] },
                ],
              },
              {
                key: "31.6",
                label: "31.6 - Manufacture of electrical equipment n.e.c.",
                children: [
                  {
                    key: "31.61",
                    label: "31.61 - Manufacture of electrical equipment for engines and vehicles n.e.c.",
                    children: [],
                  },
                  { key: "31.62", label: "31.62 - Manufacture of other electrical equipment n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "32",
            label: "32 - Manufacture of radio, television and communication equipment and apparatus",
            children: [
              {
                key: "32.1",
                label: "32.1 - Manufacture of electronic valves and tubes and other electronic components",
                children: [
                  {
                    key: "32.10",
                    label: "32.10 - Manufacture of electronic valves and tubes and other electronic components",
                    children: [],
                  },
                ],
              },
              {
                key: "32.2",
                label:
                  "32.2 - Manufacture of television and radio transmitters and apparatus for line telephony and line telegraphy",
                children: [
                  {
                    key: "32.20",
                    label:
                      "32.20 - Manufacture of television and radio transmitters and apparatus for line telephony and line telegraphy",
                    children: [],
                  },
                ],
              },
              {
                key: "32.3",
                label:
                  "32.3 - Manufacture of television and radio receivers, sound or video recording or reproducing apparatus and associated goods",
                children: [
                  {
                    key: "32.30",
                    label:
                      "32.30 - Manufacture of television and radio receivers, sound or video recording or reproducing apparatus and associated goods",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "33",
            label: "33 - Manufacture of medical, precision and optical instruments, watches and clocks",
            children: [
              {
                key: "33.1",
                label: "33.1 - Manufacture of medical and surgical equipment and orthopaedic appliances",
                children: [
                  {
                    key: "33.10",
                    label: "33.10 - Manufacture of medical and surgical equipment and orthopaedic appliances",
                    children: [],
                  },
                ],
              },
              {
                key: "33.2",
                label:
                  "33.2 - Manufacture of instruments and appliances for measuring, checking, testing, navigating and other purposes, except industrial process control equipment",
                children: [
                  {
                    key: "33.20",
                    label:
                      "33.20 - Manufacture of instruments and appliances for measuring, checking, testing, navigating and other purposes, except industrial process control equipment",
                    children: [],
                  },
                ],
              },
              {
                key: "33.3",
                label: "33.3 - Manufacture of industrial process control equipment",
                children: [
                  { key: "33.30", label: "33.30 - Manufacture of industrial process control equipment", children: [] },
                ],
              },
              {
                key: "33.4",
                label: "33.4 - Manufacture of optical instruments and photographic equipment",
                children: [
                  {
                    key: "33.40",
                    label: "33.40 - Manufacture of optical instruments and photographic equipment",
                    children: [],
                  },
                ],
              },
              {
                key: "33.5",
                label: "33.5 - Manufacture of watches and clocks",
                children: [{ key: "33.50", label: "33.50 - Manufacture of watches and clocks", children: [] }],
              },
            ],
          },
        ],
      },
      {
        key: "DM",
        label: "DM - Manufacture of transport equipment",
        children: [
          {
            key: "34",
            label: "34 - Manufacture of motor vehicles, trailers and semi-trailers",
            children: [
              {
                key: "34.1",
                label: "34.1 - Manufacture of motor vehicles",
                children: [{ key: "34.10", label: "34.10 - Manufacture of motor vehicles", children: [] }],
              },
              {
                key: "34.2",
                label:
                  "34.2 - Manufacture of bodies (coachwork) for motor vehicles; manufacture of trailers and semi-trailers",
                children: [
                  {
                    key: "34.20",
                    label:
                      "34.20 - Manufacture of bodies (coachwork) for motor vehicles; manufacture of trailers and semi-trailers",
                    children: [],
                  },
                ],
              },
              {
                key: "34.3",
                label: "34.3 - Manufacture of parts and accessories for motor vehicles and their engines",
                children: [
                  {
                    key: "34.30",
                    label: "34.30 - Manufacture of parts and accessories for motor vehicles and their engines",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "35",
            label: "35 - Manufacture of other transport equipment",
            children: [
              {
                key: "35.1",
                label: "35.1 - Building and repairing of ships and boats",
                children: [
                  { key: "35.11", label: "35.11 - Building and repairing of ships", children: [] },
                  {
                    key: "35.12",
                    label: "35.12 - Building and repairing of pleasure and sporting boats",
                    children: [],
                  },
                ],
              },
              {
                key: "35.2",
                label: "35.2 - Manufacture of railway and tramway locomotives and rolling stock",
                children: [
                  {
                    key: "35.20",
                    label: "35.20 - Manufacture of railway and tramway locomotives and rolling stock",
                    children: [],
                  },
                ],
              },
              {
                key: "35.3",
                label: "35.3 - Manufacture of aircraft and spacecraft",
                children: [{ key: "35.30", label: "35.30 - Manufacture of aircraft and spacecraft", children: [] }],
              },
              {
                key: "35.4",
                label: "35.4 - Manufacture of motorcycles and bicycles",
                children: [
                  { key: "35.41", label: "35.41 - Manufacture of motorcycles", children: [] },
                  { key: "35.42", label: "35.42 - Manufacture of bicycles", children: [] },
                  { key: "35.43", label: "35.43 - Manufacture of invalid carriages", children: [] },
                ],
              },
              {
                key: "35.5",
                label: "35.5 - Manufacture of other transport equipment n.e.c.",
                children: [
                  { key: "35.50", label: "35.50 - Manufacture of other transport equipment n.e.c.", children: [] },
                ],
              },
            ],
          },
        ],
      },
      {
        key: "DN",
        label: "DN - Manufacturing n.e.c.",
        children: [
          {
            key: "36",
            label: "36 - Manufacture of furniture; manufacturing n.e.c.",
            children: [
              {
                key: "36.1",
                label: "36.1 - Manufacture of furniture",
                children: [
                  { key: "36.11", label: "36.11 - Manufacture of chairs and seats", children: [] },
                  { key: "36.12", label: "36.12 - Manufacture of other office and shop furniture", children: [] },
                  { key: "36.13", label: "36.13 - Manufacture of other kitchen furniture", children: [] },
                  { key: "36.14", label: "36.14 - Manufacture of other furniture", children: [] },
                  { key: "36.15", label: "36.15 - Manufacture of mattresses", children: [] },
                ],
              },
              {
                key: "36.2",
                label: "36.2 - Manufacture of jewellery and related articles",
                children: [
                  { key: "36.21", label: "36.21 - Striking of coins ", children: [] },
                  { key: "36.22", label: "36.22 - Manufacture of jewellery and related articles n.e.c.", children: [] },
                ],
              },
              {
                key: "36.3",
                label: "36.3 - Manufacture of musical instruments",
                children: [{ key: "36.30", label: "36.30 - Manufacture of musical instruments", children: [] }],
              },
              {
                key: "36.4",
                label: "36.4 - Manufacture of sports goods",
                children: [{ key: "36.40", label: "36.40 - Manufacture of sports goods", children: [] }],
              },
              {
                key: "36.5",
                label: "36.5 - Manufacture of games and toys",
                children: [{ key: "36.50", label: "36.50 - Manufacture of games and toys", children: [] }],
              },
              {
                key: "36.6",
                label: "36.6 - Miscellaneous manufacturing n.e.c.",
                children: [
                  { key: "36.61", label: "36.61 - Manufacture of imitation jewellery", children: [] },
                  { key: "36.62", label: "36.62 - Manufacture of brooms and brushes", children: [] },
                  { key: "36.63", label: "36.63 - Other manufacturing n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "37",
            label: "37 - Recycling",
            children: [
              {
                key: "37.1",
                label: "37.1 - Recycling of metal waste and scrap",
                children: [{ key: "37.10", label: "37.10 - Recycling of metal waste and scrap", children: [] }],
              },
              {
                key: "37.2",
                label: "37.2 - Recycling of non-metal waste and scrap",
                children: [{ key: "37.20", label: "37.20 - Recycling of non-metal waste and scrap", children: [] }],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "E",
    label: "E - Electricity, gas and water supply",
    children: [
      {
        key: "EA",
        label: "EA - Electricity, gas and water supply",
        children: [
          {
            key: "40",
            label: "40 - Electricity, gas, steam and hot water supply",
            children: [
              {
                key: "40.1",
                label: "40.1 - Production and distribution of electricity",
                children: [
                  { key: "40.11", label: "40.11 - Production of electricity", children: [] },
                  { key: "40.12", label: "40.12 - Transmission of electricity", children: [] },
                  { key: "40.13", label: "40.13 - Distribution and trade of electricity", children: [] },
                ],
              },
              {
                key: "40.2",
                label: "40.2 - Manufacture of gas; distribution of gaseous fuels through mains",
                children: [
                  { key: "40.21", label: "40.21 - Manufacture of gas", children: [] },
                  {
                    key: "40.22",
                    label: "40.22 - Distribution and trade of gaseous fuels through mains",
                    children: [],
                  },
                ],
              },
              {
                key: "40.3",
                label: "40.3 - Steam and hot water supply",
                children: [{ key: "40.30", label: "40.30 - Steam and hot water supply", children: [] }],
              },
            ],
          },
          {
            key: "41",
            label: "41 - Collection, purification and distribution of water",
            children: [
              {
                key: "41.0",
                label: "41.0 - Collection, purification and distribution of water",
                children: [
                  { key: "41.00", label: "41.00 - Collection, purification and distribution of water", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "F",
    label: "F - Construction",
    children: [
      {
        key: "FA",
        label: "FA - Construction",
        children: [
          {
            key: "45",
            label: "45 - Construction",
            children: [
              {
                key: "45.1",
                label: "45.1 - Site preparation",
                children: [
                  { key: "45.11", label: "45.11 - Demolition and wrecking of buildings; earth moving", children: [] },
                  { key: "45.12", label: "45.12 - Test drilling and boring", children: [] },
                ],
              },
              {
                key: "45.2",
                label: "45.2 - Building of complete constructions or parts thereof; civil engineering",
                children: [
                  {
                    key: "45.21",
                    label: "45.21 - General construction of buildings and civil engineering works",
                    children: [],
                  },
                  { key: "45.22", label: "45.22 - Erection of roof covering and frames", children: [] },
                  {
                    key: "45.23",
                    label: "45.23 - Construction of motorways, roads, airfields and sport facilities",
                    children: [],
                  },
                  { key: "45.24", label: "45.24 - Construction of water projects", children: [] },
                  { key: "45.25", label: "45.25 - Other construction work involving special trades", children: [] },
                ],
              },
              {
                key: "45.3",
                label: "45.3 - Building installation",
                children: [
                  { key: "45.31", label: "45.31 - Installation of electrical wiring and fittings", children: [] },
                  { key: "45.32", label: "45.32 - Insulation work activities", children: [] },
                  { key: "45.33", label: "45.33 - Plumbing", children: [] },
                  { key: "45.34", label: "45.34 - Other building installation", children: [] },
                ],
              },
              {
                key: "45.4",
                label: "45.4 - Building completion",
                children: [
                  { key: "45.41", label: "45.41 - Plastering", children: [] },
                  { key: "45.42", label: "45.42 - Joinery installation", children: [] },
                  { key: "45.43", label: "45.43 - Floor and wall covering", children: [] },
                  { key: "45.44", label: "45.44 - Painting and glazing", children: [] },
                  { key: "45.45", label: "45.45 - Other building completion", children: [] },
                ],
              },
              {
                key: "45.5",
                label: "45.5 - Renting of construction or demolition equipment with operator",
                children: [
                  {
                    key: "45.50",
                    label: "45.50 - Renting of construction or demolition equipment with operator",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "G",
    label: "G - Wholesale and retail trade; repair of motor vehicles, motorcycles and personal and household goods",
    children: [
      {
        key: "GA",
        label:
          "GA - Wholesale and retail trade; repair of motor vehicles, motorcycles and personal and household goods",
        children: [
          {
            key: "50",
            label:
              "50 - Sale, maintenance and repair of motor vehicles and motorcycles; retail sale of automotive fuel",
            children: [
              {
                key: "50.1",
                label: "50.1 - Sale of motor vehicles",
                children: [{ key: "50.10", label: "50.10 - Sale of motor vehicles", children: [] }],
              },
              {
                key: "50.2",
                label: "50.2 - Maintenance and repair of motor vehicles",
                children: [{ key: "50.20", label: "50.20 - Maintenance and repair of motor vehicles", children: [] }],
              },
              {
                key: "50.3",
                label: "50.3 - Sale of motor vehicle parts and accessories",
                children: [
                  { key: "50.30", label: "50.30 - Sale of motor vehicle parts and accessories", children: [] },
                ],
              },
              {
                key: "50.4",
                label: "50.4 - Sale, maintenance and repair of motorcycles and related parts and accessories",
                children: [
                  {
                    key: "50.40",
                    label: "50.40 - Sale, maintenance and repair of motorcycles and related parts and accessories",
                    children: [],
                  },
                ],
              },
              {
                key: "50.5",
                label: "50.5 - Retail sale of automotive fuel",
                children: [{ key: "50.50", label: "50.50 - Retail sale of automotive fuel", children: [] }],
              },
            ],
          },
          {
            key: "51",
            label: "51 - Wholesale trade and commission trade, except of motor vehicles and motorcycles",
            children: [
              {
                key: "51.1",
                label: "51.1 - Wholesale on a fee or contract basis",
                children: [
                  {
                    key: "51.11",
                    label:
                      "51.11 - Agents involved in the sale of agricultural raw materials, live animals, textile raw materials and semi-finished goods",
                    children: [],
                  },
                  {
                    key: "51.12",
                    label: "51.12 - Agents involved in the sale of fuels, ores, metals and industrial chemicals",
                    children: [],
                  },
                  {
                    key: "51.13",
                    label: "51.13 - Agents involved in the sale of timber and building materials",
                    children: [],
                  },
                  {
                    key: "51.14",
                    label: "51.14 - Agents involved in the sale of machinery, industrial equipment, ships and aircraft",
                    children: [],
                  },
                  {
                    key: "51.15",
                    label:
                      "51.15 - Agents involved in the sale of furniture, household goods, hardware and ironmongery",
                    children: [],
                  },
                  {
                    key: "51.16",
                    label: "51.16 - Agents involved in the sale of textiles, clothing, footwear and leather goods",
                    children: [],
                  },
                  {
                    key: "51.17",
                    label: "51.17 - Agents involved in the sale of food, beverages and tobacco",
                    children: [],
                  },
                  {
                    key: "51.18",
                    label:
                      "51.18 - Agents specializing in the sale of particular products or ranges of products n.e.c.",
                    children: [],
                  },
                  { key: "51.19", label: "51.19 - Agents involved in the sale of a variety of goods", children: [] },
                ],
              },
              {
                key: "51.2",
                label: "51.2 - Wholesale of agricultural raw materials and live animals",
                children: [
                  { key: "51.21", label: "51.21 - Wholesale of grain, seeds and animal feeds", children: [] },
                  { key: "51.22", label: "51.22 - Wholesale of flowers and plants", children: [] },
                  { key: "51.23", label: "51.23 - Wholesale of live animals", children: [] },
                  { key: "51.24", label: "51.24 - Wholesale of hides, skins and leather", children: [] },
                  { key: "51.25", label: "51.25 - Wholesale of unmanufactured tobacco", children: [] },
                ],
              },
              {
                key: "51.3",
                label: "51.3 - Wholesale of food, beverages and tobacco",
                children: [
                  { key: "51.31", label: "51.31 - Wholesale of fruit and vegetables", children: [] },
                  { key: "51.32", label: "51.32 - Wholesale of meat and meat products", children: [] },
                  {
                    key: "51.33",
                    label: "51.33 - Wholesale of dairy produce, eggs and edible oils and fats",
                    children: [],
                  },
                  { key: "51.34", label: "51.34 - Wholesale of alcoholic and other beverages", children: [] },
                  { key: "51.35", label: "51.35 - Wholesale of tobacco products", children: [] },
                  {
                    key: "51.36",
                    label: "51.36 - Wholesale of sugar and chocolate and sugar confectionery",
                    children: [],
                  },
                  { key: "51.37", label: "51.37 - Wholesale of coffee, tea, cocoa and spices", children: [] },
                  {
                    key: "51.38",
                    label: "51.38 - Wholesale of other food, including fish, crustaceans and molluscs",
                    children: [],
                  },
                  {
                    key: "51.39",
                    label: "51.39 - Non-specialized wholesale of food, beverages and tobacco",
                    children: [],
                  },
                ],
              },
              {
                key: "51.4",
                label: "51.4 - Wholesale of household goods",
                children: [
                  { key: "51.41", label: "51.41 - Wholesale of textiles", children: [] },
                  { key: "51.42", label: "51.42 - Wholesale of clothing and footwear", children: [] },
                  {
                    key: "51.43",
                    label: "51.43 - Wholesale of electrical household appliances and radio and television goods",
                    children: [],
                  },
                  {
                    key: "51.44",
                    label: "51.44 - Wholesale of china and glassware, wallpaper and cleaning materials",
                    children: [],
                  },
                  { key: "51.45", label: "51.45 - Wholesale of perfume and cosmetics", children: [] },
                  { key: "51.46", label: "51.46 - Wholesale of pharmaceutical goods", children: [] },
                  { key: "51.47", label: "51.47 - Wholesale of other household goods", children: [] },
                ],
              },
              {
                key: "51.5",
                label: "51.5 - Wholesale of non-agricultural intermediate products, waste and scrap",
                children: [
                  {
                    key: "51.51",
                    label: "51.51 - Wholesale of solid, liquid and gaseous fuels and related products",
                    children: [],
                  },
                  { key: "51.52", label: "51.52 - Wholesale of metals and metal ores", children: [] },
                  {
                    key: "51.53",
                    label: "51.53 - Wholesale of wood, construction materials and sanitary equipment",
                    children: [],
                  },
                  {
                    key: "51.54",
                    label: "51.54 - Wholesale of hardware, plumbing and heating equipment and supplies",
                    children: [],
                  },
                  { key: "51.55", label: "51.55 - Wholesale of chemical products", children: [] },
                  { key: "51.56", label: "51.56 - Wholesale of other intermediate products", children: [] },
                  { key: "51.57", label: "51.57 - Wholesale of waste and scrap", children: [] },
                ],
              },
              {
                key: "51.8",
                label: "51.8 - Wholesale of machinery, equipment and supplies",
                children: [
                  { key: "51.81", label: "51.81 - Wholesale of machine tools", children: [] },
                  {
                    key: "51.82",
                    label: "51.82 - Wholesale of mining, construction and civil engineering machinery",
                    children: [],
                  },
                  {
                    key: "51.83",
                    label:
                      "51.83 - Wholesale of machinery for the textile industry and of sewing and knitting machines",
                    children: [],
                  },
                  {
                    key: "51.84",
                    label: "51.84 - Wholesale of computers, computer peripheral equipment and software",
                    children: [],
                  },
                  { key: "51.85", label: "51.85 - Wholesale of other office machinery and equipment", children: [] },
                  { key: "51.86", label: "51.86 - Wholesale of other electronic parts and equipment", children: [] },
                  {
                    key: "51.87",
                    label: "51.87 - Wholesale of other machinery for use in industry, trade and navigation",
                    children: [],
                  },
                  {
                    key: "51.88",
                    label:
                      "51.88 - Wholesale of agricultural machinery and accessories and implements, including tractors",
                    children: [],
                  },
                ],
              },
              {
                key: "51.9",
                label: "51.9 - Other wholesale",
                children: [{ key: "51.90", label: "51.90 - Other wholesale", children: [] }],
              },
            ],
          },
          {
            key: "52",
            label:
              "52 - Retail trade, except of motor vehicles and motorcycles; repair of personal and household goods",
            children: [
              {
                key: "52.1",
                label: "52.1 - Retail sale in non-specialized stores",
                children: [
                  {
                    key: "52.11",
                    label:
                      "52.11 - Retail sale in non-specialized stores with food, beverages or tobacco predominating",
                    children: [],
                  },
                  { key: "52.12", label: "52.12 - Other retail sale in non-specialized stores", children: [] },
                ],
              },
              {
                key: "52.2",
                label: "52.2 - Retail sale of food, beverages and tobacco in specialized stores",
                children: [
                  { key: "52.21", label: "52.21 - Retail sale of fruit and vegetables", children: [] },
                  { key: "52.22", label: "52.22 - Retail sale of meat and meat products", children: [] },
                  { key: "52.23", label: "52.23 - Retail sale of fish, crustaceans and molluscs", children: [] },
                  {
                    key: "52.24",
                    label: "52.24 - Retail sale of bread, cakes, flour confectionery and sugar confectionery",
                    children: [],
                  },
                  { key: "52.25", label: "52.25 - Retail sale of alcoholic and other beverages", children: [] },
                  { key: "52.26", label: "52.26 - Retail sale of tobacco products", children: [] },
                  {
                    key: "52.27",
                    label: "52.27 - Other retail sale of food, beverages and tobacco in specialized stores",
                    children: [],
                  },
                ],
              },
              {
                key: "52.3",
                label: "52.3 - Retail sale of pharmaceutical and medical goods, cosmetic and toilet articles",
                children: [
                  { key: "52.31", label: "52.31 - Dispensing chemists", children: [] },
                  { key: "52.32", label: "52.32 - Retail sale of medical and orthopaedic goods", children: [] },
                  { key: "52.33", label: "52.33 - Retail sale of cosmetic and toilet articles", children: [] },
                ],
              },
              {
                key: "52.4",
                label: "52.4 - Other retail sale of new goods in specialized stores",
                children: [
                  { key: "52.41", label: "52.41 - Retail sale of textiles", children: [] },
                  { key: "52.42", label: "52.42 - Retail sale of clothing", children: [] },
                  { key: "52.43", label: "52.43 - Retail sale of footwear and leather goods", children: [] },
                  {
                    key: "52.44",
                    label: "52.44 - Retail sale of furniture, lighting equipment and household articles n.e.c.",
                    children: [],
                  },
                  {
                    key: "52.45",
                    label: "52.45 - Retail sale of electrical household appliances and radio and television goods",
                    children: [],
                  },
                  { key: "52.46", label: "52.46 - Retail sale of hardware, paints and glass", children: [] },
                  { key: "52.47", label: "52.47 - Retail sale of books, newspapers and stationery", children: [] },
                  { key: "52.48", label: "52.48 - Other retail sale in specialized stores", children: [] },
                ],
              },
              {
                key: "52.5",
                label: "52.5 - Retail sale of second-hand goods in stores",
                children: [{ key: "52.50", label: "52.50 - Retail sale of second-hand goods in stores", children: [] }],
              },
              {
                key: "52.6",
                label: "52.6 - Retail sale not in stores",
                children: [
                  { key: "52.61", label: "52.61 - Retail sale via mail order houses", children: [] },
                  { key: "52.62", label: "52.62 - Retail sale via stalls and markets", children: [] },
                  { key: "52.63", label: "52.63 - Other non-store retail sale", children: [] },
                ],
              },
              {
                key: "52.7",
                label: "52.7 - Repair of personal and household goods",
                children: [
                  { key: "52.71", label: "52.71 - Repair of boots, shoes and other articles of leather", children: [] },
                  { key: "52.72", label: "52.72 - Repair of electrical household goods", children: [] },
                  { key: "52.73", label: "52.73 - Repair of watches, clocks and jewellery", children: [] },
                  { key: "52.74", label: "52.74 - Repair n.e.c.", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "H",
    label: "H - Hotels and restaurants",
    children: [
      {
        key: "HA",
        label: "HA - Hotels and restaurants",
        children: [
          {
            key: "55",
            label: "55 - Hotels and restaurants",
            children: [
              {
                key: "55.1",
                label: "55.1 - Hotels",
                children: [{ key: "55.10", label: "55.10 - Hotels", children: [] }],
              },
              {
                key: "55.2",
                label: "55.2 - Camping sites and other provision of short-stay accommodation",
                children: [
                  { key: "55.21", label: "55.21 - Youth hostels and mountain refuges", children: [] },
                  { key: "55.22", label: "55.22 - Camping sites, including caravan sites", children: [] },
                  { key: "55.23", label: "55.23 - Other provision of lodgings n.e.c.", children: [] },
                ],
              },
              {
                key: "55.3",
                label: "55.3 - Restaurants",
                children: [{ key: "55.30", label: "55.30 - Restaurants", children: [] }],
              },
              { key: "55.4", label: "55.4 - Bars", children: [{ key: "55.40", label: "55.40 - Bars", children: [] }] },
              {
                key: "55.5",
                label: "55.5 - Canteens and catering",
                children: [
                  { key: "55.51", label: "55.51 - Canteens", children: [] },
                  { key: "55.52", label: "55.52 - Catering", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "I",
    label: "I - Transport, storage and communication",
    children: [
      {
        key: "IA",
        label: "IA - Transport, storage and communication",
        children: [
          {
            key: "60",
            label: "60 - Land transport; transport via pipelines",
            children: [
              {
                key: "60.1",
                label: "60.1 - Transport via railways",
                children: [{ key: "60.10", label: "60.10 - Transport via railways", children: [] }],
              },
              {
                key: "60.2",
                label: "60.2 - Other land transport",
                children: [
                  { key: "60.21", label: "60.21 - Other scheduled passenger land transport", children: [] },
                  { key: "60.22", label: "60.22 - Taxi operation", children: [] },
                  { key: "60.23", label: "60.23 - Other land passenger transport", children: [] },
                  { key: "60.24", label: "60.24 - Freight transport by road", children: [] },
                ],
              },
              {
                key: "60.3",
                label: "60.3 - Transport via pipelines",
                children: [{ key: "60.30", label: "60.30 - Transport via pipelines", children: [] }],
              },
            ],
          },
          {
            key: "61",
            label: "61 - Water transport",
            children: [
              {
                key: "61.1",
                label: "61.1 - Sea and coastal water transport",
                children: [{ key: "61.10", label: "61.10 - Sea and coastal water transport", children: [] }],
              },
              {
                key: "61.2",
                label: "61.2 - Inland water transport",
                children: [{ key: "61.20", label: "61.20 - Inland water transport", children: [] }],
              },
            ],
          },
          {
            key: "62",
            label: "62 - Air transport",
            children: [
              {
                key: "62.1",
                label: "62.1 - Scheduled air transport",
                children: [{ key: "62.10", label: "62.10 - Scheduled air transport", children: [] }],
              },
              {
                key: "62.2",
                label: "62.2 - Non-scheduled air transport",
                children: [{ key: "62.20", label: "62.20 - Non-scheduled air transport", children: [] }],
              },
              {
                key: "62.3",
                label: "62.3 - Space transport",
                children: [{ key: "62.30", label: "62.30 - Space transport", children: [] }],
              },
            ],
          },
          {
            key: "63",
            label: "63 - Supporting and auxiliary transport activities; activities of travel agencies",
            children: [
              {
                key: "63.1",
                label: "63.1 - Cargo handling and storage",
                children: [
                  { key: "63.11", label: "63.11 - Cargo handling", children: [] },
                  { key: "63.12", label: "63.12 - Storage and warehousing", children: [] },
                ],
              },
              {
                key: "63.2",
                label: "63.2 - Other supporting transport activities",
                children: [
                  { key: "63.21", label: "63.21 - Other supporting land transport activities", children: [] },
                  { key: "63.22", label: "63.22 - Other supporting water transport activities", children: [] },
                  { key: "63.23", label: "63.23 - Other supporting air transport activities", children: [] },
                ],
              },
              {
                key: "63.3",
                label: "63.3 - Activities of travel agencies and tour operators; tourist assistance activities n.e.c.",
                children: [
                  {
                    key: "63.30",
                    label:
                      "63.30 - Activities of travel agencies and tour operators; tourist assistance activities n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "63.4",
                label: "63.4 - Activities of other transport agencies",
                children: [{ key: "63.40", label: "63.40 - Activities of other transport agencies", children: [] }],
              },
            ],
          },
          {
            key: "64",
            label: "64 - Post and telecommunications",
            children: [
              {
                key: "64.1",
                label: "64.1 - Post and courier activities",
                children: [
                  { key: "64.11", label: "64.11 - National post activities", children: [] },
                  {
                    key: "64.12",
                    label: "64.12 - Courier activities other than national post activities",
                    children: [],
                  },
                ],
              },
              {
                key: "64.2",
                label: "64.2 - Telecommunications",
                children: [{ key: "64.20", label: "64.20 - Telecommunications", children: [] }],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "J",
    label: "J - Financial intermediation",
    children: [
      {
        key: "JA",
        label: "JA - Financial intermediation",
        children: [
          {
            key: "65",
            label: "65 - Financial intermediation, except insurance and pension funding",
            children: [
              {
                key: "65.1",
                label: "65.1 - Monetary intermediation",
                children: [
                  { key: "65.11", label: "65.11 - Central banking", children: [] },
                  { key: "65.12", label: "65.12 - Other monetary intermediation", children: [] },
                ],
              },
              {
                key: "65.2",
                label: "65.2 - Other financial intermediation",
                children: [
                  { key: "65.21", label: "65.21 - Financial leasing", children: [] },
                  { key: "65.22", label: "65.22 - Other credit granting", children: [] },
                  { key: "65.23", label: "65.23 - Other financial intermediation n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "66",
            label: "66 - Insurance and pension funding, except compulsory social security",
            children: [
              {
                key: "66.0",
                label: "66.0 - Insurance and pension funding, except compulsory social security",
                children: [
                  { key: "66.01", label: "66.01 - Life insurance", children: [] },
                  { key: "66.02", label: "66.02 - Pension funding", children: [] },
                  { key: "66.03", label: "66.03 - Non-life insurance", children: [] },
                ],
              },
            ],
          },
          {
            key: "67",
            label: "67 - Activities auxiliary to financial intermediation",
            children: [
              {
                key: "67.1",
                label: "67.1 - Activities auxiliary to financial intermediation, except insurance and pension funding",
                children: [
                  { key: "67.11", label: "67.11 - Administration of financial markets", children: [] },
                  { key: "67.12", label: "67.12 - Security broking and fund management", children: [] },
                  {
                    key: "67.13",
                    label: "67.13 - Activities auxiliary to financial intermediation n.e.c.",
                    children: [],
                  },
                ],
              },
              {
                key: "67.2",
                label: "67.2 - Activities auxiliary to insurance and pension funding",
                children: [
                  {
                    key: "67.20",
                    label: "67.20 - Activities auxiliary to insurance and pension funding",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "K",
    label: "K - Real estate, renting and business activities",
    children: [
      {
        key: "KA",
        label: "KA - Real estate, renting and business activities",
        children: [
          {
            key: "70",
            label: "70 - Real estate activities",
            children: [
              {
                key: "70.1",
                label: "70.1 - Real estate activities with own property",
                children: [
                  { key: "70.11", label: "70.11 - Development and selling of real estate", children: [] },
                  { key: "70.12", label: "70.12 - Buying and selling of own real estate", children: [] },
                ],
              },
              {
                key: "70.2",
                label: "70.2 - Letting of own property",
                children: [{ key: "70.20", label: "70.20 - Letting of own property", children: [] }],
              },
              {
                key: "70.3",
                label: "70.3 - Real estate activities on a fee or contract basis",
                children: [
                  { key: "70.31", label: "70.31 - Real estate agencies", children: [] },
                  { key: "70.32", label: "70.32 - Management of real estate on a fee or contract basis", children: [] },
                ],
              },
            ],
          },
          {
            key: "71",
            label: "71 - Renting of machinery and equipment without operator and of personal and household goods",
            children: [
              {
                key: "71.1",
                label: "71.1 - Renting of automobiles",
                children: [{ key: "71.10", label: "71.10 - Renting of automobiles", children: [] }],
              },
              {
                key: "71.2",
                label: "71.2 - Renting of other transport equipment",
                children: [
                  { key: "71.21", label: "71.21 - Renting of other land transport equipment", children: [] },
                  { key: "71.22", label: "71.22 - Renting of water transport equipment", children: [] },
                  { key: "71.23", label: "71.23 - Renting of air transport equipment", children: [] },
                ],
              },
              {
                key: "71.3",
                label: "71.3 - Renting of other machinery and equipment",
                children: [
                  { key: "71.31", label: "71.31 - Renting of agricultural machinery and equipment", children: [] },
                  {
                    key: "71.32",
                    label: "71.32 - Renting of construction and civil engineering machinery and equipment",
                    children: [],
                  },
                  {
                    key: "71.33",
                    label: "71.33 - Renting of office machinery and equipment, including computers",
                    children: [],
                  },
                  { key: "71.34", label: "71.34 - Renting of other machinery and equipment n.e.c.", children: [] },
                ],
              },
              {
                key: "71.4",
                label: "71.4 - Renting of personal and household goods n.e.c.",
                children: [
                  { key: "71.40", label: "71.40 - Renting of personal and household goods n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "72",
            label: "72 - Computer and related activities",
            children: [
              {
                key: "72.1",
                label: "72.1 - Hardware consultancy",
                children: [{ key: "72.10", label: "72.10 - Hardware consultancy", children: [] }],
              },
              {
                key: "72.2",
                label: "72.2 - Software consultancy and supply",
                children: [
                  { key: "72.21", label: "72.21 - Publishing of software", children: [] },
                  { key: "72.22", label: "72.22 - Other software consultancy and supply", children: [] },
                ],
              },
              {
                key: "72.3",
                label: "72.3 - Data processing",
                children: [{ key: "72.30", label: "72.30 - Data processing", children: [] }],
              },
              {
                key: "72.4",
                label: "72.4 - Database activities",
                children: [{ key: "72.40", label: "72.40 - Database activities", children: [] }],
              },
              {
                key: "72.5",
                label: "72.5 - Maintenance and repair of office, accounting and computing machinery",
                children: [
                  {
                    key: "72.50",
                    label: "72.50 - Maintenance and repair of office, accounting and computing machinery",
                    children: [],
                  },
                ],
              },
              {
                key: "72.6",
                label: "72.6 - Other computer related activities",
                children: [{ key: "72.60", label: "72.60 - Other computer related activities", children: [] }],
              },
            ],
          },
          {
            key: "73",
            label: "73 - Research and development",
            children: [
              {
                key: "73.1",
                label: "73.1 - Research and experimental development on natural sciences and engineering",
                children: [
                  {
                    key: "73.10",
                    label: "73.10 - Research and experimental development on natural sciences and engineering",
                    children: [],
                  },
                ],
              },
              {
                key: "73.2",
                label: "73.2 - Research and experimental development on social sciences and humanities",
                children: [
                  {
                    key: "73.20",
                    label: "73.20 - Research and experimental development on social sciences and humanities",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "74",
            label: "74 - Other business activities",
            children: [
              {
                key: "74.1",
                label:
                  "74.1 - Legal, accounting, book-keeping and auditing activities; tax consultancy; market research and public opinion polling; business and management consultancy; holdings",
                children: [
                  { key: "74.11", label: "74.11 - Legal activities", children: [] },
                  {
                    key: "74.12",
                    label: "74.12 - Accounting, book-keeping and auditing activities; tax consultancy",
                    children: [],
                  },
                  { key: "74.13", label: "74.13 - Market research and public opinion polling", children: [] },
                  { key: "74.14", label: "74.14 - Business and management consultancy activities", children: [] },
                  { key: "74.15", label: "74.15 - Management activities of holding companies", children: [] },
                ],
              },
              {
                key: "74.2",
                label: "74.2 - Architectural and engineering activities and related technical consultancy",
                children: [
                  {
                    key: "74.20",
                    label: "74.20 - Architectural and engineering activities and related technical consultancy",
                    children: [],
                  },
                ],
              },
              {
                key: "74.3",
                label: "74.3 - Technical testing and analysis",
                children: [{ key: "74.30", label: "74.30 - Technical testing and analysis", children: [] }],
              },
              {
                key: "74.4",
                label: "74.4 - Advertising",
                children: [{ key: "74.40", label: "74.40 - Advertising", children: [] }],
              },
              {
                key: "74.5",
                label: "74.5 - Labour recruitment and provision of personnel",
                children: [
                  { key: "74.50", label: "74.50 - Labour recruitment and provision of personnel", children: [] },
                ],
              },
              {
                key: "74.6",
                label: "74.6 - Investigation and security activities",
                children: [{ key: "74.60", label: "74.60 - Investigation and security activities", children: [] }],
              },
              {
                key: "74.7",
                label: "74.7 - Industrial cleaning",
                children: [{ key: "74.70", label: "74.70 - Industrial cleaning", children: [] }],
              },
              {
                key: "74.8",
                label: "74.8 - Miscellaneous business activities n.e.c.",
                children: [
                  { key: "74.81", label: "74.81 - Photographic activities", children: [] },
                  { key: "74.82", label: "74.82 - Packaging activities", children: [] },
                  { key: "74.85", label: "74.85 - Secretarial and translation activities", children: [] },
                  { key: "74.86", label: "74.86 - Call centre activities", children: [] },
                  { key: "74.87", label: "74.87 - Other business activities n.e.c.", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "L",
    label: "L - Public administration and defence; compulsory social security",
    children: [
      {
        key: "LA",
        label: "LA - Public administration and defence; compulsory social security",
        children: [
          {
            key: "75",
            label: "75 - Public administration and defence; compulsory social security",
            children: [
              {
                key: "75.1",
                label: "75.1 - Administration of the State and the economic and social policy of the community",
                children: [
                  { key: "75.11", label: "75.11 - General (overall) public service activities", children: [] },
                  {
                    key: "75.12",
                    label:
                      "75.12 - Regulation of the activities of agencies that provide health care, education, cultural services and other social services, excluding social security",
                    children: [],
                  },
                  {
                    key: "75.13",
                    label: "75.13 - Regulation of and contribution to more efficient operation of business",
                    children: [],
                  },
                  {
                    key: "75.14",
                    label: "75.14 - Supporting service activities for the government as a whole",
                    children: [],
                  },
                ],
              },
              {
                key: "75.2",
                label: "75.2 - Provision of services to the community as a whole",
                children: [
                  { key: "75.21", label: "75.21 - Foreign affairs", children: [] },
                  { key: "75.22", label: "75.22 - Defence activities", children: [] },
                  { key: "75.23", label: "75.23 - Justice and judicial activities", children: [] },
                  { key: "75.24", label: "75.24 - Public security, law and order activities", children: [] },
                  { key: "75.25", label: "75.25 - Fire service activities", children: [] },
                ],
              },
              {
                key: "75.3",
                label: "75.3 - Compulsory social security activities",
                children: [{ key: "75.30", label: "75.30 - Compulsory social security activities", children: [] }],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "M",
    label: "M - Education",
    children: [
      {
        key: "MA",
        label: "MA - Education",
        children: [
          {
            key: "80",
            label: "80 - Education",
            children: [
              {
                key: "80.1",
                label: "80.1 - Primary education",
                children: [{ key: "80.10", label: "80.10 - Primary education", children: [] }],
              },
              {
                key: "80.2",
                label: "80.2 - Secondary education",
                children: [
                  { key: "80.21", label: "80.21 - General secondary education", children: [] },
                  { key: "80.22", label: "80.22 - Technical and vocational secondary education", children: [] },
                ],
              },
              {
                key: "80.3",
                label: "80.3 - Higher education",
                children: [{ key: "80.30", label: "80.30 - Higher education", children: [] }],
              },
              {
                key: "80.4",
                label: "80.4 - Adult and other education",
                children: [
                  { key: "80.41", label: "80.41 - Driving school activities", children: [] },
                  { key: "80.42", label: "80.42 - Adult and other education n.e.c.", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "N",
    label: "N - Health and social work",
    children: [
      {
        key: "NA",
        label: "NA - Health and social work",
        children: [
          {
            key: "85",
            label: "85 - Health and social work",
            children: [
              {
                key: "85.1",
                label: "85.1 - Human health activities",
                children: [
                  { key: "85.11", label: "85.11 - Hospital activities", children: [] },
                  { key: "85.12", label: "85.12 - Medical practice activities", children: [] },
                  { key: "85.13", label: "85.13 - Dental practice activities", children: [] },
                  { key: "85.14", label: "85.14 - Other human health activities", children: [] },
                ],
              },
              {
                key: "85.2",
                label: "85.2 - Veterinary activities",
                children: [{ key: "85.20", label: "85.20 - Veterinary activities", children: [] }],
              },
              {
                key: "85.3",
                label: "85.3 - Social work activities",
                children: [
                  { key: "85.31", label: "85.31 - Social work activities with accommodation", children: [] },
                  { key: "85.32", label: "85.32 - Social work activities without accommodation", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "O",
    label: "O - Other community, social and personal service activities",
    children: [
      {
        key: "OA",
        label: "OA - Other community, social and personal service activities",
        children: [
          {
            key: "90",
            label: "90 - Sewage and refuse disposal, sanitation and similar activities",
            children: [
              {
                key: "90.0",
                label: "90.0 - Sewage and refuse disposal, sanitation and similar activities",
                children: [
                  { key: "90.01", label: "90.01 - Collection and treatment of sewage", children: [] },
                  { key: "90.02", label: "90.02 - Collection and treatment of other waste", children: [] },
                  { key: "90.03", label: "90.03 - Sanitation, remediation and similar activities", children: [] },
                ],
              },
            ],
          },
          {
            key: "91",
            label: "91 - Activities of membership organizations n.e.c.",
            children: [
              {
                key: "91.1",
                label: "91.1 - Activities of business, employers' and professional organizations",
                children: [
                  { key: "91.11", label: "91.11 - Activities of business and employers' organizations", children: [] },
                  { key: "91.12", label: "91.12 - Activities of professional organizations", children: [] },
                ],
              },
              {
                key: "91.2",
                label: "91.2 - Activities of trade unions",
                children: [{ key: "91.20", label: "91.20 - Activities of trade unions", children: [] }],
              },
              {
                key: "91.3",
                label: "91.3 - Activities of other membership organizations",
                children: [
                  { key: "91.31", label: "91.31 - Activities of religious organizations", children: [] },
                  { key: "91.32", label: "91.32 - Activities of political organizations", children: [] },
                  { key: "91.33", label: "91.33 - Activities of other membership organizations n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "92",
            label: "92 - Recreational, cultural and sporting activities",
            children: [
              {
                key: "92.1",
                label: "92.1 - Motion picture and video activities",
                children: [
                  { key: "92.11", label: "92.11 - Motion picture and video production", children: [] },
                  { key: "92.12", label: "92.12 - Motion picture and video distribution", children: [] },
                  { key: "92.13", label: "92.13 - Motion picture projection", children: [] },
                ],
              },
              {
                key: "92.2",
                label: "92.2 - Radio and television activities",
                children: [{ key: "92.20", label: "92.20 - Radio and television activities", children: [] }],
              },
              {
                key: "92.3",
                label: "92.3 - Other entertainment activities",
                children: [
                  { key: "92.31", label: "92.31 - Artistic and literary creation and interpretation", children: [] },
                  { key: "92.32", label: "92.32 - Operation of arts facilities", children: [] },
                  { key: "92.33", label: "92.33 - Fair and amusement park activities", children: [] },
                  { key: "92.34", label: "92.34 - Other entertainment activities n.e.c.", children: [] },
                ],
              },
              {
                key: "92.4",
                label: "92.4 - News agency activities",
                children: [{ key: "92.40", label: "92.40 - News agency activities", children: [] }],
              },
              {
                key: "92.5",
                label: "92.5 - Library, archives, museums and other cultural activities",
                children: [
                  { key: "92.51", label: "92.51 - Library and archives activities", children: [] },
                  {
                    key: "92.52",
                    label: "92.52 - Museums activities and preservation of historical sites and buildings",
                    children: [],
                  },
                  {
                    key: "92.53",
                    label: "92.53 - Botanical and zoological gardens and nature reserves activities",
                    children: [],
                  },
                ],
              },
              {
                key: "92.6",
                label: "92.6 - Sporting activities",
                children: [
                  { key: "92.61", label: "92.61 - Operation of sports arenas and stadiums", children: [] },
                  { key: "92.62", label: "92.62 - Other sporting activities", children: [] },
                ],
              },
              {
                key: "92.7",
                label: "92.7 - Other recreational activities",
                children: [
                  { key: "92.71", label: "92.71 - Gambling and betting activities", children: [] },
                  { key: "92.72", label: "92.72 - Other recreational activities n.e.c.", children: [] },
                ],
              },
            ],
          },
          {
            key: "93",
            label: "93 - Other service activities",
            children: [
              {
                key: "93.0",
                label: "93.0 - Other service activities",
                children: [
                  { key: "93.01", label: "93.01 - Washing and dry-cleaning of textile and fur products", children: [] },
                  { key: "93.02", label: "93.02 - Hairdressing and other beauty treatment", children: [] },
                  { key: "93.03", label: "93.03 - Funeral and related activities", children: [] },
                  { key: "93.04", label: "93.04 - Physical well-being activities", children: [] },
                  { key: "93.05", label: "93.05 - Other service activities n.e.c.", children: [] },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "P",
    label: "P - Activities of households",
    children: [
      {
        key: "PA",
        label: "PA - Activities of households",
        children: [
          {
            key: "95",
            label: "95 - Activities of households as employers of domestic staff",
            children: [
              {
                key: "95.0",
                label: "95.0 - Activities of households as employers of domestic staff",
                children: [
                  {
                    key: "95.00",
                    label: "95.00 - Activities of households as employers of domestic staff",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "96",
            label: "96 - Undifferentiated goods producing activities of private households for own use",
            children: [
              {
                key: "96.0",
                label: "96.0 - Undifferentiated goods producing activities of private households for own use",
                children: [
                  {
                    key: "96.00",
                    label: "96.00 - Undifferentiated goods producing activities of private households for own use",
                    children: [],
                  },
                ],
              },
            ],
          },
          {
            key: "97",
            label: "97 - Undifferentiated services producing activities of private households for own use",
            children: [
              {
                key: "97.0",
                label: "97.0 - Undifferentiated services producing activities of private households for own use",
                children: [
                  {
                    key: "97.00",
                    label: "97.00 - Undifferentiated services producing activities of private households for own use",
                    children: [],
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
  {
    key: "Q",
    label: "Q - Extra-territorial organizations and bodies",
    children: [
      {
        key: "QA",
        label: "QA - Extra-territorial organizations and bodies",
        children: [
          {
            key: "99",
            label: "99 - Extra-territorial organizations and bodies",
            children: [
              {
                key: "99.0",
                label: "99.0 - Extra-territorial organizations and bodies",
                children: [{ key: "99.00", label: "99.00 - Extra-territorial organizations and bodies", children: [] }],
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
  for (let i = 0; i < input.length; i++) {
    naceCodeMap.set(assertDefined(input[i].key), input[i]);
    populateNaceCodeMap(input[i].children || []);
  }
}

populateNaceCodeMap(naceCodeTree);
