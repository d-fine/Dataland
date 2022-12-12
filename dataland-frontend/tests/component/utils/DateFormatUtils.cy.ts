import { calculateDaysFromNow, formatExpiryDate } from "@/utils/DateFormatUtils";

describe("Unit test for Date Formatting", () => {
  const Dec12_2021 = 1639284700000;
  const Dec12_2022 = 1670820700000;
  const Dec6_2021 = 1670302300000;
  const Dec11_2022 = 1670777487587;
  it("It should return the number of days to the given date", () => {
    expect(calculateDaysFromNow(Dec12_2022, Dec12_2021)).to.eql(365);
    expect(calculateDaysFromNow(Dec12_2022, Dec6_2021)).to.eql(6);
    expect(calculateDaysFromNow(Dec11_2022, Dec11_2022)).to.eql(0);
  });

  it("It should return a properly formatted date string", () => {
    expect(formatExpiryDate(365, Dec12_2021)).to.eql("Mon, Dec 12, 2022");
    expect(formatExpiryDate(0, Dec11_2022)).to.eql("Sun, Dec 11, 2022");
    expect(formatExpiryDate(6, Dec6_2021)).to.eql("Mon, Dec 12, 2022");
  });
});
