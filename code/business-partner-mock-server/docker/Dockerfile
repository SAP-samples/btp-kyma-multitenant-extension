FROM node:latest

EXPOSE 8081

COPY package.json .
RUN npm install

COPY server.js .
COPY BusinessPartner-sample.json .

CMD ["node", "server.js"]

