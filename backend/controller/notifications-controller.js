const { admin } = require('../config/firebase.config');

require('dotenv').config();
const HttpError = require('../model/http-error');
const LOG = require('../utils/logger');

const notification_options = {
  priority: process.env.NOTIFICATION_PRIORITY,
  timeToLive: parseInt(process.env.NOTIFICATION_LIVE_TIME),
};

const sendNotification = async (req, res, next) => {
  const { registrationToken, message } = req.body;
  const option = notification_options;

  try {
    await admin.messaging().sendToDevice(registrationToken, message, option);
  } catch (err) {
    LOG.error(err);

    const error = new HttpError(
      'Could not send notification user, please try again',
      500
    );
    return next(error);
  }

  res.status(200).json({ message: 'Notification sent successfully!' });
};

exports.sendNotification = sendNotification;
