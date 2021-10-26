FROM node:14

WORKDIR /app

COPY ./package.json .
RUN npm install

COPY ./approuter-start.js .
COPY ./xs-app.json .

EXPOSE 5000

# Belos is using approuter as standalone application
# CMD ["npm", "start"]

# Below is used to start customized approuter, see approuter-start.js for more details
CMD ["node", "approuter-start.js"]
