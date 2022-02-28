import axios from "axios";

export class DataStore {
    constructor(baseUrl) {
        this.axios = axios.create({
            baseURL: baseUrl,
            headers: {
                "Content-type": "application/json"
            }
        })
    }

    async getAll() {
        try {
            const results = await this.axios.get("/data");
            const data = results.data
            console.log(results.status + "-" + results.statusText)
            // ToDo: return directly the data
            return data;
        } catch (err) {
            const data = {
                id: null,
                name: null,
                payload: null
            }
            console.error(err)
            return [data];
        }
    }

    async getById(id)
    {
        try {
            const results = await this.axios.get(`/data/${id}`);
            const data = {
                id: results.data.id,
                name: results.data.name,
                payload: results.data.payload
            }
            console.log(results.status + "-" + results.statusText)
            return data;
        } catch (err) {
            const data = {
                id: null,
                name: null,
                payload: null
            }
            console.error(err)
            return data;
        }
    }
}

