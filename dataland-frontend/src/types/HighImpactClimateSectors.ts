import { naceCodeTree } from '@/components/forms/parts/elements/derived/NaceCodeTree';

const naceCodeTreeFilteredByHighImpactClimateSectors = naceCodeTree.filter((sector) => {
  if (sector?.key) {
    return ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'L'].includes(sector?.key);
  }
});

export const HighImpactClimateSectorsKeys: { [key: string]: string } = {};

export const optionsForHighImpactClimateSectors = naceCodeTreeFilteredByHighImpactClimateSectors.map((sector) => {
  const key = sector.key;
  const keyDescription = sector.label?.split(' - ')[1].trim() ?? `NACE Code ${key}`;
  HighImpactClimateSectorsKeys[`NaceCode${key}` as keyof typeof HighImpactClimateSectorsKeys] = keyDescription;
  return {
    label: sector.label,
    value: sector.key,
  };
});

export enum HighImpactClimateSectorsNaceCodes {
  'NaceCodeA' = 'A',
  'NaceCodeB' = 'B',
  'NaceCodeC' = 'C',
  'NaceCodeD' = 'D',
  'NaceCodeE' = 'E',
  'NaceCodeF' = 'F',
  'NaceCodeG' = 'G',
  'NaceCodeH' = 'H',
  'NaceCodeL' = 'L',
}
