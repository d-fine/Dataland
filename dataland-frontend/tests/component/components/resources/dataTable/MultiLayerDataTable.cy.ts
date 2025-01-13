// @ts-nocheck
import { type MLDTConfig } from '@/components/resources/dataTable/MultiLayerDataTableConfiguration';
import { MLDTDisplayComponentName } from '@/components/resources/dataTable/MultiLayerDataTableCellDisplayer';
import MultiLayerDataTable from '@/components/resources/dataTable/MultiLayerDataTable.vue';
import {
  getCellValueContainerAndCheckIconForHiddenDisplay,
  getCellValueContainer,
  getCellRowHeaderContainer,
  getSectionHeadAndCheckIconForHiddenDisplay,
  getSectionHead,
} from '@sharedUtils/components/resources/dataTable/MultiLayerDataTableTestUtils';
import { editMultiLayerDataTableConfigForHighlightingHiddenFields } from '@/components/resources/frameworkDataSearch/frameworkPanel/MultiLayerDataTableQaHighlighter';
import { DataTypeEnum, QaStatus } from '@clients/backend';
import { type DataAndMetaInformation } from '@/api-models/DataAndMetaInformation';
describe('Tests for the MultiLayerDataTable component', () => {
  /**
   * Mounts the MultiLayerDataTable with the given dataset
   * @param dataAndMetaInfo the datasets to mount together with their meta info
   * @returns the component mounting chainable
   */
  function mountMultiLayerDataTableWithDatasets(
    dataAndMetaInfo: Array<DataAndMetaInformation<DummyFrameworkForTest>>
  ): Cypress.Chainable {
    return cy.mountWithPlugins(MultiLayerDataTable, {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        config: dummyFrameworkMLDTConfig,
        dataAndMetaInfo: dataAndMetaInfo,
      },
    });
  }

  interface DummyFrameworkForTest {
    stringOnLevel1?: string;
    stringOnLevel2: string;
    stringOnLevel3: string;
  }

  const dummyFrameworkMLDTConfig: MLDTConfig<DummyFrameworkForTest> = [
    {
      type: 'cell',
      label: 'Level 1 - String',
      explanation: 'This is a test info',
      shouldDisplay: (dataset) => !!dataset.stringOnLevel1,
      valueGetter: (dataset) => ({
        displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
        displayValue: dataset.stringOnLevel1,
      }),
    },
    {
      type: 'section',
      label: 'Section 1',
      labelBadgeColor: 'blue',
      expandOnPageLoad: true,
      shouldDisplay: () => true,
      children: [
        {
          type: 'cell',
          label: 'Level 2 - String',
          shouldDisplay: () => true,
          valueGetter: (dataset) => ({
            displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
            displayValue: dataset.stringOnLevel2,
          }),
        },
        {
          type: 'section',
          label: 'Subsection 1',
          expandOnPageLoad: false,
          shouldDisplay: () => true,
          children: [
            {
              type: 'cell',
              label: 'Level 3 - String',
              shouldDisplay: () => true,
              valueGetter: (dataset) => ({
                displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
                displayValue: dataset.stringOnLevel3,
              }),
            },
          ],
        },
      ],
    },
    {
      type: 'section',
      label: 'Section 2',
      expandOnPageLoad: false,
      shouldDisplay: (dataset) => !!dataset.stringOnLevel1,
      children: [
        {
          type: 'cell',
          label: 'Static Value Cell',
          shouldDisplay: () => true,
          valueGetter: () => ({
            displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
            displayValue: 'This is static',
          }),
        },
      ],
    },
    {
      type: 'section',
      label: 'Section 3',
      expandOnPageLoad: false,
      shouldDisplay: (dataset) => !!dataset.stringOnLevel1,
      labelBadgeColor: 'red',
      children: [
        {
          type: 'cell',
          label: 'Cell under section 3',
          shouldDisplay: () => true,
          valueGetter: () => ({
            displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
            displayValue: '222',
          }),
        },
        {
          type: 'section',
          label: 'Subsection Alpha under section 3',
          expandOnPageLoad: false,
          shouldDisplay: () => true,
          children: [
            {
              type: 'cell',
              label: 'Cell under subsection Alpha',
              shouldDisplay: () => true,
              valueGetter: (dataset) => ({
                displayComponentName: MLDTDisplayComponentName.StringDisplayComponent,
                displayValue: dataset.stringOnLevel3,
              }),
            },
          ],
        },
      ],
    },
  ];

  const baseMetaInfo = {
    companyId: 'not-relevant-for-test',
    dataType: DataTypeEnum.Heimathafen,
    uploadTime: 111111111,
    currentlyActive: true,
    qaStatus: QaStatus.Accepted,
  };

  const dummyFrameworkTestDataAndMetaInfo1: DataAndMetaInformation<DummyFrameworkForTest> = {
    metaInfo: {
      ...baseMetaInfo,
      dataId: 'does-not-matter-abc1',
      reportingPeriod: 'Testing 1',
    },
    data: {
      stringOnLevel1: 'Dataset 1 - String 1',
      stringOnLevel2: 'Dataset 1 - String 2',
      stringOnLevel3: 'Dataset 1 - String 3',
    },
  };

  const dummyFrameworkTestDataAndMetaInfo2: DataAndMetaInformation<DummyFrameworkForTest> = {
    metaInfo: {
      ...baseMetaInfo,
      dataId: 'does-not-matter-abc2',
      reportingPeriod: 'Testing 2',
    },
    data: {
      stringOnLevel1: 'Dataset 2 - String 1',
      stringOnLevel2: 'Dataset 2 - String 2',
      stringOnLevel3: 'Dataset 2 - String 3',
    },
  };

  const dummyFrameworkTestDataAndMetaInfo3: DataAndMetaInformation<DummyFrameworkForTest> = {
    metaInfo: {
      ...baseMetaInfo,
      dataId: 'does-not-matter-abc3',
      reportingPeriod: 'Testing 3',
    },
    data: {
      stringOnLevel1: undefined,
      stringOnLevel2: 'Dataset 2 - String 2',
      stringOnLevel3: 'Dataset 2 - String 3',
    },
  };

  describe('Tests that nesting works as expected', () => {
    it("Tests that sections marked with 'expandOnPageLoad' are auto-expanded", () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
      getSectionHead('Section 1').should('have.attr', 'data-section-expanded', 'true');
      getCellValueContainer('Level 2 - String').should('be.visible');
    });

    it('Tests that sections can be expanded and contracted', () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
      getSectionHead('Section 1').should('have.attr', 'data-section-expanded', 'true');
      getCellValueContainer('Level 2 - String').should('be.visible');
      getSectionHead('Subsection 1').should('have.attr', 'data-section-expanded', 'false').should('be.visible');

      getSectionHead('Section 1').click();

      getSectionHead('Section 1').should('have.attr', 'data-section-expanded', 'false');
      getCellValueContainer('Level 2 - String', 0, false).should('not.be.visible');
      getSectionHead('Subsection 1', false).should('not.be.visible');
    });

    it('Tests that subsections can be expanded and contracted', () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
      getCellValueContainer('Level 3 - String', 0, false).should('not.be.visible');

      getSectionHead('Subsection 1').should('have.attr', 'data-section-expanded', 'false').click();
      getCellValueContainer('Level 3 - String').should('be.visible');

      getSectionHead('Subsection 1').should('have.attr', 'data-section-expanded', 'true').click();
      getCellValueContainer('Level 3 - String', 0, false).should('not.be.visible');
    });

    it('Tests that the state of subsection expansion is remembered when sections get expanded', () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
      getSectionHead('Subsection 1').should('have.attr', 'data-section-expanded', 'false').click();
      getCellValueContainer('Level 3 - String').should('be.visible');

      getSectionHead('Section 1').should('have.attr', 'data-section-expanded', 'true').click();
      getSectionHead('Subsection 1', false).should('not.be.visible');
      getCellValueContainer('Level 3 - String', 0, false).should('not.be.visible');

      getSectionHead('Section 1').should('have.attr', 'data-section-expanded', 'false').click();
      getSectionHead('Subsection 1').should('be.visible');
      getCellValueContainer('Level 3 - String').should('be.visible');
    });
  });

  describe('Tests that the shouldDisplay directive works', () => {
    it('Tests that fields and sections get hidden if shouldDisplay is false', () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo3]);
      getCellValueContainer('Level 1 - String', 0, false).should('not.exist');
      getSectionHead('Section 2').should('not.exist');
    });

    it('Tests that fields and sections should get displayed if at least one of the datasets has shouldDisplay = true', () => {
      mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1, dummyFrameworkTestDataAndMetaInfo3]);
      getCellValueContainer('Level 1 - String').should('be.visible');
      getSectionHead('Section 2').should('be.visible');
    });
  });

  it('Tests that datasets can be displayed in parallel with correct values', () => {
    mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1, dummyFrameworkTestDataAndMetaInfo2]);
    cy.get('th[data-dataset-index=0]').should('contain.text', 'Testing 1');
    cy.get('th[data-dataset-index=1]').should('contain.text', 'Testing 2');
    console.log();
    getCellValueContainer('Level 2 - String', 0).should('contain.text', 'Dataset 1 - String 2');
    getCellValueContainer('Level 2 - String', 1).should('contain.text', 'Dataset 2 - String 2');

    mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo2, dummyFrameworkTestDataAndMetaInfo1]);
    getCellValueContainer('Level 2 - String', 1).should('contain.text', 'Dataset 1 - String 2');
    getCellValueContainer('Level 2 - String', 0).should('contain.text', 'Dataset 2 - String 2');
  });

  it('Tests that header badge coloring works', () => {
    mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
    getSectionHead('Section 1').find('span.p-badge.badge-blue').should('exist');
    getSectionHead('Section 2').find('span.p-badge').should('not.exist');
  });

  it('Tests that explanation texts are shown iff they are defined', () => {
    mountMultiLayerDataTableWithDatasets([dummyFrameworkTestDataAndMetaInfo1]);
    getCellRowHeaderContainer('Level 1 - String').find('em').trigger('mouseenter', 'center');
    cy.get('.p-tooltip').should('be.visible').contains('This is a test info');
    getCellRowHeaderContainer('Level 1 - String').find('em').trigger('mouseleave');

    getCellRowHeaderContainer('Level 2 - String').find('em').should('not.exist');
  });

  it('Validate that show-if hidden fields and sections are displayed and highlighted in review mode', () => {
    cy.mountWithPlugins(MultiLayerDataTable, {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      props: {
        config: editMultiLayerDataTableConfigForHighlightingHiddenFields(dummyFrameworkMLDTConfig, true, false),
        dataAndMetaInfo: [dummyFrameworkTestDataAndMetaInfo3],
        inReviewMode: true,
      },
    });
    getCellValueContainerAndCheckIconForHiddenDisplay('Level 1 - String', true);

    getSectionHeadAndCheckIconForHiddenDisplay('Section 1', false);
    getCellValueContainerAndCheckIconForHiddenDisplay('Level 2 - String', false);

    getSectionHeadAndCheckIconForHiddenDisplay('Section 2', true).click();
    getCellValueContainerAndCheckIconForHiddenDisplay('Static Value Cell', true)
      .should('be.visible')
      .should('contain.text', 'This is static');

    getSectionHead('Section 3').click();
    getSectionHeadAndCheckIconForHiddenDisplay('Subsection Alpha under section 3', true).click();
    getCellValueContainerAndCheckIconForHiddenDisplay('Cell under subsection Alpha', true)
      .should('be.visible')
      .should('contain.text', 'Dataset 2 - String 3');
  });
});
