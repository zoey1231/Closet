const express = require('express');

const outfitsController = require('../controller/outfits-controller');
const checkAuth = require('../middleware/check-auth');

const router = express.Router();

router.use(checkAuth);

// Requests for one outfit
router.get('/one', outfitsController.getOneOutfit);

/*  Get multiple outfits
    CAUTION: might change later to combine with notifications !!!
 */
router.get('/multiple', outfitsController.getMultipleOutfits);

module.exports = router;
