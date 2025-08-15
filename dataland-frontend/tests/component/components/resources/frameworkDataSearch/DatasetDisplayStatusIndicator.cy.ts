import DatasetDisplayStatusIndicator from '@/components/resources/frameworkDataSearch/DatasetDisplayStatusIndicator.vue';
import { DataTypeEnum, QaStatus, type DataMetaInformation } from '@clients/backend';
import router from '@/router';
describe('Component Tests for DatasetDisplayStatusIndicator', () => {
  const acceptedAndActiveDataset: DataMetaInformation = {
    dataId: 'mock-data-id',
    companyId: 'mock-company-id',
    dataType: DataTypeEnum.Lksg,
    qaStatus: QaStatus.Accepted,
    currentlyActive: true,
    reportingPeriod: 'mock-reporting-period',
    uploaderUserId: 'mock-uploader-user-id',
    uploadTime: 1672527600000, // 1.1.2023 00:00:00:0000
  };

  let routerPushMock: Cypress.Agent<sinon.SinonSpy>;

  beforeEach(() => {
    routerPushMock = cy.stub();
    cy.stub(router, 'push').callsFake(routerPushMock);
  })

  it('Should display a superseded warning message when the dataset is superseded', () => {
    const supersededDataset = structuredClone(acceptedAndActiveDataset);
    supersededDataset.currentlyActive = false;
    supersededDataset.qaStatus = QaStatus.Accepted;
    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: supersededDataset,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
            [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
          ]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').contains('superseded').should('exist');
    cy.get('[data-test=datasetDisplayStatusLink]').contains('VIEW ACTIVE').click()
      .then(() => {
        expect(routerPushMock).to.have.been.calledWith(
          `/companies/${supersededDataset.companyId}/frameworks/${DataTypeEnum.Lksg}/reportingPeriods/${supersededDataset.reportingPeriod}`
        );
      });
  });

  it('Should display a QA-Pending warning message when the dataset is pending QA', () => {
    const datasetPendingReview = structuredClone(acceptedAndActiveDataset);
    datasetPendingReview.currentlyActive = false;
    datasetPendingReview.qaStatus = QaStatus.Pending;

    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: datasetPendingReview,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>(),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').contains('pending').should('exist');
    cy.get('[data-test=datasetDisplayStatusLink]').should('not.exist');
  });

  it('Should display a show all message when only one active dataset is viewed when multiview and multiple are available', () => {
    const otherReportingPeriod = structuredClone(acceptedAndActiveDataset);
    otherReportingPeriod.reportingPeriod = 'other-reporting-period';
    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: acceptedAndActiveDataset,
          isMultiview: true,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
            [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
            [otherReportingPeriod.reportingPeriod, otherReportingPeriod],
          ]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').contains('single').should('exist');
    cy.get('[data-test=datasetDisplayStatusLink]').contains('VIEW ALL').click()
      .then(() => {
        expect(routerPushMock).to.have.been.calledWith(
          `/companies/${acceptedAndActiveDataset.companyId}/frameworks/${DataTypeEnum.Lksg}`
        );
      });
  });

  it('Should not display anything when only one active dataset is viewed when singleview and multiple are available', () => {
    const otherReportingPeriod = structuredClone(acceptedAndActiveDataset);
    otherReportingPeriod.reportingPeriod = 'other-reporting-period';
    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: acceptedAndActiveDataset,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
            [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
            [otherReportingPeriod.reportingPeriod, otherReportingPeriod],
          ]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').should('not.exist');
    cy.get('[data-test=datasetDisplayStatusLink]').should('not.exist');
  });

  it('Should not display anything when only one active dataset is viewed when multiview and no other active are available', () => {
    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: acceptedAndActiveDataset,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
            [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
          ]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').should('not.exist');
    cy.get('[data-test=datasetDisplayStatusLink]').should('not.exist');
  });

  it('Should display button if rejected dataset is displayed and accepted one exists', () => {
    const rejectedDataset = structuredClone(acceptedAndActiveDataset);
    rejectedDataset.currentlyActive = false;
    rejectedDataset.qaStatus = QaStatus.Rejected;

    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: rejectedDataset,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
            [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
          ]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').should('contain.text', 'rejected');
    cy.get('[data-test=datasetDisplayStatusLink]').should('exist');
  });

  it('Should not display button if only a rejected dataset exists', () => {
    const rejectedDataset = structuredClone(acceptedAndActiveDataset);
    rejectedDataset.currentlyActive = false;
    rejectedDataset.qaStatus = QaStatus.Rejected;

    //@ts-ignore
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {
      data() {
        return {
          displayedDataset: rejectedDataset,
          receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([]),
        };
      },
    });

    cy.get('div[data-test=datasetDisplayStatusContainer]').should('contain.text', 'rejected');
    cy.get('[data-test=datasetDisplayStatusLink]').should('not.exist');
  });
});
