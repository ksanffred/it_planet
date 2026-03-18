# Nuxt Minimal Starter

Look at the [Nuxt documentation](https://nuxt.com/docs/getting-started/introduction) to learn more.

## Used Technologies

- Nuxt 4
- Nitro 2
- Vue 3
- Vite 7
- Yarn 4
- Vue Router 5
- Husky 9
- Oxlint
- Oxfmt
- Typescript 5
- Vitetest 4
- Nuxt/test-utils 4
- Scss

## Setup

Make sure to install dependencies:

```bash
# npm
npm install

# pnpm
pnpm install

# yarn
yarn install

# bun
bun install
```

## Development Server

Start the development server on `http://localhost:3000`:

```bash
# npm
npm run dev

# pnpm
pnpm dev

# yarn
yarn dev

# bun
bun run dev
```

## Production

Build the application for production:

```bash
# npm
npm run build

# pnpm
pnpm build

# yarn
yarn build

# bun
bun run build
```

Locally preview production build:

```bash
# npm
npm run preview

# pnpm
pnpm preview

# yarn
yarn preview

# bun
bun run preview
```

Check out the [deployment documentation](https://nuxt.com/docs/getting-started/deployment) for more information.

## Launch with docker

Build and run docker image for production

```bash
# build docker image
docker build -t tramplin-frontend .

# launch docker container
docker run -d -p 3000:80 --rm tramplin-frontend
```

## Testing

Test application with vitest and nuxt/test-utils

```bash
# Launch all tests
yarn test

# Launch e2e testing
yarn test:e2e

# Launch unit testing
yarn test:unit

# Launch nuxt runtime testing
yarn test:nuxt
```

## Versioning

The project version is determines by the date of the last commit
