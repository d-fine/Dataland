import { Generator } from '@e2e/utils/FakeFixtureUtils';
import type {
  NuclearAndGasAlignedDenominator,
  NuclearAndGasAlignedNumerator,
  NuclearAndGasEligibleButNotAligned,
  NuclearAndGasEnvironmentalObjective,
  NuclearAndGasNonEligible,
} from '@clients/backend';

export class NuclearAndGasGenerator extends Generator {
  generateNuclearAndGasEnvironmentalObjective(): NuclearAndGasEnvironmentalObjective {
    return {
      mitigationAndAdaptation: this.randomPercentageValue(),
      mitigation: this.randomPercentageValue(),
      adaptation: this.randomPercentageValue(),
    };
  }

  /**
   * Generates random NuclearAndGasAlignedDenominator data
   * @returns random NuclearAndGasAlignedDenominator data
   */
  generateNuclearAndGasAlignedDenominator(): NuclearAndGasAlignedDenominator {
    return {
      taxonomyAlignedShareDenominatorNAndG426: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorNAndG427: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorNAndG428: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorNAndG429: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorNAndG430: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorNAndG431: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominatorOtherActivities: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareDenominator: this.generateNuclearAndGasEnvironmentalObjective(),
    };
  }

  /**
   * Generates random NuclearAndGasAlignedNumerator data
   * @returns random NuclearAndGasAlignedNumerator data
   */
  generateNuclearAndGasAlignedNumerator(): NuclearAndGasAlignedNumerator {
    return {
      taxonomyAlignedShareNumeratorNAndG426: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorNAndG427: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorNAndG428: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorNAndG429: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorNAndG430: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorNAndG431: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumeratorOtherActivities: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyAlignedShareNumerator: this.generateNuclearAndGasEnvironmentalObjective(),
    };
  }

  /**
   * Generates random NuclearAndGasEligibleButNotAligned data
   * @returns random NuclearAndGasEligibleButNotAligned data
   */
  generateNuclearAndGasEligibleButNotAligned(): NuclearAndGasEligibleButNotAligned {
    return {
      taxonomyEligibleButNotAlignedShareNAndG426: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareNAndG427: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareNAndG428: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareNAndG429: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareNAndG430: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareNAndG431: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShareOtherActivities: this.generateNuclearAndGasEnvironmentalObjective(),
      taxonomyEligibleButNotAlignedShare: this.generateNuclearAndGasEnvironmentalObjective(),
    };
  }

  /**
   * Generates random NuclearAndGasNonEligible data
   * @returns random NuclearAndGasNonEligible data
   */
  generateNuclearAndGasNonEligible(): NuclearAndGasNonEligible {
    return {
      taxonomyNonEligibleShareNAndG426: this.randomPercentageValue(),
      taxonomyNonEligibleShareNAndG427: this.randomPercentageValue(),
      taxonomyNonEligibleShareNAndG428: this.randomPercentageValue(),
      taxonomyNonEligibleShareNAndG429: this.randomPercentageValue(),
      taxonomyNonEligibleShareNAndG430: this.randomPercentageValue(),
      taxonomyNonEligibleShareNAndG431: this.randomPercentageValue(),
      taxonomyNonEligibleShareOtherActivities: this.randomPercentageValue(),
      taxonomyNonEligibleShare: this.randomPercentageValue(),
    };
  }
}
