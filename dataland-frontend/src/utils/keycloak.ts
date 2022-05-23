import axios, {AxiosResponse} from "axios";
const qs = require('qs');
const data = qs.stringify({
    'grant_type': 'password',
    'client_id': 'dataland-frontend',
    'client_secret': 'ihNr3Dp4RozH3TvAAAivOWy7G9HIqv4g',
    'username': 'myuser',
    'password': '123456'
});
const config = {
    method: 'post',
    url: 'http://localhost:8095/realms/myrealm/protocol/openid-connect/token',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
    },
    data : data
};

axios(config)
    .then(function (response:AxiosResponse) {
        console.log(JSON.stringify(response.data));
    })
    .catch(function (error:Error) {
        console.log(error);
    });