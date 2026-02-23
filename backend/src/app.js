const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');
const authRoutes = require('./routes/authRoutes');
const caseRoutes = require('./routes/caseRoutes');
const chatRoutes = require('./routes/chatRoutes');
const sequelize = require('./config/database');
const path = require('path');
// IMPORTANT: Import models/index to register all Sequelize associations (hasMany, belongsTo)
require('./models/index');

dotenv.config();

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} ${req.method} ${req.path}`);
    next();
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/cases', caseRoutes);
app.use('/api/messages', chatRoutes);

// Serve uploaded images
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

// Basic Route
app.get('/', (req, res) => {
    res.send('PathoVision Backend is running');
});

// 404 handler
app.use((req, res) => {
    console.log(`404 - Not Found: ${req.method} ${req.path}`);
    res.status(404).json({ message: `Cannot ${req.method} ${req.path}` });
});

// Database Connection and Server Start
const PORT = process.env.PORT || 5000;

const startServer = async () => {
    try {
        await sequelize.authenticate();
        console.log('Database connected successfully.');
        await sequelize.sync({ alter: true }); // Create/update tables automatically
        app.listen(PORT, () => {
            console.log(`Server running on port ${PORT}`);
        });
    } catch (error) {
        console.error('Unable to connect to the database:', error);
    }
};

startServer();
