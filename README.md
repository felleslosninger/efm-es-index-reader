# efm-es-index-reader

This application takes a Elasticsearch(ES) index name and retrieves it from the ES cluster to ship log data to Kafka. It also runs a scheduled job to retrieve yesterdays new indices and sends them to logging-proxy to be put in Kafka!


