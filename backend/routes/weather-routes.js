const express = require('express');

const weatherController = require('../controller/weather-controller');
const checkAuth = require('../middleware/check-auth');

const weatherRouter = express.Router();

weatherRouter.use(checkAuth);

weatherRouter.get('/:place', weatherController.getWeather);

module.exports = weatherRouter;
