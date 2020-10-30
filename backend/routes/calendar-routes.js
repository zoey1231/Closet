const express = require('express');

const calendarController = require('../controller/calendar-controller');
const checkAuth = require('../middleware/check-auth');

const calendarRouter = express.Router();

calendarRouter.use(checkAuth);

calendarRouter.post('/:date', calendarController.getEvents);

module.exports = calendarRouter;
