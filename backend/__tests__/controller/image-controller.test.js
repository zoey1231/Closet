const { postImage, deleteImage } = require('../../controller/image-controller')

const path = require('path');
const fs = require('fs');

describe('Image controller tests mocking file system', () => {
  it('file system post exception', async () => {
    jest.spyOn(fs.promises, 'unlink').mockImplementationOnce(() =>
      Promise.reject(new Error('ERROR!'))
    );

    const req = {
      params: {
        clothingId: "CLOTHING_ID",
        userId: "USER_ID"
      },
      userData: {
        userId: "USER_ID"
      },
      file: {
        path: "../backend/static/And you don't seem to understand.png",
        originalname: "And you don't seem to understand.whatever"
      }
    }

    const result = postImage(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('file system delete exception', async () => {
    jest.spyOn(path, 'join').mockImplementationOnce(() =>
      "TARGET_PATH"
    );

    jest.spyOn(fs.promises, 'access').mockImplementationOnce(() =>
      Promise.resolve()
    );

    jest.spyOn(fs.promises, 'unlink').mockImplementationOnce(() =>
      Promise.reject(new Error('ERROR!'))
    );

    const req = {
      params: {
        clothingId: "CLOTHING_ID",
        userId: "USER_ID"
      },
      userData: {
        userId: "USER_ID"
      },
      file: {
        path: "../backend/static/And you don't seem to understand.png",
        originalname: "And you don't seem to understand.whatever"
      }
    }

    const result = deleteImage(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

  it('file system post exception', async () => {
    jest.spyOn(path, 'join').mockImplementation(() =>
      "TARGET_PATH"
    );

    jest.spyOn(fs.promises, 'unlink').mockImplementationOnce(() =>
      Promise.resolve()
    );

    jest.spyOn(fs.promises, 'mkdir').mockImplementationOnce(() =>
      Promise.reject(new Error('ERROR!'))
    );

    const req = {
      params: {
        clothingId: "CLOTHING_ID",
        userId: "USER_ID"
      },
      userData: {
        userId: "USER_ID"
      },
      file: {
        path: "../backend/static/And you don't seem to understand.png",
        originalname: "And you don't seem to understand.png"
      }
    }

    const result = postImage(req, null, jest.fn());
    expect(result).toBeTruthy();
    expect(result).toBeInstanceOf(Promise);
  });

});
