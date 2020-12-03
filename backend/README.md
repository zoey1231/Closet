# Set-up
**Requirements**
- NodeJS 14
- MongoDB 4

Install
```sh
npm install
npm ci        # for merely running backend
mongod        # mongo server running
```

Copy environment variables
```sh
cp .env.sample .env
```

# Running
**npm**
```sh
npm start                   # start server (port 80)
npm run dev                 # run development server (port 8080)

npm test                    # run all tests (also generate coverage)
npm run test:users          # run user component related tests
npm run test:outfits        # run outfit component related tests
npm run test:closet         # run intergation tests
```
