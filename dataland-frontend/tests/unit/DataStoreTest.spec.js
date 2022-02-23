import axios from "axios";
import MockAdapter from "axios-mock-adapter";

import {DataStore} from "@/service/DataStore.js";

const BASE_URL = "https://dummy.com";

describe("DataStore", () => {
  let mock;

  beforeAll(() => {
    mock = new MockAdapter(axios);
  });

  afterEach(() => {
    mock.reset();
  });

  it("should return users list", async () => {
    // given
    const data = [
      {id: 1, name: "John"},
      {id: 2, name: "Andrew"},
    ];
    mock.onGet(`${BASE_URL}/data`).reply(200, data);

    // when
    const dataStore = new DataStore(BASE_URL)
    const allData = await dataStore.getAll()

    expect(allData.data).toEqual(data);
  });
});
