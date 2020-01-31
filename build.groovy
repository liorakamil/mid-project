node {
 def customImage = ""
 stage("build docker") {
    customImage = docker.build("liorakamil/liorakamil:1.0.1")
    withDockerRegistry(credentialsId: 'eb94b483-01ea-46c4-ad1a-747b29513365') {
        customImage.push()   
    }
 }
 stage("verify dockers") {
  sh "docker images"
 }
}