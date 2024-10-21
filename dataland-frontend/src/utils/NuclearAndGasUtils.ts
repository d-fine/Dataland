import type {
  NuclearAndGasAlignedDenominator,
  NuclearAndGasAlignedNumerator,
  NuclearAndGasEligibleButNotAligned,
  NuclearAndGasNonEligible,
} from '@clients/backend';

/**
 * TypeGuard for NuclearAlignedDenominator Type
 * Checks if the provided NuclearAndGasData represents an aligned denominator economic activity.
 * @param nuclearAndGasData The input data representing one of the Nuclear and Gas data points.
 * @returns Returns true if the data represents an aligned denominator activity, otherwise false.
 */
export function isAlignedDenominator(
  nuclearAndGasData: NuclearAndGasType
): nuclearAndGasData is NuclearAndGasAlignedDenominator {
  return typeof nuclearAndGasData === 'object' && 'taxonomyAlignedShareDenominatorNAndG426' in nuclearAndGasData;
}

/**
 * TypeGuard for NuclearAlignedNumerator Type
 * Checks if the provided NuclearAndGasData represents an aligned numerator economic activity.
 * @param nuclearAndGasData The input data representing one of the Nuclear and Gas data points.
 * @returns Returns true if the data represents an aligned numerator activity, otherwise false.
 */
export function isAlignedNumerator(
  nuclearAndGasData: NuclearAndGasType
): nuclearAndGasData is NuclearAndGasAlignedNumerator {
  return typeof nuclearAndGasData === 'object' && 'taxonomyAlignedShareNumeratorNAndG426' in nuclearAndGasData;
}

/**
 * TypeGuard for NuclearAndGasEligibleButNotAligned Type
 * Checks if the provided NuclearAndGasData represents an eligible but non-aligned economic activity.
 * @param nuclearAndGasData The input data representing one of the Nuclear and Gas data points.
 * @returns Returns true if the data represents an eligible but non-aligned numerator activity, otherwise false.
 */
export function isEligibleButNotAlignedNumerator(
  nuclearAndGasData: NuclearAndGasType
): nuclearAndGasData is NuclearAndGasEligibleButNotAligned {
  return typeof nuclearAndGasData === 'object' && 'taxonomyEligibleButNotAlignedShareNAndG426' in nuclearAndGasData;
}

/**
 * TypeGuard for NuclearAndGasNonEligible Type
 * Checks if the provided NuclearAndGasData represents a non-eligible economic activity.
 * @param nuclearAndGasData The input data representing one of the Nuclear and Gas data points.
 * @returns Returns true if the data represents a non-eligible activity, otherwise false.
 */
export function isNonEligible(nuclearAndGasData: NuclearAndGasType): nuclearAndGasData is NuclearAndGasNonEligible {
  return typeof nuclearAndGasData === 'object' && 'taxonomyNonEligibleShareNAndG426' in nuclearAndGasData;
}

export type NuclearAndGasType =
  | NuclearAndGasAlignedDenominator
  | NuclearAndGasAlignedNumerator
  | NuclearAndGasEligibleButNotAligned
  | NuclearAndGasNonEligible;
