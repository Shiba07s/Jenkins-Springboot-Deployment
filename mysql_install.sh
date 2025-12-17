#!/bin/bash

################################################################################
# MySQL Installation Script for EC2
# This script installs MySQL Server and configures it for remote access
################################################################################

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

################################################################################
# Main Installation
################################################################################

log "Starting MySQL installation..."

# Update system packages
log "Updating system packages..."
sudo apt-get update -y || error "Failed to update packages"

# Install MySQL Server
log "Installing MySQL Server..."
sudo apt-get install mysql-server -y || error "Failed to install MySQL"

# Start MySQL service
log "Starting MySQL service..."
sudo systemctl start mysql || error "Failed to start MySQL"
sudo systemctl enable mysql || log "MySQL enabled to start on boot"

# Check MySQL status
log "Checking MySQL status..."
sudo systemctl status mysql --no-pager || warning "MySQL status check failed"

################################################################################
# MySQL Configuration
################################################################################

log "Configuring MySQL for remote access..."

# Set MySQL root password
MYSQL_ROOT_PASSWORD="Ashutosh@2000"
log "Setting root password..."

sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD}';" || error "Failed to set root password"
sudo mysql -e "FLUSH PRIVILEGES;" || error "Failed to flush privileges"

# Create remote user
REMOTE_USER="ashu"
REMOTE_PASSWORD="Ashutosh@2000"

log "Creating remote user..."
sudo mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<EOF || error "Failed to create remote user"
CREATE USER IF NOT EXISTS '${REMOTE_USER}'@'%' IDENTIFIED BY '${REMOTE_PASSWORD}';
GRANT ALL PRIVILEGES ON *.* TO '${REMOTE_USER}'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

# Configure MySQL to listen on all interfaces
log "Configuring MySQL to accept remote connections..."
sudo sed -i "s/^bind-address.*/bind-address = 0.0.0.0/" /etc/mysql/mysql.conf.d/mysqld.cnf || error "Failed to update bind-address"

# Restart MySQL to apply changes
log "Restarting MySQL service..."
sudo systemctl restart mysql || error "Failed to restart MySQL"

################################################################################
# Display Connection Information
################################################################################

# Get EC2 public IP
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
PRIVATE_IP=$(hostname -I | awk '{print $1}')

echo ""
echo "=========================================================================="
echo -e "${GREEN}MySQL Installation Complete!${NC}"
echo "=========================================================================="
echo ""
echo "Connection Details:"
echo "-------------------"
echo "Public IP:      ${PUBLIC_IP}"
echo "Private IP:     ${PRIVATE_IP}"
echo "Port:           3306"
echo ""
echo "Root Credentials:"
echo "  Username:     root"
echo "  Password:     ${MYSQL_ROOT_PASSWORD}"
echo ""
echo "Remote User Credentials:"
echo "  Username:     ${REMOTE_USER}"
echo "  Password:     ${REMOTE_PASSWORD}"
echo ""
echo "=========================================================================="
echo -e "${YELLOW}IMPORTANT SECURITY STEPS:${NC}"
echo "=========================================================================="
echo "1. Add MySQL port to Security Group:"
echo "   - Go to EC2 Console > Security Groups"
echo "   - Edit Inbound Rules"
echo "   - Add Rule: Type=MySQL/Aurora, Port=3306, Source=Your IP"
echo ""
echo "2. Change the default passwords immediately!"
echo ""
echo "3. For production, restrict access to specific IPs only"
echo "=========================================================================="
echo ""
echo "MySQL Workbench Connection:"
echo "  Connection Name: EC2 MySQL"
echo "  Hostname:        ${PUBLIC_IP}"
echo "  Port:            3306"
echo "  Username:        ${REMOTE_USER}"
echo "  Password:        ${REMOTE_PASSWORD}"
echo "=========================================================================="