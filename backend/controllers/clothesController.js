const clothesCountrol = require("express").Router();

clothesCountrol.get("/", async (req, res) => {
  res.json("HELLO!");
});

module.exports = clothesCountrol;
