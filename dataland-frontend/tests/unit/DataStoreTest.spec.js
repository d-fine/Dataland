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

  beforeAll(() => {
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    mock.reset();
  });

  it("should return data if everything is fine", async () => {

    mock.onGet(`${BASE_URL}/data`).reply(200, data);

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getAll()

    expect(receivedData.data).toEqual(data);
  });

  it("should return an empty list in case of a network error", async () => {
    mock.onGet(`${BASE_URL}/data`).networkErrorOnce();

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getAll()

    expect(receivedData).toEqual([]);
  });

  it("should return an empty list with a wrong address", async () => {
    mock.onGet("https://newdummyhost:newdummyport/data");

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getAll()

    expect(receivedData).toEqual([]);
  });

  it("should return data if everything is fine", async () => {

    mock.onGet(`${BASE_URL}/data`).reply(200, data);

    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getAll()

    expect(receivedData.data).toEqual(data);
  });

  it("should return data for the given id", async () => {

    const dataStore = new DataStore(BASE_URL)

    for (const d of data) {
      mock.onGet(`${BASE_URL}/data/${d.id}`).reply(200, d);
    }

    const receivedData = await dataStore.getById(0)
    expect(receivedData.data).toEqual(data[0]);

  });

  it("should return an empty list if the id does not exist", async () => {

    for (const d of data) {
      mock.onGet(`${BASE_URL}/data/${d.id}`).reply(200, d);
    }
    const dataStore = new DataStore(BASE_URL)
    const receivedData = await dataStore.getById(-1)

    expect(receivedData).toEqual([]);
  });
});
