package org.goblinframework.dao.mongo.bson.deserializer;

import org.bson.BsonDocument;
import org.goblinframework.dao.mongo.bson.BsonConversionService;
import org.goblinframework.test.runner.GoblinTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

@RunWith(GoblinTestRunner.class)
@ContextConfiguration("/UT.xml")
public class BsonBooleanDeserializerTest {

  public static class Data {
    public Boolean a, b, c, d, e;
  }

  @Test
  public void deserialize() {
    LinkedHashMap<String, Object> map = new LinkedHashMap<>();
    map.put("a", true);
    map.put("b", false);
    map.put("c", "true");
    map.put("d", "false");
    map.put("e", "I'm not boolean value.");
    BsonDocument document = (BsonDocument) BsonConversionService.toBson(map);
    Data data = BsonConversionService.toObject(document, Data.class);
    assertTrue(data.a);
    assertFalse(data.b);
    assertTrue(data.c);
    assertFalse(data.d);
    assertNull(data.e);
  }
}