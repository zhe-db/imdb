# IMDB

## Overall description

We built an OLTP system including an relational database (postgres) and a distributed backend (Scala Akka) and an OLAP system based on Delta Lake with Spark SQL. Our OLTP system has 35+ HTTP Endpoints for users to send requests to interact with our movie database. Our OLAP system is for powering latency insensitive batch workloads such as ML, DS, we built query API with Spark SQL. In total, we wrote 6031 lines of Scala Code.

## Setup (Ubuntu)

1. Install JDK 8
    ```
    sudo apt-get update`
    sudo apt-get install openjdk-8-jdk
    ```
   - Add JAVA_HOME to your bash profile
    ```
    export JAVA_HOME=path_to_java_home
    export PATH=$PATH:$JAVA_HOME/bin
    ```
2. Install Sbt (Scala)
   ```
   sdk install sbt
   ```
3. Install and Setup Hadoop
   - Install SSH 
   ```
   sudo apt install openssh-server openssh-client -y
   ```
      - Create Hadoop User
   ```
   sudo adduser hdoop
   su - hdoop
   ```
   - Enable Passwordless SSH for Hadoop User 
   ```
   ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
   ```
   - Use the cat command to store the public key as authorized_keys in the ssh directory 
   ```
   cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
   ```
   - Download and Install Hadoop on Ubuntu
   - Configure Hadoop Environment Variables in your bash profile 
   ```
   #Hadoop Related Options 
   export HADOOP_HOME=/home/hdoop/hadoop-3.2.1
   export HADOOP_INSTALL=$HADOOP_HOME
   export HADOOP_MAPRED_HOME=$HADOOP_HOME
   export HADOOP_COMMON_HOME=$HADOOP_HOME
   export HADOOP_HDFS_HOME=$HADOOP_HOME
   export YARN_HOME=$HADOOP_HOME
   export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_HOME/lib/native
   export PATH=$PATH:$HADOOP_HOME/sbin:$HADOOP_HOME/bin
   export HADOOP_OPTS"-Djava.library.path=$HADOOP_HOME/lib/nativ"
   ```
   Edit hadoop-env.sh File 
   ```
   sudo nano $HADOOP_HOME/etc/hadoop/hadoop-env.sh
   ```
   Add following line 
   ```
   export JAVA_HOME=PATH_TO_YOUR_JDK
   ```
   Edit core-site.xml File 
   ```
   sudo nano $HADOOP_HOME/etc/hadoop/core-site.xml
   ```
   Add following lines with your desired port number and tmp directory
   ```
   <configuration>
         <property>
            <name>hadoop.tmp.dir</name>
            <value>/home/hdoop/tmpdata</value>
         </property>
         <property>
            <name>fs.default.name</name>
            <value>hdfs://127.0.0.1:SomePort}</value>
         </property>
   </configuration>
   ```
   Edit hdfs-site.xml File
   ```
   <configuration>
      <property>
         <name>dfs.data.dir</name>
         <value>/home/hdoop/dfsdata/namenode</value>
      </property>
      <property>
         <name>dfs.data.dir</name>
         <value>/home/hdoop/dfsdata/datanode</value>
      </property>
      <property>
         <name>dfs.replication</name>
         <value>1</value>
      </property>
   </configuration>
   ```
   Edit mapred-site.xml File
   ```
   <configuration> 
      <property> 
      <name>mapreduce.framework.name</name> 
      <value>yarn</value> 
      </property> 
   </configuration>
   ```
   Edit yarn-site.xml File
   ```
   <configuration>
      <property>
      <name>yarn.nodemanager.aux-services</name>
      <value>mapreduce_shuffle</value>
      </property>
      <property>
      <name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>
      <value>org.apache.hadoop.mapred.ShuffleHandler</value>
      </property>
      <property>
      <name>yarn.resourcemanager.hostname</name>
      <value>127.0.0.1</value>
      </property>
      <property>
      <name>yarn.acl.enable</name>
      <value>0</value>
      </property>
      <property>
      <name>yarn.nodemanager.env-whitelist</name>   
      <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PERPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
      </property>
   </configuration>
   ```
   Format HDFS NameNode
   ```
   hdfs namenode -format
   ```
   Start Hadoop Cluster
   ```
   ./start-dfs.sh
   ./start-yarn.sh
   ```
   Run `jps` to make sure datanode and namenode are working
4. Install Docker
```
$ sudo apt-get update

$ sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
$ echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
$ sudo apt-get update
$ sudo apt-get install docker-ce docker-ce-cli containerd.io
```
Install Docker-Compose
```
$ sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```
Install Postgres Official Docker Image
```
docker pull postgres
```
Download movielens training and testing files ml-1m: https://grouplens.org/datasets/movielens/1m/

## Run Project
1. Edit DockerCompose config file (docker-compose.yml) configuration file for your desired postgres port and run
```docker compose up```
2. Edit Project settings file (src/main/resources/application.conf)
   - Change Postgres related settings under database.postgre.properties (serverName, portNumber)
   - Change HDFS settings under hdfs (domain, port)
   - Change backend domain and port under application (host, port)
   - Change Movielens data files under movielens.ml-1m (input_dir and files)
   - Change Movielens data files on HDFS under movielens.ml-1m.hdfs (input_dir)
   - Change APIKey for TMDB API under tmdb.apiKey
   - Change Delta Lake tables saving path under delta.save_path (can be local FS or HDFS)
   - Change Encrpytion key for encrpyting passwords under crypto.key
## Start backend
3.  Run `sbt run`, then select option 1 to start our OLTP system
