require('dotenv').config();
const bcrypt = require('bcrypt');
const User = require('./src/models/User');
const sequelize = require('./src/config/database');

async function createPatient() {
    try {
        await sequelize.authenticate();
        console.log('Connected to DB');
        
        // Check if patient already exists
        const existing = await User.findOne({ where: { role: 'PATIENT' }});
        if (existing) {
            console.log('Patient already exists with ID:', existing.id, existing.name);
            process.exit(0);
        }

        const hashedPassword = await bcrypt.hash('password123', 10);
        const patient = await User.create({
            name: 'John Doe (Sample Patient)',
            email: 'patient@example.com',
            password: hashedPassword,
            role: 'PATIENT'
        });
        console.log('Successfully created patient:', patient.id, patient.name);
    } catch (e) {
        console.error('Error creating patient:', e);
    } finally {
        process.exit();
    }
}
createPatient();
