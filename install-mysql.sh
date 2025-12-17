#!/bin/bash

# Simple MySQL Installation Script for EC2

echo "Installing MySQL..."
sudo apt-get update -y
sudo apt-get install mysql-server -y

echo "Starting MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql

echo "Setting up MySQL..."

# For fresh MySQL installation, root has no password and uses auth_socket
# Set root password and allow password authentication
sudo mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Ashutosh@2000';
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'Ashutosh@2000';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

echo "Allowing remote connections..."
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql

echo "===================="
echo "Installation Done!"
echo "===================="
echo ""
echo "MySQL Workbench Settings:"
echo "  IP: 172.28.92.023"
echo "  Port: 3306"
echo "  Username: root"
echo "  Password: Ashutosh@2000"
echo ""
echo "⚠️  Don't forget to open port 3306 in EC2 Security Group!"