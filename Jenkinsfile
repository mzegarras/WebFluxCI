
pipeline {
   
   agent none
   
    stages {
       /*
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'ls -lta ./target/'
            }
        }*/
        stage('Test') {
            agent {
                docker { image 'maven:3.6.3-openjdk-11-slim' }
            }
            
            steps {
               script {
                  def props = readProperties file: 'config/dev.env'
                  env.APP = props.APP
                  env.APP_MODULE = props.APP_MODULE 
              }
                sh "echo The weather is $WEATHER"
                sh 'mvn -B verify'
            }
            post{
                always{
                    //junit './target/surefire-reports/*xml'
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/*.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java', exclusionPattern: 'src/test*'
                    
                    
                    recordIssues enabledForFailure: true, tools: [mavenConsole(), java(), javaDoc()]
                    recordIssues enabledForFailure: true, tool: checkStyle()
                    recordIssues enabledForFailure: true, tool: spotBugs()
                    recordIssues enabledForFailure: true, tool: cpd(pattern: '**/target/cpd.xml')
                    recordIssues enabledForFailure: true, tool: pmdParser(pattern: '**/target/pmd.xml')
                }
                success {
                    archiveArtifacts artifacts: 'target/lab04-0.0.1-SNAPSHOT.jar', fingerprint: true, onlyIfSuccessful: true
                }
            }
        }
         stage('Docker') {
            agent any
            steps {
                
               
                copyArtifacts filter: 'target/*.jar', 
                              fingerprintArtifacts: true, 
                              projectName: '${JOB_NAME}', 
                              flatten: true,
                              selector: specific('${BUILD_NUMBER}'),
                              target: 'target'

                sh 'ls -lta '
                sh 'docker build --file ./src/main/docker/Dockerfile --tag demo:latest .'
                sh 'docker ps'
                 //run: docker build --file ./src/main/docker/Dockerfile --tag ${{ steps.dotenv.outputs.DOCKER_REPOSITORY}}/${{ steps.dotenv.outputs.APP}}-${{ steps.dotenv.outputs.APP_MODULE}}:latest .

            }
        }
       /*
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }*/
      
    }
}
