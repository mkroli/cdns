CDNS
====
A DNS server which stores domain information in Cassandra.

Cassandra
---------
CDNS will use the Keyspace with the name "cdns".
If it doesn't exist yet it will be created with the following settings:
```
CREATE KEYSPACE cdns WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
}
```
In any non-testing environment the Keyspace should probably be pre-created.

CDNS will use the Column-Family "cdns.cdns" which it will automatically create:
```
CREATE COLUMNFAMILY cdns.cdns (
  question text,
  type text,
  answers list<text>,
  shuffle boolean,
  PRIMARY KEY (question, type)
)
```

Entries can be added using the following statement:
```
UPDATE cdns.cdns SET
    answers = ['1.2.3.4', '2.3.4.5'],
    shuffle = True
  WHERE question = 'example.com'
    AND type = 'A';
```

Docker
------

Cassandra
```
docker run -d --name cassandra poklet/cassandra
```

cqlsh
```
docker run --rm -i -t --link cassandra:cassandra poklet/cassandra /bin/sh -c 'cqlsh ${CASSANDRA_PORT_9160_TCP_ADDR}'
```

cdns
```
docker run --name cdns -d --link cassandra:cassandra -p 53:53/udp mkroli/cdns
```
