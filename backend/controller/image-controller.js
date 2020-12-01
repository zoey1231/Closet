const path = require('path');
const fs = require('fs').promises;

require('dotenv').config();

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');

const ALLOWED_EXTENSIONS = ['.jpg', '.png', '.jpeg', '.jpe'];

/**
 * Post image
 * - userId
 * - clothingId
 */
const postImage = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId !== userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  // image will be saved in a temporary location first
  // we will check the file while it is in the temporary location
  // then we will move the file away
  const imageFile = req.file;
  const tempPath = path.join(imageFile.path);

  // check file extension
  const imageFileExtension = path
    .extname(imageFile.originalname)
    .toLocaleLowerCase();

  // limiting to one file extension for now
  if (!ALLOWED_EXTENSIONS.includes(imageFileExtension)) {
    try {
      await fs.unlink(tempPath);
    } catch (err) {
      return next(new HttpError('Failed to upload image', 500));
    }
    return res.status(403).json({ message: 'Extension not allowed' });
  }

  // setup for saving file
  const targetFolder = path.join(
    `./${process.env.IMAGE_FOLDER_NAME}/${userId}/`
  );
  const targetPath = path.join(
    `./${process.env.IMAGE_FOLDER_NAME}/${userId}/${clothingId}${imageFileExtension}`
  );

  try {
    await fs.mkdir(targetFolder);
  } catch (exception) {
    if (exception.code !== 'EEXIST') {
      LOG.error(req._id, exception);
      return next(new HttpError('Failed uploading image', 500));
    }
  }

  try {
    await fs.rename(tempPath, targetPath);
  } catch (exception) {
    LOG.error(req._id, exception);
    return next(new HttpError('Failed uploading image', 500));
  }

  return res.status(201).json({ message: 'Uploaded image!' });
};

const deleteImage = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId !== userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }


  let fileExists;
  let deletePath;
  for await (const extension of ALLOWED_EXTENSIONS) {
    const targetPath = path.join(
      `./${process.env.IMAGE_FOLDER_NAME}/${userId}/${clothingId}${extension}`
    );

    try {
      await fs.access(targetPath);
      fileExists = true;
      deletePath = targetPath;
      break;
    } catch {
      fileExists = false;
    }
  }

  if (!fileExists || !deletePath) {
    return next(new HttpError('Image does not exist', 500));
  }

  try {
    await fs.unlink(deletePath);
  } catch (exception) {
    LOG.error(req._id, exception.message);
    return next(new HttpError('Failed to delete image', 500));
  }

  return res.status(200).json({ message: 'Deleted image' });
};

module.exports = {
  postImage,
  deleteImage,
};
