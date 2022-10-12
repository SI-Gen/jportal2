## Setup the project directory

Now we are ready to create our project.   

### Directory setup
Create a new directory called `jportal2-demo`. This will be the root directory of your project.  

Inside the root directory, create directories called `sql/si`. The name of this directory is completely up to you, you can put JPortal files anywhere you want in your project, but to make the demo easier to follow, we suggest following our structure.  

Your directory structure should now look like this:
```
jportal2-demo
└───sql
    └───si
```

### docker-compose setup  
Next create a directory called`docker`. This directory will contain a number of container related files.


Finally, inside the `docker` directory, create a docker-compose.yml file, with the contents given below

Your directory structure should now look like this:
```
jportal2-demo
└───docker
    └── docker-compose.yml
└───sql
    └───si
```


**docker-compose.yml**
```yaml
version: "3.7"
name: jportal-demo
services:
### PostgreSQL containers ###
  postgresql_database:
    container_name: postgresql_database
    image: postgres:latest
    environment:
      POSTGRES_PASSWORD: magical_password
      POSTGRES_USER: postgres_admin
    volumes:
      - ./sql:/docker-entrypoint-initdb.d/
    ports:
      - 5432:5432
    networks:
      - jportal_demo_network
    healthcheck:
      test:
        [
          "CMD",
          "pg_isready",
          "-q",
          "-U",
          "postgres_admin",
          "-d",
          "insurance"
        ]
      interval: 5s
      timeout: 1s
      retries: 2
    restart: unless-stopped

  pgadmin:
    container_name: pgadmin
    image: dpage/pgadmin4
    environment:
      POSTGRES_USER: "postgres_admin"
      POSTGRES_PASSWORD: "magical_password"
      POSTGRES_DB: "Vanguard"
      PGDATA: /var/lib/postgresql/data
      PGADMIN_DEFAULT_EMAIL: "postgres_admin@a.com"
      PGADMIN_DEFAULT_PASSWORD: "magical_password"
    ports:
      - 5050:80
    networks:
      - jportal_demo_network
    depends_on:
      - postgresql_database
    restart: unless-stopped
```

The docker-compose file will start up postgresql in a container, and also start up [pgAdmin](https://www.pgadmin.org/) 
in a container. pgAdmin is (as the name suggests) an administration tool to allow you to view and interact with postgres
databases.


