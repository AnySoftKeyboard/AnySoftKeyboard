import test from 'node:test';
import assert from 'node:assert';
import {
  // Named imports for clarity
  calculateDeploymentName,
  promoteOnWednesday,
  promoteByDay,
} from './deployment_config.js';

test.describe('deployment_config', () => {
  test.describe('promoteOnWednesday', () => {
    test.test('returns 1 on Wednesday', () => {
      // 2024-06-12 is a Wednesday
      const wednesday = new Date('2024-06-12T12:00:00Z').getTime();
      assert.equal(promoteOnWednesday(wednesday, 0), 1);
    });
    test.test('returns 0 on other days', () => {
      // 2024-06-13 is a Thursday
      const thursday = new Date('2024-06-13T12:00:00Z').getTime();
      assert.equal(promoteOnWednesday(thursday, 0), 0);
      // 2024-06-09 is a Sunday
      const sunday = new Date('2024-06-09T12:00:00Z').getTime();
      assert.equal(promoteOnWednesday(sunday, 0), 0);
    });
  });

  test.describe('promoteByDay', () => {
    test.test('returns 0 when same day', () => {
      const now = Date.now();
      assert.equal(promoteByDay(now, now), 0);
    });
    test.test('returns 1 after one day', () => {
      const day1 = new Date('2024-06-10T00:00:00Z').getTime();
      const day2 = new Date('2024-06-11T00:00:00Z').getTime();
      assert.equal(promoteByDay(day2, day1), 1);
    });
    test.test('returns 2 after two days', () => {
      const day1 = new Date('2024-06-10T00:00:00Z').getTime();
      const day3 = new Date('2024-06-12T00:00:00Z').getTime();
      assert.equal(promoteByDay(day3, day1), 2);
    });
  });

  test.describe('calculateDeploymentName', () => {
    test.test('main ref, ime shard', () => {
      assert.equal(calculateDeploymentName('main', 'ime'), 'imeMain');
    });
    test.test('main ref, addOns shard', () => {
      assert.equal(calculateDeploymentName('main', 'addOns'), 'addOnsMain');
    });
    test.test('non-main ref, ime shard', () => {
      assert.equal(calculateDeploymentName('release_branch', 'ime'), 'imeProduction');
    });
    test.test('non-main ref, addOns shard', () => {
      assert.equal(calculateDeploymentName('release_branch', 'addOns'), 'addOnsProduction');
    });
  });
});
