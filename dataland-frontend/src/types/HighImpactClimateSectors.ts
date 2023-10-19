import { naceCodeTree } from "@/components/forms/parts/elements/derived/NaceCodeTree";

const naceCodeTreeFilteredByHighImpactClimateSectors = naceCodeTree.filter((sector) => {
  if (sector?.key) {
    return ["A", "B", "C", "D", "E", "F", "G", "H", "L"].includes(sector?.key);
  }
});

export const HighImpactClimateSectorsKeys: { [key: string]: string } = {};

export const optionsForHighImpactClimateSectors = naceCodeTreeFilteredByHighImpactClimateSectors.map((sector) => {
  const key = sector.key as string;
  const keyDescription = sector.label?.split(" - ")[1].trim() ?? `NACE Code ${key} in GWh`;
  HighImpactClimateSectorsKeys[`NaceCode${key}InGWh` as keyof typeof HighImpactClimateSectorsKeys] = keyDescription;
  return {
    label: sector.label,
    value: sector.key,
  };
});

export enum HighImpactClimateSectorsNaceCodes {
  "NaceCodeAInGWh" = "A",
  "NaceCodeBInGWh" = "B",
  "NaceCodeCInGWh" = "C",
  "NaceCodeDInGWh" = "D",
  "NaceCodeEInGWh" = "E",
  "NaceCodeFInGWh" = "F",
  "NaceCodeGInGWh" = "G",
  "NaceCodeHInGWh" = "H",
  "NaceCodeLInGWh" = "L",
}
