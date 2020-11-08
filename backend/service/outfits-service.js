require('dotenv').config();
const { getCalendarEvents } = require('./calendar-service');
const { getWeatherInfo } = require('./weather-service');
const { timestampToDate } = require('../utils/time-helper');
const { hashCode, randomInt } = require('../utils/hash');
const LOG = require('../utils/logger');

const Clothes = require('../model/clothes');
const Outfit = require('../model/outfit');
const { hash } = require('bcrypt');

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

/**
 * Entry point for complex logic
 *
 * @param {String} userId
 */
const generateOutfit = async req => {
  const userId = req.userData.userId;

  /* All necessary variables */
  // Need to initialize during preparation
  let AllOutfits = [];
  let AllClothes = [];
  let TodayFormalEvents = [];
  let TodayOutfits = [];
  let TodayFormalOutfits = [];
  // Need to initialized during creating a normal outfit
  let TodayWhether = {};

  /* Initialization functions */
  // Return all outfits in the database
  const getAllOutfits = async () => {
    try {
      AllOutfits = await Outfit.find({ user: userId });
    } catch (exception) {
      LOG.error(exception.message);
    }
  };

  // Return all clothes in database
  const getAllClothes = async () => {
    try {
      AllClothes = await Clothes.find({ user: userId });
    } catch (exception) {
      LOG.error(exception.message);
    }
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
          TodayFormalEvents.push(e.summary);
          break;
        }
      }
    });
  };

  // Return outfits generated today
  const getTodayOutfits = () => {
    const today = new Date()
      .toLocaleString('sv', { timeZoneName: 'short' })
      .substr(0, 10);

    TodayOutfits = AllOutfits.filter(
      outfit => outfit.created.toISOString().substr(0, 10) === today
    );
  };

  // Return only formal outfits generated today
  const getTodayFormalOutfits = () => {
    TodayFormalOutfits = TodayOutfits.filter(outfit =>
      outfit.occasions.includes('formal')
    );
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

    TodayWhether = { temperature: temp, weather: weather[0].description };
  };

  /* Core functions */
  // Save the outfit into database
  const saveOutfit = async result => {
    // Failure if success is not true
    if (!result.success) {
      return {
        success: false,
        message: 'Failed to generate an outfit',
      };
    }

    // Input an existing outfit, no need to save it
    if (result.existing) {
      let chosenUpperClothes;
      let chosenTrousers;
      let chosenShoes;

      // Check if clothes information are include in the result
      if (
        !result.chosenUpperClothes ||
        !result.chosenTrousers ||
        !result.chosenShoes
      ) {
        // If not, we need to manually find them
        chosenUpperClothes = AllClothes.find(c => outfit.clothes[0]);
        chosenTrousers = AllClothes.find(c => outfit.clothes[1]);
        chosenShoes = AllClothes.find(c => outfit.clothes[2]);
      } else {
        chosenUpperClothes = result.chosenUpperClothes;
        chosenTrousers = result.chosenTrousers;
        chosenShoes = result.chosenShoes;
      }

      const { success, warning, outfit } = result;

      return {
        success,
        message: 'New outfit generated successfully!',
        warning,
        outfit: {
          ...outfit,
          chosenUpperClothes,
          chosenTrousers,
          chosenShoes,
        },
      };
    }

    // Otherwise, create a new outfit and save it into the database
    const {
      success,
      warning,
      _id,
      occasions,
      seasons,
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
    } = result;

    const newOutfit = new Outfit({
      _id,
      clothes: [chosenUpperClothes.id, chosenTrousers.id, chosenShoes.id],
      occasions,
      seasons,
      opinion: 'unknown',
      user: userId,
      created: new Date().setTime(
        new Date().getTime() - new Date().getTimezoneOffset() * 60 * 1000
      ),
    });

    try {
      await newOutfit.save();
    } catch (exception) {
      LOG.error(exception.message);
      return {
        success: false,
        message: 'Failed to save outfit',
      };
    }

    const { clothes, created, opinion } = newOutfit;

    return {
      success,
      message: 'New outfit generated successfully!',
      warning,
      outfit: {
        _id,
        clothes,
        created,
        occasions,
        seasons,
        opinion,
        user: userId,
        chosenUpperClothes,
        chosenTrousers,
        chosenShoes,
      },
    };
  };

  // Create a formal outfit
  const createFormalOutfit = async () => {
    /* Decide to choose from user liked formal outfits or try to generate a new one */
    const type = randomInt(2);

    /* Get a user liked formal outfit */
    if (type === 0) {
      const likedFormalOutfits = AllOutfits.filter(
        outfit =>
          outfit.occasions.includes('formal') && outfit.opinion === 'like'
      );

      if (likedFormalOutfits.length) {
        const chosenOutfit =
          LikedFormalOutfits[randomInt(likedFormalOutfits.length)];
        return {
          success: true,
          existing: true,
          outfit: chosenOutfit,
        };
      }
    }

    /* Try to generated a new formal outfit */
    const allFormal = AllClothes.filter(c => c.occasions.includes('formal'));
    const formalOuterwear = allFormal.filter(c => c.category === 'outerwear');
    const formalShirt = allFormal.filter(c => c.category === 'shirt');
    const formalTrousers = allFormal.filter(c => c.category === 'trousers');
    const formalShoes = allFormal.filter(c => c.category === 'shoes');

    /*
      Requirements to return a formal outfit
      1. have formal outerwear or formal shirt
      2. have formal trousers
      3. have formal shoes
     */

    /* Case 1: user does not have enough formal clothes => add warning and generate a normal outfit */
    if (
      (!formalOuterwear.length && !formalShirt.length) ||
      !formalTrousers.length ||
      !formalShoes.length
    ) {
      let warning =
        'We notice you have the following events today, but you do not have enough formal clothes!\n';
      TodayFormalEvents.forEach(event => {
        warning += `${event}\n`;
      });

      // Generate a normal outfit instead
      const result = await createNormalOutfit();
      return {
        ...result,
        warning,
      };
    }

    /* Case 2: user have enough formal clothes => generate a formal outfit */

    /* Generate all possible combinations */
    const allCombinations = cartesian(
      [...formalOuterwear, ...formalShirt],
      formalTrousers,
      formalShoes
    );

    /*  
      Special check:
        If we found the user has disliked all possible combinations, 
        we will randomly return a disliked one with a warning message
     */
    const dislikedFormalOutfits = AllOutfits.filter(
      outfit =>
        outfit.occasions.includes('formal') && outfit.opinion === 'dislike'
    );
    if (allCombinations.length === dislikedFormalOutfits.length) {
      const chosenOutfit =
        dislikedFormalOutfits[randomInt(dislikedFormalOutfits.length)];
      const warning =
        'You have disliked all formal outfits. Maybe you change your opinion on this one!';
      return {
        success: true,
        existing: true,
        warning,
        outfit: chosenOutfit,
      };
    }

    /* Exclude the disliked ones */
    const combinations = allCombinations.filter(combo => {
      const hashId = hashCode(combo[0].id + combo[1].id + combo[2].id);
      const index = dislikedFormalOutfits.findIndex(
        outfit => outfit._id === hashId
      );
      return index === -1;
    });

    /* Randomly choose one combination */
    const chosenCombination = combinations[randomInt(combinations.length)];
    const chosenUpperClothes = chosenCombination[0];
    const chosenTrousers = chosenCombination[1];
    const chosenShoes = chosenCombination[2];

    /* Check if the outfit has existed */
    const hashId = hashCode(
      chosenUpperClothes.id + chosenTrousers.id + chosenShoes.id
    );
    const existingOutfit = AllOutfits.find(outfit => outfit._id === hashId);

    if (existingOutfit) {
      // Existed
      return {
        success: true,
        existing: true,
        outfit: existingOutfit,
        chosenUpperClothes,
        chosenTrousers,
        chosenShoes,
      };
    }

    // Not existed
    return {
      success: true,
      _id: hashId,
      occasions: ['formal'],
      seasons: ['All'],
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
    };
  };

  // Create a normal outfit
  const createNormalOutfit = async () => {
    /* Decide to choose from user liked normal outfits or try to generate a new one */
    const type = randomInt(2);

    /*
          Definition of a normal outfit
          1. does not have 'formal' occasion at all
          2. has multiple occasions, may or may not include 'formal'
         */
    const normalOutfits = AllOutfits.filter(
      outfit =>
        !outfit.occasions.includes('formal') || outfit.occasions.length > 2
    );

    /* Get a user liked normal outfit */
    if (type === 0) {
      const likedNormalOutfits = normalOutfits.filter(
        outfit => outfit.opinion === 'like'
      );

      if (likedNormalOutfits.length) {
        const chosenOutfit =
          LikedFormalOutfits[randomInt(likedNormalOutfits.length)];
        return {
          success: true,
          existing: true,
          outfit: chosenOutfit,
        };
      }
    }

    /*
          Plan:
          1. get user like normal outfits
          2. get all normal combinations
          3. exclude user disliked normal outfits
          4. get weather info
          5. check color restrictions
          6. check for existence
    
          7. (M9) how to avoid duplicated outfits returned today
         */

    const allNormal = AllClothes.filter(c => !c.occasions.includes('formal'));

    let normalOuterwear = allNormal.filter(c => c.category === 'outerwear');
    let normalShirt = allNormal.filter(c => c.category === 'shirt');
    let normalTrousers = allNormal.filter(c => c.category === 'trousers');
    let normalShoes = allNormal.filter(c => c.category === 'shoes');

    /*
      Requirements to return a normal outfit
      1. have normal outerwear or normal shirt
      2. have normal trousers
      3. have normal shoes
     */

    if (
      (!normalOuterwear.length && !normalShirt.length) ||
      !normalTrousers.length ||
      !normalShoes.length
    ) {
      return {
        success: false,
        message: 'Add more clothes to get an outfit!',
      };
    }

    // TODO: need better weather implementation!

    // get weather & season tag
    // await getTodayWeather();

    // const { temperature, description } = todayWhether;
    // let season = getSeasonFromTemperature(temperature);

    let currSeason = getSeasonNorth();

    // filter out other seasons
    normalOuterwear = normalOuterwear.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    normalShirt = normalShirt.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    normalShoes = normalShoes.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    normalTrousers = normalTrousers.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );

    // check if outfit exists
    let allOutfitIds = await (await Outfit.find({ user: userId })).map(
      outfit => outfit._id
    );

    // loop through all current clothing
    // TODO: these loops looks bad bad bad --- look into cartesian product
    // https://stackoverflow.com/questions/12303989/cartesian-product-of-multiple-arrays-in-javascript

    const outfitWithShirt = cartesian(normalShirt, normalTrousers, normalShoes);
    const outfitWithOutwear = cartesian(
      normalOuterwear,
      normalTrousers,
      normalShoes
    );

    let chosenUpperClothes, chosenTrousers, chosenShoes;
    for (const outerwear of normalOuterwear) {
      for (const shirt of normalShirt) {
        for (const trousers of normalTrousers) {
          for (const shoes of normalShoes) {
            let random = randomInt(2);

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

    // TODO (M8): return error if run out of clothes

    return {
      success: true,
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
      occasions: ['normal'], // TODO: what about occasion?
      seasons: [currSeason],
    };
  };

  /**
   * Preparation and setup
   * Initialize the following things
   * 1. all outfits in database
   * 2. all clothes in database
   * 3. today's formal events
   * 4. all outfits generated today
   * 5. formal outfits generated today
   */
  try {
    await getAllOutfits();
    await getAllClothes();
    await getTodayFormalEvents();
    getTodayOutfits();
    getTodayFormalOutfits();
  } catch (exception) {
    LOG.error(exception.message);
    return {
      success: false,
      message: 'Failed to initialize',
      warning: exception.message,
    };
  }

  let result;
  if (!TodayFormalOutfits.length && TodayFormalEvents.length) {
    // Case 1: no formal outfits today and have formal events => create a formal outfit
    result = await createFormalOutfit();
  } else {
    /*  All other three cases:
        Case 2: no formal outfits today and no formal events
        Case 3: have formal outfit today and no formal events
        Case 4: have formal outfit today and have formal events
        => create a normal outfit
     */
    result = await createNormalOutfit();
  }

  const outfit = await saveOutfit(result);
  return outfit;
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

/**
 * Generate the cartesian product of the input arrays
 *
 * e.g.
 * input: [10, 20], [100, 200]
 * output: [[10, 100], [10, 200], [20, 100], [20, 200]]
 *
 * https://stackoverflow.com/questions/12303989/cartesian-product-of-multiple-arrays-in-javascript
 * @param  {...any} a Array
 */
const cartesian = (...a) =>
  a.reduce((a, b) => a.flatMap(d => b.map(e => [d, e].flat())));

module.exports = {
  generateOutfit,
};
