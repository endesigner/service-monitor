# service-monitor
## Build
```
mvn clean package
mvn exec:java
```

Use
##
```
# List services
curl localhost:9090/service

# Get
curl localhost:9090/service/{UUID}

# Add a service
curl --header "Content-Type: application/json" --request POST --data '{"name": "Example", "url":"example.com"}' localhost:9090/service

# Update
curl --header "Content-Type: application/json" --request PUT --data '{"name": "Example", "url":"example.com"}' localhost:9090/service/{UUID}

# Delete
curl --header "Content-Type: application/json" --request DELETE --data '["UUID1", "UUID2", "UUID3", ..., "UUIDN"]' localhost:9090/service
```
