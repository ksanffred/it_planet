FROM node:22-alpine AS build
WORKDIR /app

COPY package.json .yarnrc.yml yarn.lock* ./
# HUSKY=0 prevents postinstall from running git hooks inside Docker
RUN HUSKY=0 npm install -g yarn && yarn install --ignore-scripts
RUN yarn nuxt prepare

COPY . .
RUN yarn build

FROM node:22-alpine
WORKDIR /app
COPY --from=build /app/.output .output
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node", ".output/server/index.mjs"]
