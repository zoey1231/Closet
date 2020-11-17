const assert = require('assert');
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
  if (!req.userData.userId || req.userData.userId !== userId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  let savedClothes;
  try {
    savedClothes = req.query.category
      ? await Clothes.find({ user: userId, category: req.query.category })
      : await Clothes.find({ user: userId });

    if (
      !savedClothes ||
      savedClothes.length === 0 ||
      !Array.isArray(savedClothes)
    ) {
      return res.status(404).json({ message: 'Not found' });
    }
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(new HttpError('Failed getting clothes', 500));
  }

  res.status(200).json({ clothes: savedClothes });
};

/**
 * Get one clothing for one user
 * - userId
 * - clothingId
 */
const getClothing = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  if (!req.userData.userId || req.userData.userId !== userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  let savedClothing;
  try {
    savedClothing = await Clothes.findById(clothingId);

    if (!savedClothing) {
      return res.status(404).json({ message: 'Not found' });
    }
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(new HttpError('Failed getting clothing', 500));
  }

  res.status(200).json(savedClothing);
};

/**
 * Add one clothing
 * - userId
 */
const postClothing = async (req, res, next) => {
  const body = req.body; // required(category, color, seasons, occasion) optional: (name) toBeFilled: (user)
  const userId = req.params.userId;

  // ===== validate token =====
  if (req.userData.userId === null || req.userData.userId !== userId) {
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
    return next(new HttpError('Invalid occasions; should be an array', 400));
  }

  const SeasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
  if (!Array.isArray(seasons) || !seasons.every(i => SeasonList.includes(i))) {
    return next(
      new HttpError(`Invalid seasons; can only include ${SeasonList}`, 400)
    );
  }

  try {
    const user = await User.findById(userId);

    const clothes = new Clothes({
      category,
      color,
      seasons,
      occasions,
      name: body.name || '',
      user: userId, // TODO: perhaps use the validated id
    });

    const savedClothing = await clothes.save();
    await user.clothes.push(savedClothing.id);
    await user.save();
    res.status(201).json(savedClothing);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(new HttpError('Failed adding clothing', 500));
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
  if (!req.userData.userId || req.userData.userId !== userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  try {
    const deletedClothing = await Clothes.findOneAndDelete({
      _id: clothingId,
      user: userId,
    });

    if (!deletedClothing) {
      return next(new HttpError('Not found or already deleted', 404));
    }

    const user = await User.findById(userId);
    await user.clothes.remove(clothingId);
    await user.save();
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(new HttpError('Failed deleting clothing', 500));
  }

  res.status(200).json({ message: 'Deleted clothing' }).end();
};

/**
 * Update one clothing
 * - userId
 * - clothingId
 */
const updateClothing = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  const body = req.body;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId !== userId || !clothingId) {
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
    return next(new HttpError('Invalid occasions; should be an array', 400));
  }

  const SeasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
  if (!Array.isArray(seasons) || !seasons.every(i => SeasonList.includes(i))) {
    return next(
      new HttpError(`Invalid seasons; can only include ${SeasonList}`, 400)
    );
  }

  try {
    const updateClothing = {
      category,
      color,
      seasons,
      occasions,
      name: body.name || '',
    };

    const savedClothing = await Clothes.findOneAndUpdate(
      {
        _id: clothingId,
        user: userId,
      },
      updateClothing,
      {
        new: true,
      }
    );

    assert(updateClothing.category === savedClothing.category);
    assert(updateClothing.color === savedClothing.color);
    assert(
      updateClothing.seasons.length === savedClothing.seasons.length &&
        updateClothing.seasons.every((u, i) => u === savedClothing.seasons[i])
    );
    assert(
      updateClothing.occasions.length === savedClothing.occasions.length &&
        updateClothing.occasions.every(
          (u, i) => u === savedClothing.occasions[i]
        )
    );

    assert(updateClothing.name === savedClothing.name);

    res.status(200).json(savedClothing);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    if (exception.name === 'AssertionError') {
      next(new HttpError('Error updating clothes', 500));
    }

    next(new HttpError('Failed updating clothes', 500));
  }
};

module.exports = {
  getClothes,
  getClothing,
  postClothing,
  deleteClothing,
  updateClothing,
};
