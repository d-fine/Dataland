import os
import datetime
import logging
import urllib.request
import base64
import json


_keycloak_base_url = "http://keycloak:8080/keycloak"

_client_id = "dataland-automated-qa-service"
_client_secret = os.environ["DATALAND_AUTOMATED_QA_SERVICE_CLIENT_SECRET"]

_lifetime_threshold_in_seconds = 30

_current_access_token: str | None = None
_current_access_token_expire_time: datetime.datetime | None = None


def get_access_token() -> str | None:
    """
    :returns: a valid access token which is guaranteed to be valid for at least 30 seconds
    """
    if _current_access_token is None or _is_close_to_invalidation():
        _update_token()
    return _current_access_token


def _is_close_to_invalidation() -> bool:
    if _current_access_token_expire_time is None:
        return True
    time_delta = _current_access_token_expire_time - datetime.datetime.now()
    return time_delta.total_seconds() < _lifetime_threshold_in_seconds


def _update_token() -> None:
    logging.info("Updating Keycloak Access Token.")
    credentials = f"{_client_id}:{_client_secret}"
    encoded_credentials = credentials.encode("utf-8")
    authorization_header = base64.b64encode(encoded_credentials)

    request = urllib.request.Request(
        method="POST",
        url=f"{_keycloak_base_url}/realms/datalandsecurity/protocol/openid-connect/token",
        headers={
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": f"Basic {authorization_header.decode('utf-8')}",
        },
        data="grant_type=client_credentials".encode("utf-8"),
    )
    response = urllib.request.urlopen(request)
    parsed_response_body = json.loads(response.read())
    global _current_access_token, _current_access_token_expire_time  # noqa: PLW0603
    _current_access_token = parsed_response_body["access_token"]
    _current_access_token_expire_time = datetime.datetime.now() + datetime.timedelta(
        seconds=parsed_response_body["expires_in"]
    )
    logging.info("Acquired new access token!")
