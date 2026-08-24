### .env example
```
DB_URL=jdbc:postgresql://localhost:5432/phm
DB_USERNAME=postgres
DB_PASSWORD=2013
JWT_SECRET_KEY=tu_clave_secreta_muy_segura_con_al_menos_32_caracteres_para_hmac_sha256
```

### Configuración de BD Mongo (sharding)
```
docker compose up -d

docker compose exec configsvr01 sh -c "mongosh < /scripts/init-configserver.js"

docker compose exec shard01-a sh -c "mongosh < /scripts/init-shard01.js"
docker compose exec shard02-a sh -c "mongosh < /scripts/init-shard02.js"

Esperar 45seg aprox para correr el siguiente comando
docker compose exec router01 sh -c "mongosh < /scripts/init-router.js"

docker exec -it router-01 mongosh

use admin

db.createUser({
user: 'capo',
pwd: 'eyra',
roles: [
{
role: 'root',
db: 'admin'
}
]
});

use libros

sh.enableSharding("libros")

db.libros.createIndex({
"_id": "hashed"
})

sh.shardCollection(
"libros.libros",
{ "_id": "hashed" }
)
```

### Configuración de REDIS
```
Al ingresar a http://localhost:5540
1 - add database
2 - Connection settings
3 - Host: redis-books 
4 - Username: dejar vacío
5 - Puerto: 6379
```