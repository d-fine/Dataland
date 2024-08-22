import {
  activityApiNameToHumanizedName,
  ActivityName,
} from '@/components/resources/frameworkDataSearch/EuTaxonomyActivityNames';
import { Activity } from '@clients/backend';

describe('Unit tests for the eu taxonomy activity name file', () => {
  it('Assure that all the actual activities are covered in the hand-made activity name file', () => {
    const originalActivityKeys = Object.keys(Activity);
    const handMadeActivityNameKeys = Object.keys(ActivityName);

    expect(originalActivityKeys).to.have.lengthOf(handMadeActivityNameKeys.length);

    originalActivityKeys.forEach((key) => {
      expect(handMadeActivityNameKeys).to.include(key);
    });
  });

  it('Assure that the function which humanizes the activity-enums works properly', () => {
    expect(activityApiNameToHumanizedName(Activity.AirportInfrastructure)).to.equal(ActivityName.AirportInfrastructure);
  });
});
