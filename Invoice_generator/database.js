    const sqlite3 = require('sqlite3').verbose();

    // database.db naam ki ek file banayegi ya use karegi
    const db = new sqlite3.Database('./database.db', (err) => {
        if (err) {
            console.error(err.message);
        }
        console.log('Connected to the database.');
    });

    // Users table banane ka command
    db.serialize(() => {
        db.run(`
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT
            )
        `, (err) => {
            if (err) {
                console.error(err.message);
            }
            console.log("Users table is ready.");
        });
    });

    // Connection band karna
    db.close((err) => {
        if (err) {
            console.error(err.message);
        }
        console.log('Closed the database connection.');
    });


