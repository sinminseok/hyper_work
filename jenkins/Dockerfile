
FROM jenkins/jenkins:lts-jdk17
USER root

ARG DOCKER_GID=999

RUN apt-get update && \
    apt-get install -y ca-certificates curl gnupg lsb-release && \
    mkdir -p /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
    https://download.docker.com/linux/debian $(lsb_release -cs) stable" \
    | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN export GROUP_NAME=$(getent group $DOCKER_GID | cut -d: -f1) && \
    echo "Adding jenkins to existing group $GROUP_NAME (GID $DOCKER_GID)" && \
    usermod -aG $GROUP_NAME jenkins


USER jenkins