import axios from "axios";
import MockAdapter from "axios-mock-adapter";

import {DataStore} from "@/service/DataStore.js";

const BASE_URL = "http://dummyhost:dummyport";

describe("DataStoreTest", () => {
  let mock;
  const data = [
    {id: 1, name: "John"},
    {id: 2, name: "Andrew"},
  ];

  beforeAll(() => {
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    mock.reset();
  });

  it("should return data if everything is fine", async () => {

    mock.onGet(`${BASE_URL}/data`).reply(200, data);

    const dataStore = new DataStore(BASE_URL)
    const allData = await dataStore.getAll()

    expect(allData.data).toEqual(data);
  });

  it("should return an empty list in case of a network error", async () => {
    mock.onGet(`${BASE_URL}/data`).networkErrorOnce();

    const dataStore = new DataStore(BASE_URL)
    const allData = await dataStore.getAll()

    expect(allData).toEqual([]);
  });

  it("should return an empty list with a wrong address", async () => {
    mock.onGet("http://newdummyhost:newdummyport/data");

    const dataStore = new DataStore(BASE_URL)
    const allData = await dataStore.getAll()

    expect(allData).toEqual([]);
  });

});
