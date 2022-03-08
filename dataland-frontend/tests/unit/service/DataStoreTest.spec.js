import axios from "axios";
import MockAdapter from "axios-mock-adapter";

import {DataStore} from "@/service/DataStore.js";

const BASE_URL = "https://dummyhost:dummyport";

describe("DataStoreTest", () => {
  let mock;
  const data = [
    {id: 0, name: "John"},
    {id: 1, name: "Andrew"},
  ];
  const code = "DEU";
  const name = "BMW";

  beforeAll(() => {
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    mock.reset();
  });

  it("should return data if everything is fine", async () => {

    mock.onGet(`${BASE_URL}/data/skyminder/${code}/${name}`).reply(200, data);

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toEqual(data);
    expect(receivedData.length).toEqual(data.length);
  });

  it("should return an empty list in case of a network error", async () => {
    mock.onGet(`${BASE_URL}/data/skyminder/${code}/${name}`).networkErrorOnce();

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toBeNull();
  });

  it("should return an empty list with a wrong address", async () => {
    mock.onGet("https://newdummyhost:newdummyport/data");

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getByName(code, name)

    expect(receivedData).toBeNull();
  });


});
