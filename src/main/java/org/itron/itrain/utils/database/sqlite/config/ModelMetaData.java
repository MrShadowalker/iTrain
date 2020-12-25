package org.itron.itrain.utils.database.sqlite.config;

import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.itron.itrain.utils.database.sqlite.JpaConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 构建一个存放表名和 model 实体 class 的对应关系，如 member_entity : MemberEntity.class
 *
 * @author Shadowalker
 */
@Configuration
@AutoConfigureAfter(JpaConfiguration.class)
public class ModelMetaData {

    @SuppressWarnings({"rawtypes", "deprecation"})
    @Bean(name = "metaMap")
    public Map<String, Class> metaMap(EntityManagerFactory factory) throws ClassNotFoundException {
        if (factory.unwrap(SessionFactory.class) == null) {
            throw new NullPointerException("factory is not a hibernate factory");
        }
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) factory.unwrap(SessionFactory.class);
        Map<String, EntityPersister> persisterMap = sessionFactory.getEntityPersisters();
        Map<String, Class> map = new HashMap<>();
        for (Map.Entry<String, EntityPersister> entity : persisterMap.entrySet()) {
            Class targetClass = entity.getValue().getMappedClass();
            SingleTableEntityPersister persister = (SingleTableEntityPersister) entity.getValue();
            String tableName = persister.getTableName();// Entity对应的表的英文名
            map.put(tableName, targetClass);
        }
        return map;
    }

}

