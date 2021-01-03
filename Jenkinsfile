
pipeline {
    // Cualquier agente
    agent {
                docker { image 'maven:3.6.3-openjdk-11-slim' }
            }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'ls -lta ./target/'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B verify'
                //archive "target/*.jar"
                //stash includes: "target/*.jar", name: "jar"
            }
            post{
                always{
                    //junit './target/surefire-reports/*xml'
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/*.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java', exclusionPattern: 'src/test*'

                    //step([$class: 'PmdPublisher', pattern: '**/target/pmd.xml', unstableTotalAll:'0'])
                    //step([$class: 'FindBugsPublisher', pattern: '**/findbugsXml.xml', unstableTotalAll:'0'])
                }
                success {

                    archiveArtifacts artifacts: 'target/lab04-0.0.1-SNAPSHOT.jar', fingerprint: true, onlyIfSuccessful: true
                    //archiveArtifacts artifacts: 'generatedFile.txt', onlyIfSuccessful: true
                }
            }
        }
         stage('Docker') {
            steps {
                sh 'docker build --file ./src/main/docker/Dockerfile --tag demo:latest .'
                sh 'docker ps'
                 //run: docker build --file ./src/main/docker/Dockerfile --tag ${{ steps.dotenv.outputs.DOCKER_REPOSITORY}}/${{ steps.dotenv.outputs.APP}}-${{ steps.dotenv.outputs.APP_MODULE}}:latest .

            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
