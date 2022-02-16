import { makeSafe } from './karma-utils';

describe('makeSafe()', () => {
  let doneFn: any;
  let anotherDoneFn: any;

  beforeEach(() => {
    doneFn = jasmine.createSpy();
    doneFn.fail = jasmine.createSpy();
    anotherDoneFn = jasmine.createSpy();
    anotherDoneFn.fail = jasmine.createSpy();
  });

  it('Should only call the real done() function once', () => {
    const safeDoneFn = makeSafe(doneFn);
    safeDoneFn();
    safeDoneFn();
    safeDoneFn();
    expect(doneFn).toHaveBeenCalledTimes(1);
    expect(doneFn.fail).toHaveBeenCalledTimes(0);
  });

  it('Should only call the real done.fail() once', () => {
    const safeDoneFn = makeSafe(doneFn);
    safeDoneFn.fail();
    safeDoneFn.fail();
    safeDoneFn.fail();
    expect(doneFn).toHaveBeenCalledTimes(0);
    expect(doneFn.fail).toHaveBeenCalledTimes(1);
  });

  it('Should only call one of done() or done.fail()', () => {
    const safeDoneFn1 = makeSafe(doneFn);
    safeDoneFn1();
    safeDoneFn1.fail();
    expect(doneFn).toHaveBeenCalledTimes(1);
    expect(doneFn.fail).toHaveBeenCalledTimes(0);

    const safeDoneFn2 = makeSafe(anotherDoneFn);
    safeDoneFn2.fail();
    safeDoneFn2();
    expect(anotherDoneFn).toHaveBeenCalledTimes(0);
    expect(anotherDoneFn.fail).toHaveBeenCalledTimes(1);
  });
});
