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
            return {
                status: res.status + "-" + res.statusText,
                headers: res.headers,
                data: res.data,
            };
        } catch (err) {
            console.log(err)
            return [];
        }
    }

    async getById(id) {
        if (id) {
            try {
                const res = await this.axios.get(`/data/${id}`);
                // const result = {
                //   data: res.data,
                //   status: res.status,
                //   statusText: res.statusText,
                //   headers: res.headers,
                //   config: res.config,
                // };
                return {
                    status: res.status + "-" + res.statusText,
                    headers: res.headers,
                    data: res.data,
                };
            } catch (err) {
                console.log(err)
                return [];
            }
        }
    }
}

