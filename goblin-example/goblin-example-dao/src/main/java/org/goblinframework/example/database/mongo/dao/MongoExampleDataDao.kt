package org.goblinframework.example.database.mongo.dao

import org.bson.types.ObjectId
import org.goblinframework.dao.annotation.PersistenceConnection
import org.goblinframework.dao.mongo.persistence.GoblinStaticDao
import org.goblinframework.dao.mongo.persistence.MongoPersistenceCollection
import org.goblinframework.dao.mongo.persistence.MongoPersistenceDatabase
import org.goblinframework.example.database.mongo.entity.MongoExampleData
import org.springframework.stereotype.Repository

@Repository
@PersistenceConnection(connection = "example")
@MongoPersistenceDatabase(database = "test")
@MongoPersistenceCollection(collection = "mongo_example_data")
class MongoExampleDataDao : GoblinStaticDao<MongoExampleData, ObjectId>()