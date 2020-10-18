const clothesRouter = require("express").Router();

clothesRouter.get("/", async (req, res) => {
  res.json("HELLO!");
});

module.exports = clothesRouter;
