const { admin } = require('../config/firebase.config');

require('dotenv').config();
const HttpError = require('../model/http-error');

const notification_options = {
  priority: process.env.NOTIFICATION_PRIORITY,
  timeToLive: parseInt(process.env.NOTIFICATION_LIVE_TIME),
};

const sendNotification = async (req, res, next) => {
  const { registrationToken, message } = req.body;
  const option = notification_options;

  console.log('=====================================');
  console.log('registrationToken', registrationToken);
  console.log('message', message);
  console.log('=====================================');

  try {
    await admin.messaging().sendToDevice(registrationToken, message, option);
  } catch (err) {
    console.log('=====================================');
    console.log(err);
    console.log('=====================================');

    const error = new HttpError(
      'Could not send notification user, please try again',
      500
    );
    return next(error);
  }

  res.status(200).json({ message: 'Notification sent successfully!' });
};

exports.sendNotification = sendNotification;
