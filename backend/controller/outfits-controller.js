require('dotenv').config();
const HttpError = require('../model/http-error');
const Outfit = require('../model/outfit');

const getOneOutfit = (req, res, next) => {
  const userId = req.userData.userId;

  const exampleBody = {
    id: '1234567890',
    clothes: [
      {
        seasons: ['Summer', 'Fall'],
        occasions: ['home'],
        category: 't-shirt',
        color: 'blue',
        name: '',
        image_url: '',
        user: '5f8e85a397320028983485e1',
        updated: '2020-10-24T01:34:03.990Z',
        id: '5f93848bbe8de13900c31f3c',
      },
      {
        seasons: ['Summer', 'Fall'],
        occasions: ['home'],
        category: 't-shirt',
        color: 'blue',
        name: '',
        image_url: '',
        user: '5f8e85a397320028983485e1',
        updated: '2020-10-24T01:34:49.217Z',
        id: '5f9384b9be8de13900c31f3d',
      },
    ],
  };

  res.status(200).json(exampleBody).end();
};

const getMultipleOutfits = (req, res, next) => {
  // Complex Logic + Notification go here
};

module.exports = {
  getOneOutfit,
  getMultipleOutfits,
};
