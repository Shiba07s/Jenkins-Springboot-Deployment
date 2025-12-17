#!/bin/bash

# Simple MySQL Installation Script for EC2

echo "Installing MySQL..."
sudo apt-get update -y
sudo apt-get install mysql-server -y

echo "Starting MySQL..."
sudo systemctl start mysql
sudo systemctl enable mysql

echo "Setting up MySQL..."

# Set root password (root stays local only - more secure)
sudo mysql <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'Ashutosh@2000';
FLUSH PRIVILEGES;
EOF

# Create admin user for remote access
echo "Creating admin user..."
sudo mysql -u root -p'Ashutosh@2000' <<EOF
CREATE USER IF NOT EXISTS 'dbadmin'@'%' IDENTIFIED BY 'Admin@2000';
GRANT ALL PRIVILEGES ON *.* TO 'dbadmin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;
EOF

echo "Allowing remote connections..."
sudo sed -i 's/bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql

echo "===================="
echo "Installation Done!"
echo "===================="
echo ""
echo "ROOT ACCESS (Local EC2 only):"
echo "  Username: root"
echo "  Password: Ashutosh@2000"
echo "  Access: localhost only (more secure)"
echo ""
echo "ADMIN ACCESS (Remote - MySQL Workbench):"
echo "  IP: 172.28.92.023"
echo "  Port: 3306"
echo "  Username: dbadmin"
echo "  Password: Admin@2000"
echo "  Access: Full control from anywhere"
echo ""
echo "⚠️  Don't forget to open port 3306 in EC2 Security Group!"

# Create a read-only user
#sudo mysql -u root -p'Ashutosh@2000' -e "CREATE USER 'readonly'@'%' IDENTIFIED BY 'Read@123'; GRANT SELECT ON *.* TO 'readonly'@'%'; FLUSH PRIVILEGES;"

# Create a user for specific database only
#sudo mysql -u root -p'Ashutosh@2000' -e "CREATE USER 'appuser'@'%' IDENTIFIED BY 'App@123'; GRANT ALL ON myapp_db.* TO 'appuser'@'%'; FLUSH PRIVILEGES;"