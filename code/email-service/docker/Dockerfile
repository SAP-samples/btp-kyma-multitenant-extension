FROM node:latest

EXPOSE 3002

COPY package.json .
RUN npm install

COPY server.js .

CMD ["node", "server.js"]

