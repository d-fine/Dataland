export type TypeOfProductionSitesNames = {
  name: string;
  isInHouseProductionOrIsContractProcessing: string;
  address: string;
  listOfGoodsOrServices: [];
};

export type TypeOfProductionSitesConvertedNames = {
  name: string;
  isInHouseProductionOrIsContractProcessing: string;
  country: string;
  city: string;
  streetAndHouseNumber: string;
  postalCode: string;
  listOfGoodsOrServices: string;
};
