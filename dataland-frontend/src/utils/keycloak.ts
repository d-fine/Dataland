import axios, {AxiosResponse} from "axios";
import Keycloak from "keycloak-js";
const qs = require('qs');

function updateToken(refreshToken: string): void {
    const refresh_data = qs.stringify({
        'grant_type': 'refresh_token',
        'client_id': 'dataland-frontend',
        'refresh_token ': refreshToken
    });
    setInterval(() =>
        axios({
                method: 'post',
                url: 'http://localhost:8095/realms/myrealm/protocol/openid-connect/token',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                data: refresh_data
            }
        ).then(function (response: AxiosResponse) {
            console.log(JSON.stringify(response.data));
        })
            .catch(function (error: Error) {
                console.log(error);
            }), 6000)
}

function keycloack_activate(access_token:string, refresh_token:string){
    const initOptions = {
        url: 'http://localhost:8095/', realm: 'myrealm', clientId: 'dataland-frontend'
    }
    const keycloak = new Keycloak(initOptions)
    keycloak.init({checkLoginIframe: false,
        token: access_token, refreshToken: refresh_token}).then((auth) => {
        if (auth) {
            console.log("Authenticated");
            alert("Authenticated")
        } else {
            console.log("Not Authenticated");

        }
        if (keycloak.token) {
            window.sessionStorage.setItem('keycloakToken', keycloak.token)
        }
//Token Refresh
        setInterval(() => {
            keycloak.updateToken(70).then((refreshed) => {
                if (refreshed) {
                    console.log('Token refreshed' + refreshed);
                } else {
                    console.warn('Token not refreshed, valid for ')
                }
            }).catch(() => {
                console.error('Failed to refresh token');
            });
        }, 6000)

    }).catch(() => {
        console.error("Authenticated Failed");
    });
}


export function authenticate() {
    const data = qs.stringify({
        'grant_type': 'password',
        'client_id': 'dataland-frontend',
        'username': 'myuser',
        'password': '123456'
    });
    const config = {
        method: 'post',
        url: 'http://localhost:8095/realms/myrealm/protocol/openid-connect/token',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        data: data
    };
    axios(config)
        .then(function (response: AxiosResponse) {
            console.log(JSON.stringify(response.data));
            keycloack_activate(response.data.access_token, response.data.refresh_token)
        })
        .catch(function (error: Error) {
            console.log(error);
        })
}

