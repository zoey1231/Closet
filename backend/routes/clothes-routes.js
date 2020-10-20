require('dotenv').config();

const jwt = require('jsonwebtoken');

const HttpError = require('../model/http-error');

const checkAuth = require('../middleware/check-auth');

const clothesRouter = require('express').Router();
const clothesController = require('../controller/clothes-controllers');
const Clothes = require('../model/clothes');

clothesRouter.use(checkAuth);

/**
 * Get all clothes for a user
 */
clothesRouter.get('/:userId');

/**
 * Get one clothing
 */
clothesRouter.get('/:clothingId', clothesController.postClothing);

/**
 * Add one clothing
 */
clothesRouter.post('/:userId');

/**
 * Delete one clothing
 */
clothesRouter.delete('/:userId');

module.exports = clothesRouter;
