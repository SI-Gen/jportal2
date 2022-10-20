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

### Postgres database setup  
Next create a file called start_postgres.sh, with the contents below:

**start_postgres.sh**
```sh
docker run --name postgres -e POSTGRES_PASSWORD=magic_password postgres
```

make this script executable via `chmod +x start_postgres.sh`, then run it in a terminal window. You should see a bunch 
of scrolling text output, and finally the text  

**```database system is ready to accept connections```**   

Once you see this, it means postgres is up an running.
