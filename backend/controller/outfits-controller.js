require('dotenv').config();

const HttpError = require('../model/http-error');
const Outfit = require('../model/outfit');
const { generateOutfit } = require('../service/outfits-service');
const LOG = require('../utils/logger');

const getOneOutfit = async (req, res, next) => {
  const userId = req.userData.userId;

  let response;
  try {
    response = await generateOutfit(userId);
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
  const MULTIPLE_OUTFITS_LIMIT = parseInt(
    process.env.MULTIPLE_OUTFITS_LIMIT,
    10
  );

  const userId = req.userData.userId;
  const messages = [];
  const warnings = [];
  const outfits = [];

  let count = 0;
  do {
    // TODO
    let response;
    try {
      response = await generateOutfit(userId);
    } catch (exception) {
      LOG.error(req._id, exception.message);
      next(new HttpError('Failed to generate an outfit', 500));
    }
    count++;
  } while (count !== MULTIPLE_OUTFITS_LIMIT);

  res.status(200).json({ messages, warnings, outfits });
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
};
