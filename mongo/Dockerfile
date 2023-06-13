# Build with --network="host" so mongosh can find the mongo server
# By default it will check on the host machine, this makes it check on the container

# docker build mongo --network="host" -t=liberty_mongo

FROM mongo:6.0.6

COPY init.js /init.js

RUN /bin/mongosh  < init.js