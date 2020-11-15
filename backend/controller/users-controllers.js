require('dotenv').config();

const { validationResult } = require('express-validator');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const LOG = require('../utils/logger');

const HttpError = require('../model/http-error');
const User = require('../model/user');

const signup = async (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return next(
      new HttpError('Invalid inputs passed, please check your data.', 422)
    );
  }
  const { name, email, password } = req.body;

  let hashedPassword;
  try {
    hashedPassword = await bcrypt.hash(password, parseInt(process.env.SALT));
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(new HttpError('Could not create user, please try again', 500));
  }

  // Every new user will have "Vancouver" as the default city
  const createdUser = new User({
    name,
    email,
    password: hashedPassword,
    clothes: [],
    city: process.env.DEFAULT_USER_CITY,
    lat: process.env.DEFAULT_USER_LATITUDE,
    lng: process.env.DEFAULT_USER_LONGITUDE,
  });

  try {
    await createdUser.save();
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(
      new HttpError('User exists already, please login instead', 422)
    );
  }

  let token;
  try {
    token = jwt.sign(
      { userId: createdUser.id, email: createdUser.email },
      process.env.JWT_SECRET,
      { expiresIn: process.env.TOKEN_EXPIRE_TIME }
    );
  } catch {
    return next(new HttpError('Signing Up failed, please try again.', 500));
  }

  res
    .status(201)
    .json({ userId: createdUser.id, email: createdUser.email, token });
};

const login = async (req, res, next) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return next(
      new HttpError('Invalid credentials, could not log you in.', 401)
    );
  }

  let existingUser;
  try {
    existingUser = await User.findOne({ email });
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(
      new HttpError('Logging in failed, please try again later', 500)
    );
  }

  if (!existingUser) {
    return next(
      new HttpError('Invalid credentials, could not log you in.', 401)
    );
  }

  let isValidPassword = false;
  try {
    isValidPassword = await bcrypt.compare(password, existingUser.password);
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(
      new HttpError(
        'Could not log you in, please check your credentials and try again',
        500
      )
    );
  }

  if (!isValidPassword) {
    return next(
      new HttpError('Invalid credentials, could not log you in.', 401)
    );
  }

  let token;
  try {
    token = jwt.sign(
      { userId: existingUser.id, email: existingUser.email },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );
  } catch {
    return next(new HttpError('Logging Up failed, please try again.', 500));
  }

  res.json({
    userId: existingUser.id,
    email: existingUser.email,
    token,
  });
};

const getUserProfile = async (req, res, next) => {
  const { userId } = req.userData;

  console.log(userId);

  let user;
  try {
    user = await User.findById(userId, '-password');
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(
      new HttpError(
        'Failed to get the profile information, please try again later',
        500
      )
    );
  }

  res.status(200).json({ user });
};

module.exports = {
  signup,
  login,
  getUserProfile,
};
