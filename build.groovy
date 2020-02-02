node {
    properties([
        parameters ([
            string(name: 'EKS', description: 'EKS cluster name')
        ])
    ])
    def customImage = ""
    stage("pull code") {
        checkout scm
    }
    
    stage("build docker") {
        customImage = docker.build("liorakamil/mid-project")
        withDockerRegistry(credentialsId: 'dockerhub') {
            customImage.push()   
        }
    }
    stage("verify dockers") {
        sh "docker images"
    }
    stage('Apply Kubernetes files') {
        withAWS(region: 'us-east-1', credentials: 'AWSK8S') {
            sh """
            aws eks update-kubeconfig --name ${params.EKS}
            kubectl apply -f deploy.yml
            """
        }
    }
}