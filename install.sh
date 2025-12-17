#!/bin/bash

set -e
set -o pipefail

echo "🚀 Starting installation of Docker, Docker Compose, Jenkins, and Maven..."

# ----------------------------
# Update system
# ----------------------------
sudo apt-get update -y

# ----------------------------
# 1. Install Docker
# ----------------------------
if ! command -v docker &>/dev/null; then
  echo "📦 Installing Docker..."
  sudo apt-get install -y docker.io

  sudo systemctl enable docker
  sudo systemctl start docker

  echo "👤 Adding current user to docker group..."
  sudo usermod -aG docker "$USER"

  echo "✅ Docker installed."
else
  echo "✅ Docker already installed."
fi

# ----------------------------
# 2. Install Docker Compose
# ----------------------------
if ! command -v docker-compose &>/dev/null; then
  echo "📦 Installing Docker Compose..."
  sudo apt-get install -y docker-compose
  echo "✅ Docker Compose installed."
else
  echo "✅ Docker Compose already installed."
fi

# ----------------------------
# 3. Install Jenkins
# ----------------------------
if ! command -v jenkins &>/dev/null; then
  echo "📦 Installing Jenkins..."

  sudo apt-get install -y openjdk-17-jdk

  curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee \
    /usr/share/keyrings/jenkins-keyring.asc > /dev/null

  echo deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
    https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
    /etc/apt/sources.list.d/jenkins.list > /dev/null

  sudo apt-get update -y
  sudo apt-get install -y jenkins

  sudo systemctl enable jenkins
  sudo systemctl start jenkins

  echo "✅ Jenkins installed and started."
else
  echo "✅ Jenkins already installed."
fi

# ----------------------------
# 4. Install Maven
# ----------------------------
if ! command -v mvn &>/dev/null; then
  echo "📦 Installing Maven..."
  sudo apt-get install -y maven
  echo "✅ Maven installed."
else
  echo "✅ Maven already installed."
fi

# ----------------------------
# 5. Confirm Versions
# ----------------------------
echo
echo "🔍 Installed Versions:"
docker --version
docker-compose --version
java --version
mvn --version
jenkins --version || echo "Jenkins running as a service"

echo
echo "⚠️ Logout & login again to apply Docker group permissions"
echo "🎉 Installation complete!"
