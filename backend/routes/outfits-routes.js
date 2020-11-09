const express = require('express');

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

// Update user opinion on one outfit
outfitsRouter.patch('/:outfitId', outfitsController.updateUserOpinion);

module.exports = outfitsRouter;
