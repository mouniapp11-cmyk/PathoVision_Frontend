const sequelize = require('./src/config/database');
const User = require('./src/models/User');

async function clearUsers() {
    try {
        await sequelize.authenticate();
        const count = await User.destroy({ where: {}, truncate: true });
        console.log('✅ All users deleted from the database.');
    } catch (e) {
        console.error('❌ Error clearing users:', e.message);
        process.exit(1);
    } finally {
        await sequelize.close();
    }
}

clearUsers();
