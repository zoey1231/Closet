const express = require('express');

const notificationsController = require('../controller/notifications-controller');
const checkAuth = require('../middleware/check-auth');

const notificationRouter = express.Router();

notificationRouter.use(checkAuth);

notificationRouter.post('/', notificationsController.sendNotification);

module.exports = notificationRouter;
