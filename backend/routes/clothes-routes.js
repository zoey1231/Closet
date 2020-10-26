require('dotenv').config();

const { check } = require('express-validator');

const checkAuth = require('../middleware/check-auth');

const clothesRouter = require('express').Router();
const clothesController = require('../controller/clothes-controllers');

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
clothesRouter.delete('/:userId/:clothingId', clothesController.deleteClothing);

/**
 * Update one clothing
 */
clothesRouter.put(
  '/:userId/:clothingId',
  [
    check('category').not().isEmpty(),
    check('color').not().isEmpty(),
    check('seasons').not().isEmpty(),
    check('occasions').not().isEmpty(),
  ],
  clothesController.updateClothing
);

module.exports = clothesRouter;
