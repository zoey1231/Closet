require('dotenv').config();

const jwt = require('jsonwebtoken');

const HttpError = require('../model/http-error');

const checkAuth = require('../middleware/check-auth');

const clothesRouter = require('express').Router();
const clothesController = require('../controller/clothes-controllers');
const Clothes = require('../model/clothes');

const { check } = require('express-validator');

clothesRouter.use(checkAuth);

/**
 * Get all clothes for a user
 */
clothesRouter.get('/:userId', clothesController.getClothes);

/**
 * Get one clothing
 */
clothesRouter.get('/:userId/:clothingId', clothesController.getClothing);

/**
 * Add one clothing
 */
clothesRouter.post(
  '/:userId',
  [
    check('category').not().isEmpty(),
    check('color').not().isEmpty(),
    check('seasons').not().isEmpty(),
    check('occasions').not().isEmpty(),
  ],
  clothesController.postClothing
);

/**
 * Delete one clothing
 */
clothesRouter.delete('/:userId');

module.exports = clothesRouter;
