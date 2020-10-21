const jwt = require('jsonwebtoken');

require('dotenv').config();
const HttpError = require('../model/http-error');

const LOG = require('../utils/logger');

module.exports = (req, res, next) => {
  try {
    const token = req.headers.authorization.split(' ')[1];
    if (!token) {
      throw new Error('Authentication failed!');
    }
    const decodedToken = jwt.verify(token, process.env.JWT_SECRET);
    req.userData = { userId: decodedToken.userId };
    next();
  } catch (err) {
    LOG.error(err);
    const error = new HttpError('Authentication failed!', 401);
    return next(error);
  }
};
