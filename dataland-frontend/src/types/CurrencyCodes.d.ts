declare module "currency-codes/data" {
  interface CurrencyCodeEntry {
    code: string;
    currency: string;
  }

  const data: Array<CurrencyCodeEntry>;
  export default data;
}
