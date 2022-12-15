FROM node:gallium-alpine3.17 AS builder

RUN mkdir /app

COPY . /app

WORKDIR /app

RUN npm install

CMD ["node", "main.js"]