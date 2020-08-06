@Library('jenkins-helpers') _

mvnLibraryPipeline {
    beforeVerify = [
        'mvn versions:set -DnewVersion="5.0.2_$(git rev-list HEAD --count)"'
    ]
}
