require('dotenv').config();
const { getCalendarEvents } = require('./calendar-service');
const { getWeatherInfo } = require('./weather-service');
const { timestampToDate } = require('../utils/time-helper');
const { hashCode, randomInt } = require('../utils/hash');
const LOG = require('../utils/logger');

const Clothes = require('../model/clothes');
const Outfit = require('../model/outfit');

const FORMAL_KEYWORDS = [
  'conference',
  'interview',
  'meeting',
  'presentation',
  'speech',
];

const COLOURS = [
  'Red',
  'Orange',
  'Yellow',
  'Blue',
  'Green',
  'Purple',
  'Pink',
  'Grey',
  'White',
  'Black',
];

let userId;
let allOutfits = [];
let todayFormalOutfits = [];
let todayFormalEvents = [];
let allClothes = []; // Array of Clothes object
let todayWhether = {};

/**
 * Entry point for complex logic
 *
 * @param {String} userId
 */
const generateOutfit = async user_id => {
  userId = user_id;

  await preparation();

  let result;

  if (!todayFormalOutfits.length) {
    if (todayFormalEvents.length) {
      // Case 1: no formal outfits and have formal events => create a formal outfit
      result = await createFormalOutfit();
    } else {
      // Case 2: no formal outfits but no formal events => create a normal outfit
      result = await createNormalOutfit();
    }
  } else {
    // Case 3: have formal outfit => create a normal outfit
    result = await createNormalOutfit();
  }

  const outfit = await saveOutfit(result);
  return outfit;

  // Dummy response - return the first outfit in database
  // return allOutfits[0];
};

// Save the outfit into database
const saveOutfit = async result => {
  if (!result.success) {
    return {
      success: result.success,
      message: 'TBD',
    };
  }

  const {
    chosenUpperClothes,
    chosenTrousers,
    chosenShoes,
    occasions,
    seasons,
    warning,
  } = result;

  const _id = hashCode(
    chosenUpperClothes.id + chosenTrousers.id + chosenShoes.id
  );

  const newOutfit = new Outfit({
    _id,
    clothes: [chosenUpperClothes.id, chosenTrousers.id, chosenShoes.id],
    occasions,
    seasons,
    opinion: 'unknown',
    user: userId,
  });

  try {
    await newOutfit.save();
  } catch (exception) {
    LOG.error(exception.message);
    return {
      success: false,
      message: 'TBD',
    };
  }

  return {
    success: true,
    message: 'New outfit generated successfully!',
    warning,
    outfit: {
      id: newOutfit.id,
      clothes: newOutfit.clothes,
      created: newOutfit.created,
      occasions,
      seasons,
      opinion: newOutfit.opinion,
      user: userId,
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
    },
  };
};

// Create a formal outfit
const createFormalOutfit = async () => {
  await getAllClothes();

  const formalOuterwear = [];
  const formalShirt = [];
  const formalTrousers = [];
  const formalShoes = [];

  allClothes.forEach(c => {
    switch (c.category) {
      case 'outerwear':
        if (c.occasions.includes('formal')) {
          formalOuterwear.push(c);
        }
        break;
      case 'shirt':
        if (c.occasions.includes('formal')) {
          formalShirt.push(c);
        }
        break;
      case 'trousers':
        if (c.occasions.includes('formal')) {
          formalTrousers.push(c);
        }
        break;
      case 'shoes':
        if (c.occasions.includes('formal')) {
          formalShoes.push(c);
        }
        break;
      default:
        break;
    }
  });

  /* Requirements to return a formal outfit
      1. have formal outerwear and formal shirt
      2. have formal trousers
      3. have formal shoes
      if no formal outwear and no formal shirt
   */
  if (
    (!formalOuterwear.length && !formalShirt.length) ||
    !formalTrousers.length ||
    !formalShoes.length
  ) {
    let warning =
      'We notice you have these events today, but you do not have any formal clothes!\n';
    for (const event of todayFormalEvents) {
      warning += `${event}\n`;
    }

    // Generate a normal outfit instead
    const result = await createNormalOutfit();
    return {
      ...result,
      warning,
    };
  }

  let chosenUpperClothes, chosenTrousers, chosenShoes;
  if (!formalOuterwear.length) {
    chosenUpperClothes = formalShirt[randomInt(formalShirt.length)];
  } else if (!formalShirt.length) {
    chosenUpperClothes = formalOuterwear[randomInt(formalOuterwear.length)];
  } else {
    // If we have both formal outerwear and formal shirt, choose one of them randomly
    const r = randomInt(2);
    if (r === 0) {
      chosenUpperClothes = formalShirt[randomInt(formalShirt.length)];
    } else {
      chosenUpperClothes = formalOuterwear[randomInt(formalOuterwear.length)];
    }
  }

  chosenTrousers = formalTrousers[randomInt(formalTrousers.length)];
  chosenShoes = formalShoes[randomInt(formalShoes.length)];

  /*  success: indication whether we can generate an outfit or not
      chosenUpperClothes: upper clothes (outerwear or shirt) to include
      chosenTrousers: trousers to include
      chosenShoes: shoes to include
      occasions: formal outfit,
      seasons: all seasons,
   */
  return {
    success: true,
    chosenUpperClothes,
    chosenTrousers,
    chosenShoes,
    occasions: ['formal'],
    seasons: ['All'],
  };
};

// Create a normal outfit
const createNormalOutfit = async () => {
  await getAllClothes();

  let normalOuterwear = [];
  let normalShirt = [];
  let normalTrousers = [];
  let normalShoes = [];

  allClothes.forEach(c => {
    switch (c.category) {
      case 'outerwear':
        if (!c.occasions.includes('formal')) {
          normalOuterwear.push(c);
        }
        break;
      case 'shirt':
        if (!c.occasions.includes('formal')) {
          normalShirt.push(c);
        }
        break;
      case 'trousers':
        if (!c.occasions.includes('formal')) {
          normalTrousers.push(c);
        }
        break;
      case 'shoes':
        if (!c.occasions.includes('formal')) {
          normalShoes.push(c);
        }
        break;
      default:
        break;
    }
  });

  // Must have the follow three
  // 1. outerwear OR shirt
  // 2. trousers
  // 3. shoe

  if (
    (!normalOuterwear.length && !normalShirt.length) ||
    !normalTrousers.length ||
    !normalShoes.length
  ) {
    return {
      success: false,
      message: 'Add more clothes to get outfit!',
    };
  }

  // TODO: need better weather implementation!

  // get weather & season tag
  // await getTodayWeather();

  // const { temperature, description } = todayWhether;
  // let season = getSeasonFromTemperature(temperature);

  let currSeason = getSeasonNorth();

  // filter out other seasons
  normalOuterwear = normalOuterwear.filter(c => c.seasons.includes(currSeason));
  normalShirt = normalShirt.filter(c => c.seasons.includes(currSeason));
  normalShoes = normalShoes.filter(c => c.seasons.includes(currSeason));
  normalTrousers = normalTrousers.filter(c => c.seasons.includes(currSeason));

  // check if outfit exists
  let allOutfitIds = await (await Outfit.find({ user: userId })).map(
    outfit => outfit.id
  );

  // loop through all current clothing
  // TODO: these loops looks bad bad bad --- look into cartesian product
  let chosenUpperClothes, chosenTrousers, chosenShoes;
  for (const outerwear in normalOuterwear) {
    for (const shirt in normalShirt) {
      for (const trousers in normalTrousers) {
        for (const shoes in normalShoes) {
          let random = randomInt(1);

          let outfitId = random
            ? hashCode(shirt.id + trousers.id + shoes.id)
            : hashCode(outerwear.id + trousers.id + shoes.id);

          // if this outfit does not already exist
          if (!allOutfitIds.includes(outfitId)) {
            chosenUpperClothes = random ? shirt : outerwear;
            chosenTrousers = trousers;
            chosenShoes = shoes;
            break;
          }
        }
      }
    }
  }

  return {
    success: true,
    chosenUpperClothes,
    chosenTrousers,
    chosenShoes,
    occasions: '', // TODO: what about occasion?
    seasons: [currSeason],
  };
};

// Return all clothes in database
const getAllClothes = async () => {
  try {
    allClothes = await Clothes.find({ user: userId });
  } catch (exception) {
    LOG.error(exception.message);
  }
};

/*  Preparation
    Initialize the following things
      1. all outfits in database
      2. formal outfits generated today
      3. today's formal events
*/
const preparation = async () => {
  await getAllOutfits();
  getTodayFormalOutfits();
  await getTodayFormalEvents();
};

// Return all outfits in the database
const getAllOutfits = async () => {
  try {
    allOutfits = await Outfit.find({ user: userId });
  } catch (exception) {
    LOG.error(exception.message);
  }
};

// Return formal outfits generated today
const getTodayFormalOutfits = () => {
  const today = new Date().toISOString().substr(0, 10);

  todayFormalOutfits = allOutfits.filter(
    outfit =>
      outfit.created.toISOString().substr(0, 10) === today &&
      outfit.occasions.includes('formal')
  );
};

// Return today's formal events
const getTodayFormalEvents = async () => {
  const time = timestampToDate(Date.now());
  const date = `${time.month.monthDesc}-${time.date}-${time.year}`;

  let response;
  try {
    response = await getCalendarEvents(date);
  } catch (exception) {
    LOG.error(exception.message);
    return;
  }

  const { events } = response;

  events.forEach(e => {
    const keyword = e.summary.toLowerCase().split(' ');
    for (const word of keyword) {
      if (FORMAL_KEYWORDS.includes(word)) {
        todayFormalEvents.push(e.summary);
        break;
      }
    }
  });
};

// Return today's weather information
const getTodayWeather = async () => {
  // Hard code place for now (using vancouver)

  let response;
  try {
    response = await getWeatherInfo('vancouver');
  } catch (exception) {
    LOG.error(exception.message);
    return;
  }

  const { today } = response;
  const { temp, weather } = today;

  todayWhether = { temperature: temp, weather: weather[0].description };
};

/**
 * Get Northern hemisphere season
 * @returns one of ['Winter', 'Spring', 'Summer', 'Fall']
 */
const getSeasonNorth = () =>
  ['Winter', 'Spring', 'Summer', 'Fall'][
    Math.floor((new Date().getMonth() / 12) * 4) % 4
  ];

/**
 * Get season from temperature
 * @param {int} temperature
 * @return {String} Season "Spring" "Summer" "Fall" "Winter"
 */
const getSeasonFromTemperature = temperature => {
  if (temperature > 20) return 'Summer';
  if (temperature <= 20 && temperature >= 15) return 'Fall';
  if (temperature < 15 && temperature >= 10) return 'Spring';
  if (temperature > 10) return 'Winter';
};

module.exports = {
  generateOutfit,
};
