const bcrypt = require('bcrypt');
const sequelize = require('./src/config/database');
const User = require('./src/models/User');

async function updatePassword() {
    try {
        await sequelize.authenticate();
        console.log('Connected to database');

        const hashedPassword = await bcrypt.hash('password123', 10);
        
        const result = await User.update(
            { password_hash: hashedPassword },
            { where: { email: 'hemanthbezawada7@gmail.com' } }
        );

        console.log(`Updated ${result[0]} user(s)`);
        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

updatePassword();
