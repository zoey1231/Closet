require('dotenv').config();

const checkAuth = require('../middleware/check-auth');

const uploadRouter = require('express').Router();
const uploadController = require('../controller/upload-controller');

uploadRouter.use(checkAuth);

/*
 * Get one image
 */
uploadRouter.get('/:userId/:clothingId', uploadController.getImage);

/**
 * Add one clothing
 */
uploadRouter.post('/:userId', uploadController.postImage);

/**
 * Delete one clothing
 */
uploadRouter.delete('/:userId/:clothingId', uploadController.deleteImage);

module.exports = uploadRouter;
