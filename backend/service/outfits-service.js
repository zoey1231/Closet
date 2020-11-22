require('dotenv').config();
const { getCalendarEvents } = require('./calendar-service');
const { getWeatherInfo } = require('./weather-service');
const {
  timestampToDate,
  getTodayDateInTimezone,
} = require('../utils/time-helper');
const { hashCode, randomInt } = require('../utils/hash');
const LOG = require('../utils/logger');

const Clothes = require('../model/clothes');
const Outfit = require('../model/outfit');
const TodayOutfit = require('../model/today-outfit');

const FORMAL_KEYWORDS = [
  'conference',
  'interview',
  'meeting',
  'presentation',
  'speech',
];

const ALLOWED_COLORS = ['Grey', 'White', 'Black'];

/**
 * Entry point for complex logic
 *
 * @param {String} user_id
 */
const generateOutfit = async user_id => {
  const userId = user_id;

  /* All necessary variables */
  // Need to initialize during preparation
  let AllOutfits = [];
  let AllClothes = [];
  let TodayFormalEvents = [];
  let TodayOutfits = [];
  let TodayOutfitsIds = [];
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
  const getTodayOutfits = async () => {
    const today = getTodayDateInTimezone();

    try {
      outfitsReturnedToday = await TodayOutfit.find({
        user: userId,
        returnedTime: today,
      });
    } catch (exception) {
      LOG.error(exception.message);
    }

    for (const outfit of outfitsReturnedToday) {
      TodayOutfitsIds.push(outfit._id);
    }

    TodayOutfits = AllOutfits.filter(outfit =>
      TodayOutfitsIds.includes(outfit._id)
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
      response = await getWeatherInfo(userId);
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
        message: result.message,
        manual: result.manual,
        warning: result.warning,
      };
    }

    // Input an existing outfit, no need to save it
    if (result.existing) {
      let chosenUpperClothes;
      let chosenTrousers;
      let chosenShoes;

      const { success, warning, outfit } = result;
      const {
        _id,
        clothes,
        created,
        occasions,
        seasons,
        opinion,
        user,
      } = outfit;

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
          user,
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
        message: 'Failed to save outfit, please try again later',
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
          likedFormalOutfits[randomInt(likedFormalOutfits.length)];

        /* Add to today returned outfits */
        const newTodayOutfit = TodayOutfit({
          _id: chosenOutfit._id,
          returnedTime: getTodayDateInTimezone(),
          user: userId,
        });

        try {
          await newTodayOutfit.save();
        } catch (exception) {
          LOG.error(exception.message);
          return {
            success: false,
            message:
              'There is a problem when generating the outfit, please try again later',
          };
        }

        return {
          success: true,
          existing: true,
          outfit: chosenOutfit,
        };
      }
    }

    /* Try to generated a new formal outfit */
    const allFormal = AllClothes.filter(c => c.occasions.includes('formal'));
    const formalOuterwear = allFormal.filter(
      c => c.category.toLowerCase() === 'outerwear'
    );
    const formalShirt = allFormal.filter(
      c => c.category.toLowerCase() === 'shirt'
    );
    const formalTrousers = allFormal.filter(
      c => c.category.toLowerCase() === 'trousers'
    );
    const formalShoes = allFormal.filter(
      c => c.category.toLowerCase() === 'shoes'
    );

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
        we will a warning message to let the user manually create an outfit for himself/herself
     */
    const dislikedFormalOutfits = AllOutfits.filter(
      outfit =>
        outfit.occasions.includes('formal') && outfit.opinion === 'dislike'
    );
    if (allCombinations.length === dislikedFormalOutfits.length) {
      let message =
        'We notice you have the following events today, but you disliked all the generated clothes!\n';
      TodayFormalEvents.forEach(event => {
        message += `${event}\n`;
      });
      message += 'Do you want to generate an outfit manually for yourself?';
      return {
        success: false,
        manual: true,
        message,
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

    /* Add to today returned outfits */
    const newTodayOutfit = TodayOutfit({
      _id: hashId,
      returnedTime: getTodayDateInTimezone(),
      user: userId,
    });

    try {
      await newTodayOutfit.save();
    } catch (exception) {
      LOG.error(exception.message);
      return {
        success: false,
        message:
          'There is a problem when generating the outfit, please try again later',
      };
    }

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
    /** Preparation **/

    /*
      Definition of a normal outfit
      1. does not have 'formal' occasion at all
      2. has multiple occasions, may or may not include 'formal'
    */
    const normalOutfits = AllOutfits.filter(
      outfit =>
        !outfit.occasions.includes('formal') || outfit.occasions.length > 1
    );

    /* Obtain weather info */
    await getTodayWeather();
    const currSeason = getSeasonFromTemperature(TodayWhether.temperature.max);

    /* Potential warning message*/
    let warning;

    /** Main logic starts **/

    /* Decide to choose from user liked normal outfits or try to generate a new one */
    const type = randomInt(2);

    /* Get a user liked normal outfit */
    if (type === 0) {
      /* Find user-liked outfits that fit the weather (currSeason and All) */
      const likedNormalOutfitsWithSeason = normalOutfits.filter(
        outfit =>
          outfit.opinion === 'like' &&
          (outfit.seasons.includes(currSeason) ||
            outfit.seasons.includes('All'))
      );

      if (likedNormalOutfitsWithSeason.length) {
        numOfTries = 0;

        while (numOfTries < likedNormalOutfitsWithSeason.length) {
          const chosenOutfit =
            likedNormalOutfitsWithSeason[
              randomInt(likedNormalOutfitsWithSeason.length)
            ];

          if (!TodayOutfitsIds.includes(chosenOutfit._id)) {
            /* Add to today returned outfits */
            const newTodayOutfit = TodayOutfit({
              _id: chosenOutfit._id,
              returnedTime: getTodayDateInTimezone(),
              user: userId,
            });

            try {
              await newTodayOutfit.save();
            } catch (exception) {
              LOG.error(exception.message);
              return {
                success: false,
                message:
                  'There is a problem when generating the outfit, please try again later',
              };
            }

            return {
              success: true,
              existing: true,
              outfit: chosenOutfit,
            };
          }

          numOfTries++;
        }
      }
    }

    /* Try to generated a new normal outfit */
    const allNormal = AllClothes.filter(
      c => !c.occasions.includes('formal') || c.occasions.length > 1
    );
    let normalOuterwear = allNormal.filter(
      c => c.category.toLowerCase() === 'outerwear'
    );
    let normalShirt = allNormal.filter(
      c => c.category.toLowerCase() === 'shirt'
    );
    let normalTrousers = allNormal.filter(
      c => c.category.toLowerCase() === 'trousers'
    );
    let normalShoes = allNormal.filter(
      c => c.category.toLowerCase() === 'shoes'
    );

    /*
      Requirements to return a normal outfit
      1. have normal outerwear or normal shirt
      2. have normal trousers
      3. have normal shoes
     */

    /* 
      Case 1: user does not have enough normal clothes 
              => add a warning and return as a failure 
     */
    if (
      (!normalOuterwear.length && !normalShirt.length) ||
      !normalTrousers.length ||
      !normalShoes.length
    ) {
      return {
        success: false,
        message:
          'Too few clothes in your closet, please add more clothes to get an outfit!',
      };
    }

    /* Fit normal clothes that fit that the current season */
    const normalOuterwearWithSeason = normalOuterwear.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    const normalShirtWithSeason = normalShirt.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    const normalTrousersWithSeason = normalTrousers.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );
    const normalShoesWithSeason = normalShoes.filter(
      c => c.seasons.includes(currSeason) || c.seasons.includes('All')
    );

    /* 
      Case 2: user does not have enough normal clothes that fit the current weather 
              => add a warning and generate a normal outfit without considering the weather
     */
    if (
      (!normalOuterwearWithSeason.length && !normalShirtWithSeason.length) ||
      !normalTrousersWithSeason.length ||
      !normalShoesWithSeason.length
    ) {
      warning =
        "There are not enough clothes to fit today's weather in your closet";
    } else {
      normalOuterwear = normalOuterwearWithSeason;
      normalShirt = normalShirtWithSeason;
      normalTrousers = normalTrousersWithSeason;
      normalShoes = normalShoesWithSeason;
    }

    /* Generate all possible combinations */
    const allCombinations = cartesian(
      [...normalOuterwear, ...normalShirt],
      normalTrousers,
      normalShoes
    );

    /*  
      Special check:
        If we found the user has disliked all possible combinations, 
        we will a warning message to let the user manually create an outfit for himself/herself
     */
    const dislikedNormalOutfits = normalOutfits.filter(
      outfit => outfit.opinion === 'dislike'
    );
    if (allCombinations.length === dislikedNormalOutfits.length) {
      const message =
        'You have disliked all normal outfits. Do you want to generate an outfit manually for yourself';
      return {
        success: false,
        manual: true,
        message,
      };
    }

    /* Exclude the disliked ones */
    const combinations = allCombinations.filter(combo => {
      const hashId = hashCode(combo[0].id + combo[1].id + combo[2].id);
      const index = dislikedNormalOutfits.findIndex(
        outfit => outfit._id === hashId
      );
      return index === -1;
    });

    let upperClothes, trousers, shoes;
    let colors = [];
    let color;
    const combinationWithColorRestriction = combinations.filter(combo => {
      upperClothes = combo[0];
      trousers = combo[1];
      shoes = combo[2];
      colors = [upperClothes.color, trousers.color, shoes.color];
      color = upperClothes.color;

      /*
        A simple color restriction (the following color combination is NOT allowed)
        1. Except for Grey, White, Black, All three clothes have the same color
        2. Green and Red 
      */
      if (colors.includes('Green') && colors.includes('Red')) {
        return false;
      }
      if (
        !ALLOWED_COLORS.includes(color) &&
        trousers.color === color &&
        shoes.color === color
      ) {
        return false;
      }
      return true;
    });

    if (!combinationWithColorRestriction.length) {
      return {
        success: false,
        message:
          'Could find a good color combination, please add more clothes in your closet!',
      };
    }

    /* Randomly choose one combination (with color restrictions) */
    let chosenUpperClothes, chosenTrousers, chosenShoes;
    let numOfTries = 0;
    let hashId;

    for (const combo of combinationWithColorRestriction) {
      chosenUpperClothes = combo[0];
      chosenTrousers = combo[1];
      chosenShoes = combo[2];

      hashId = hashCode(
        chosenUpperClothes.id + chosenTrousers.id + chosenShoes.id
      );

      if (!TodayOutfitsIds.includes(hashId)) {
        /* Add to today returned outfits */
        const newTodayOutfit = TodayOutfit({
          _id: hashId,
          returnedTime: getTodayDateInTimezone(),
          user: userId,
        });

        try {
          await newTodayOutfit.save();
        } catch (exception) {
          LOG.error(exception.message);
          return {
            success: false,
            message:
              'There is a problem when generating the outfit, please try again later',
          };
        }

        break;
      }

      numOfTries++;
    }

    /* End while loop with failure */
    if (numOfTries === combinationWithColorRestriction.length) {
      return {
        success: false,
        message:
          'We have generated all possible outfits. Do you want to create one manually?',
      };
    }

    /* Check if the outfit has existed */
    const existingOutfit = AllOutfits.find(outfit => outfit._id === hashId);

    if (existingOutfit) {
      // Existed
      return {
        success: true,
        existing: true,
        warning,
        outfit: existingOutfit,
        chosenUpperClothes,
        chosenTrousers,
        chosenShoes,
      };
    }

    return {
      success: true,
      warning,
      _id: hashId,
      occasions: ['normal'],
      seasons: [currSeason],
      chosenUpperClothes,
      chosenTrousers,
      chosenShoes,
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
    await getTodayOutfits();
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
 * Get season from temperature
 * @param {int} temperature
 * @return {String} Season "Spring" "Summer" "Fall" "Winter"
 */
const getSeasonFromTemperature = temperature => {
  const temp = parseInt(temperature, 10);
  if (temp > 20) return 'Summer';
  if (temp <= 20 && temp >= 15) return 'Fall';
  if (temp < 15 && temp >= 10) return 'Spring';
  if (temp < 10) return 'Winter';
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
