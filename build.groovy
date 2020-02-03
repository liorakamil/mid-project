node {
 def customImage = ""
 stage("pull code") {
     checkout scm
 }

 stage("build docker") {
    customImage = docker.build("liorakamil/mid-project:${env.BUILD_ID}")
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
        aws eks update-kubeconfig --name eks-cluster-flask
        
        cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: flask-service
  labels:
    app: flask
spec:
  type: LoadBalancer
  ports:
  - protocol: TCP
    port: 80
    targetPort: 5000
  selector:
    app: flask
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: flask-deployment
spec:
  selector:
    matchLabels:
      app: flask
  replicas: 2
  template:
    metadata:
      labels:
        app: flask
    spec:
      containers:
      - name: flask
        image: liorakamil/mid-project:${env.BUILD_ID}
        ports:
        - containerPort: 5000       
        EOF
        """
    }
  }
}