pipeline {
    agent { label "product"}

    environment {
        MAVEN_OPTS = "-Xms128m -Xmx256m"
    }

    stages {
        stage("code") {
            steps  {
                git url: "https://github.com/Shiba07s/Jenkins-Springboot-Deployment.git",
                branch : "master"
            }
        }
        stage("mvn build") {
            steps {
                echo 'Building Spring Boot application...'
                sh 'mvn clean package -DskipTests'
                echo "maven build completed"
            }
        }
        stage("docker build" ) {
            steps {
                sh "docker build -t product-service:latest ."
                echo "docker build completed"
            }
        }
        stage("docker run") {
            steps {
                sh "docker run -d -p 2020:2020 product-service:latest"
                echo "docker run completed"
            }
        }
        stage("deploy") {
            steps {
                echo "deploy completed"
            }
        }
    }
}