
// shared library code

@Library("Shared") _
pipeline {
    agent { label "product"}

    environment {
        MAVEN_OPTS = "-Xms128m -Xmx256m"
    }

    stages {
        stage("code") {
            steps  {
                script {
                    clone("https://github.com/Shiba07s/Jenkins-Springboot-Deployment.git","master")
                }
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
                script {
                    docker_build("product-service","latest","shiba07s")
                }
            }
        }
        stage("push to docker hub") {
            steps {
                script {
                    docker_push("product-service","latest","shiba07s")
                }
            }
        }
        stage("deploy") {
            steps {
                sh "docker-compose down -v && docker-compose up -d "
                echo "deploy completed"
            }
        }
    }
}





// pipeline {
//     agent { label "product"}
//
//     environment {
//         MAVEN_OPTS = "-Xms128m -Xmx256m"
//     }
//
//     stages {
//         stage("code") {
//             steps  {
//                 git url: "https://github.com/Shiba07s/Jenkins-Springboot-Deployment.git",
//                 branch : "master"
//             }
//         }
//         stage("mvn build") {
//             steps {
//                 echo 'Building Spring Boot application...'
//                 sh 'mvn clean package -DskipTests'
//                 echo "maven build completed"
//             }
//         }
//         stage("docker build" ) {
//             steps {
//                 sh "docker build -t shiba07s/product-service:latest ."
//                 echo "docker build completed"
//             }
//         }
//         stage("push to docker hub") {
//             steps {
//                   withCredentials([usernamePassword(credentialsId: "dockerHubCred",passwordVariable:"dockerHubPass",usernameVariable:"dockerHubUser")]){
//                     sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPass}"
//
//                   sh "docker image tag product-service:latest ${env.dockerHubUser}/product-service:latest"
//                   sh "docker push ${env.dockerHubUser}/product-service:latest"
//                   echo "docker push completed"
//                   }
//             }
//         }
//         stage("deploy") {
//             steps {
//                 sh "docker-compose down -v && docker-compose up -d "
//                 echo "deploy completed"
//             }
//         }
//     }
// }