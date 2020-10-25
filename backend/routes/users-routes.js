const express = require('express');
const { check } = require('express-validator');

const usersController = require('../controller/users-controllers');
const checkAuth = require('../middleware/check-auth');

const usersRouter = express.Router();

usersRouter.post(
  '/signup',
  [
    check('name').not().isEmpty(),
    check('email').normalizeEmail().isEmail(),
    check('password').isLength({ min: 6 }),
  ],
  usersController.signup
);

usersRouter.post('/login', usersController.login);

usersRouter.use(checkAuth);

usersRouter.get('/', usersController.getUsers);

module.exports = usersRouter;
