require('dotenv').config();

const multer = require('multer');

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');

const Clothes = require('../model/clothes');
const User = require('../model/user');

const getImage = async (req, res, next) => {};

const postImage = async (req, res, next) => {};

const deleteImage = async (req, res, next) => {};

module.exports = {
  getImage,
  postImage,
  deleteImage,
};
