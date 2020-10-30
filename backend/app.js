const fs = require('fs');
const path = require('path');

const config = require('./utils/config');
const LOG = require('./utils/logger');

const cors = require('cors');

const express = require('express');
const app = express();
const morgan = require('morgan');

const mongoose = require('mongoose');

const HttpError = require('./model/http-error');

// routers
const clothesRoutes = require('./routes/clothes-routes');
const usersRoutes = require('./routes/users-routes');
const outfitsRoutes = require('./routes/outfits-routes');
const imageRoutes = require('./routes/image-routes');
const notificationRoutes = require('./routes/notifications-routes');
const weatherRoutes = require('./routes/weather-routes');
const calendarRoutes = require('./routes/calendar-routes');

// connect to db
LOG.info('⌛connecting to', config.MONGODB_URI);

mongoose
  .connect(config.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  })
  .then(() => {
    LOG.info('✅connected to MongoDB');
  })
  .catch(error => {
    LOG.error('❌error connecting to MongoDB:', error.message);
  });

// app setting
app.use(cors());
app.use(express.json());
app.use(morgan('tiny'));
app.use(express.static(__dirname + '/static'));

// api
app.get('/version', (req, res) => {
  res.status(200).json({ message: config.VERSION });
});

// if this exceptions or fails - app.js will just break
// TODO: should probably improve this
const imageFolder = path.join(`./${process.env.IMAGE_FOLDER_NAME}`);
if (!fs.existsSync(imageFolder)) {
  LOG.info(
    `Image storage path ${imageFolder} does not exist... making directory`
  );
  fs.mkdirSync(imageFolder);
}

// routes
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'Origin, X-Requested-With, Content-Type, Accept, Authorization'
  );
  res.setHeader(
    'Access-Control-Allow-Methods',
    'GET, POST, PUT, DELETE, PATCH'
  );

  next();
});

app.use('/api/users', usersRoutes);
app.use('/api/clothes', clothesRoutes);
app.use('/api/outfits', outfitsRoutes);
app.use('/api/images', imageRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api/weather', weatherRoutes);
app.use('/api/calendar', calendarRoutes);

app.use((req, res, next) => {
  return next(new HttpError('Could not find this route', 404));
});

app.use((error, req, res, next) => {
  if (res.headerSent) {
    return next(error);
  }
  res.status(error.code || 500);
  res.json({ message: error.message || 'An unknown error occurred!' });
});

module.exports = app;
