const jwt = require('jsonwebtoken');

const HttpError = require('../model/http-error');

module.exports = (req, res, next) => {
  try {
    const token = req.headers.authorization.split(' ')[1];
    if (!token) {
      throw new Error('Authentication failed!');
    }
    const decodedToken = jwt.verify(
      token,
      'cpen_211_zoey_summer_steven_john_super_secret_do_not_share'
    );
    req.userData = { userId: decodedToken.userId };
    next();
  } catch (err) {
    const error = new HttpError('Authentication failed!', 401);
    return next(error);
  }
};
