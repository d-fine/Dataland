import EutaxonomyNonFinancials202673BaseFrameworkDefinition from '@/frameworks/eutaxonomy-non-financials-2026-73/BaseFrameworkDefinition';
import {
  Configuration,
  DataTypeEnum,
  type EutaxonomyNonFinancials202673Data,
  EutaxonomyNonFinancials202673DataControllerApi,
} from '@clients/backend';
import { type BasePublicFrameworkDefinition } from '@/frameworks/BasePublicFrameworkDefinition';
import { BaseDataIntegrityTest } from '@e2e/specs/eutaxonomy-2026-73/BaseDataIntegrityTest';

class EutaxonomyNonFinancials202673DataIntegrityTest extends BaseDataIntegrityTest<EutaxonomyNonFinancials202673Data> {
  /**
   * @returns the framework definition used by this suite.
   */
  protected getFrameworkDefinition(): BasePublicFrameworkDefinition<EutaxonomyNonFinancials202673Data> {
    return EutaxonomyNonFinancials202673BaseFrameworkDefinition;
  }

  /**
   * @returns fixture file for this framework.
   */
  protected getFixtureFileName(): string {
    return 'CompanyInformationWithEutaxonomyNonFinancials202673PreparedFixtures.json';
  }

  /**
   * @returns fixture key used in this test.
   */
  protected getFixtureName(): string {
    return 'All-fields-defined-for-EU-Taxonomy-Non-Financials-202673-Framework-Company';
  }

  /**
   * @returns describe block text.
   */
  protected getDescribeText(): string {
    return (
      'As a user, I expect to be able to upload EU Taxonomy Non-Financials (2026/73) data via the api, and that the uploaded ' +
      'data is displayed correctly in the frontend'
    );
  }

  /**
   * @returns test case title.
   */
  protected getTestTitle(): string {
    return (
      'Create a company and an EU Taxonomy Non-Financials (2026/73) dataset via api and assure that the data is ' +
      'stored correctly by retrieving and comparing it'
    );
  }

  /**
   * @returns generated company name prefix.
   */
  protected getTestCompanyPrefix(): string {
    return 'Company-Created-In-EU-Taxonomy-Non-Financials-202673-Blanket-Test-';
  }

  /**
   * @returns data type enum for routing and intercept.
   */
  protected getDataTypeEnum(): DataTypeEnum {
    return DataTypeEnum.EutaxonomyNonFinancials202673;
  }

  /**
   * Validates uploaded data against backend response.
   */
  protected validateUploadedDataset(
    token: string,
    dataId: string,
    initiallyUploadedData: EutaxonomyNonFinancials202673Data
  ): Promise<void> {
    return new EutaxonomyNonFinancials202673DataControllerApi(new Configuration({ accessToken: token }))
      .getCompanyAssociatedEutaxonomyNonFinancials202673Data(dataId)
      .then((response) => {
        this.compareUploadedData(initiallyUploadedData, response.data.data);
      });
  }
}

new EutaxonomyNonFinancials202673DataIntegrityTest().registerDataIntegrityTest();
