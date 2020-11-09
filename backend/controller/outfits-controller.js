const assert = require('assert');
const { validationResult } = require('express-validator');

require('dotenv').config();
const HttpError = require('../model/http-error');
const Outfit = require('../model/outfit');
const { generateOutfit } = require('../service/outfits-service');
const LOG = require('../utils/logger');

const getOneOutfit = async (req, res, next) => {
  const { userId } = req.userData;

  let response;
  try {
    response = await generateOutfit(userId);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError('Failed to generate an outfit, please try again later', 500)
    );
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

  const { userId } = req.userData;
  const messages = [];
  const warnings = [];
  const outfits = [];

  let count = 0;
  do {
    let response;
    try {
      response = await generateOutfit(userId);
    } catch (exception) {
      LOG.error(req._id, exception.message);
      return next(
        new HttpError(
          'Failed to generate an outfit, please try again later',
          500
        )
      );
    }

    const { success, message, warning, outfit } = response;

    if (message && !messages.includes(message)) {
      messages.push(message);
    }

    if (warning && !warnings.includes(warning)) {
      warnings.push(warning);
    }

    if (success) {
      // Avoid duplicate outfits
      const index = outfits.findIndex(o => o._id === outfit._id);
      if (index === -1) {
        outfits.push(outfit);
      }
    }

    count++;
  } while (count !== MULTIPLE_OUTFITS_LIMIT);

  res.status(200).json({ messages, warnings, outfits });
};

const updateUserOpinion = async (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return next(
      new HttpError('Invalid inputs passed, please check your data', 422)
    );
  }

  const { userId } = req.userData;
  const { outfitId } = req.params;
  const { opinion } = req.body;

  if (!outfitId) {
    return next(
      new HttpError('Missing parameters, please check your request', 400)
    );
  }

  let updatedOutfit;
  try {
    updatedOutfit = await Outfit.findOneAndUpdate(
      { _id: outfitId, user: userId },
      { opinion },
      { new: true }
    );

    assert(updatedOutfit.opinion === opinion);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError(
        'Failed to change user opinion of the outfit, please try again later',
        500
      )
    );
  }

  res.status(200).json({
    message: 'Updated user opinion successfully!',
    updatedOutfit,
  });
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
  updateUserOpinion,
};
