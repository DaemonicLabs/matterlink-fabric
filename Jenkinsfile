pipeline {
    agent any
    stages {
        stage('Build') {
            steps {
                sh "rm -rf build/libs/"
                sh "chmod +x gradlew"
                sh "./gradlew clean build --refresh-dependencies --full-stacktrace"
            }
        }

        stage('Upload to curseforge') {
            when {
                branch 'master'
            }
            steps {
                sh 'git log --format=format:%B ${GIT_PREVIOUS_SUCCESSFUL_COMMIT}..${GIT_COMMIT} > changelog.txt'
                sh './gradlew -Prelease -Pchangelog_file=changelog.txt curseforge287323 --stacktrace'
            }
        }

        stage('Archive artifacts') {
            when {
                branch 'master'
            }
            steps {
                sh "./gradlew publish"
            }
        }

        stage('increment buildnumber') {
            when {
                branch 'master'
            }
            steps {
                sh "./gradlew buildnumberIncrement"
            }
        }
    }
}