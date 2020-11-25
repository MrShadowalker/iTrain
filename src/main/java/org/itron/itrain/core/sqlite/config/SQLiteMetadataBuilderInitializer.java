package org.itron.itrain.core.sqlite.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.spi.MetadataBuilderInitializer;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.DialectResolverSet;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;

/**
 * SQLite 工具
 *
 * @author Shadowalker
 */
@Slf4j
public class SQLiteMetadataBuilderInitializer implements MetadataBuilderInitializer {

    private static final SQLiteDialect dialect = new SQLiteDialect();

    private static final DialectResolver resolver = new DialectResolver() {
        @Override
        public Dialect resolveDialect(DialectResolutionInfo info) {
            if (info.getDatabaseName().equals("SQLite")) {
                return dialect;
            }
            return null;
        }
    };

    @Override
    public void contribute(MetadataBuilder metadataBuilder, StandardServiceRegistry standardServiceRegistry) {
        DialectResolver dialectResolver = standardServiceRegistry.getService(DialectResolver.class);
        if (!(dialectResolver instanceof DialectResolverSet)) {
            log.warn("DialectResolver {} is not an instance of DialectResolverSet, not registering SQLiteDialect", dialectResolver);
            return;
        }
        ((DialectResolverSet) dialectResolver).addResolver(resolver);
    }
}
