require('dotenv').config();

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');
const Clothes = require('../model/clothes');
const Outfit = require('../model/outfit');

const getOneOutfit = async (req, res, next) => {
  const userId = req.userData.userId;

  const events = parseInt(req.query.events) || 0;

  let savedClothes; // array
  try {
    savedClothes = await Clothes.find({ user: userId });
  } catch (exception) {
    LOG.error(exception);
    next(new HttpError('Failed getting outfit: failed getting clothes', 500));
  }

  // won't generate an outfit if less than 3 clothes
  if (savedClothes.length <= 3) {
    // TODO:
    // - generate hash and check database
    // - save into database if not present
    return res.status.json({ message: savedClothes }).end();
  }

  const currentSeason = getSeasonNorth();

  res.status(200).json(exampleBody).end();
};

const getMultipleOutfits = async (req, res, next) => {
  // Complex Logic + Notification go here
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
};
