if [ -z "$1" ]; then
  echo 'Please supply the table name (argument 1).';
  exit -1;
fi

if [ -z "$2" ]; then
  echo 'Please supply the revision number (argument 2).';
  exit -1;
fi

DB=allclear
DBUSER=root
DBPWD=password
AUTHOR=smalleyd
PACKAGE=app.allclear.platform
# DRIVER=om.mysql.jdbc.Driver
DRIVER=com.mysql.cj.jdbc.Driver
URL="jdbc:mysql://localhost:3306/$DB?serverTimezone=UTC"

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanJPA out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.entity "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanValueObject out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.value "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanFilter out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.filter "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanDAO out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.dao "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityBeanDAOTest out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.dao "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityJerseyResource out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.rest "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.EntityJerseyResourceTest out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.rest "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.JDBiMapper out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.mapper "1.0.$2" $1

java -cp $MYSQL_JDBC/mysql.jar:$SMALL_LIBRARY_JAR com.small.library.ejb.gen.JDBiSqlObject out "${URL}" $DBUSER $DBPWD $DRIVER $AUTHOR $PACKAGE.mapper "1.0.$2" $1
