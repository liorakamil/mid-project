FROM alpine:latest

RUN apk update && \
    apk add  python3 

COPY ./requirements.txt /app/requirements.txt

WORKDIR /app

RUN pip3 install -r requirements.txt

COPY . /app

ENTRYPOINT ["python3"]

CMD ["app.py"]