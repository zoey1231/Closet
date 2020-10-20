require('dotenv').config();

const jwt = require('jsonwebtoken');
const HttpError = require('../model/http-error');

const Clothes = require('../model/clothes');
const User = require('../model/user');

const getClothing = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  try {
    const savedClothes = await Clothes.findById(clothingId);
    res.status(200).json(savedClothes);
  } catch (exception) {
    next(new HttpError(`Failed getting clothes: ${exception}`, 500));
  }
};

/**
 * Add one clothing (need userId)
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
    next(new HttpError(`Failed adding new clothes: ${exception}`, 500));
  }
};

module.exports = {
  getClothing,
  postClothing,
};
