const sequelize = require('../config/database');
const User = require('./User');
const Case = require('./Case');
const Message = require('./Message');

// User - Case Associations
User.hasMany(Case, { as: 'CasesAsPathologist', foreignKey: 'pathologist_id' });
User.hasMany(Case, { as: 'CasesAsPatient', foreignKey: 'patient_id' });

Case.belongsTo(User, { as: 'Pathologist', foreignKey: 'pathologist_id' });
Case.belongsTo(User, { as: 'Patient', foreignKey: 'patient_id' });

// Case - Message Associations
Case.hasMany(Message, { foreignKey: 'case_id' });
Message.belongsTo(Case, { foreignKey: 'case_id' });

// User - Message Associations
User.hasMany(Message, { as: 'SentMessages', foreignKey: 'sender_id' });
User.hasMany(Message, { as: 'ReceivedMessages', foreignKey: 'receiver_id' });

Message.belongsTo(User, { as: 'Sender', foreignKey: 'sender_id' });
Message.belongsTo(User, { as: 'Receiver', foreignKey: 'receiver_id' });

module.exports = {
    sequelize,
    User,
    Case,
    Message,
};
