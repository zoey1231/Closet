const assert = require('assert');
const { validationResult } = require('express-validator');

require('dotenv').config();
const HttpError = require('../model/http-error');
const Outfit = require('../model/outfit');
const Clothes = require('../model/clothes');
const { generateOutfit } = require('../service/outfits-service');
const { hashCode } = require('../utils/hash');
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
    const { message, manual, warning } = response;
    return res.status(400).json({ message, manual, warning });
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

const createOneOutfit = async (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return next(
      new HttpError('Invalid inputs passed, please check your data', 422)
    );
  }

  const { userId } = req.userData;
  const { clothes, occasions, seasons } = req.body;

  // Check the number of given clothes
  if (clothes.length !== 3) {
    return next(
      new HttpError(
        'An outfit should consist of three clothes, please check your selections',
        400
      )
    );
  }

  // Validate all given clothes
  const upperClothesId = clothes[0];
  const trousersId = clothes[1];
  const shoesId = clothes[2];
  let chosenUpperClothes;
  let chosenTrousers;
  let chosenShoes;

  try {
    chosenUpperClothes = await Clothes.findById(upperClothesId);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError('Invalid upper clothes id, please check your clothes', 404)
    );
  }

  try {
    chosenTrousers = await Clothes.findById(trousersId);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError('Invalid trousers id, please check your clothes', 404)
    );
  }

  try {
    chosenShoes = await Clothes.findById(shoesId);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError('Invalid shoes id, please check your clothes', 404)
    );
  }

  // Avoid duplicated outfits
  const _id = hashCode(upperClothesId, trousersId, shoesId);

  let existingOutfit;

  try {
    existingOutfit = await Outfit.findOneAndUpdate(
      { _id, user: userId },
      { opinion: 'like' },
      { new: true }
    );
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(
      new HttpError('Failed when checking outfits, please try again later', 500)
    );
  }

  let created, opinion;
  if (existingOutfit) {
    // Case for outfit has been created
    created = existingOutfit.created;
    opinion = existingOutfit.opinion;
  } else {
    // Case for outfit not been created
    const newOutfit = new Outfit({
      _id,
      clothes,
      occasions,
      seasons,
      opinion: 'like',
      user: userId,
      created: new Date().setTime(
        new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000
      ),
    });

    try {
      await newOutfit.save();
    } catch (exception) {
      LOG.error(exception.message);
      return next(
        new HttpError('Failed to create outfit, please try again later', 500)
      );
    }

    created = newOutfit.created;
    opinion = newOutfit.opinion;
  }

  res.status(201).json({
    success: true,
    message: 'Outfit created successfully!',
    outfit: {
      _id,
      clothes,
      created,
      occasions,
      seasons,
      opinion,
      user: userId,
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
    },
  });
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
  updateUserOpinion,
  createOneOutfit,
};
