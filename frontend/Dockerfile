FROM node:22-alpine AS build
WORKDIR /app

RUN corepack enable

COPY package.json .yarnrc.yml yarn.lock* ./

RUN corepack install

RUN yarn 

COPY . .
RUN yarn nuxt prepare
RUN yarn build

FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/.output .output
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
