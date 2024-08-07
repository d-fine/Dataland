// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
import {
  type FrontendFrameworkDefinition,
  type FrameworkViewConfiguration,
} from '@/frameworks/BaseFrameworkDefinition';
import { eutaxonomyNonFinancialsViewConfiguration } from '@/frameworks/eutaxonomy-non-financials/ViewConfig';
import { BaseEutaxonomyNonFinancialsFrameworkDefinition } from '@/frameworks/eutaxonomy-non-financials/BaseFrameworkDefinition';
import { type EutaxonomyNonFinancialsData } from '@clients/backend';

export class FrontendEutaxonomyNonFinancialsFrameworkDefinition
  extends BaseEutaxonomyNonFinancialsFrameworkDefinition
  implements FrontendFrameworkDefinition<EutaxonomyNonFinancialsData>
{
  getFrameworkViewConfiguration(): FrameworkViewConfiguration<EutaxonomyNonFinancialsData> {
    return {
      type: 'MultiLayerDataTable',
      configuration: eutaxonomyNonFinancialsViewConfiguration,
    };
  }
}

export default new FrontendEutaxonomyNonFinancialsFrameworkDefinition();
