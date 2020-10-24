const path = require('path');
const fs = require('fs');

require('dotenv').config();

const multer = require('multer');

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');

const Clothes = require('../model/clothes');
const User = require('../model/user');

const getImage = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId || !clothingId) {
    return next(new HttpError('Token missing or invalid', 401));
  }

  const imageFileExtension = '.jpg';
  const targetPath = path.join(
    `./${process.env.IMAGE_FOLDER_NAME}/${userId}/${clothingId}${imageFileExtension}`
  );

  try {
    if (!fs.existsSync(targetPath)) {
      return next(new HttpError('Image does not exist', 500));
    }
  } catch (exception) {
    return next(new HttpError('Failed to get image', 500));
  }

  res.sendFile(targetPath, {
    root: path.join(__dirname, '../'),
  });
};

const postImage = async (req, res, next) => {
  const clothingId = req.params.clothingId;
  const userId = req.params.userId;
  // ===== validate token =====
  if (!req.userData.userId || req.userData.userId != userId || !clothingId) {
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
  // TODO: allow other image file extension
  if (imageFileExtension !== '.jpg') {
    fs.unlink(tempPath, err => {
      if (err) return next(new HttpError('Failed to upload image', 500));
    });

    res.status(403).json('Only .jpg files are allowed');
  }

  // setup for saving file
  const targetFolder = path.join(
    `./${process.env.IMAGE_FOLDER_NAME}/${userId}/`
  );
  const targetPath = path.join(
    `./${process.env.IMAGE_FOLDER_NAME}/${userId}/${clothingId}${imageFileExtension}`
  );

  try {
    // check folder exists, else create it
    if (!fs.existsSync(targetFolder)) {
      fs.mkdirSync(targetFolder);
    }

    // move image from temp location to userId location
    fs.rename(tempPath, targetPath, err => {
      if (err) return next(new HttpError('Unable to move image', 500));
    });

    // make sure moved image exists
    if (!fs.existsSync(targetPath)) {
      return next(new HttpError('Error moving image', 500));
    }
  } catch (exception) {
    return next(new HttpError('Failed uploading image', 500));
  }

  res.status(201).json('Uploaded image!').end();
};

const deleteImage = async (req, res, next) => {};

module.exports = {
  getImage,
  postImage,
  deleteImage,
};
