// <--WARNING--> THIS FILE IS AUTO-GENERATED BY THE FRAMEWORK-TOOLBOX AND WILL BE OVERWRITTEN
import { type BasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkDefinition';
import { type Configuration, type EutaxonomyFinancialsData } from '@clients/backend';
import { type AxiosInstance } from 'axios';
import { type PublicFrameworkDataApi } from '@/utils/api/UnifiedFrameworkDataApi';
import { EutaxonomyFinancialsApiClient } from '@/frameworks/eutaxonomy-financials/ApiClient';

export class BaseEutaxonomyFinancialsFrameworkDefinition
  implements BasePublicFrameworkDefinition<EutaxonomyFinancialsData>
{
  readonly identifier = 'eutaxonomy-financials';
  readonly explanation = 'Additional Taxonomy for Financials';
  readonly label = 'EU Taxonomy Financials';
  getPublicFrameworkApiClient(
    configuration?: Configuration,
    axiosInstance?: AxiosInstance
  ): PublicFrameworkDataApi<EutaxonomyFinancialsData> {
    return new EutaxonomyFinancialsApiClient(configuration, axiosInstance);
  }
}

export default new BaseEutaxonomyFinancialsFrameworkDefinition();
