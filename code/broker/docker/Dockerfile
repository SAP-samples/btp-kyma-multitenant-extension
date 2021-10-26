FROM node:14

EXPOSE 3002

COPY package.json .
RUN npm install

COPY server.js .
COPY createApiRule.js .

CMD ["node", "server.js"]

