import CompanyDataTable from "@/components/general/CompanyDataTable.vue";
import { shallowMount } from "@vue/test-utils";

describe("Component test for CompanyDataTable", () => {
  const wrapper = shallowMount(CompanyDataTable);

  it("Check that the initial values are correct", () => {
    expect(wrapper.vm.kpiDataObjects).to.be.an("array").that.is.empty;
    expect(wrapper.vm.DataDateOfDataSets).to.be.an("array").that.is.empty;
    expect(wrapper.vm.kpiNameMappings).to.be.an("object").that.is.empty;
    expect(wrapper.vm.kpiInfoMappings).to.be.an("object").that.is.empty;
    expect(wrapper.vm.subAreaNameMappings).to.be.an("object").that.is.empty;
    expect(wrapper.vm.tableDataTitle).to.be.a("string").that.is.empty;
  });
});
