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

 stage("verify docker") {
  sh """
  docker_id=$(docker run -d -p 5000:5000 liorakamil/mid-project:${env.BUILD_ID})
  status_code=$(curl --write-out %{http_code} --silent --output /dev/null localhost:5000)
  if [[ "\$status_code" -ne 200 ]] ; then
    echo "Docker failed with \$status_code"
    exit 1
  else
    echo "Docker ok"
  fi
  docker rm -f \$docker_id
  """
 }
 stage('Apply Kubernetes files') {
    withAWS(region: 'us-east-1', credentials: 'AWSK8S') {
sh """
aws eks update-kubeconfig --name eks-cluster-flask

cat <<EOF | kubectl apply -f -
apiVersion: v1      # for versions before 1.9.0 use apps/v1beta2
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
  replicas: 2 # tells deployment to run 2 pods matching the template
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