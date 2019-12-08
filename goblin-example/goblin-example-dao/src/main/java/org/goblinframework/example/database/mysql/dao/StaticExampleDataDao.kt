package org.goblinframework.example.database.mysql.dao

import org.goblinframework.dao.annotation.PersistenceConnection
import org.goblinframework.dao.ql.Criteria
import org.goblinframework.dao.ql.Query
import org.goblinframework.database.mysql.persistence.GoblinStaticPersistence
import org.goblinframework.example.database.mysql.entity.StaticExampleData
import org.springframework.stereotype.Repository

@Repository
@PersistenceConnection(connection = "exmaple")
class StaticExampleDataDao : GoblinStaticPersistence<StaticExampleData, Long>() {

  fun queryByName(name: String): List<StaticExampleData> {
    val criteria = Criteria.where("name").`is`(name)
    return directQuery(Query.query(criteria))
  }
}
