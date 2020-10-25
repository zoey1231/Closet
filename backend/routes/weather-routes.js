const express = require('express');

const weatherController = require('../controller/weather-controller');
const checkAuth = require('../middleware/check-auth');

const router = express.Router();

router.use(checkAuth);

router.get('/:place', weatherController.getWeather);

module.exports = router;
