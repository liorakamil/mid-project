node {
 def customImage = ""
 stage("pull code") {
     checkout scm
 }
 stage("build docker") {
    customImage = docker.build("liorakamil/mid-project")
    withDockerRegistry(credentialsId: '949088b6-b85c-4b4f-a6cf-6557f0d6b783') {
        customImage.push()   
    }
 }
 stage("verify dockers") {
  sh "docker images"
 }
 stage('Apply Kubernetes files') {
    withAWS(region: 'us-east-1', credentials: 'AWSK8S') {
        sh """
        aws eks update-kubeconfig --name opsSchool-eks-qTHooZOH
        kubectl apply -f deploy.yml
        """
    }
  }
}