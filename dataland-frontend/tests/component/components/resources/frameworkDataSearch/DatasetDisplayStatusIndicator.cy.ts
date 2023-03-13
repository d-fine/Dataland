import DatasetDisplayStatusIndicator from "@/components/resources/frameworkDataSearch/DatasetDisplayStatusIndicator.vue";
import { DataMetaInformation } from "@clients/backend";
import { DataTypeEnum, QAStatus } from "@clients/backend";
describe("Component Tests for DatasetStatusIndicator", () => {
  const acceptedAndActiveDataset: DataMetaInformation = {
    dataId: "mock-data-id",
    companyId: "mock-company-id",
    dataType: DataTypeEnum.Lksg,
    qaStatus: QAStatus.Accepted,
    currentlyActive: true,
    reportingPeriod: "mock-reporting-period",
    uploaderUserId: "mock-uploader-user-id",
    uploadTime: 1672527600000, // 1.1.2023 00:00:00:0000
  };

  it("Should display a outdated warning message when the dataset is outdated", () => {
    const outdatedDataset = structuredClone(acceptedAndActiveDataset) as DataMetaInformation;
    outdatedDataset.currentlyActive = false;
    outdatedDataset.qaStatus = QAStatus.Accepted;

    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: outdatedDataset,
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").contains("outdated").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").contains("View Active").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("have.attr", "href",
        `/companies/${outdatedDataset.companyId}/frameworks/${DataTypeEnum.Lksg}/reportingPeriods/${outdatedDataset.reportingPeriod}`);
  });

  it("Should display a QA-Pending warning message when the dataset is pending QA", () => {
    const datasetPendingReview = structuredClone(acceptedAndActiveDataset) as DataMetaInformation;
    datasetPendingReview.currentlyActive = false;
    datasetPendingReview.qaStatus = QAStatus.Pending;

    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: datasetPendingReview,
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").contains("pending").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("not.exist");
  });

  it("Should display a show all message when only one active dataset is viewed when multiview and multiple are available", () => {
    const otherReportingPeriod = structuredClone(acceptedAndActiveDataset) as DataMetaInformation;
    otherReportingPeriod.reportingPeriod = "other-reporting-period";
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: acceptedAndActiveDataset,
        isMultiview: true,
        receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
          [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
          [otherReportingPeriod.reportingPeriod, otherReportingPeriod],
        ]),
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").contains("single").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").contains("View All").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("have.attr", "href",
        `/companies/${acceptedAndActiveDataset.companyId}/frameworks/${DataTypeEnum.Lksg}`);
  });

  it("Should not display anything when only one active dataset is viewed when singleview and multiple are available", () => {
    const otherReportingPeriod = structuredClone(acceptedAndActiveDataset) as DataMetaInformation;
    otherReportingPeriod.reportingPeriod = "other-reporting-period";
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: acceptedAndActiveDataset,
        receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
          [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
          [otherReportingPeriod.reportingPeriod, otherReportingPeriod],
        ]),
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").should("not.exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("not.exist");
  });

  it("Should not display anything when only one active dataset is viewed when multiview and no other active are available", () => {
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: acceptedAndActiveDataset,
        receivedMapOfReportingPeriodsToActiveDataMetaInfo: new Map<string, DataMetaInformation>([
          [acceptedAndActiveDataset.reportingPeriod, acceptedAndActiveDataset],
        ]),
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").should("not.exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("not.exist");
  });
});
