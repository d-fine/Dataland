import { calculateDaysFromNow, formatExpiryDate } from "@/utils/DateFormatUtils";

describe("Unit test for Date Formatting", () => {
  it("It should return the number of days to the given date", () => {
    // startDate: 1639263600000 - 12 Dec 2021
    // toDate: 1670777487587 - 11 Dec 2022
    expect(calculateDaysFromNow(1670777487587, 1639263600000)).to.eql(365);
    // startDate: 1670777487587 - 11 Dec 2022
    // toDate: 1670777487587 - 11 Dec 2022
    expect(calculateDaysFromNow(1670777487587, 1670777487587)).to.eql(0);
    // startDate: 1670281200000 - 6 Dec 2022
    // toDate: 1670777487587 - 11 Dec 2022
    expect(calculateDaysFromNow(1670777487587, 1670281200000)).to.eql(6);
  });

  it("It should return the number of days to the given date", () => {
    // we add 2.7h to avoid counting from midnight
    // startDate: 1639263600000 - 12 Dec 2021
    expect(formatExpiryDate(365, 1639263600000 + 21100000)).to.eql("Mon, Dec 12, 2022");
    // startDate: 1670777487587 - 11 Dec 2022
    expect(formatExpiryDate(0, 1670777487587 + 21100000)).to.eql("Sun, Dec 11, 2022");
    // startDate: 1670281200000 - 6 Dec 2022
    expect(formatExpiryDate(6, 1670281200000 + 21100000)).to.eql("Mon, Dec 12, 2022");
  });
});
