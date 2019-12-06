package org.goblinframework.dao.mongo.persistence.internal;

import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.Success;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.goblinframework.core.reactor.*;
import org.goblinframework.core.util.MapUtils;
import org.goblinframework.core.util.NumberUtils;
import org.goblinframework.database.core.eql.Criteria;
import org.goblinframework.database.core.eql.Update;
import org.goblinframework.database.core.mapping.EntityRevisionField;
import org.goblinframework.database.mongo.bson.BsonConversionService;
import org.goblinframework.database.mongo.eql.MongoCriteriaTranslator;
import org.goblinframework.database.mongo.eql.MongoQueryTranslator;
import org.goblinframework.database.mongo.eql.MongoUpdateTranslator;
import org.goblinframework.database.mongo.support.MongoConversionSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract public class MongoPersistenceSupport<E, ID> extends MongoConversionSupport<E, ID> {

  protected final MongoCriteriaTranslator criteriaTranslator;
  protected final MongoQueryTranslator queryTranslator;
  protected final MongoUpdateTranslator updateTranslator;

  protected MongoPersistenceSupport() {
    this.criteriaTranslator = MongoCriteriaTranslator.INSTANCE;
    this.queryTranslator = MongoQueryTranslator.INSTANCE;
    this.updateTranslator = MongoUpdateTranslator.INSTANCE;
  }

  private <T> SingleResultPublisher<T> createSingleResultPublisher() {
    return new SingleResultPublisher<>(CoreScheduler.getInstance());
  }

  private <T> MultipleResultsPublisher<T> createMultipleResultsPublisher() {
    return new MultipleResultsPublisher<>(CoreScheduler.getInstance());
  }

  public void insert(@Nullable E entity) {
    Publisher<E> publisher = __insert(entity);
    BlockingMonoSubscriber<E> subscriber = new BlockingMonoSubscriber<>();
    publisher.subscribe(subscriber);
    subscriber.block();
  }

  public void inserts(@Nullable Collection<E> entities) {
    Publisher<E> publisher = __inserts(entities);
    BlockingListSubscriber<E> subscriber = new BlockingListSubscriber<>();
    publisher.subscribe(subscriber);
    subscriber.block();
  }

  @Nullable
  public E load(@Nullable ID id) {
    Publisher<E> publisher = __load(id);
    BlockingMonoSubscriber<E> subscriber = new BlockingMonoSubscriber<>();
    publisher.subscribe(subscriber);
    return subscriber.block();
  }

  @NotNull
  public Map<ID, E> loads(@Nullable Collection<ID> ids) {
    Publisher<E> publisher = __loads(ids);
    BlockingListSubscriber<E> subscriber = new BlockingListSubscriber<>();
    publisher.subscribe(subscriber);
    Map<ID, E> result = subscriber.block().stream()
        .collect(Collectors.toMap(this::getEntityId, Function.identity()));
    return MapUtils.resort(result, ids);
  }

  @NotNull
  final public Publisher<E> __insert(@Nullable E entity) {
    if (entity == null) {
      SingleResultPublisher<E> publisher = createSingleResultPublisher();
      publisher.complete(null, null);
      return publisher;
    }
    return __inserts(Collections.singleton(entity));
  }

  @NotNull
  final public Publisher<E> __inserts(@Nullable Collection<E> entities) {
    MultipleResultsPublisher<E> publisher = createMultipleResultsPublisher();
    if (entities == null || entities.isEmpty()) {
      publisher.complete(null);
      return publisher;
    }

    long millis = System.currentTimeMillis();
    for (E entity : entities) {
      generateEntityId(entity);
      try {
        requireEntityId(entity);
      } catch (Exception ex) {
        publisher.complete(ex);
        return publisher;
      }
      touchCreateTime(entity, millis);
      touchUpdateTime(entity, millis);
      initializeRevision(entity);
    }

    LinkedMultiValueMap<MongoNamespace, E> grouped = groupEntities(entities);
    publisher.initializeCount(grouped.size());
    grouped.forEach((ns, es) -> {
      MongoDatabase database = getNativeMongoClient().getDatabase(ns.getDatabaseName());
      MongoCollection<BsonDocument> collection = database.getCollection(ns.getCollectionName(), BsonDocument.class);
      BsonArray array = (BsonArray) BsonConversionService.toBson(es);
      List<BsonDocument> docs = array.stream().map(e -> (BsonDocument) e).collect(Collectors.toList());
      if (docs.size() == 1) {
        collection.withWriteConcern(WriteConcern.ACKNOWLEDGED)
            .insertOne(docs.iterator().next())
            .subscribe(new Subscriber<Success>() {
              @Override
              public void onSubscribe(Subscription s) {
                s.request(1);
              }

              @Override
              public void onNext(Success success) {
                publisher.onNext(es.iterator().next());
              }

              @Override
              public void onError(Throwable t) {
                publisher.complete(t);
              }

              @Override
              public void onComplete() {
                publisher.release();
              }
            });
      } else {
        collection.withWriteConcern(WriteConcern.ACKNOWLEDGED)
            .insertMany(docs)
            .subscribe(new Subscriber<Success>() {
              @Override
              public void onSubscribe(Subscription s) {
                s.request(docs.size());
              }

              @Override
              public void onNext(Success success) {
              }

              @Override
              public void onError(Throwable t) {
                publisher.complete(t);
              }

              @Override
              public void onComplete() {
                es.forEach(publisher::onNext);
                publisher.release();
              }
            });
      }
    });
    return publisher;
  }

  @NotNull
  final public Publisher<E> __load(@Nullable ID id) {
    if (id == null) {
      SingleResultPublisher<E> publisher = createSingleResultPublisher();
      publisher.complete(null, null);
      return publisher;
    }
    return __loads(Collections.singleton(id));
  }

  @NotNull
  final public Publisher<E> __loads(@Nullable Collection<ID> ids) {
    MultipleResultsPublisher<E> publisher = createMultipleResultsPublisher();
    if (ids == null || ids.isEmpty()) {
      publisher.complete(null);
      return publisher;
    }
    LinkedMultiValueMap<MongoNamespace, ID> grouped = groupIds(ids);
    publisher.initializeCount(grouped.size());
    grouped.forEach((ns, ds) -> {
      MongoDatabase database = getNativeMongoClient().getDatabase(ns.getDatabaseName());
      MongoCollection<BsonDocument> collection = database.getCollection(ns.getCollectionName(), BsonDocument.class);
      Criteria criteria;
      if (ds.size() == 1) {
        criteria = Criteria.where("_id").is(ds.iterator().next());
      } else {
        criteria = Criteria.where("_id").in(ds);
      }
      Bson filter = criteriaTranslator.translate(criteria);
      FindPublisher<BsonDocument> findPublisher = collection.find(filter, BsonDocument.class);
      findPublisher.subscribe(new Subscriber<BsonDocument>() {
        @Override
        public void onSubscribe(Subscription s) {
          s.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(BsonDocument bsonDocument) {
          try {
            E loaded = convertBsonDocument(bsonDocument);
            Objects.requireNonNull(loaded);
            publisher.onNext(loaded);
          } catch (Exception ex) {
            publisher.complete(ex);
          }
        }

        @Override
        public void onError(Throwable t) {
          publisher.complete(t);
        }

        @Override
        public void onComplete() {
          publisher.release();
        }
      });
    });

    return publisher;
  }

  @NotNull
  final public Publisher<Boolean> __exists(@Nullable ID id, @Nullable ReadPreference readPreference) {
    SingleResultPublisher<Boolean> publisher = createSingleResultPublisher();
    if (id == null) {
      publisher.complete(false, null);
      return publisher;
    }
    MongoNamespace namespace = getIdNamespace(id);
    Criteria criteria = Criteria.where("_id").is(id);
    Bson filter = criteriaTranslator.translate(criteria);

    MongoDatabase database = getNativeMongoClient().getDatabase(namespace.getDatabaseName());
    MongoCollection<BsonDocument> collection = database.getCollection(namespace.getCollectionName(), BsonDocument.class);
    if (readPreference != null) {
      collection = collection.withReadPreference(readPreference);
    }
    collection.countDocuments(filter).subscribe(new Subscriber<Long>() {
      @Override
      public void onSubscribe(Subscription s) {
        s.request(1);
      }

      @Override
      public void onNext(Long aLong) {
        long count = NumberUtils.toLong(aLong);
        publisher.complete(count > 0, null);
      }

      @Override
      public void onError(Throwable t) {
        publisher.complete(null, t);
      }

      @Override
      public void onComplete() {
      }
    });
    return publisher;
  }

  final public Publisher<E> __replace(@NotNull final E entity) {
    SingleResultPublisher<E> publisher = createSingleResultPublisher();
    ID id = getEntityId(entity);
    if (id == null) {
      String errMsg = "Id must not be null when executing replace operation";
      publisher.complete(null, new IllegalArgumentException(errMsg));
      return publisher;
    }
    long millis = System.currentTimeMillis();
    touchUpdateTime(entity, millis);

    Criteria criteria = Criteria.where("_id").is(id);
    EntityRevisionField revisionField = entityMapping.revisionField;
    if (revisionField != null) {
      Object revision = revisionField.getField().get(entity);
      if (revision != null) {
        // revision specified, use it for optimistic concurrency checks
        criteria = criteria.and(revisionField.getName()).is(revision);
      }
    }
    Bson filter = criteriaTranslator.translate(criteria);

    Update update = new Update();
    entityMapping.updateTimeFields.forEach(ut -> {
      Object val = ut.getField().get(entity);
      if (val != null) {
        update.set(ut.getName(), val);
      }
    });
    entityMapping.normalFields.forEach(n -> {
      Object val = n.getField().get(entity);
      if (val != null) {
        update.set(n.getName(), val);
      }
    });
    if (revisionField != null) {
      update.inc(revisionField.getName(), 1);
    }

    if (update.export().isEmpty()) {
      String errMsg = "There is nothing field(s) found when executing replace operation";
      publisher.complete(null, new IllegalArgumentException(errMsg));
      return publisher;
    }

    MongoNamespace namespace = getIdNamespace(id);
    MongoDatabase database = getNativeMongoClient().getDatabase(namespace.getDatabaseName());
    MongoCollection<BsonDocument> collection = database.getCollection(namespace.getCollectionName(), BsonDocument.class);
    FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().upsert(false).returnDocument(ReturnDocument.AFTER);
    collection.withWriteConcern(WriteConcern.ACKNOWLEDGED)
        .findOneAndUpdate(filter, updateTranslator.translate(update), options)
        .subscribe(new Subscriber<BsonDocument>() {
          @Override
          public void onSubscribe(Subscription s) {
            s.request(1);
          }

          @Override
          public void onNext(BsonDocument bsonDocument) {
            E replaced = convertBsonDocument(bsonDocument);
            publisher.complete(replaced, null);
          }

          @Override
          public void onError(Throwable t) {
            publisher.complete(null, t);
          }

          @Override
          public void onComplete() {
          }
        });
    return publisher;
  }
}
