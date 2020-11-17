require('dotenv').config();
const HttpError = require('../model/http-error');
const { getCalendarEvents } = require('../service/calendar-service');
const LOG = require('../utils/logger');

const getEvents = async (req, res, next) => {
  const { date } = req.params;
  const { code } = req.body;
  
  if (!date || !code) {
    return next(new HttpError('Missing parameters', 400));
  }

  let response;
  try {
    response = await getCalendarEvents(date, code);
  } catch (err) {
    LOG.error(err);
    return next(
      new HttpError('There is an error occurred, please try again later', 500)
    );
  }

  const { success, reason, events } = response;

  let message;
  let statusCode = 500;

  if (!success) {
    switch (reason) {
      case process.env.CALENDAR_DATE_ERROR:
        message =
          'Failed to fetch calendar events, please check the date format';
        break;
      case process.env.CALENDAR_FILE_ERROR:
        message = 'Failed to load credentials, please try again later';
        break;
      case process.env.CALENDAR_CODE_ERROR:
        message = 'Authentication failed, please enter the correct code';
        statusCode = 400;
        break;
      case process.env.CALENDAR_EVENTS_ERROR:
        message = 'Failed to fetch calendar events, please try again later';
        break;
      default:
        message = 'There is an error occurred, please try again later';
        break;
    }

    return next(new HttpError(message, statusCode));
  }

  res.status(200).json(events);
};

exports.getEvents = getEvents;
