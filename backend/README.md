# Set-up
**Requirements**
- NodeJS 14
- MongoDB 4
- Redis 6

Install
```
npm install
mongod        # mongo server running
redis-server  # redis server running
```

Copy environment variables
```
cp .env.sample .env
```

**Optional**
- Docker

# Running
**npm**
```
npm run dev     # run development
npm run test    # run test
npm start       # start server
```

**Docker**
```
docker-compose build  # build image
docker-compose up     # run image
docker-compose down   # stop image
```

```
docker-compose run node npm run test # run all tests
```


