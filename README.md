# CI-plugins
1.Tools for CI components: git,jenkins,maven,nexus.Using these clients to call API.
2.Axure publish html page in github

# Docker image
docker pull jenkins
docker pull gitlab/gitlab-ce


# Docker run
docker run -d --name jenkins -p 8082:8080 -p 50000:50000 -v /c/Users/jenkins:/var/jenkins_home jenkins
sudo docker run --detach --name gitlab \
    --hostname gitlab.reed.com \
    --publish 443:443 --publish 80:80 --publish 2222:22 \
    --restart always \
    --volume /c/Users/gitlab/config:/etc/gitlab \
    --volume /c/Users/gitlab/logs:/var/log/gitlab \
    gitlab/gitlab-ce:latest

boot2docker使用的外部共享文件夹与gitlab容器文件系统不匹配，无法使用如下配置：    
    --volume /c/Users/gitlab/data:/var/opt/gitlab \
查看容器日志
docker logs -f gitlab    
docker logs -f jenkins
