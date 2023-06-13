db = connect( 'mongodb://localhost/testdb' );
db.createUser( {user: 'sampleUser', pwd:'openliberty', roles: [{ role: 'readWrite', db:'testdb'}]} )