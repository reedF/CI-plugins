# CI-plugins
1.Tools for CI components: git,jenkins,maven,nexus.Using these clients to call API.
2.Axure publish html page in github

# Docker image
docker pull jenkins
docker pull gitlab/gitlab-ce


# Docker run
docker run -d --name jenkins -p 8082:8080 -p 50000:50000 -v /c/Users/jenkins:/var/jenkins_home jenkins
docker run --detach --name gitlab \
    --hostname gitlab.example.com \
    --publish 443:443 --publish 80:80 --publish 22:22 \
    --restart always \
    --volume /c/Users/gitlab/config:/etc/gitlab \
    --volume /c/Users/gitlab/logs:/var/log/gitlab \
    --volume /c/Users/gitlab/data:/var/opt/gitlab \
    gitlab/gitlab-ce:latest

