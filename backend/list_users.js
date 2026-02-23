const sequelize = require('./src/config/database');
const User = require('./src/models/User');

async function listUsers() {
    try {
        await sequelize.authenticate();
        console.log('Connected to database');

        const users = await User.findAll();
        console.log('All users:');
        users.forEach(user => {
            console.log(`ID: ${user.id}, Name: ${user.name}, Email: ${user.email}, Role: ${user.role}`);
        });

        process.exit(0);
    } catch (error) {
        console.error('Error:', error);
        process.exit(1);
    }
}

listUsers();
