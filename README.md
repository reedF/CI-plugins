# CI-plugins
1.Tools for CI components: git,jenkins,maven,nexus.Using these clients to call API

 
2.Axure publish html page in github


3.Private Git repo:https://reedf.visualstudio.com

# Docker image
docker pull jenkins
docker pull gitlab/gitlab-ce
# gitlab for version 9
由于gitlab 10.0以后版本移除了private token功能，需使用老版本gitlab适配java-gitlab-client
使用：https://hub.docker.com/r/sameersbn/gitlab/
docker pull sameersbn/postgresql
docker pull sameersbn/redis
docker pull sameersbn/gitlab


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
# gitlab version 9
docker run -d --name pg \
    --env 'DB_NAME=gitlabhq_production' \
    --env 'DB_USER=gitlab' --env 'DB_PASS=123456' \
    --env 'DB_EXTENSION=pg_trgm' \
    --restart always \
    sameersbn/postgresql    
docker run -d --name redis -d --restart=always -p 6379:6379 sameersbn/redis    
docker run -d --name gitlab -p 80:80 -p 2222:22 \
    --link pg:postgresql --link redis:redisio \
    --env 'GITLAB_PORT=80' --env 'GITLAB_SSH_PORT=2222' \
    --env 'GITLAB_SECRETS_DB_KEY_BASE=long-and-random-alpha-numeric-string' \
    --env 'GITLAB_SECRETS_SECRET_KEY_BASE=long-and-random-alpha-numeric-string' \
    --env 'GITLAB_SECRETS_OTP_KEY_BASE=long-and-random-alpha-numeric-string' \
    --restart always \
	sameersbn/gitlab:9.2.1    
	
查看容器日志
docker logs -f gitlab    
docker logs -f jenkins
