import { parseStringPromise } from 'xml2js';
import fetch, { Response } from 'node-fetch';

type Fetcher = (url: string) => Promise<Response>;

export async function getLatestMavenVersion(
  mavenRepoUrl: string,
  groupId: string,
  artifactId: string,
  fetcher: Fetcher = fetch,
): Promise<string> {
  const metadataUrl = `${mavenRepoUrl}/${groupId.replace(/\./g, '/')}/${artifactId}/maven-metadata.xml`;

  try {
    const response = await fetcher(metadataUrl);
    if (!response.ok) {
      throw new Error(`Failed to fetch XML metadata from ${metadataUrl}: ${response.status} ${response.statusText}`);
    }
    const xml = await response.text();
    const parsed = await parseStringPromise(xml);

    if (!parsed || typeof parsed !== 'object') {
      throw new Error('Invalid XML structure: Root element missing or invalid.');
    }
    if (!parsed.metadata) {
      throw new Error('Invalid XML structure: <metadata> tag missing.');
    }
    if (!parsed.metadata.versioning || !parsed.metadata.versioning[0]) {
      throw new Error('Invalid XML structure: <versioning> tag missing or empty.');
    }
    
    const versioning = parsed.metadata.versioning[0];
    if (!versioning.versions || !versioning.versions[0]) {
      throw new Error('Invalid XML structure: <versions> tag missing or empty inside <versioning>.');
    }

    const versionsNode = versioning.versions[0];
    if (!versionsNode.version || !Array.isArray(versionsNode.version) || versionsNode.version.length === 0) {
      throw new Error('Invalid XML structure: <version> tags missing, not an array, or empty inside <versions>.');
    }

    const versions: string[] = versionsNode.version;

    const filteredVersions = versions.filter(
      (v) => !v.toLowerCase().includes('beta') && !v.toLowerCase().includes('rc')
    );

    if (filteredVersions.length === 0) {
      throw new Error('No stable versions found.');
    }

    // Sort versions (simple string sort might not be perfect for all versioning schemes,
    // but is a common approach for simple x.y.z).
    // A more robust solution might involve a version comparison library.
    filteredVersions.sort((a, b) => {
      const partsA = a.split('.').map(Number);
      const partsB = b.split('.').map(Number);
      for (let i = 0; i < Math.max(partsA.length, partsB.length); i++) {
        const numA = partsA[i] || 0;
        const numB = partsB[i] || 0;
        if (numA !== numB) {
          return numB - numA; // Sort descending
        }
      }
      return 0;
    });

    return filteredVersions[0];
  } catch (error) {
    if (error instanceof Error) {
      // Re-throw known errors, or wrap unknown errors for more context
      throw error;
    }
    throw new Error(`An unexpected error occurred while fetching the latest Maven version: ${String(error)}`);
  }
}

interface GitHubRelease {
  tag_name: string;
  prerelease: boolean;
  name?: string; // Optional name field
  body?: string; // Optional body field for release notes
}

export async function getLatestGitHubRelease(
  owner: string,
  repo: string,
  fetcher: Fetcher = fetch,
): Promise<string> {
  const apiUrl = `https://api.github.com/repos/${owner}/${repo}/releases`;

  try {
    const response = await fetcher(apiUrl);
    if (!response.ok) {
      throw new Error(`Failed to fetch releases from ${apiUrl}: ${response.status} ${response.statusText}`);
    }
    const releases: GitHubRelease[] = await response.json();

    // This is where malformed JSON will cause issues, as per the test scenarios.
    const stableReleases = releases
      .filter((r) => r.prerelease === false && r.tag_name) // Ensure tag_name exists
      .map((r) => r.tag_name)
      // Basic sort for version tags like v1.2.3, 1.2.3, v1.2.3-suffix
      // More complex sorting might be needed for production.
      .sort((a, b) => {
        // Remove 'v' prefix and any suffixes like -alpha, -beta for comparison
        const verA = a.replace(/^v/, '').split('-')[0];
        const verB = b.replace(/^v/, '').split('-')[0];
        const partsA = verA.split('.').map(Number);
        const partsB = verB.split('.').map(Number);

        for (let i = 0; i < Math.max(partsA.length, partsB.length); i++) {
          const numA = partsA[i] || 0;
          const numB = partsB[i] || 0;
          if (numA !== numB) {
            return numB - numA; // Sort descending
          }
        }
        return 0;
      });

    if (stableReleases.length === 0) {
      throw new Error('No stable release tags found.');
    }

    return stableReleases[0];
  } catch (error) {
    if (error instanceof Error) {
      // Re-throw known errors (like TypeError from .filter on non-array)
      // or wrap unknown errors for more context
      throw error;
    }
    // Wrap non-Error exceptions
    throw new Error(`An unexpected error occurred while fetching the latest GitHub release: ${String(error)}`);
  }
}
