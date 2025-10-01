// Zaroori libraries ko import karna
const express = require('express');
const path = require('path');
const sqlite3 = require('sqlite3').verbose();
const bcrypt = require('bcrypt');
const session = require('express-session');

// Express app banana
const app = express();
const PORT = 3000;

// Database se connect karna
const db = new sqlite3.Database('./database.db', err => {
    if (err) {
        return console.error(err.message);
    }
    console.log('Connected to the SQLite database.');
});

// --- Middleware ---
app.use(express.static(path.join(__dirname, 'public')));
app.use(express.json());
app.use(session({
    secret: 'a-very-secret-key-that-is-long-and-random',
    resave: false,
    saveUninitialized: false,
    cookie: { 
        secure: false, // http par kaam karne ke liye false
        httpOnly: true,
        maxAge: 24 * 60 * 60 * 1000 // 24 ghante
    }
}));

// Authentication Middleware
const checkAuth = (req, res, next) => {
    if (req.session.userId) {
        next();
    } else {
        res.redirect('/login.html');
    }
};


// --- Routes (URL Endpoints) ---

// Homepage route - Protected
app.get('/', checkAuth, (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

// User ka data get karne ke liye route
app.get('/api/user', (req, res) => {
    if (req.session.username) {
        res.json({ username: req.session.username });
    } else {
        res.status(401).json({ message: 'Not authenticated' });
    }
});

// Registration route
app.post('/register', async (req, res) => {
    try {
        const { username, password } = req.body;
        const row = await new Promise((resolve, reject) => {
            db.get('SELECT * FROM users WHERE username = ?', [username], (err, row) => (err ? reject(err) : resolve(row)));
        });

        if (row) {
            return res.status(400).json({ message: 'Username already exists' });
        }
        const hashedPassword = await bcrypt.hash(password, 10);
        db.run('INSERT INTO users (username, password) VALUES (?, ?)', [username, hashedPassword], (err) => {
            if (err) return res.status(500).json({ message: 'Error registering user' });
            res.status(201).json({ message: 'User registered successfully!' });
        });
    } catch (error) {
        res.status(500).json({ message: 'Server error' });
    }
});

// Login route
app.post('/login', async (req, res) => {
    try {
        const { username, password } = req.body;
        const user = await new Promise((resolve, reject) => {
            db.get('SELECT * FROM users WHERE username = ?', [username], (err, row) => (err ? reject(err) : resolve(row)));
        });

        if (!user) {
            return res.status(400).json({ message: 'Invalid username or password' });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: 'Invalid username or password' });
        }
        
        req.session.userId = user.id;
        req.session.username = user.username;
        res.status(200).json({ message: 'Logged in successfully!' });

    } catch (error) {
        res.status(500).json({ message: 'Server error' });
    }
});

// Logout route
app.post('/logout', (req, res) => {
    req.session.destroy(err => {
        if (err) {
            return res.status(500).json({ message: 'Could not log out.' });
        }
        res.clearCookie('connect.sid');
        res.status(200).json({ message: 'Logged out successfully' });
    });
});

// Server ko start karna
app.listen(PORT, () => {
  console.log(`Server http://localhost:${PORT} par live hai.`);
});

