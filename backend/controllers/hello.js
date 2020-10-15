const helloRouter = require("express").Router();

helloRouter.get("/", async (req, res) => {
  res.json("HELLO!");
});

helloRouter.get("/time", async (req, res) => {
  res.json((new Date).toUTCString());
});

module.exports = helloRouter;