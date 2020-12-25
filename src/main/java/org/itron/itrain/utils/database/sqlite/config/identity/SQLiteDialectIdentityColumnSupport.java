package org.itron.itrain.utils.database.sqlite.config.identity;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.identity.IdentityColumnSupportImpl;

/**
 * @author Shadowalker
 */
public class SQLiteDialectIdentityColumnSupport extends IdentityColumnSupportImpl {

    public SQLiteDialectIdentityColumnSupport(Dialect dialect) {
        super();
    }

    @Override
    public boolean supportsIdentityColumns() {
        return true;
    }

    @Override
    public boolean supportsInsertSelectIdentity() {
        return true;
    }

    @Override
    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    @Override
    public String getIdentitySelectString(String table, String column, int type) {
        return "select last_insert_rowid";
    }

    @Override
    public String getIdentityColumnString(int type) {
        return "integer";
    }
}
