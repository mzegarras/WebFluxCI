
pipeline {
    // Cualquier agente
    agent {
                docker { image 'maven:3.6.3-openjdk-11-slim' }
            }

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh 'ls -lta'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B verify'
            }
        }
        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }
}
