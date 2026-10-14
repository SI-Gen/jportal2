# WORKFLOW
The release runs on JDK 17. The GraalVM native plugin is Java 17 bytecode, so Maven cannot load it on JDK 11.
The workflow publishes the release to Maven Central and then prepares `pom.xml` for the next development version.

** Note that the release version will be based off the current version in the pom. The pom MUST have a `-SNAPSHOT`.
```xml
<version>1.5.6-SNAPSHOT</version>
```

### WORKFLOW Process
A merge into `master` releases automatically:

1. The Build workflow must succeed on the merge commit.
2. The release runs only when that commit belongs to a pull request merged into `master`. Direct pushes, and the version commits created by the release itself, do not start another release.
3. The patch version is incremented from the current `-SNAPSHOT` in `pom.xml` (for example `1.11.0-SNAPSHOT` becomes release `1.11.0`, then development continues at `1.11.1-SNAPSHOT`).
4. The workflow tags that release, publishes it, uploads the jar, pushes the container image, and queues the native binaries onto the same GitHub release.

To cut a minor or major release instead, run the `Release` workflow manually from the Actions tab and choose `minor` or `major`. A description is optional. The `pom.xml` version must still be a `-SNAPSHOT`.

![Work Flow](/docs/img/workflow.png "Work Flow")



### Contributions
* [GitHub Actions](https://github.com/features/actions)
* [anothrNick/github-tag-action](https://github.com/anothrNick/github-tag-action)
* [qcastel/github-actions-maven-release](https://github.com/qcastel/github-actions-maven-release)
* [WyriHaximus/github-action-get-previous-tag](https://github.com/WyriHaximus/github-action-get-previous-tag)
* [mikepenz/release-changelog-builder-action](https://github.com/mikepenz/release-changelog-builder-action)
* [svenstaro/upload-release-action](https://github.com/svenstaro/upload-release-action)