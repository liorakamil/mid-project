node {
 def customImage = ""
 stage("pull code") {
     checkout scm
 }
 stage("build docker") {
    customImage = docker.build("liorakamil/mid-project")
    withDockerRegistry(credentialsId: 'eb94b483-01ea-46c4-ad1a-747b29513365') {
        customImage.push()   
    }
 }
 stage("verify dockers") {
  sh "docker images"
 }
 stage('Apply Kubernetes files') {
    withAWS(region: 'us-east-1', credentials: 'AWSK8S') {
        sh """
        aws eks update-kubeconfig --name opsSchool-eks-rC0FPoaR
        kubectl apply -f deploy.yml
        """
    }
  }
}