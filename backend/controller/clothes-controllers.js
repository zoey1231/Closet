require('dotenv').config();

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');

const Clothes = require('../model/clothes');
const User = require('../model/user');

/**
 * Get all clothes for a user
 * - userId
 */
const getClothes = async (req, res, next) => {
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  try {
    const savedClothes = await Clothes.find({ user: userId });
    res.status(200).json(savedClothes);
  } catch (exception) {
    LOG.error(exception);
    next(new HttpError('Failed getting clothes', 500));
  }
};

/**
 * Get one clothing for one user
 * - userId
 * - clothingId
 */
const getClothing = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  try {
    const savedClothing = await Clothes.findById(clothingId);
    res.status(200).json(savedClothing);
  } catch (exception) {
    LOG.error(exception);
    next(new HttpError('Failed getting clothes', 500));
  }
};

/**
 * Add one clothing
 * - userId
 */
const postClothing = async (req, res, next) => {
  const body = req.body; // required(category, color, seasons, occasion) optional: (name) toBeFilled: (image_url, user)
  const userId = req.params.userId;

  // ===== validate token =====
  if (req.userData.userId == null || req.userData.userId != userId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  // ===== validate request body =====
  if (Object.keys(body).length === 0 || !userId) {
    return next(new HttpError('Missing parameters', 400));
  }

  let { category, color, seasons, occasions } = body;

  if (!category || !color || !seasons || !occasions) {
    return next(new HttpError('Missing clothes values', 400));
  }

  if (!Array.isArray(occasions)) {
    return next(
      new HttpError(`Invalid occasions: ${occasions}; should be an array`, 400)
    );
  }

  const seasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
  if (!Array.isArray(seasons) || seasonList.every(i => seasons.includes(i))) {
    return next(
      new HttpError(
        `invalid seasons: ${seasons}; can only include ${seasonList}`,
        400
      )
    );
  }

  try {
    const user = await User.findById(userId);

    const clothes = new Clothes({
      category: category,
      color: color,
      seasons: seasons,
      occasions: occasions,
      name: body.name || '',
      image_url: '', // TODO: handle upload and store image
      user: userId, // TODO: perhaps use the validated id
    });

    const savedClothes = await clothes.save();
    user.clothes = user.clothes.concat(savedClothes.id);
    await user.save();
    res.status(201).json(savedClothes);
  } catch (exception) {
    LOG.error(exception);
    next(new HttpError('Failed adding new clothes', 500));
  }
};

/**
 * Delete one clothing
 * - userId
 * - clothingId
 */
const deleteClothing = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  try {
    const deletedClothing = await Clothes.findOneAndDelete({
      _id: clothingId,
      user: userId,
    });
    if (!deletedClothing) {
      res.status(404).end(); // not found or already deleted
    } else {
      res.status(200).end(); // deleted successfully
    }
  } catch (exception) {
    LOG.error(exception);
    next(new HttpError('Failed getting clothes', 500));
  }
};

module.exports = {
  getClothes,
  getClothing,
  postClothing,
  deleteClothing,
};
