import { DEPENDENCIES, type Dep } from './gradle_deps_to_update.js'; // Adjust path if necessary based on actual file location
import test from 'node:test';
import assert from 'node:assert';

test.describe('gradle_deps_to_update tests', () => {
  test('DEPENDENCIES should be an array', () => {
    assert.ok(Array.isArray(DEPENDENCIES), 'DEPENDENCIES should be an array.');
  });

  test('DEPENDENCIES elements should have correct properties if not empty', () => {
    if (DEPENDENCIES.length > 0) {
      const firstDep: Dep = DEPENDENCIES[0];
      assert.strictEqual(typeof firstDep.mavenUrl, 'string', 'mavenUrl should be a string');
      assert.strictEqual(typeof firstDep.groupId, 'string', 'groupId should be a string');
      assert.strictEqual(typeof firstDep.artifactId, 'string', 'artifactId should be a string');
      assert.strictEqual(typeof firstDep.friendlyName, 'string', 'friendlyName should be a string');
    } else {
      // If the array is empty, this test is trivially true.
      // Alternatively, could assert that it's non-empty if that's a requirement,
      // but "simple tests" implies just checking structure if present.
      assert.ok(true, 'DEPENDENCIES array is empty, test passes.');
    }
  });

  test('All DEPENDENCIES elements should conform to Dep structure if array is not empty', () => {
    if (DEPENDENCIES.length > 0) {
      for (const dep of DEPENDENCIES) {
        assert.strictEqual(typeof dep.mavenUrl, 'string', `mavenUrl should be a string for ${dep.friendlyName}`);
        assert.ok(dep.mavenUrl.length > 0, `mavenUrl should not be empty for ${dep.friendlyName}`);
        assert.strictEqual(typeof dep.groupId, 'string', `groupId should be a string for ${dep.friendlyName}`);
        assert.ok(dep.groupId.length > 0, `groupId should not be empty for ${dep.friendlyName}`);
        assert.strictEqual(typeof dep.artifactId, 'string', `artifactId should be a string for ${dep.friendlyName}`);
        assert.ok(dep.artifactId.length > 0, `artifactId should not be empty for ${dep.friendlyName}`);
        assert.strictEqual(typeof dep.friendlyName, 'string', `friendlyName should be a string for ${dep.friendlyName}`);
        assert.ok(dep.friendlyName.length > 0, `friendlyName should not be empty for ${dep.friendlyName}`);
      }
    } else {
      assert.ok(true, 'DEPENDENCIES array is empty, structural test passes.');
    }
  });
});
