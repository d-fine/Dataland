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
            const res = await this.axios.get("/data");
            const result = {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                data: res.data,
            };
            return result;
        } catch (err) {
            return [];
        }
    }
}