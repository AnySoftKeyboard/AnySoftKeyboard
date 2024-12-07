import { expect } from 'chai';
import { describe, it } from 'mocha';
import { isAddOnsFile, getAddOnsFromXml } from './unique_addon_id.js'; // Replace with actual file name

describe('Add-on utilities', () => {
  describe('isAddOnsFile', () => {
    it('should return true for valid add-on file names', () => {
      expect(isAddOnsFile('keyboards.xml')).to.be.true;
      expect(isAddOnsFile('blah_keyboards.xml')).to.be.true;
      expect(isAddOnsFile('pinky_dictionaries.xml')).to.be.true;
      expect(isAddOnsFile('dictionaries.xml')).to.be.true;
      expect(isAddOnsFile('quick_text_keys.xml')).to.be.true;
      expect(isAddOnsFile('rainbow_dash_quick_text_keys.xml')).to.be.true;
      expect(isAddOnsFile('themes.xml')).to.be.true;
      expect(isAddOnsFile('boo_themes.xml')).to.be.true;
    });

    it('should return false for invalid add-on file names', () => {
      expect(isAddOnsFile('invalid.xml')).to.be.false;
      expect(isAddOnsFile('keyboard.xml')).to.be.false; // Missing 's'
      expect(isAddOnsFile('themes.json')).to.be.false; // Wrong extension
    });

    it('should handle case-insensitive file names', () => {
      expect(isAddOnsFile('LANG_KEYBOARDS.XML')).to.be.true;
      expect(isAddOnsFile('What_Dictionaries.xml')).to.be.true;
    });
  });

  describe('getAddOnsFromXml', () => {
    it('should extract add-on IDs from valid XML with multiple add-ons', () => {
      const xmlRoot = {
        Keyboards: {
          Keyboard: [{ attr_id: 'keyboard1' }, { attr_id: 'keyboard2' }],
        },
      };
      expect(getAddOnsFromXml(xmlRoot)).to.deep.equal(['keyboard1', 'keyboard2']);
    });

    it('should extract add-on ID from valid XML with a single add-on', () => {
      const xmlRoot = {
        Dictionaries: {
          Dictionary: { attr_id: 'dictionary1' },
        },
      };
      expect(getAddOnsFromXml(xmlRoot)).to.deep.equal(['dictionary1']);
    });

    it('should return an empty array for XML without supported add-ons', () => {
      const xmlRoot = {
        Unsupported: {
          Item: { attr_id: 'item1' },
        },
      };
      expect(getAddOnsFromXml(xmlRoot)).to.deep.equal([]);
    });

    it('should handle empty add-ons list in XML', () => {
      const xmlRoot = {
        Keyboards: {
          Keyboard: [],
        },
      };
      expect(getAddOnsFromXml(xmlRoot)).to.deep.equal([]);
    });
  });
});
