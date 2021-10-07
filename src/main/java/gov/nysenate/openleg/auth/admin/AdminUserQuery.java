package gov.nysenate.openleg.auth.admin;

import gov.nysenate.openleg.common.dao.BasicSqlQuery;
import gov.nysenate.openleg.common.dao.SqlTable;

enum AdminUserQuery implements BasicSqlQuery
{
    INSERT_ADMIN(
            "INSERT INTO public." + SqlTable.ADMIN + "\n" +
            "        ( username,  password,  master,  active)\n" +
            " VALUES (:username, :password, :master, :active)"
    ),
    SELECT_ALL(
            "SELECT * FROM public." + SqlTable.ADMIN
    ),
    SELECT_BY_NAME(
            "SELECT * FROM public." + SqlTable.ADMIN + " WHERE username = :username"
    ),
    DELETE_BY_NAME(
            "DELETE FROM public." + SqlTable.ADMIN + " WHERE username = :username"
    ),
    UPDATE_ADMIN(
            "UPDATE public." + SqlTable.ADMIN + "\n" +
            "SET password = :password, master = :master, active = :active\n"+
            "WHERE username = :username"
    );

    @Override
    public String getSql() { return this.sql; }

    private String sql;
    AdminUserQuery(String sql) {
        this.sql = sql;
    }
}
