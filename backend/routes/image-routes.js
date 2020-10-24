require('dotenv').config();

const multer = require('multer');
let upload = multer({ dest: `${process.env.TEMP_IMAGE_FOLDER_NAME}` });

const checkAuth = require('../middleware/check-auth');

const imageRouter = require('express').Router();
const imageController = require('../controller/image-controller');

imageRouter.use(checkAuth);

/*
 * Get one image
 */
imageRouter.get('/:userId/:clothingId', imageController.getImage);

/**
 * Add one clothing
 */
imageRouter.post(
  '/:userId/:clothingId',
  upload.single('ClothingImage'),
  imageController.postImage
);

/**
 * Delete one clothing
 */
// imageRouter.delete('/:userId/:clothingId', imageController.deleteImage);

module.exports = imageRouter;
