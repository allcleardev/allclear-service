if [ -z "$1" ]; then
  echo 'Please supply the table name (argument 1).';
  exit -1;
fi

if [ -z "$2" ]; then
  echo 'Please supply the revision number (argument 2).';
  exit -1;
fi

DB=allclear
DBUSER=$DB
DBPWD=$DBUSER
AUTHOR=smalleyd
PACKAGE=app.allclear.platform

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanJPA out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.entity "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanValueObject out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.value "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanFilter out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.filter "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanDAO out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.dao "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanDAOTest out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.dao "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityJerseyResource out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.rest "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityJerseyResourceTest out jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.rest "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.JDBiMapper out jdbc:mysql://localhost:3306/$DB?serverTimeZone=UTC $DBUSER $DBPWD com.mysql.jdbc.Driver $AUTHOR $PACKAGE.mapper "1.0.$2" $1
