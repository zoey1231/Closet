require('dotenv').config();

const HttpError = require('../model/http-error');
const Clothes = require('../model/clothes');
const Outfit = require('../model/outfit');
const { generateOutfit } = require('../service/outfits-service');
const LOG = require('../utils/logger');

const getOneOutfit = async (req, res, next) => {
  let response;
  try {
    response = await generateOutfit(req);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    next(new HttpError('Failed to generate an outfit', 500));
  }

  if (!response.success) {
    const { message, warning } = response;
    return res.status(400).json({ message, warning });
  }

  res.status(200).json(response);
};

const getMultipleOutfits = async (req, res, next) => {
  // Complex Logic + Notification go here
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
};
