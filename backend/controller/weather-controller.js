require('dotenv').config();
const HttpError = require('../model/http-error');
const { getWeatherInfo } = require('../service/weather-service');
const LOG = require('../utils/logger');

const getWeather = async (req, res, next) => {
  const { userId } = req.userData;

  let response;
  try {
    response = await getWeatherInfo(userId);
  } catch (err) {
    LOG.error(req._id, err.message);
    return next(
      new HttpError(
        'Could not get weather information, please try again later',
        500
      )
    );
  }

  if (!response.success) {
    const { code, message } = response;
    return next(new HttpError(message, code));
  }

  const { current, today, tomorrow } = response;
  res.status(200).json({ current, today, tomorrow });
};

exports.getWeather = getWeather;
