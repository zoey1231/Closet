const { createOneOutfit } = require('../../controller/outfits-controller')

jest.mock('../../model/clothes');
const Clothes = require('../../model/clothes');

jest.mock('../../model/outfit');
const Outfit = require('../../model/outfit');

describe('Outfits controller with database mocking', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('createOneOutfit expect database exception: Outfit found', () => {
    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "1"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "2"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "3"
    });

    jest.spyOn(Outfit, 'findOneAndUpdate').mockImplementationOnce(() => {
      return {
        created: "CREATED",
        opinion: "OPINION"
      }
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        clothes: ["1", "2", "3"],
        occasions: ["Home"],
        seasons: ["All"]
      }
    }

    const result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });


  it('createOneOutfit expect database exception: Outfit save', () => {
    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "1"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "2"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "3"
    });

    jest.spyOn(Outfit, 'findOneAndUpdate').mockImplementationOnce(() => {
      return null;
    });

    jest.spyOn(Outfit.prototype, 'save').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        clothes: ["1", "2", "3"],
        occasions: ["Home"],
        seasons: ["All"]
      }
    }

    const result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('createOneOutfit expect database exception: Outfit findOneAndUpdate', () => {
    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "1"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "2"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "3"
    });

    jest.spyOn(Outfit, 'findOneAndUpdate').mockImplementationOnce(() => {
      throw "Error!!!";
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        clothes: ["1", "2", "3"],
        occasions: ["Home"],
        seasons: ["All"]
      }
    }

    const result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('createOneOutfit expect database exception: Clothes findById', () => {
    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "1"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "2"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
     throw "Error!!!"
    });

    const req = {
      userData: {
        userId: "USERID"
      },
      body: {
        clothes: ["1", "2", "3"],
        occasions: ["Home"],
        seasons: ["All"]
      }
    }

    let result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      return "1"
    });

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);

    jest.spyOn(Clothes, 'findById').mockImplementationOnce(() => {
      throw "Error!!!"
    });

    result = createOneOutfit(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });
});
