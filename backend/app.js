const config = require("./utils/config");
const logger = require("./utils/logger");

const cors = require("cors");

const express = require("express");
const app = express();
const morgan = require('morgan');

const mongoose = require("mongoose");
const redis = require("redis");

// routers
// const helloRouter = require("./controllers/hello");

// connect to db
logger.info("⌛connecting to", config.MONGODB_URI);

mongoose
  .connect(config.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => {
    logger.info("✅connected to MongoDB");
  })
  .catch((error) => {
    logger.error("❌error connecting to MongoDB:", error.message);
  });

// connect to redis
logger.info("⌛connecting to", config.REDIS_URI);

const redisClient = redis.createClient({
  url: config.REDIS_URI
})

redisClient.on('connect', async () => {
  logger.error("✅connected to Redis");
  redisClient.flushall(); // TODO: comment this out!
})

redisClient.on('error', (error) => {
  redisClient.quit();
  logger.error("❌error connecting redis:", error);
})

// redisClient.set('version', config.VERSION, redis.print);
// redisClient.get('version', redis.print);

// app setting
app.use(cors());
app.use(express.json());
app.use(morgan('tiny'));


// api
// app.use("/hello", helloRouter);
app.get("/version", (req, res) => {
  res.status(200).json(config.VERSION);
})

module.exports = { app, redisClient };
