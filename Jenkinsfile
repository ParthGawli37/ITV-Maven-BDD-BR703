pipeline {
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '10'))
    }

    triggers {
        // Weekdays at a stable hashed time around 02:00 Jenkins-server time.
        cron('H 2 * * 1-5')
    }

    parameters {
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'edge', 'firefox', 'ALL'],
            description: 'Browser to execute. ALL runs Chrome, Edge and Firefox.'
        )
        choice(
            name: 'TEST_SCOPE',
            choices: ['@SmokeTest or @RegressionTest', '@SmokeTest', '@RegressionTest'],
            description: 'Cucumber tag expression to execute.'
        )
    }

    environment {
        // Configure Jenkins as Pipeline script from SCM using this repository:
        // https://github.com/ParthGawli37/ITV-Maven-BDD-BR703.git
        REPORT_ROOT = 'reports'
    }

    stages {
        stage('Checkout & Environment') {
            steps {
                checkout scm
                script {
                    if (isUnix()) {
                        sh 'mvn -version'
                        sh 'java -version'
                    } else {
                        bat 'mvn -version'
                        bat 'java -version'
                    }
                }
            }
        }

        stage('Execute Tests') {
            steps {
                script {
                    def browsers = params.BROWSER == 'ALL'
                        ? ['chrome', 'edge', 'firefox']
                        : [params.BROWSER.toLowerCase()]

                    def jobs = [:]

                    browsers.each { browser ->
                        def selectedBrowser = browser
                        jobs[selectedBrowser] = {
                            ws("${env.WORKSPACE}@${selectedBrowser}") {
                                deleteDir()
                                checkout scm

                                catchError(buildResult: 'FAILURE', stageResult: 'FAILURE') {
                                    // Headless execution is deliberately disabled for diagnosis.
                                    // The framework/configuration default is headless=false.
                                    def command = "mvn -B clean test -Dbrowser=${selectedBrowser} -Dcucumber.filter.tags=\"${params.TEST_SCOPE}\""
                                    if (isUnix()) {
                                        sh label: "Run ${selectedBrowser}", script: command
                                    } else {
                                        bat label: "Run ${selectedBrowser}", script: command
                                    }
                                }

                                stash(
                                    name: "results-${selectedBrowser}",
                                    includes: 'target/cucumber-report.html,target/cucumber.json,target/cucumber.xml,target/surefire-reports/**,target/screenshots/**,logs/**',
                                    allowEmpty: true
                                )
                            }
                        }
                    }

                    parallel jobs
                }
            }
        }

        stage('Publish Professional Reports') {
            steps {
                script {
                    def browsers = params.BROWSER == 'ALL'
                        ? ['chrome', 'edge', 'firefox']
                        : [params.BROWSER.toLowerCase()]

                    deleteDir()

                    browsers.each { browser ->
                        dir("${env.REPORT_ROOT}/${browser}") {
                            unstash "results-${browser}"
                        }

                        junit(
                            testResults: "${env.REPORT_ROOT}/${browser}/target/cucumber.xml",
                            allowEmptyResults: true,
                            skipPublishingChecks: true
                        )

                        publishHTML(target: [
                            allowMissing: true,
                            alwaysLinkToLastBuild: true,
                            keepAll: true,
                            reportDir: "${env.REPORT_ROOT}/${browser}/target",
                            reportFiles: 'cucumber-report.html',
                            reportName: "${browser.capitalize()} - Cucumber Report"
                        ])
                    }

                    archiveArtifacts(
                        artifacts: "${env.REPORT_ROOT}/**/*",
                        allowEmptyArchive: true,
                        fingerprint: true
                    )
                }
            }
        }
    }

    post {
        always {
            echo "Build completed. Browser=${params.BROWSER}, Scope=${params.TEST_SCOPE}, Headless=false"
        }
        success {
            echo 'All selected browser executions passed.'
        }
        unstable {
            echo 'One or more selected browser executions reported an unstable result.'
        }
        failure {
            echo 'One or more selected browser executions failed. Open the per-browser Cucumber reports and screenshots in this build.'
        }
    }
}
