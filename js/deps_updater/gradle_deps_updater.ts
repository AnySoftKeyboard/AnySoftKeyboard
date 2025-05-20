import fs from 'fs';
import path from 'path';
import { getLatestMavenVersion } from './latest_version_locator.js';
import { DEPENDENCIES, type Dep } from './gradle_deps_to_update.js';

export const update_gradle_deps = async (rootFolder: string, depsToUpdate: Dep[] = DEPENDENCIES): Promise<void> => {
  const newDepsPromises = depsToUpdate.map((dep) =>
    getLatestMavenVersion(dep.mavenUrl, dep.groupId, dep.artifactId).then((newVersion) => {
      console.log(` - ${dep.groupId}:${dep.artifactId}: latest version ${newVersion}`);
      return { dep, newVersion };
    }),
  );
  const newDeps = await Promise.all(newDepsPromises);

  // Writing new versions
  const writePromises = fs
    .globSync(path.join(rootFolder, '**', 'build.gradle'), { withFileTypes: false })
    .map((fileName) => {
      return { fileName, content: fs.readFileSync(fileName, 'utf-8') };
    })
    .map((data) => {
      let newContent = data.content;
      newDeps.forEach((newDep) => {
        const depPattern = new RegExp(`${newDep.dep.groupId}:${newDep.dep.artifactId}:[^'"\s]+`, 'g');
        newContent = newContent.replace(
          depPattern,
          `${newDep.dep.groupId}:${newDep.dep.artifactId}:${newDep.newVersion}`,
        );
      });
      return { fileName: data.fileName, content: newContent };
    })
    .map((data) => fs.promises.writeFile(data.fileName, data.content, { encoding: 'utf-8', flag: 'w' }));

  await Promise.all(writePromises);
};
