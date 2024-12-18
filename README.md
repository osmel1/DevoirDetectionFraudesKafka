# Détection de Fraudes en Temps Réel avec Kafka Streams, InfluxDB et Grafana

##  Description du projet  
Ce projet consiste à développer un pipeline de détection de fraudes en temps réel basé sur **Kafka Streams**, une base de données **InfluxDB** et un tableau de bord **Grafana**.  
Il lit des transactions financières à partir d'un **topic Kafka**, filtre les transactions suspectes selon des règles définies ( montant > 10 000), et les stocke dans **InfluxDB** pour une visualisation en temps réel dans **Grafana**.

---

##  Architecture du projet  

Le projet s'articule autour des composants suivants :
![Frame 4 (2)](https://github.com/user-attachments/assets/c62260f6-0f24-4e8f-8b18-f9ad85399399)


- **Kafka Streams** : Traitement des transactions financières.
- **InfluxDB** : Stockage des transactions suspectes.
- **Grafana** : Visualisation des données en temps réel.
- **Docker Compose** : Déploiement des services.
  

## Installation et exécution
1. Cloner le projet
   
```bash
git clone https://github.com/osmel1/DevoirDetectionFraudesKafka.git

```

2. Déployer l'environnement avec Docker Compose
```bash
docker-compose up -d

```

3. Générer des transactions d'exemple
-  Exécutez la commande suivante pour envoyer des messages dans le topic transactions-input :
```
kafka-console-producer.sh --topic transactions-input --bootstrap-server localhost:9092 
```
-  Les messages doivent être au format JSON suivant : 
   {
    "userId": "12345",
    "amount": 15000,
    "timestamp": "2024-12-04T15:00:00Z"
   }


## Configuration des Topics Kafka
Assurez-vous que les topics transactions-input et fraud-alerts sont créés.
```
# Créer le topic transactions-input
kafka-topics.sh --create --topic transactions-input --bootstrap-server localhost:9092

# Créer le topic fraud-alerts
kafka-topics.sh --create --topic fraud-alerts --bootstrap-server localhost:9092

```
## Tableau de bord Grafana
Le tableau de bord inclut les éléments suivants :

1. **Montant Moyenne des transactions suspectes**

```

from(bucket: "fraud-detection-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "suspicious_transactions")
  |> filter(fn: (r) => r["_field"] == "amount")
  |> group(columns: []) // Pas de regroupement pour calculer globalement
  |> mean(column: "_value") // Calcule la moyenne des montants
  |> yield(name: "average_fraud_amount")

```

2. **Nombre totale des transactions suspectes**
```

from(bucket: "fraud-detection-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "suspicious_transactions")
  |> filter(fn: (r) => r["_field"] == "amount")
  |> group(columns: [])
  |> count()
  |> yield(name: "total_fraud_amount")
```
3. **Montant Maximum des transactions suspectes**

```

from(bucket: "fraud-detection-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "suspicious_transactions")
  |> filter(fn: (r) => r["_field"] == "amount")
  |> group(columns: []) // Si vous ne regroupez pas, toutes les données sont considérées globalement
  |> max(column: "_value") // Trouve le montant maximum
  |> yield(name: "max_fraud_amount")

```

4. **Nombre de transactions suspectes par utilisateur**

```

from(bucket: "fraud-detection-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "suspicious_transactions")
  |> filter(fn: (r) => r["_field"] == "amount")
  |> group(columns: ["userId"])
  |> count()
  |> keep(columns: ["userId", "_value"])
  |> rename(columns: {"_value": " ", "userId": "id"})

```


5. **Montant totale de fraudes**

```
from(bucket: "fraud-detection-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "suspicious_transactions")
  |> filter(fn: (r) => r["_field"] == "amount")
  |> group(columns: [])
  |> sum()
  |> yield(name: "total_fraud_amount")
```

5. **Demonstration**

![demo](https://github.com/user-attachments/assets/4c1b3599-5c43-477a-911e-b8c90ce2f168)

