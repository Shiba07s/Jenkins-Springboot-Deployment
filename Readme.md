# Jenkins Master-Agent Setup Guide

A comprehensive guide for setting up Jenkins in a master-agent architecture on AWS EC2 instances.

## 📋 Table of Contents

- [Architecture Overview](#architecture-overview)
- [Prerequisites](#prerequisites)
- [Jenkins Master Setup](#jenkins-master-setup)
- [Jenkins Agent Setup](#jenkins-agent-setup)
- [Configure Agent Node in Jenkins](#configure-agent-node-in-jenkins)
- [Create Jenkins Pipeline](#create-jenkins-pipeline)
- [Troubleshooting](#troubleshooting)
- [Security Best Practices](#security-best-practices)
- [Key Files Reference](#key-files-reference)

## 🏗️ Architecture Overview

This setup consists of:
- **Jenkins Master**: Manages the CI/CD pipeline and orchestrates build jobs
- **Jenkins Agent**: Executes the actual build jobs

## ✅ Prerequisites

- 2 AWS EC2 instances (Ubuntu)
- Security groups configured to allow:
  - SSH (port 22)
  - Jenkins (port 8080)
- SSH access to both instances

---

## 🚀 Jenkins Master Setup

### Step 1: Install Java
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
```

**Verify installation:**
```bash
java -version
```

### Step 2: Install Jenkins
```bash
# Add Jenkins repository key
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key

# Add Jenkins repository
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null

# Update package list and install Jenkins
sudo apt update
sudo apt install jenkins -y
```

### Step 3: Start Jenkins Service
```bash
sudo systemctl start jenkins
sudo systemctl enable jenkins
sudo systemctl status jenkins
```

### Step 4: Initial Jenkins Setup

1. Access Jenkins UI at `http://<master-public-ip>:8080`
2. Retrieve the initial admin password:
```bash
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

3. Complete the setup wizard
4. Install suggested plugins
5. Create your admin user

### Step 5: Generate SSH Keys on Master
```bash
cd ~/.ssh/
ssh-keygen 
```

**This creates two files:**
- `id_ed25519` - **Private key** (stays on Jenkins Master)
- `id_ed25519.pub` - **Public key** (copy to Jenkins Agent)

**View the keys:**
```bash
ls ~/.ssh/
# Output: authorized_keys  id_ed25519  id_ed25519.pub
```

**Display public key to copy:**
```bash
cat ~/.ssh/id_ed25519.pub
```

---

## 🖥️ Jenkins Agent Setup

### Step 1: Launch EC2 Instance

Launch an Ubuntu EC2 instance for the agent node.

### Step 2: Install Java on Agent
```bash
sudo apt update
sudo apt install openjdk-17-jdk -y
```

**Verify installation:**
```bash
java -version
```

### Step 3: Add Master's Public Key to Agent

On the **Jenkins Agent** instance:
```bash
cd ~/.ssh/
vim authorized_keys
```

**Copy the public key from the Jenkins Master** (`id_ed25519.pub`) and paste it into the agent's `authorized_keys` file.

On **Jenkins Master**, run:
```bash
cat ~/.ssh/id_ed25519.pub
```

Copy the output and paste it into the agent's `authorized_keys` file.

**Save and verify:**
```bash
cat ~/.ssh/authorized_keys
```

Expected content example:
```
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDD7n9LLuZ78Kit... product
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAILZ55oKqO7eAkGJQ... ubuntu@ip-172-31-7-25
```

---

## ⚙️ Configure Agent Node in Jenkins

### Step 1: Add Credentials in Jenkins

1. Go to **Jenkins Dashboard** → **Manage Jenkins** → **Credentials**
2. Click on **(global)** domain → **Add Credentials**
3. Configure:
  - **Kind**: SSH Username with private key
  - **Username**: `ubuntu`
  - **Private Key**: Select "Enter directly"
  - Click **Add** and paste the content of `id_ed25519` from Jenkins Master
```bash
   # On Master - copy this output
   cat ~/.ssh/id_ed25519
```

- **ID**: `jenkins-agent-key` (or your choice)
- **Description**: Jenkins Agent SSH Key
4. Click **Create**

### Step 2: Create Agent Node

1. Go to **Manage Jenkins** → **Nodes** → **New Node**
2. Enter node name: `product-agent` (or your choice)
3. Select **Permanent Agent**
4. Click **Create**

### Step 3: Node Configuration

Configure the following settings:

| Setting | Value |
|---------|-------|
| **Description** | Jenkins Agent for product builds |
| **Number of executors** | `1` |
| **Remote root directory** | `/home/ubuntu` |
| **Labels** | `product` |
| **Usage** | Use this node as much as possible |
| **Launch method** | Launch agents via SSH |
| **Host** | `<agent-private-ip>` (e.g., `172.31.3.44` or `13.200.215.242`) |
| **Credentials** | Select the credentials you created (`jenkins-agent-key`) |
| **Host Key Verification Strategy** | Non verifying Verification Strategy |
| **Availability** | Keep this agent online as much as possible |

4. Click **Save**

### Step 4: Launch Agent

1. Go to **Dashboard** → **Nodes**
2. Click on your agent node (`product-agent`)
3. Click **Launch agent**
4. Check the logs to verify successful connection

✅ **Success indicator**: You should see "Agent successfully connected and online"

---

## 🔧 Create Jenkins Pipeline

### Sample Pipeline Configuration

1. Create a new **Pipeline** job
2. In the pipeline configuration, use the following example:
```groovy
pipeline {
    agent { label "Test"}
    environment {
        MAVEN_OPTS = "-Xms128m -Xmx256m"
    }
    stages {
        stage("code") {
            steps  {
                git url: "https://github.com/Shiba07s/Jenkins-Springboot-Deployment.git",
                branch : "master"
            }
        }
        stage("mvn build") {
            steps {
                echo 'Building Spring Boot application...'
                sh 'mvn clean package -DskipTests'
                echo "maven build completed"
            }
        }
        stage("docker build" ) {
            steps {
                sh "docker build -t shiba07s/product-service:latest ."
                echo "docker build completed"
            }
        }
           stage("push to docker hub") {
            steps {
                  withCredentials([usernamePassword(credentialsId: "dockerHubCred",passwordVariable:"dockerHubPass",usernameVariable:"dockerHubUser")]){
                  sh "docker login -u ${env.dockerHubUser} -p ${env.dockerHubPass}"
                  sh "docker image tag product-service:latest ${env.dockerHubUser}/product-service:latest"
                  sh "docker push ${env.dockerHubUser}/product-service:latest"
                  echo "docker push completed"
                  }
            }
        }
        stage("deploy") {
            steps {
                sh "docker-compose down -v && docker-compose up -d "
                echo "deploy completed"
            }
        }
    }
}
```

3. Click **Save**
4. Click **Build Now** to test the pipeline

---

## 🔍 Troubleshooting

### Issue: SSH Connection Failed

**Symptoms**: Agent node shows "SSH connection failed" in logs

**Solutions**:

1. **Verify public key on agent:**
```bash
# On Agent
cat ~/.ssh/authorized_keys
```
Ensure the master's public key is present.

2. **Test SSH connectivity manually:**
```bash
# From Master
ssh -i ~/.ssh/id_ed25519 ubuntu@<agent-ip>
```

3. **Check security group rules:**
  - Ensure port 22 (SSH) is open between master and agent
  - Verify the agent's private IP is accessible from the master

4. **Verify credentials in Jenkins:**
  - Ensure the private key in Jenkins matches the public key on the agent
  - Check that the username is `ubuntu`

5. **Check file permissions:**
```bash
# On Agent
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

### Issue: Agent Shows Offline

**Possible Causes**:
- Network connectivity issues
- Agent EC2 instance stopped
- Incorrect credentials
- Java not installed on agent
- Firewall blocking connection

**Solutions**:
1. Check agent logs in Jenkins UI: **Nodes** → Click agent → **Log**
2. Verify agent instance is running
3. Ensure Java is installed on agent
4. Check network connectivity

### Issue: "Host Key Verification Failed"

**Solution**: Ensure you selected "Non verifying Verification Strategy" in the agent configuration.

---

## 🔒 Security Best Practices

- ✅ Use private IPs for agent communication when instances are in the same VPC
- ✅ Restrict security group rules to specific IP ranges
- ✅ Regularly update Jenkins and all plugins
- ✅ Use SSH key authentication (never use passwords)
- ✅ Keep Java and system packages updated on both instances
- ✅ Implement least privilege access for Jenkins users
- ✅ Enable audit logging in Jenkins
- ✅ Use HTTPS for Jenkins UI (configure reverse proxy with SSL)

---

## 📁 Key Files Reference

### Jenkins Master

| File | Location | Purpose |
|------|----------|---------|
| Private Key | `~/.ssh/id_ed25519` | Used for SSH authentication to agent |
| Public Key | `~/.ssh/id_ed25519.pub` | Copied to agent's authorized_keys |
| Jenkins Home | `/var/lib/jenkins` | Jenkins configuration and data |
| Initial Admin Password | `/var/lib/jenkins/secrets/initialAdminPassword` | First-time setup |

### Jenkins Agent

| File | Location | Purpose |
|------|----------|---------|
| Authorized Keys | `~/.ssh/authorized_keys` | Contains master's public key |
| Agent Workspace | `/home/ubuntu` | Remote root directory for builds |

---

## 📝 Quick Setup Commands

### On Jenkins Master
```bash
# Install Java and Jenkins
sudo apt update
sudo apt install openjdk-17-jdk -y
sudo wget -O /etc/apt/keyrings/jenkins-keyring.asc \
  https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key
echo "deb [signed-by=/etc/apt/keyrings/jenkins-keyring.asc]" \
  https://pkg.jenkins.io/debian-stable binary/ | sudo tee \
  /etc/apt/sources.list.d/jenkins.list > /dev/null
sudo apt update
sudo apt install jenkins -y
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Generate SSH keys
cd ~/.ssh/
ssh-keygen -t ed25519
cat id_ed25519.pub  # Copy this output
```

### On Jenkins Agent
```bash
# Install Java
sudo apt update
sudo apt install openjdk-17-jdk -y

# Add master's public key
cd ~/.ssh/
vim authorized_keys  # Paste master's public key here
chmod 600 authorized_keys
```

---

## 🎯 Next Steps

- [ ] Configure pipeline for your specific application
- [ ] Add additional agents for parallel builds
- [ ] Set up webhooks for automatic builds on git push
- [ ] Configure email/Slack notifications for build status
- [ ] Implement backup strategies for Jenkins configuration
- [ ] Set up Jenkins behind a reverse proxy with SSL
- [ ] Configure Jenkins security realm and authorization
- [ ] Integrate with monitoring tools (Prometheus, Grafana)

---

## 📚 Additional Resources

- [Jenkins Official Documentation](https://www.jenkins.io/doc/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Managing Jenkins Nodes](https://www.jenkins.io/doc/book/managing/nodes/)
- [Jenkins Security Best Practices](https://www.jenkins.io/doc/book/security/)
- [AWS EC2 Documentation](https://docs.aws.amazon.com/ec2/)

---

## 📧 Support

For issues or questions:
- Create an issue in this repository
- Check Jenkins community forums
- Review Jenkins documentation

---

## 📄 License

This project documentation is provided as-is for educational and reference purposes.

---

**Last Updated**: December 2025

# PERMANENT FIXES (Highly Recommended)
🔥 1️⃣ Add Swap Memory (VERY IMPORTANT)

This alone fixes 90% SSH issues.

sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile


Make it permanent:

echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab


Check:

free -h

🔥 2️⃣ Reduce memory usage

If you’re running Docker / Jenkins / Java (you are 😉):

docker system prune -af


Limit Java memory:

-Xms256m -Xmx512m
