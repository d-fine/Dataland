FROM python:3.11.7

WORKDIR /usr/src/app

COPY ./dataland-automated-qa-service/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY ./dataland-automated-qa-service/src/ ./

ENTRYPOINT python ./entrypoint.py
