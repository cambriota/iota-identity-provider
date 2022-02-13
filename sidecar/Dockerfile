FROM node:lts-alpine
ENV NODE_ENV=production

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY . .

EXPOSE 7000

CMD ["npm", "run", "start"]
