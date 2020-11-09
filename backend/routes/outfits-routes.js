const express = require('express');
const { check } = require('express-validator');

const outfitsController = require('../controller/outfits-controller');
const checkAuth = require('../middleware/check-auth');

const outfitsRouter = express.Router();

outfitsRouter.use(checkAuth);

// Requests for one outfit
outfitsRouter.get('/one', outfitsController.getOneOutfit);

/*  Get multiple outfits
    CAUTION: might change later to combine with notifications !!!
 */
outfitsRouter.get('/multiple', outfitsController.getMultipleOutfits);

// Update user opinion of one specific outfit
outfitsRouter.put(
  '/:outfitId',
  [check('opinion').notEmpty()],
  outfitsController.updateUserOpinion
);

module.exports = outfitsRouter;
