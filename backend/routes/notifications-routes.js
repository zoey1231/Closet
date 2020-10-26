const express = require('express');

const notificationsController = require('../controller/notifications-controller');
const checkAuth = require('../middleware/check-auth');

const router = express.Router();

router.use(checkAuth);

router.post('/', notificationsController.sendNotification);

module.exports = router;
