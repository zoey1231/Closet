require('dotenv').config();

const jwt = require('jsonwebtoken');

const clothesRouter = require('express').Router();
const Clothes = require('../models/clothes');

/**
 * Get all clothes for a user
 */
clothesRouter.get('/:userId', async (req, res) => {});

/**
 * Get one clothing
 */
clothesRouter.get('/:clothingId', async (req, res) => {
  // TODO: validate token
});

/**
 * Add one clothing
 */
clothesRouter.post('/:userId', async (req, res) => {
  const body = req.body; // required(category, color, seasons, occasion) optional: (name) toBeFilled: (image_url, user)
  const userId = req.params.userId;

  if (!body || !userId) {
    return res.status(400).end();
  }

  let { category, color, seasons, occasions } = body;

  if (!category || !color || !seasons || !occasions) {
    return res.status(400).json('missing clothes values').end();
  }

  if (!Array.isArray(occasions)) {
    return res
      .status(400)
      .json(`invalid occasions: ${occasions}; should be an array`)
      .end();
  }

  const seasonList = ['Spring', 'Summer', 'Fall', 'Winter', 'All'];
  if (!Array.isArray(seasons) || seasonList.every(i => seasons.includes(i))) {
    return res
      .status(400)
      .json(`invalid seasons: ${seasons}; can only include ${seasonList}`)
      .end();
  }

  try {
    // TODO: validate token - decode token - compare token's id with userId

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

    res.status(201).json(savedClothes);
  } catch (exception) {
    next(exception);
  }
});

/**
 * Delete one clothing
 */
clothesRouter.delete('/:userId', async (req, res) => {
  // TODO: validate token
});

module.exports = clothesRouter;
