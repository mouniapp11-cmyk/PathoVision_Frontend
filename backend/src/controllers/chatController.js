const Message = require('../models/Message');
const User = require('../models/User');
const Case = require('../models/Case');
const { Op } = require('sequelize');

const sendMessage = async (req, res) => {
    try {
        const { case_id, message_text, receiver_id } = req.body;
        const sender_id = req.user.id;

        // Verify case existence
        const caseItem = await Case.findByPk(case_id, {
            include: [
                { model: User, as: 'Pathologist', attributes: ['id'] },
                { model: User, as: 'Patient', attributes: ['id'] }
            ]
        });
        if (!caseItem) {
            return res.status(404).json({ message: 'Case not found' });
        }

        // Access control: Check if sender is involved in this case
        const senderInvolved = 
            sender_id === caseItem.Pathologist?.id || 
            sender_id === caseItem.Patient?.id;
        
        if (!senderInvolved) {
            return res.status(403).json({ message: 'You do not have access to this case' });
        }

        // Verify receiver is also involved in case or authorized
        const receiverUser = await User.findByPk(receiver_id);
        if (!receiverUser) {
            return res.status(404).json({ message: 'Receiver not found' });
        }

        const newMessage = await Message.create({
            case_id,
            sender_id,
            receiver_id,
            message_text,
            is_read: false
        });

        const messageWithSender = await Message.findByPk(newMessage.id, {
            include: { model: User, as: 'Sender', attributes: ['id', 'name', 'role', 'profile_picture'] }
        });

        res.status(201).json(messageWithSender);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

const getMessages = async (req, res) => {
    try {
        const { caseId } = req.params;
        const userId = req.user.id;

        // Verify user is involved in the case
        const caseItem = await Case.findByPk(caseId, {
            include: [
                { model: User, as: 'Pathologist', attributes: ['id', 'role'] },
                { model: User, as: 'Patient', attributes: ['id', 'role'] }
            ]
        });
        if (!caseItem) {
            return res.status(404).json({ message: 'Case not found' });
        }

        const userInvolved = 
            userId === caseItem.Pathologist?.id || 
            userId === caseItem.Patient?.id;
        
        if (!userInvolved && req.user.role !== 'STUDENT') {
            return res.status(403).json({ message: 'You do not have access to this case' });
        }

        const messages = await Message.findAll({
            where: { case_id: caseId },
            include: [
                { model: User, as: 'Sender', attributes: ['id', 'name', 'role', 'profile_picture'] },
                { model: User, as: 'Receiver', attributes: ['id', 'name', 'role'] }
            ],
            order: [['created_at', 'ASC']],
        });

        // Mark messages as read for current user
        await Message.update(
            { is_read: true },
            {
                where: {
                    case_id: caseId,
                    receiver_id: userId,
                    is_read: false
                }
            }
        );

        res.json(messages);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

// Get inbox: show all conversations for the current user
const getInbox = async (req, res) => {
    try {
        const userId = req.user.id;

        // Get all messages involving this user (sent or received)
        const messages = await Message.findAll({
            where: {
                [Op.or]: [
                    { sender_id: userId },
                    { receiver_id: userId }
                ]
            },
            include: [
                { model: User, as: 'Sender', attributes: ['id', 'name', 'role', 'profile_picture'] },
                { model: User, as: 'Receiver', attributes: ['id', 'name', 'role'] },
                {
                    model: Case,
                    attributes: ['id', 'title', 'image_url', 'ai_prediction', 'confidence_score'],
                    include: [
                        { model: User, as: 'Pathologist', attributes: ['id', 'name'] },
                        { model: User, as: 'Patient', attributes: ['id', 'name'] }
                    ]
                }
            ],
            order: [['created_at', 'DESC']],
        });

        // Group messages by conversation (case + other user)
        const conversations = {};
        const unreadCounts = {};

        messages.forEach(msg => {
            const otherUser = msg.sender_id === userId ? msg.Receiver : msg.Sender;
            const conversationKey = `${msg.case_id}_${otherUser.id}`;

            if (!conversations[conversationKey]) {
                conversations[conversationKey] = {
                    case_id: msg.case_id,
                    case_title: msg.Case.title,
                    case_image: msg.Case.image_url,
                    case_prediction: msg.Case.ai_prediction,
                    other_user: otherUser,
                    last_message: msg,
                    last_message_time: msg.created_at,
                    unread_count: 0
                };
            }

            // Update if this is a more recent message
            if (new Date(msg.created_at) > new Date(conversations[conversationKey].last_message_time)) {
                conversations[conversationKey].last_message = msg;
                conversations[conversationKey].last_message_time = msg.created_at;
            }

            // Count unread messages
            if (msg.receiver_id === userId && !msg.is_read) {
                conversations[conversationKey].unread_count += 1;
            }
        });

        // Sort by most recent
        const inbox = Object.values(conversations)
            .sort((a, b) => new Date(b.last_message_time) - new Date(a.last_message_time));

        res.json(inbox);
    } catch (error) {
        console.error(error);
        res.status(500).json({ message: 'Server error' });
    }
};

module.exports = { sendMessage, getMessages, getInbox };
