import EutaxonomyFinancials202673BaseFrameworkDefinition from '@/frameworks/eutaxonomy-financials-2026-73/BaseFrameworkDefinition';
import {
  Configuration,
  DataTypeEnum,
  type EutaxonomyFinancials202673Data,
  EutaxonomyFinancials202673DataControllerApi,
} from '@clients/backend';
import { type BasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkDefinition';
import { BaseDataIntegrityTest } from '@e2e/specs/eutaxonomy-2026-73/BaseDataIntegrityTest';

class EutaxonomyFinancials202673DataIntegrityTest extends BaseDataIntegrityTest<EutaxonomyFinancials202673Data> {
  /**
   * @returns the framework definition used by this suite.
   */
  protected getFrameworkDefinition(): BasePublicFrameworkDefinition<EutaxonomyFinancials202673Data> {
    return EutaxonomyFinancials202673BaseFrameworkDefinition;
  }

  /**
   * @returns fixture file for this framework.
   */
  protected getFixtureFileName(): string {
    return 'CompanyInformationWithEutaxonomyFinancials202673PreparedFixtures.json';
  }

  /**
   * @returns fixture key used in this test.
   */
  protected getFixtureName(): string {
    return 'All-fields-defined-for-EU-Taxonomy-Financials-202673-Framework-Company';
  }

  /**
   * @returns describe block text.
   */
  protected getDescribeText(): string {
    return (
      'As a user, I expect to be able to upload EU Taxonomy Financials (2026/73) data via the api, and that the uploaded ' +
      'data is displayed correctly in the frontend'
    );
  }

  /**
   * @returns test case title.
   */
  protected getTestTitle(): string {
    return (
      'Create a company and an EU Taxonomy Financials (2026/73) dataset via api and assure that the data is ' +
      'stored correctly by retrieving and comparing it'
    );
  }

  /**
   * @returns generated company name prefix.
   */
  protected getTestCompanyPrefix(): string {
    return 'Company-Created-In-EU-Taxonomy-Financials-202673-Blanket-Test-';
  }

  /**
   * @returns data type enum for routing and intercept.
   */
  protected getDataTypeEnum(): DataTypeEnum {
    return DataTypeEnum.EutaxonomyFinancials202673;
  }

  /**
   * Validates uploaded data against backend response.
   */
  protected validateUploadedDataset(
    token: string,
    dataId: string,
    initiallyUploadedData: EutaxonomyFinancials202673Data
  ): Promise<void> {
    return new EutaxonomyFinancials202673DataControllerApi(new Configuration({ accessToken: token }))
      .getCompanyAssociatedEutaxonomyFinancials202673Data(dataId)
      .then((response) => {
        this.compareUploadedData(initiallyUploadedData, response.data.data);
      });
  }
}

new EutaxonomyFinancials202673DataIntegrityTest().registerDataIntegrityTest();
