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

  it("Should not display anything if the dataset is active", () => {
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: acceptedAndActiveDataset,
      });
    });

    cy.get("div[data-test=datasetDisplayStatusContainer]").should("not.exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("not.exist");
  });

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
    cy.get("a[data-test=datasetDisplayStatusLink]").should("not.exist");
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
  it("Should display a button with a link to an active dataset if one is provided", () => {
    const mockActiveDatasetLink = "/url-to-active-dataset";
    const outdatedDataset = structuredClone(acceptedAndActiveDataset) as DataMetaInformation;
    outdatedDataset.currentlyActive = false;
    outdatedDataset.qaStatus = QAStatus.Accepted;
    cy.mountWithPlugins(DatasetDisplayStatusIndicator, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        displayedDataset: outdatedDataset,
        linkToActivePage: mockActiveDatasetLink,
      });
    });
    cy.get("div[data-test=datasetDisplayStatusContainer]").contains("outdated").should("exist");
    cy.get("a[data-test=datasetDisplayStatusLink]").should("have.attr", "href", mockActiveDatasetLink);
  });
});
