# Deployment Guide

This guide provides detailed deployment instructions for the MCP Server for Jakarta EE across all supported application servers.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Building the Application](#building-the-application)
- [GlassFish Deployment](#glassfish-deployment)
- [Payara Deployment](#payara-deployment)
- [WildFly Deployment](#wildfly-deployment)
- [Open Liberty Deployment](#open-liberty-deployment)
- [Production Considerations](#production-considerations)
- [Monitoring and Troubleshooting](#monitoring-and-troubleshooting)

## Prerequisites

### System Requirements

- **Java**: Java 23 or later (Java 25 for GlassFish)
- **Maven**: Maven 3.8 or later
- **Memory**: Minimum 2GB RAM (4GB recommended)
- **Disk Space**: At least 1GB free space

### Environment Setup

1. **Install Java**

   ```bash
   # Verify Java installation
   java -version
   
   # Should show Java 23 or later
   ```

2. **Install Maven**

   ```bash
   # Verify Maven installation
   mvn -version
   
   # Should show Maven 3.8 or later
   ```

3. **Set Environment Variables**

   ```bash
   export JAVA_HOME=/path/to/java
   export PATH=$JAVA_HOME/bin:$PATH
   ```

## Building the Application

### Standard Build

```bash
# Clone the repository
git clone <repository-url>
cd mcp-for-jakartaee

# Build the WAR file
mvn clean package
```

The WAR file will be created at: `target/dukes-mcp.war`

### Build for Specific Server

```bash
# For GlassFish
mvn clean package -Pglassfish

# For Payara
mvn clean package -Ppayara

# For WildFly
mvn clean package -Pwildfly

# For Open Liberty
mvn clean package -Pliberty
```

### Skip Tests (for faster builds)

```bash
mvn clean package -DskipTests
```

## GlassFish Deployment

### Quick Start with Cargo

The easiest way to run on GlassFish:

```bash
mvn clean package cargo:run -Pglassfish
```

This will:
1. Download GlassFish 8.0.0
2. Build the application
3. Deploy and start the server

### Manual Deployment

#### 1. Download and Install GlassFish

```bash
# Download GlassFish 8.0.0
wget https://github.com/eclipse-ee4j/glassfish/releases/download/8.0.0/glassfish-8.0.0.zip

# Extract
unzip glassfish-8.0.0.zip
cd glassfish8
```

#### 2. Start GlassFish

```bash
# Start the default domain
./bin/asadmin start-domain

# Verify it's running
./bin/asadmin list-domains
```

#### 3. Deploy the Application

```bash
# Deploy the WAR file
./bin/asadmin deploy /path/to/dukes-mcp.war

# Verify deployment
./bin/asadmin list-applications
```

#### 4. Access the Application

- Application: http://localhost:8080/dukes-mcp/
- MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
- Admin Console: http://localhost:4848 (admin/admin)

#### 5. Undeploy and Stop

```bash
# Undeploy
./bin/asadmin undeploy dukes-mcp

# Stop the domain
./bin/asadmin stop-domain
```

### GlassFish Configuration

#### Configure Database Connection Pool

```bash
# Create JDBC connection pool
./bin/asadmin create-jdbc-connection-pool \
  --datasourceclassname org.postgresql.ds.PGSimpleDataSource \
  --restype javax.sql.DataSource \
  --property user=dbuser:password=dbpass:serverName=localhost:portNumber=5432:databaseName=mydb \
  PostgresPool

# Create JDBC resource
./bin/asadmin create-jdbc-resource \
  --connectionpoolid PostgresPool \
  jdbc/MyDataSource
```

#### Configure JVM Options

```bash
# Set heap size
./bin/asadmin create-jvm-options -Xms512m
./bin/asadmin create-jvm-options -Xmx2048m

# Enable remote debugging
./bin/asadmin create-jvm-options "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9009"
```

#### Configure Logging

```bash
# Set log level
./bin/asadmin set-log-levels dukes.mcp=FINE

# View logs
tail -f glassfish8/glassfish/domains/domain1/logs/server.log
```

## Payara Deployment

### Quick Start with Cargo

```bash
mvn clean package cargo:run -Ppayara
```

### Manual Deployment

#### 1. Download and Install Payara

```bash
# Download Payara 7.2025.2
wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/7.2025.2/payara-7.2025.2.zip

# Extract
unzip payara-7.2025.2.zip
cd payara7
```

#### 2. Start Payara

```bash
# Start the default domain
./bin/asadmin start-domain

# Verify it's running
./bin/asadmin list-domains
```

#### 3. Deploy the Application

```bash
# Deploy the WAR file
./bin/asadmin deploy /path/to/dukes-mcp.war

# Verify deployment
./bin/asadmin list-applications
```

#### 4. Access the Application

- Application: http://localhost:8080/dukes-mcp/
- MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
- Admin Console: http://localhost:4848 (admin/admin)

#### 5. Undeploy and Stop

```bash
# Undeploy
./bin/asadmin undeploy dukes-mcp

# Stop the domain
./bin/asadmin stop-domain
```

### Payara-Specific Features

#### Enable Hazelcast Clustering

```bash
# Enable Hazelcast
./bin/asadmin set-hazelcast-configuration --enabled=true

# Configure cluster
./bin/asadmin set configs.config.server-config.hazelcast-runtime-configuration.enabled=true
```

#### Configure Health Check

```bash
# Enable health check service
./bin/asadmin set-healthcheck-configuration --enabled=true --dynamic=true

# Enable specific checkers
./bin/asadmin healthcheck-configure --serviceName=healthcheck-cpu --enabled=true
./bin/asadmin healthcheck-configure --serviceName=healthcheck-heap --enabled=true
```

## WildFly Deployment

### Quick Start with Maven Plugin

```bash
mvn clean package wildfly:run
```

This will:
1. Download WildFly 39.0.1.Final
2. Build the application
3. Deploy and start the server

### Manual Deployment

#### 1. Download and Install WildFly

```bash
# Download WildFly 39.0.1.Final
wget https://github.com/wildfly/wildfly/releases/download/39.0.1.Final/wildfly-39.0.1.Final.zip

# Extract
unzip wildfly-39.0.1.Final.zip
cd wildfly-39.0.1.Final
```

#### 2. Start WildFly

```bash
# Start in standalone mode
./bin/standalone.sh

# Or on Windows
./bin/standalone.bat
```

#### 3. Deploy the Application

**Option A: Hot Deployment**

```bash
# Copy WAR to deployments directory
cp /path/to/dukes-mcp.war ./standalone/deployments/
```

**Option B: CLI Deployment**

```bash
# Connect to CLI
./bin/jboss-cli.sh --connect

# Deploy
deploy /path/to/dukes-mcp.war

# Verify
deployment-info

# Exit CLI
exit
```

#### 4. Access the Application

- Application: http://localhost:8080/dukes-mcp/
- MCP Endpoint: http://localhost:8080/dukes-mcp/mcp
- Admin Console: http://localhost:9990 (create user first)

#### 5. Undeploy and Stop

```bash
# Using CLI
./bin/jboss-cli.sh --connect
undeploy dukes-mcp.war
exit

# Stop server
./bin/jboss-cli.sh --connect command=:shutdown
```

### WildFly Configuration

#### Create Admin User

```bash
# Add management user
./bin/add-user.sh

# Follow prompts to create admin user
```

#### Configure DataSource

```bash
# Connect to CLI
./bin/jboss-cli.sh --connect

# Add PostgreSQL driver (example)
module add --name=org.postgresql --resources=/path/to/postgresql-42.7.0.jar --dependencies=javax.api,javax.transaction.api

# Add driver
/subsystem=datasources/jdbc-driver=postgresql:add(driver-name=postgresql,driver-module-name=org.postgresql,driver-class-name=org.postgresql.Driver)

# Add datasource
data-source add --name=MyDS --jndi-name=java:jboss/datasources/MyDS --driver-name=postgresql --connection-url=jdbc:postgresql://localhost:5432/mydb --user-name=dbuser --password=dbpass

# Enable datasource
data-source enable --name=MyDS
```

#### Configure JVM Options

Edit `standalone/configuration/standalone.conf`:

```bash
# Add to JAVA_OPTS
JAVA_OPTS="$JAVA_OPTS -Xms512m -Xmx2048m"
JAVA_OPTS="$JAVA_OPTS -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"
```

## Open Liberty Deployment

### Quick Start with Maven Plugin

```bash
mvn clean package liberty:run
```

This will:
1. Download Open Liberty 26.0.0.3-beta
2. Build the application
3. Deploy and start the server

### Manual Deployment

#### 1. Download and Install Open Liberty

```bash
# Download Open Liberty
wget https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/26.0.0.3-beta/openliberty-26.0.0.3-beta.zip

# Extract
unzip openliberty-26.0.0.3-beta.zip
cd wlp
```

#### 2. Create Server

```bash
# Create a new server
./bin/server create mcpServer
```

#### 3. Configure Server

Edit `usr/servers/mcpServer/server.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<server description="MCP Server">
    <!-- Enable Jakarta EE 11 features -->
    <featureManager>
        <feature>jakartaee-11.0</feature>
    </featureManager>

    <!-- HTTP Endpoint -->
    <httpEndpoint id="defaultHttpEndpoint"
                  host="*"
                  httpPort="9080"
                  httpsPort="9443" />

    <!-- Application -->
    <webApplication id="dukes-mcp"
                    location="dukes-mcp.war"
                    contextRoot="/dukes-mcp" />

    <!-- DataSource (if needed) -->
    <dataSource id="DefaultDataSource">
        <jdbcDriver libraryRef="PostgreSQLLib"/>
        <properties.postgresql serverName="localhost"
                              portNumber="5432"
                              databaseName="mydb"
                              user="dbuser"
                              password="dbpass"/>
    </dataSource>

    <library id="PostgreSQLLib">
        <fileset dir="${server.config.dir}/lib" includes="postgresql-*.jar"/>
    </library>
</server>
```

#### 4. Deploy Application

```bash
# Copy WAR to dropins directory
cp /path/to/dukes-mcp.war ./usr/servers/mcpServer/dropins/
```

#### 5. Start Server

```bash
# Start the server
./bin/server start mcpServer

# View logs
./bin/server run mcpServer
```

#### 6. Access the Application

- Application: http://localhost:9080/dukes-mcp/
- MCP Endpoint: http://localhost:9080/dukes-mcp/mcp

#### 7. Stop Server

```bash
./bin/server stop mcpServer
```

### Open Liberty Configuration

#### Configure JVM Options

Create `usr/servers/mcpServer/jvm.options`:

```
-Xms512m
-Xmx2048m
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
```

#### Configure Logging

Edit `usr/servers/mcpServer/bootstrap.properties`:

```properties
# Set log level
com.ibm.ws.logging.trace.specification=*=info:dukes.mcp.*=fine

# Log format
com.ibm.ws.logging.console.format=simple
com.ibm.ws.logging.console.log.level=INFO
```

## Production Considerations

### Security

#### 1. Enable HTTPS

**GlassFish/Payara:**

```bash
# Create keystore
keytool -genkey -alias server -keyalg RSA -keystore keystore.jks

# Configure HTTPS listener
./bin/asadmin create-ssl --type http-listener --certname server http-listener-2
```

**WildFly:**

```bash
# Generate keystore
keytool -genkeypair -alias server -keyalg RSA -keystore server.keystore

# Configure HTTPS in CLI
/core-service=management/security-realm=ApplicationRealm/server-identity=ssl:add(keystore-path=server.keystore,keystore-password=changeit,alias=server)
```

**Open Liberty:**

Add to `server.xml`:

```xml
<keyStore id="defaultKeyStore" password="changeit" />
<ssl id="defaultSSLConfig" keyStoreRef="defaultKeyStore" />
```

#### 2. Configure Authentication

Add security constraints to `web.xml`:

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>MCP Endpoint</web-resource-name>
        <url-pattern>/mcp/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>mcp-user</role-name>
    </auth-constraint>
</security-constraint>

<login-config>
    <auth-method>BASIC</auth-method>
    <realm-name>MCP Realm</realm-name>
</login-config>

<security-role>
    <role-name>mcp-user</role-name>
</security-role>
```

#### 3. Rate Limiting

Implement rate limiting in your application or use server features:

```java
@ApplicationScoped
public class RateLimitFilter implements ContainerRequestFilter {
    
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String clientId = getClientId(requestContext);
        RateLimiter limiter = limiters.computeIfAbsent(clientId, 
            k -> RateLimiter.create(10.0)); // 10 requests per second
        
        if (!limiter.tryAcquire()) {
            requestContext.abortWith(
                Response.status(429).entity("Rate limit exceeded").build()
            );
        }
    }
}
```

### Performance Tuning

#### 1. JVM Tuning

```bash
# Heap size
-Xms2g -Xmx4g

# Garbage collection
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200

# Metaspace
-XX:MetaspaceSize=256m
-XX:MaxMetaspaceSize=512m
```

#### 2. Connection Pool Tuning

```bash
# GlassFish/Payara
./bin/asadmin set resources.jdbc-connection-pool.PostgresPool.max-pool-size=50
./bin/asadmin set resources.jdbc-connection-pool.PostgresPool.steady-pool-size=10
```

#### 3. Thread Pool Tuning

```bash
# WildFly
/subsystem=io/worker=default:write-attribute(name=io-threads,value=8)
/subsystem=io/worker=default:write-attribute(name=task-max-threads,value=128)
```

### Monitoring

#### 1. Enable JMX

```bash
# Add JVM options
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=9999
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

#### 2. Application Metrics

Use MicroProfile Metrics or Jakarta EE monitoring:

```java
@ApplicationScoped
public class MCPMetrics {
    
    @Inject
    @Metric(name = "mcp.tools.executed")
    private Counter toolExecutions;
    
    @Inject
    @Metric(name = "mcp.tools.duration")
    private Timer toolDuration;
}
```

#### 3. Health Checks

Implement health check endpoints:

```java
@Health
@ApplicationScoped
public class MCPHealthCheck implements HealthCheck {
    
    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse
            .named("mcp-server")
            .up()
            .withData("tools", toolRegistry.listTools().size())
            .build();
    }
}
```

### Backup and Recovery

#### 1. Database Backups

```bash
# PostgreSQL example
pg_dump -U dbuser -d mydb -F c -f backup.dump

# Restore
pg_restore -U dbuser -d mydb backup.dump
```

#### 2. Configuration Backups

```bash
# Backup server configuration
tar -czf config-backup-$(date +%Y%m%d).tar.gz \
  glassfish8/glassfish/domains/domain1/config/
```

## Monitoring and Troubleshooting

### Log Locations

**GlassFish/Payara:**
```
glassfish8/glassfish/domains/domain1/logs/server.log
```

**WildFly:**
```
wildfly-39.0.1.Final/standalone/log/server.log
```

**Open Liberty:**
```
wlp/usr/servers/mcpServer/logs/messages.log
```

### Common Issues

#### 1. Port Already in Use

```bash
# Find process using port 8080
lsof -i :8080

# Kill the process
kill -9 <PID>
```

#### 2. Out of Memory

Increase heap size in JVM options:
```bash
-Xmx4g
```

#### 3. Deployment Failures

Check logs for detailed error messages:
```bash
tail -f <server-log-file>
```

#### 4. Database Connection Issues

Verify database is running and accessible:
```bash
psql -h localhost -U dbuser -d mydb
```

### Performance Monitoring

#### 1. Thread Dumps

```bash
# Get thread dump
jstack <PID> > thread-dump.txt
```

#### 2. Heap Dumps

```bash
# Get heap dump
jmap -dump:format=b,file=heap-dump.hprof <PID>
```

#### 3. GC Logs

Enable GC logging:
```bash
-Xlog:gc*:file=gc.log:time,uptime:filecount=5,filesize=10M
```

---

This deployment guide covers the essential steps for deploying the MCP Server across all supported Jakarta EE application servers. For production deployments, always follow your organization's security and operational best practices.
