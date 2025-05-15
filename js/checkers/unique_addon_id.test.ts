import test from 'node:test';
import assert from 'node:assert';
import { isAddOnsFile, getAddOnsFromXml } from './unique_addon_id.js';

test.describe('Add-on utilities', () => {
  test.describe('isAddOnsFile', () => {
    test.test('should return true for valid add-on file names', () => {
      assert.equal(isAddOnsFile('keyboards.xml'), true);
      assert.equal(isAddOnsFile('blah_keyboards.xml'), true);
      assert.equal(isAddOnsFile('pinky_dictionaries.xml'), true);
      assert.equal(isAddOnsFile('dictionaries.xml'), true);
      assert.equal(isAddOnsFile('quick_text_keys.xml'), true);
      assert.equal(isAddOnsFile('rainbow_dash_quick_text_keys.xml'), true);
      assert.equal(isAddOnsFile('themes.xml'), true);
      assert.equal(isAddOnsFile('boo_themes.xml'), true);
    });

    test.test('should return false for invalid add-on file names', () => {
      assert.equal(isAddOnsFile('invalid.xml'), false);
      assert.equal(isAddOnsFile('keyboard.xml'), false); // Missing 's'
      assert.equal(isAddOnsFile('themes.json'), false); // Wrong extension
    });

    test.test('should handle case-insensitive file names', () => {
      assert.equal(isAddOnsFile('LANG_KEYBOARDS.XML'), true);
      assert.equal(isAddOnsFile('What_Dictionaries.xml'), true);
    });
  });

  test.describe('getAddOnsFromXml', () => {
    test.test('should extract add-on IDs from valid XML with multiple add-ons', () => {
      const xmlRoot = {
        Keyboards: {
          Keyboard: [{ attr_id: 'keyboard1' }, { attr_id: 'keyboard2' }],
        },
      };
      assert.deepEqual(getAddOnsFromXml(xmlRoot), ['keyboard1', 'keyboard2']);
    });

    test.test('should extract add-on ID from valid XML with a single add-on', () => {
      const xmlRoot = {
        Dictionaries: {
          Dictionary: { attr_id: 'dictionary1' },
        },
      };
      assert.deepEqual(getAddOnsFromXml(xmlRoot), ['dictionary1']);
    });

    test.test('should return an empty array for XML without supported add-ons', () => {
      const xmlRoot = {
        Unsupported: {
          Item: { attr_id: 'item1' },
        },
      };
      assert.deepEqual(getAddOnsFromXml(xmlRoot), []);
    });

    test.test('should handle empty add-ons list in XML', () => {
      const xmlRoot = {
        Keyboards: {
          Keyboard: [],
        },
      };
      assert.deepEqual(getAddOnsFromXml(xmlRoot), []);
    });
  });
});
