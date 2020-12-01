jest.mock('../../service/calendar-service');
const { getCalendarEvents } = require('../../service/calendar-service');

jest.mock('../../service/weather-service');
const { getWeatherInfo } = require('../../service/weather-service');

const Outfit = require('../../model/outfit');
jest.mock('../../model/outfit');

const Clothes = require('../../model/clothes');
jest.mock('../../model/clothes');

const TodayOutfit = require('../../model/today-outfit');
jest.mock('../../model/today-outfit.js');

const { generateOutfit } = require('../../service/outfits-service');

describe('Outfit service tests', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  const userId = 'USER_ID';
  const oneClothing = {
    _id: '5f9f59acc6f89303de2fd47a',
    seasons: [],
    occasions: ['Home'],
    category: 'Outerwear',
    color: 'Red',
    name: '',
    user: userId,
  };
  const outfit = {
    _id: -1793359594,
    clothes: [
      '5fa7611755db7d66c8bc38f9',
      '5fa7853455db7d66c8bc38ff',
      '5fa7857555db7d66c8bc3901',
    ],
    occasions: ['normal'],
    seasons: ['Winter'],
    opinion: 'unknown',
    user: userId,
    created: new Date(),
  };

  it('failed to initialize', async () => {
    const result = await generateOutfit(userId);
    expect(result).toBeTruthy();
    expect(result.success).toEqual(false);
    expect(result.message).toEqual('Failed to initialize');
    expect(result.warning).toBeTruthy();
  });

  it('failed to initialize: database exceptions', async () => {
    jest
      .spyOn(Outfit, 'find')
      .mockImplementationOnce(() => { throw "Error!!!" });
    jest
      .spyOn(Clothes, 'find')
      .mockImplementationOnce(() => { throw "Error!!!" });

    getCalendarEvents.mockImplementationOnce(() => {
      throw "Error!!!";
    });

    jest
      .spyOn(TodayOutfit, 'find')
      .mockImplementationOnce(() => { throw "Error!!!" });

    const result = await generateOutfit(userId);
    expect(result).toBeTruthy();
    expect(result.success).toEqual(false);
    expect(result.message).toEqual('Failed to initialize');
    expect(result.warning).toBeTruthy();
  });

  it('failed generate outfit: need more clothes', async () => {
    jest
      .spyOn(Outfit, 'find')
      .mockImplementation(() => Promise.resolve([outfit]));
    jest
      .spyOn(Clothes, 'find')
      .mockImplementation(() => Promise.resolve([oneClothing]));

    getCalendarEvents.mockImplementation(() => {
      return {
        events: [
          {
            summary: 'regular event',
          },
        ],
      };
    });

    getWeatherInfo.mockImplementation(() => {
      return {
        today: {
          temp: {
            max: 15,
          },
          weather: [
            {
              description: 'weather description!',
            },
          ],
        },
      };
    });

    const result = await generateOutfit(userId);

    expect(result).toBeTruthy();
    expect(result.success).toEqual(false);
    expect(result.message).toEqual(
      'Too few clothes in your closet, please add more clothes to get an outfit!'
    );
  });

  const normalClothes = [
    oneClothing,
    {
      _id: '5f9f59acc6f89303de2fd47b',
      seasons: [],
      occasions: ['Home'],
      category: 'Shirt',
      color: 'Yellow',
      name: '',
      user: userId,
    },
    {
      _id: '5f9f59acc6f89303de2fd47c',
      seasons: [],
      occasions: ['Home'],
      category: 'Trousers',
      color: 'Pink',
      name: '',
      user: userId,
    },
    {
      _id: '5f9f59acc6f89303de2fd47d',
      seasons: [],
      occasions: ['Home'],
      category: 'Shoes',
      color: 'Blue',
      name: '',
      user: userId,
    },
  ];

  it('generate normal outfit', async () => {
    jest
      .spyOn(Outfit, 'find')
      .mockImplementationOnce(() => Promise.resolve([outfit]));
    jest
      .spyOn(Clothes, 'find')
      .mockImplementation(() => Promise.resolve(normalClothes));

    getCalendarEvents.mockImplementation(() => {
      return {
        events: [
          {
            summary: 'regular event',
          },
        ],
      };
    });

    getWeatherInfo.mockImplementation(() => {
      return {
        today: {
          temp: {
            max: 15,
          },
          weather: [
            {
              description: 'weather description!',
            },
          ],
        },
      };
    });

    let result;
    result = await generateOutfit(userId);
    expect(result).toBeTruthy();
    expect(result.success).toBeTruthy();
    expect(result.message).toEqual('New outfit generated successfully!');
    expect(result.outfit).toBeTruthy();
    expect(result.outfit.user).toEqual(userId);
    expect(result.outfit.occasions).toEqual(['normal']);
    expect(result.outfit.seasons).toEqual(['Fall']);
    expect(result.outfit.chosenUpperClothes).toBeTruthy();
    expect(result.outfit.chosenTrousers).toBeTruthy();
    expect(result.outfit.chosenShoes).toBeTruthy();

    result = await generateOutfit(userId);
    expect(result).toBeTruthy();
    expect(result.success).toBeFalsy();
    expect(result.message).toEqual(
      'We have generated all possible outfits. Do you want to create one manually?'
    );
    expect(result.manual).toBeTruthy();
  });

  const formalClothes = [
    oneClothing,
    {
      _id: '5f9f59acc6f89303de2fd47b',
      seasons: [],
      occasions: ['Formal', 'formal'],
      category: 'Shirt',
      color: 'Yellow',
      name: '',
      user: userId,
    },
    {
      _id: '5f9f59acc6f89303de2fd47c',
      seasons: [],
      occasions: ['Formal', 'formal'],
      category: 'Trousers',
      color: 'Pink',
      name: '',
      user: userId,
    },
    {
      _id: '5f9f59acc6f89303de2fd47d',
      seasons: [],
      occasions: ['Formal', 'formal'],
      category: 'Shoes',
      color: 'Blue',
      name: '',
      user: userId,
    },
  ];

  it('generate formal outfit', async () => {
    jest
      .spyOn(Outfit, 'find')
      .mockImplementationOnce(() => Promise.resolve([outfit]));
    jest
      .spyOn(Clothes, 'find')
      .mockImplementation(() => Promise.resolve(formalClothes));

    getCalendarEvents.mockImplementation(() => {
      return {
        events: [
          {
            summary: 'formal meeting event',
          },
        ],
      };
    });

    getWeatherInfo.mockImplementation(() => {
      return {
        today: {
          temp: {
            max: 15,
          },
          weather: [
            {
              description: 'weather description!',
            },
          ],
        },
      };
    });

    let result;
    result = await generateOutfit(userId);
    expect(result).toBeTruthy();
    expect(result.success).toBeTruthy();
    expect(result.message).toEqual('New outfit generated successfully!');
    expect(result.outfit).toBeTruthy();
    expect(result.outfit.user).toEqual(userId);
    expect(result.outfit.occasions).toEqual(['formal']);
    expect(result.outfit.seasons).toEqual(['All']);
    expect(result.outfit.chosenUpperClothes).toBeTruthy();
    expect(result.outfit.chosenTrousers).toBeTruthy();
    expect(result.outfit.chosenShoes).toBeTruthy();
  });
});
