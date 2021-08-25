# WORKFLOW
The workflow is based off the git-repo [SI-Gen/github-actions-maven-release](https://github.com/SI-Gen/github-actions-maven-release).
The workflow will provide a release to nexus and then prep the pom for the next development release based on the input provided

** Note that the release version will be based off the current version in the pom. The pom MUST have a `-SNAPSHOT`.
```xml
<version>1.5.6-SNAPSHOT</version>
```

### WORKFLOW Process
1. When ready for a release, head over to the github actions tab 
2. You should see the `Release`  workflow under the work-flows list
3. The work flow is initiated manually on master. Provide a description of the release (not-required) 
and what the next version of the release will be (patch, minor, major).
4. Initiate Workflow and let the magic happen.


### Contributions
* [GitHub Actions](https://github.com/features/actions)
* [anothrNick/github-tag-action](https://github.com/anothrNick/github-tag-action)
* [qcastel/github-actions-maven-release](https://github.com/qcastel/github-actions-maven-release)
* [WyriHaximus/github-action-get-previous-tag](https://github.com/WyriHaximus/github-action-get-previous-tag)
* [mikepenz/release-changelog-builder-action](https://github.com/mikepenz/release-changelog-builder-action)
* [svenstaro/upload-release-action](https://github.com/svenstaro/upload-release-action)